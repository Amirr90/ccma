package com.ccma.Utility;

import java.util.List;

public interface ApiCallbackInterface {
    void onSuccess(Object object);

    void onFailed(String msg);
}
