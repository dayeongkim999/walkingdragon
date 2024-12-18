package com.example.walkingdragon;

import android.util.Log;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class CoordinateUtils {

    public static double[] processCoordinates(long mapx, long mapy) {
        // mapx: 정수 3자리 + 소수점 나머지
        double processedMapx = mapx / 10_000_000.0; // 1269763817 -> 126.9763817

        // mapy: 정수 2자리 + 소수점 나머지
        double processedMapy = mapy / 10_000_000.0; // 375706325 -> 37.5706325

        return new double[]{processedMapx, processedMapy};
    }
    // KATECH -> WGS84 변환
    public static double[] katechToWgs84(double katechX, double katechY) {
        // CRS 정의
        CRSFactory crsFactory = new CRSFactory();

        // KATECH 좌표계 정의
        String katechProj = "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=GRS80 +units=m +no_defs";
        CoordinateReferenceSystem katechSystem = crsFactory.createFromParameters("KATECH", katechProj);

        // WGS84 좌표계 정의
        String wgs84Proj = "+proj=longlat +datum=WGS84 +no_defs";
        CoordinateReferenceSystem wgs84System = crsFactory.createFromParameters("WGS84", wgs84Proj);

        // 좌표 변환 객체 생성
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(katechSystem, wgs84System);

        // 변환할 좌표 지정
        ProjCoordinate source = new ProjCoordinate(katechX, katechY);
        ProjCoordinate target = new ProjCoordinate();

        // 변환 실행
        transform.transform(source, target);

        // 로그 출력
        Log.d("CoordinateTransform", String.format("KATECH -> WGS84: KATECH(%f, %f) -> WGS84(%f, %f)", katechX, katechY, target.y, target.x));

        // 결과 반환 (위도, 경도)
        return new double[]{target.y, target.x};
    }
    // TM128 -> WGS84 변환
    public static double[] tm128ToWgs84(double tmX, double tmY) {
        // CRS 정의
        CRSFactory crsFactory = new CRSFactory();

        // TM128 좌표계 정의
        String tm128Proj = "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=200000 +y_0=500000 +ellps=GRS80 +units=m +no_defs";
        CoordinateReferenceSystem tm128System = crsFactory.createFromParameters("TM128", tm128Proj);

        // WGS84 좌표계 정의
        String wgs84Proj = "+proj=longlat +datum=WGS84 +no_defs";
        CoordinateReferenceSystem wgs84System = crsFactory.createFromParameters("WGS84", wgs84Proj);

        // 좌표 변환 객체 생성
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(tm128System, wgs84System);

        // 변환할 좌표 지정
        ProjCoordinate source = new ProjCoordinate(tmX, tmY);
        ProjCoordinate target = new ProjCoordinate();

        // 변환 실행
        transform.transform(source, target);

        // 로그 출력
        Log.d("CoordinateTransform", String.format("TM128 -> WGS84: TM128(%f, %f) -> WGS84(%f, %f)", tmX, tmY, target.y, target.x));

        // 결과 반환 (위도, 경도)
        return new double[]{target.y, target.x};
    }

    // WGS84 -> TM128 변환
    public static double[] wgs84ToTm128(double lat, double lon) {
        // CRS 정의
        CRSFactory crsFactory = new CRSFactory();

        // WGS84 좌표계 정의
        String wgs84Proj = "+proj=longlat +datum=WGS84 +no_defs";
        CoordinateReferenceSystem wgs84System = crsFactory.createFromParameters("WGS84", wgs84Proj);

        // TM128 좌표계 정의
        String tm128Proj = "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=200000 +y_0=500000 +ellps=GRS80 +units=m +no_defs";
        CoordinateReferenceSystem tm128System = crsFactory.createFromParameters("TM128", tm128Proj);

        // 좌표 변환 객체 생성
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = ctFactory.createTransform(wgs84System, tm128System);

        // 변환할 좌표 지정
        ProjCoordinate source = new ProjCoordinate(lon, lat);
        ProjCoordinate target = new ProjCoordinate();

        // 변환 실행
        transform.transform(source, target);

        // 로그 출력
        Log.d("CoordinateTransform", String.format("WGS84 -> TM128: WGS84(%f, %f) -> TM128(%f, %f)", lat, lon, target.x, target.y));

        // 결과 반환 (TM128 x, y)
        return new double[]{target.x, target.y};
    }

    // WGS84 좌표 간 거리 계산
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반경 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 결과값: 거리 (km)
    }

    public static double calculateTm128Distance(double x1, double y1, double x2, double y2) {
        // 두 TM128 좌표 간 유클리드 거리 계산
        double dx = x2 - x1; // x 좌표 차이
        double dy = y2 - y1; // y 좌표 차이
        double distanceInMeters = Math.sqrt(dx * dx + dy * dy); // 피타고라스 정리를 이용한 거리 계산
        return distanceInMeters / 1000.0; // 결과를 km 단위로 변환
    }
}
