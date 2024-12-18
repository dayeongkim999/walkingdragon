package com.example.walkingdragon;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    String latitude = "0";
    String longitude = "0";
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

        // TextView 참조
        TextView titleTextView = findViewById(R.id.titleTextView1);

        // 비동기 처리 스레드
        new Thread(new Runnable() {
            @Override
            public void run() {
                NaverAPI naverAPI = new NaverAPI();
                String result = naverAPI.searchLocal("병원");
                Log.d("NaverAPIResult", result);

                // JSON 파싱
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray items = jsonObject.getJSONArray("items");
                    if (items.length() > 0) {
                        JSONObject firstItem = items.getJSONObject(0);
                        String title = firstItem.getString("title"); // "옥토한의원" 등

                        // UI 업데이트는 메인스레드에서
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // titleTextView에 title 반영
                                titleTextView.setText(title);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//
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
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        Toast.makeText(this, "위도: " + latitude + ", 경도: " + longitude, Toast.LENGTH_LONG).show();
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
}