package com.ccma.Utility;

import com.ccma.Modals.AccountRes;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCall {
    public static void getAllAccount(String email, final ApiCallbackInterface apiCallbackInterface) {

        try {
            final Api api = ApiUtils.getAPIService();
            Call<AccountRes> call = api.getAccountsData(email);
            call.enqueue(new Callback<AccountRes>() {
                @Override
                public void onResponse(@NotNull Call<AccountRes> call, @NotNull Response<AccountRes> response) {
                    if (response.code() != 200) {
                        apiCallbackInterface.onFailed(String.valueOf(response.code()));
                        return;
                    }

                    AccountRes res = response.body();
                    apiCallbackInterface.onSuccess(res.getResponseValue());


                }

                @Override
                public void onFailure(@NotNull Call<AccountRes> call, @NotNull Throwable t) {
                    apiCallbackInterface.onFailed(t.getLocalizedMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            apiCallbackInterface.onFailed(e.getLocalizedMessage());
        }
    }
}
