package com.fojrking.gson;

import com.google.gson.stream.JsonReader;

import java.io.StringReader;

/**
 * @Description: 用于提供空的对象和数组对外使用，此操作会给没有给出json字符全部赋值
 * @Author: 岛主
 * @CreateDate: 2019/11/4 0004 下午 5:08
 * @Version: 1.0.0
 */
final class EmptyAdapter {
    public static final String EMPTY_NUM = "0";
    public static final String EMPTY_ARRAY = "[]";
    public static final String EMPTY_OBJ = "{}";


    public static JsonReader getJsonReader(String json) {
        return new JsonReader(new StringReader(json));
    }
}
