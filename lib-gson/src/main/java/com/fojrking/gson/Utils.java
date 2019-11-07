package com.fojrking.gson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;


/**
 * 字符串转换，使用包装类
 * 原理：消费调用无法解析的类型对用的json，即可跳过这个字段
 * {@link JsonReader}提供了方法  {@link JsonReader#skipValue()}
 * 读取跳过没有进行解析性能好于其他 {@link JsonReader#nextDouble()} 、{@link JsonReader#nextString()}  、{@link JsonReader#nextName()}
 */
class Utils {

    /**
     * 整形转换
     *
     * @param data 输入
     * @return Integer
     */
    private static Integer toInt(String data) {
        Integer result = 0;
        try {
            result = Integer.valueOf(data);
        } catch (Exception e) {
            // TODO
        }
        return result;
    }

    /**
     * Long转换
     *
     * @param data 输入
     * @return Long
     */
    private static Long toLong(String data) {
        Long result = 0L;
        try {
            result = Long.valueOf(data);
        } catch (Exception e) {
            // TODO
        }
        return result;
    }

    /**
     * 浮点转换
     *
     * @param data 输入
     * @return Float
     */
    private static Float toFloat(String data) {
        Float result = 0.0f;
        if (data != null && data.length() > 0) {
            try {
                result = Float.valueOf(data);
            } catch (Exception e) {
                // TODO
            }
        }
        return result;
    }

    /**
     * 浮点转换
     *
     * @param data 输入
     * @return Double
     */
    private static Double toDouble(String data) {
        Double result = 0.0;
        try {
            result = Double.valueOf(data);
        } catch (Exception e) {
            // TODO
        }
        return result;
    }

