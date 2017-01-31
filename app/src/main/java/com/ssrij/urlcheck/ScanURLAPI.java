package com.ssrij.urlcheck;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ScanURLAPI {

    String BASE_URL = "https://www.circl.lu/urlabuse/";

    @GET("_result/{id}")
    Call<Object> getScanResults(@Path("id") String id);

    @POST("virustotal_report")
    Call<String> getScanResultID(@Body ScanRequestBody body);
}
