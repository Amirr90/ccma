package com.ccma.Utility;


import com.ccma.Modals.Demomodel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {


    @GET("getAccounts")
    Call<Demomodel> getAccounts(@Query("email") String email,
                                @Query("hash") String hash);

}
