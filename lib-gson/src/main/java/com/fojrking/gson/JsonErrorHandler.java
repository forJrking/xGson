package com.fojrking.gson;

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
        if (listener != null) {
            mListener = listener;
        }
    }

    /**
     * used for array、collection、map、object
     * skipValue when expected token error
     *
     * @param in            input json reader
     * @param expectedToken expected token
     */
    public static void checkJsonToken(JsonReader in, JsonToken expectedToken) {
        if (in == null || expectedToken == null) {
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
        if (inToken != JsonToken.NULL) {
            String exception = "expected " + expectedToken + " but was " + inToken + " path " + in.getPath();
            exception = exception + "\nJson: " + getJson(in);
            notifyJsonSyntaxError(exception);
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
        if (in == null || exception == null) {
            return;
        }
        String json = getJson(in);
        // DES: 获取原来in内容
        notifyJsonSyntaxError(exception.getMessage() + "\n" + json);
    }

    private static String getJson(JsonReader in) {
        StringBuilder json = new StringBuilder();
        try {
            Class<? extends JsonReader> c = in.getClass();
            Field field = c.getDeclaredField("in");
            field.setAccessible(true);
            Object o = field.get(in);
            if (o instanceof StringReader) {
                StringReader reader = (StringReader) o;
                reader.reset();
                int i;
                while ((i = reader.read()) != -1) {
                    json.append((char) i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private static void notifyJsonSyntaxError(String exception) {
        if (mListener == null) {
            return;
        }
        String invokeStack = Log.getStackTraceString(new Exception("syntax exception"));
        mListener.onJsonSyntaxError(exception, invokeStack);
    }

}
