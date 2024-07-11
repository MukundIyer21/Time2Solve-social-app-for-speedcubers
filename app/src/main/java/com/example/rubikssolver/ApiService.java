package com.example.rubikssolver;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("predict")
    Call<ApiResponse> predictImage(@Body ImageRequest imageRequest);

    @POST("scramble")
    Call<ScrambleResponse> getScramble();

}