    /**
     * 数字处理适配器
     *
     * @param type 0(int.class, Integer.class ) 1(short.class, Short.class) 2(long.class,
     *             Long.class) 3(double.class, Double.class) 4(float.class, Float.class)
     */
    public static TypeAdapter<Number> numberAdapter(final int type) {

        return new TypeAdapter<Number>() {
            @Override
            public Number read(JsonReader in) throws IOException {
                JsonErrorHandler.checkJsonToken(in, JsonToken.NUMBER);
                boolean isNot = false;
                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    isNot = true;
                } else if (peek == JsonToken.BEGIN_OBJECT) {
                    //增加判断是错误OBJECT的类型（应该是Number）,移动in的下标到结束，移动下标的代码在下方
//                    Utils.readObject(in);
                    in.skipValue();
                    isNot = true;
                } else if (peek == JsonToken.NAME) {
                    //增加判断是错误的name的类型（应该是Number）,移动in的下标到结束，移动下标的代码在下方
//                    in.nextName();
                    in.skipValue();
                    isNot = true;
                } else if (peek == JsonToken.BOOLEAN) {
                    //增加判断是错误的boolean的类型（应该是Number）,移动in的下标到结束，移动下标的代码在下方
                    in.nextBoolean();
                    isNot = true;
                } else if (peek == JsonToken.BEGIN_ARRAY) {
                    //增加判断是错误的array的类型（应该是Number）,移动in的下标到结束，移动下标的代码在下方
//                    readArray(in);
                    in.skipValue();
                    isNot = true;
                }
                // DES: 不是以上类型  则有 STRING NUMBER
                if (isNot) {
                    switch (type) {
                        case 0:
                            return 0;
                        case 1:
                            return (short) 0;
                        case 2:
                            return 0L;
                        case 3:
                            return (double) 0;
                        case 4:
                            return (float) 0;
                        default:
                            return 0;
                    }
                }
                try {
                    switch (type) {
                        case 0:
                            if (peek == JsonToken.STRING) {
                                return Utils.toInt(in.nextString());
                            }
                            return in.nextInt();
                        case 1:
                            if (peek == JsonToken.STRING) {
                                return Utils.toInt(in.nextString()).shortValue();
                            }
                            return (short) in.nextInt();
                        case 2:
                            if (peek == JsonToken.STRING) {
                                return Utils.toLong(in.nextString());
                            }
                            return in.nextLong();
                        case 3:
                            if (peek == JsonToken.STRING) {
                                return Utils.toDouble(in.nextString());
                            }
                            return in.nextDouble();
                        case 4:
                            if (peek == JsonToken.STRING) {
                                return Utils.toFloat(in.nextString());
                            }
                            return (float) in.nextDouble();
                        default:
                    }
                    return in.nextLong();
                } catch (NumberFormatException e) {
                    JsonErrorHandler.onJsonTokenParseException(in, e);
                    throw new JsonSyntaxException(e);
                }
            }

            @Override
            public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value);
            }
        };
    }

    /**
     * 处理字符的适配器
     */
    public static TypeAdapter<String> stringTypeAdapter() {

        return new TypeAdapter<String>() {

            @Override
            public String read(JsonReader in) throws IOException {
                JsonErrorHandler.checkJsonToken(in, JsonToken.STRING);

                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return "";
                } else if (peek == JsonToken.BOOLEAN) {
                    return Boolean.toString(in.nextBoolean());
                } else if (peek == JsonToken.BEGIN_OBJECT) {
//                    Utils.readObject(in);
                    in.skipValue();
                    return "";
                } else if (peek == JsonToken.NAME) {
                    //增加判断是错误的name的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
                    in.skipValue();
                    return "";
                } else if (peek == JsonToken.BEGIN_ARRAY) {
                    //增加判断是错误的ARRAY的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
//                    Utils.readArray(in);
                    in.skipValue();
                    return "";
                }

                return in.nextString();
            }

            @Override
            public void write(JsonWriter out, String value) throws IOException {
                out.value(value);
            }
        };
    }

    /**
     * DES: boolean适配器
     * TIME: 2019/11/5 0005 下午 6:59
     */
    public static TypeAdapter<Boolean> booleanTypeAdapter() {

        return new TypeAdapter<Boolean>() {
            @Override
            public Boolean read(JsonReader in) throws IOException {
                JsonErrorHandler.checkJsonToken(in, JsonToken.BOOLEAN);
                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return false;
                } else if (peek == JsonToken.STRING) {
                    return Boolean.parseBoolean(in.nextString());
                } else if (peek == JsonToken.BEGIN_OBJECT) {
                    in.skipValue();
                    return false;
                } else if (peek == JsonToken.NAME) {
                    //增加判断是错误的name的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
                    in.skipValue();
                    return false;
                } else if (peek == JsonToken.BEGIN_ARRAY) {
                    //增加判断是错误的ARRAY的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
                    in.skipValue();
                    return false;
                } else if (peek == JsonToken.NUMBER) {
                    // C语言规定,1为真(TRUE), 0为假(FALSE) emmmm 纠结
                    double aDouble = in.nextDouble();
                    if (aDouble == 1d) {
                        return true;
                    } else {
                        return false;
                    }
                }

                return in.nextBoolean();
            }

            @Override
            public void write(JsonWriter out, Boolean value) throws IOException {
                out.value(value);
            }
        };
    }

    /**
     * 消费掉 jsonArray
     */
    static void readArray(JsonReader in) throws IOException {
        in.beginArray();
        readJson(in);
        in.endArray();
    }

    /**
     * 消费掉 jsonObject
     */
    static void readObject(JsonReader in) throws IOException {
        in.beginObject();
        readJson(in);
        in.endObject();
    }

    /**
     * 消费整个json数据
     *
     * @param in json数据
     * @throws IOException
     */
    private static void readJson(JsonReader in) throws IOException {
        while (in.hasNext()) {
            JsonToken peek = in.peek();
            if (peek == JsonToken.BEGIN_ARRAY) {
                readArray(in);
            } else if (peek == JsonToken.NUMBER) {
                in.nextDouble();
            } else if (peek == JsonToken.STRING) {
                in.nextString();
            } else if (peek == JsonToken.NULL) {
                in.nextNull();
            } else if (peek == JsonToken.NAME) {
                in.nextName();
            } else if (peek == JsonToken.BOOLEAN) {
                in.nextBoolean();
            } else if (peek == JsonToken.BEGIN_OBJECT) {
                readObject(in);
            }
        }
    }
}
