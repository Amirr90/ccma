package com.ccma;

import com.ccma.Modals.ExcelModel;
import com.ccma.Modals.ItemModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {


    @GET("exec")
    Call<ItemModel> updateExcelData(@Query("action") String action,
                                    @Query("ss_id") String ss_id);


}

