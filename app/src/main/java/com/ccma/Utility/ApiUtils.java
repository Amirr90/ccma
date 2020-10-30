package com.ccma.Utility;

public class ApiUtils {

    private ApiUtils() {
    }

    public static final String BASE_URL = "https://us-central1-ccma-94882.cloudfunctions.net/";

    public static Api getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(Api.class);

    }
}