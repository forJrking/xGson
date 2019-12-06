package com.fojrking.gson;

import android.os.Build;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

/**
 * @Description: Json 错误监听器插入代码监听 JSON 错误
 * @Author: 岛主
 * @CreateDate: 2019/11/5 0005 上午 11:27
 * @Version: 1.0.0
 */
public class JsonErrorHandler {

    static JsonSyntaxErrorListener mListener = JsonSyntaxErrorListener.DEFAULT;

    public static void setListener(JsonSyntaxErrorListener listener) {
        mListener = listener;
    }

    /**
     * used for array、collection、map、object
     * skipValue when expected token error
     *
     * @param in            input json reader
     * @param expectedToken expected token
     */
    public static void checkJsonToken(JsonReader in, JsonToken expectedToken) {
        if (mListener == null || expectedToken == null || in == null) {
            return;
        }
        JsonToken inToken = null;
        try {
            inToken = in.peek();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inToken == expectedToken) {
            return;
        }
        if ((expectedToken == JsonToken.NUMBER) && inToken == JsonToken.STRING) {
            return;
        }
        if (inToken != JsonToken.NULL) {
            String exception = "expected " + expectedToken + " but was " + inToken + " path " + in.getPath();
            exception = exception + "\nJson: " + getJson(in);
            String invokeStack = Log.getStackTraceString(new Exception("syntax exception"));
            mListener.onJsonSyntaxError(exception, invokeStack);
        }
    }

    /**
     * used for basic data type, we only deal type Number and Boolean
     * skipValue when json parse error
     *
     * @param in        input json reader
     * @param exception json parse exception
     */
    public static void onJsonTokenParseException(JsonReader in, Exception exception) {
        if (mListener == null || exception == null || in == null) {
            return;
        }
        // DES: 获取原来in内容
        String invokeStack = Log.getStackTraceString(new Exception("syntax exception"));
        mListener.onJsonSyntaxError(exception.getMessage(), invokeStack);
    }

    private static String getJson(JsonReader in) {
        String json = "restore json is not supported";
        // DES: 9.0获取不到不用进入了
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            try {
                Class<? extends JsonReader> c = in.getClass();
                Field field = c.getDeclaredField("in");
                field.setAccessible(true);
                Object o = field.get(in);
                if (o instanceof StringReader) {
                    StringReader reader = (StringReader) o;
                    Field str = reader.getClass().getDeclaredField("str");
                    str.setAccessible(true);
                    json = (String) str.get(reader);
                }
            } catch (Exception e) {
//            e.printStackTrace();
            }
        }
        return json;
    }
}
