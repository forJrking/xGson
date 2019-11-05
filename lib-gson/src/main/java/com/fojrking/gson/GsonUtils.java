package com.fojrking.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.TypeAdapters;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @DES: json 解析工具
 * @AUTHOR: 岛主
 * @TIME: 2019/10/30 0030 下午 3:55
 * <p>
 * 使用方法
 * 1）MGson.newGson()得到Gson对象
 * 2)然后使用相对应的gson方法即可
 * 解决:
 * 1)要{}后端给[]返回实例对象
 * 2)要[]后端给{}返回空数组
 * 3)要int.class, Integer.class,short.class, Short.class,long.class, Long.class,double.class, Double.class,
 * float.class, Float.class后端给的非数字类型返回0
 * 4)要String后端给了[],{}等类型返回""
 */
public class GsonUtils {

    private GsonUtils() {
    }

    private static Gson gson = null;

    public static void setGson(Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson == null");
        }
        GsonUtils.gson = gson;
    }

    public static Gson getInstance() {
        return getGson();
    }

    /**
     * Gson 提供对外调用方法
     */
    public static Gson getGson() {
        if (gson == null) {
            synchronized (GsonUtils.class) {
                if (gson == null) {
                    gson = newGson();
                }
            }
        }
        return gson;
    }

    /**
     * 生成注册自定义的对象处理器与集合处理器的Gson，方法
     */
    private static Gson newGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        /*如果不设置serializeNulls,序列化时默认忽略NULL*/
        gsonBuilder.serializeNulls()
                .disableHtmlEscaping();
        /*使打印的json字符串更美观，如果不设置，打印出来的字符串不分行*/
//        .setPrettyPrinting();
        try {
            //注册String类型处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, Utils.stringTypeAdapter()));
            // boolean.class, Boolean.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, Utils.booleanTypeAdapter()));
            //注册int.class, Integer.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, Utils.numberAdapter(0)));
            //注册short.class, Short.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(short.class, Short.class, Utils.numberAdapter(1)));
            //注册long.class, Long.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, Utils.numberAdapter(2)));
            //注册double.class, Double.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, Utils.numberAdapter(3)));
            //注册float.class, Float.class处理器
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, Utils.numberAdapter(4)));
            //通过反射得到构造器 出现异常说明源码变动了
            Class builder = gsonBuilder.getClass();
            Field f = builder.getDeclaredField("instanceCreators");
            f.setAccessible(true);
            //得到此属性的值
            final Map<Type, InstanceCreator<?>> val = (Map<Type, InstanceCreator<?>>) f.get(gsonBuilder);
            //注册反射对象的处理器
            gsonBuilder.registerTypeAdapterFactory(new ReflectiveTypeAdapterFactory(new ConstructorConstructor(val), FieldNamingPolicy.IDENTITY, Excluder.DEFAULT));
            //注册集合的处理器
            gsonBuilder.registerTypeAdapterFactory(new CollectionTypeAdapterFactory(new ConstructorConstructor(val)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gsonBuilder.create();
    }

    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return getGson().fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws Exception {
        return getGson().fromJson(json, typeOfT);
    }

    public static <T> T fromJson(Reader reader, Type typeOfT) throws Exception {
        return getGson().fromJson(reader, typeOfT);
    }

    /**
     * 根据key获取某个值
     */
    public static final String getValue(String json, String key) {
        try {
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            return obj.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
