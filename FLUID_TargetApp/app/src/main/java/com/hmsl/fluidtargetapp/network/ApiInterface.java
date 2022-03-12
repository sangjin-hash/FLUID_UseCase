package com.hmsl.fluidtargetapp.network;

import com.hmsl.fluidtargetapp.model.Sample;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("sample.php")
    Call<List<Sample>> getSample();
}
