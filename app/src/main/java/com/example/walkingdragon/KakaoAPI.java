//package com.example.walkingdragon;
//import retrofit2.Call;
//import retrofit2.http.GET;
//import retrofit2.http.Header;
//import retrofit2.http.Query;
//
//public interface KakaoAPI {
//
//    @GET("v2/local/search/keyword.json") // Keyword.json의 정보를 받아옴
//    Call<ResultSearchKeword> getSearchKeyword(
//            @Header("Authorization") String key, // 카카오 API 인증키 [필수]
//            @Query("query") String query         // 검색을 원하는 질의어 [필수]
//            //@Query("category_group_code") String category,
//            //@Query("x") String x,
//            //@Query("y") String y,
//            //@Query("radius") Integer radius
//    );
//}
