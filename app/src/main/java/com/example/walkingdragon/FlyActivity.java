package com.example.walkingdragon;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class FlyActivity extends AppCompatActivity {

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
    }


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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
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