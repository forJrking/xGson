package com.fojrking.gson;

import android.util.Log;

/**
 * @Description: gson 解析监听器
 * @Author: 岛主
 * @CreateDate: 2019/11/5 0005 上午 11:33
 * @Version: 1.0.0
 */
public interface JsonSyntaxErrorListener {

    JsonSyntaxErrorListener DEFAULT = new JsonSyntaxErrorListener() {
        @Override
        public void onJsonSyntaxError(String exception, String invokeStack) {
            Log.w("json", "syntax exception: " + exception);
            Log.w("json", "stack exception: " + invokeStack);
        }
    };

    /**
     * @param exception   异常的json位置和json
     * @param invokeStack 异常的调用栈堆
     */
    void onJsonSyntaxError(String exception, String invokeStack);
}
