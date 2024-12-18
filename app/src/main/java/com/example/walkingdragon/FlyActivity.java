package com.example.walkingdragon;

import static com.example.walkingdragon.CoordinateUtils.calculateDistance;
import static com.example.walkingdragon.CoordinateUtils.calculateTm128Distance;
import static com.example.walkingdragon.CoordinateUtils.tm128ToWgs84;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;

public class FlyActivity extends AppCompatActivity {

    // 상수 정의

    private double gpsLatitude = 0.0;
    private double gpsLongitude = 0.0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly); // activity_fly.xml과 연결

        // GPS 초기화
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 버튼 객체 가져오기
        ImageButton backButton = findViewById(R.id.backButton);

        // 버튼 클릭 시 현재 Activity 종료
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 전환 애니메이션
            });

        // GPS 정보 가져오기
        requestLocation();

      // 비동기 처리 스레드
        new Thread(() -> {
            NaverAPI naverAPI = new NaverAPI();
            String result = naverAPI.searchLocal("병원");
            Log.d("NaverAPIResult", result);

            // JSON 파싱
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray items = jsonObject.getJSONArray("items");

                // 최대 3번 반복 (0, 1, 2)
                int itemCount = Math.min(items.length(), 3); // 최대 3개만 처리
                for (int i = 0; i < itemCount; i++) {
                    JSONObject item = items.getJSONObject(i);
                    String title = item.getString("title"); // 장소 이름
                    double mapX = Double.parseDouble(item.getString("mapx"));
                    double mapY = Double.parseDouble(item.getString("mapy"));

                    // 좌표 변환
                    double[] Coords = CoordinateUtils.processCoordinates((long) mapX, (long) mapY);

                    // 거리 계산
                    double distance = calculateDistance(Coords[0], Coords[1], gpsLongitude, gpsLatitude);
                    Log.d("GPSLocation", String.format("Item %d: GPS Coordinates: Latitude=%.6f, Longitude=%.6f, mapX=%.6f, mapY=%.6f", i, gpsLatitude, gpsLongitude, Coords[0], Coords[1]));

                    // 예상 시간 계산
                    double timeInHours = distance / 10.0;
                    int hours = (int) timeInHours;
                    int minutes = (int) ((timeInHours - hours) * 60);

                    // UI 업데이트는 메인스레드에서
                    int finalI = i;
                    runOnUiThread(() -> {
                        // 동적으로 TextView 참조
                        TextView titleTextView = findViewById(getResources().getIdentifier("titleTextView" + (finalI + 1), "id", getPackageName()));
                        TextView distanceTextView = findViewById(getResources().getIdentifier("distanceTextView" + (finalI + 1), "id", getPackageName()));
                        TextView timeTextView = findViewById(getResources().getIdentifier("timeTextView" + (finalI + 1), "id", getPackageName()));

                        // TextView 업데이트
                        titleTextView.setText(title);
                        distanceTextView.setText(String.format("거리: %.2f km", distance));
                        timeTextView.setText(String.format("예상 시간: %d시간 %d분", hours, minutes));

                        // 동적으로 CardView 참조
                        CardView cardView = findViewById(getResources().getIdentifier("cardView" + (finalI + 1), "id", getPackageName()));

                        // 클릭 이벤트 추가
                        cardView.setOnClickListener(v -> {
                            showFlightConfirmDialog(title, hours, minutes, () -> {
                                // 예상 시간을 ms로 계산하여 MainActivity로 전달
                                long durationInMillis = (hours * 60 + minutes) * 60 * 1000;
                                sendFlightToMainActivity(durationInMillis);
                            });
                        });
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

//
    /**
     * 비행 실행: 정해진 시간 동안 드래곤 숨기기
     */
    private void sendFlightToMainActivity(long duration) {
        Intent intent = new Intent(FlyActivity.this, MainActivity.class);
        intent.putExtra("FLIGHT_DURATION", duration); // 비행 시간(ms) 전달
        startActivity(intent); // MainActivity로 이동
        finish(); // FlyActivity 종료
    }
//    // 키워드 검색 함수
//    private void searchKeyword(String keyword, String x, String y) {
//        // Retrofit 객체 생성
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Retrofit 인터페이스 객체 생성
//        KakaoAPI api = retrofit.create(KakaoAPI.class);
//        int radius = 2000;
//
//        //category 설정
//        String category = "학교";
//        if(keyword.equals("학교"))
//            category = "SC4";
//        else if(keyword.equals("병원"))
//            category = "HP8";
//        // API 호출
//        Call<ResultSearchKeword> call = api.getSearchKeyword(API_KEY, keyword);
//
//        // 비동기 요청 처리
//        call.enqueue(new Callback<ResultSearchKeword>() {
//            @Override
//            public void onResponse(Call<ResultSearchKeword> call, Response<ResultSearchKeword> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    // 통신 성공, 결과 처리
//                    Log.d("FlyActivity", "Raw: " + response.raw());
//                    Log.d("FlyActivity", "Body: " + response.body());
//                } else {
//                    // 통신 성공했지만 응답에 문제가 있는 경우
//                    Log.d("FlyActivity", "Raw Response: " + response.raw().toString());
//                    Log.w("FlyActivity", "응답 실패: " + response.errorBody());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResultSearchKeword> call, Throwable t) {
//                // 통신 실패
//                Log.w("FlyActivity", "통신 실패: " + t.getMessage());
//            }
//        });
//    }

    private void requestLocation() {
        // 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // 마지막 위치 가져오기
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            handleLocationUpdate(lastKnownLocation); // 마지막 위치 처리
        } else {
            Toast.makeText(this, "마지막 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // 실시간 위치 업데이트 요청
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                handleLocationUpdate(location); // 새로운 위치 처리
                stopGPS(); // GPS 해제
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(FlyActivity.this, "GPS가 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        // GPS 업데이트 요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    private void handleLocationUpdate(Location location) {
        gpsLatitude = location.getLatitude();
        gpsLongitude = location.getLongitude();
        Toast.makeText(this, "GPS 위치: 위도=" + gpsLatitude + ", 경도=" + gpsLongitude, Toast.LENGTH_SHORT).show();
    }
    private void stopGPS() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGPS(); // Activity 종료 시 GPS 해제
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation(); // 권한 허용 후 위치 요청
            } else {
                Toast.makeText(this, "GPS 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFlightConfirmDialog(String title, int hours, int minutes, Runnable onConfirm) {
        // Custom Dialog 초기화
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_flight_confirm);

        // Dialog 창 크기 설정
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog UI 요소 참조
        TextView flightTitle = dialog.findViewById(R.id.flightTitle);
        TextView flightMessage = dialog.findViewById(R.id.flightMessage);
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        // 데이터 설정
        flightTitle.setText(title);
        flightMessage.setText(String.format("비행을 보내시겠습니까?\n예상 시간: %d시간 %d분", hours, minutes));

        // 확인 버튼 클릭 이벤트
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss(); // Dialog 닫기
            onConfirm.run(); // 비행 실행
        });

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dialog 닫기

        // Dialog 표시
        dialog.show();
    }

}

