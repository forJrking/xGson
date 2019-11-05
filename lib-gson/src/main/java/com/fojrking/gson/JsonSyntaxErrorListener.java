package com.fojrking.gson;

import android.util.Log;

/**
 * @Description: com.fojrking.gson
 * @Author: 岛主
 * @CreateDate: 2019/11/5 0005 上午 11:33
 * @Version: 1.0.0
 */
public interface JsonSyntaxErrorListener {

    JsonSyntaxErrorListener DEFAULT = new JsonSyntaxErrorListener() {
        @Override
        public void onJsonSyntaxError(String exception, String invokeStack) {
            Log.e("json", "syntax exception: " + exception);
            Log.e("json", "stack exception: " + invokeStack);
        }
    };

    void onJsonSyntaxError(String exception, String invokeStack);
}
