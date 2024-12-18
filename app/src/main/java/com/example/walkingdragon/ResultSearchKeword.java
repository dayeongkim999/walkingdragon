package com.example.walkingdragon;

import java.util.List;

public class ResultSearchKeword {
    private List<String> documents;
}

class RegionInfo{
    List<String> region;        // 질의어에서 인식된 지역의 리스트, ex) '중앙로 맛집' 에서 중앙로에 해당하는 지역 리스트
    String keyword;              // 질의어에서 지역 정보를 제외한 키워드, ex) '중앙로 맛집' 에서 '맛집'
    String selected_region;        // 인식된 지역 리스트 중, 현재 검색에 사용된 지역 정보

}


class Place {
    String id;                     // 장소 ID
    String place_name;           // 장소명, 업체명
    String category_name;         // 카테고리 이름
    String category_group_code;    // 중요 카테고리만 그룹핑한 카테고리 그룹 코드
    String category_group_name;    // 중요 카테고리만 그룹핑한 카테고리 그룹명
    String x;                      // X 좌표값 혹은 longitude
    String y;                      // Y 좌표값 혹은 latitude
    String distance;             // 중심좌표까지의 거리. 단, x,y 파라미터를 준 경우에만 존재. 단위는 meter
}