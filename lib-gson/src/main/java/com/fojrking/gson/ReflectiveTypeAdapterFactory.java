package com.fojrking.gson;

import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.internal.reflect.ReflectionAccessor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;


/**
 * 自定义对象Object解析适配器
 */
class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final Excluder excluder;
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();
    private final ArrayList<Class> typeTokens;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
                                        FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.typeTokens = new ArrayList<>();
        this.typeTokens.add(String.class);
        this.typeTokens.add(Integer.class);
        this.typeTokens.add(Boolean.class);
        this.typeTokens.add(Byte.class);
        this.typeTokens.add(Short.class);
        this.typeTokens.add(Long.class);
        this.typeTokens.add(Double.class);
        this.typeTokens.add(Float.class);
        this.typeTokens.add(Number.class);
        this.typeTokens.add(AtomicInteger.class);
        this.typeTokens.add(AtomicBoolean.class);
        this.typeTokens.add(AtomicLong.class);
        this.typeTokens.add(AtomicLongArray.class);
        this.typeTokens.add(AtomicIntegerArray.class);
        this.typeTokens.add(Character.class);
        this.typeTokens.add(StringBuilder.class);
        this.typeTokens.add(StringBuffer.class);
        this.typeTokens.add(BigDecimal.class);
        this.typeTokens.add(BigInteger.class);
        this.typeTokens.add(URL.class);
        this.typeTokens.add(URI.class);
        this.typeTokens.add(UUID.class);
        this.typeTokens.add(Currency.class);
        this.typeTokens.add(Locale.class);
        this.typeTokens.add(InetAddress.class);
        this.typeTokens.add(BitSet.class);
        this.typeTokens.add(Date.class);
        this.typeTokens.add(GregorianCalendar.class);
        this.typeTokens.add(Calendar.class);
        this.typeTokens.add(Time.class);
        this.typeTokens.add(java.sql.Date.class);
        this.typeTokens.add(Timestamp.class);
        this.typeTokens.add(Class.class);
    }

    public boolean excludeField(Field f, boolean serialize) {
        return excludeField(f, serialize, excluder);
    }

    static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
        return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
    }

    /** first element holds the default name */
    private List<String> getFieldNames(Field f) {
        SerializedName annotation = f.getAnnotation(SerializedName.class);
        if (annotation == null) {
            String name = fieldNamingPolicy.translateName(f);
            return Collections.singletonList(name);
        }
        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }
        List<String> fieldNames = new ArrayList<String>(alternates.length + 1);
        fieldNames.add(serializedName);
        for (String alternate : alternates) {
            fieldNames.add(alternate);
        }
        return fieldNames;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();
        //不对以下类型做适配 以Gson有参构造顺序加入

        if (typeTokens.contains(raw)) {
            return null;
        }
        if ((type.getType() instanceof GenericArrayType || type.getType() instanceof Class &&
                ((Class<?>) type.getType()).isArray())) {
            return null;
        }
        if (!Object.class.isAssignableFrom(raw)) {
            return null;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            return null;
        }
        if (Map.class.isAssignableFrom(raw)) {
            return null;
        }
        JsonAdapter annotation = raw.getAnnotation(JsonAdapter.class);
        if (annotation != null) {
            return null;
        }
        if (Enum.class.isAssignableFrom(raw) && raw != Enum.class) {
            return null;
        }
        //结束
        ObjectConstructor<T> constructor = constructorConstructor.get(type);

        return new Adapter<T>(constructor, getBoundFields(gson, type, raw));
    }

    private ReflectiveTypeAdapterFactory.BoundField createBoundField(final Gson context, final Field field,
                                                                     final String name, final TypeToken<?> fieldType,
                                                                     boolean serialize, boolean deserialize) {

        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        // special casing primitives here saves ~5% on Android...
        JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = getTypeAdapter(constructorConstructor, context, fieldType, annotation);
        }
        final boolean jsonAdapterPresent = mapped != null;
        if (mapped == null) mapped = context.getAdapter(fieldType);

        final TypeAdapter<?> typeAdapter = mapped;
        return new ReflectiveTypeAdapterFactory.BoundField(name, field, serialize, deserialize) {

            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                TypeAdapter t = jsonAdapterPresent ? typeAdapter
                        : new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
                t.write(writer, fieldValue);
            }

            @Override
            void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }

            @Override
            public boolean writeField(Object value) throws IOException, IllegalAccessException {
                if (!serialized) {
                    return false;
                }
                Object fieldValue = field.get(value);
                return fieldValue != value;
            }
        };
    }


    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (raw.isInterface()) {
            return result;
        }

        Type declaredType = type.getType();
        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                boolean serialize = excludeField(field, true);
                boolean deserialize = excludeField(field, false);
                if (!serialize && !deserialize) {
                    continue;
                }
                accessor.makeAccessible(field);
                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
                List<String> fieldNames = getFieldNames(field);
                BoundField previous = null;
                for (int i = 0; i < fieldNames.size(); ++i) {
                    String name = fieldNames.get(i);
                    if (i != 0) {
                        // only serialize the default name
                        serialize = false;
                    }
                    BoundField boundField = createBoundField(context, field, name,
                            TypeToken.get(fieldType), serialize, deserialize);
                    BoundField replaced = result.put(name, boundField);
                    if (previous == null) {
                        previous = replaced;
                    }
                }
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    static abstract class BoundField {
        final String name;
        final Field field;
        final boolean serialized;
        final boolean deserialized;

        protected BoundField(String name, Field field, boolean serialized, boolean deserialized) {
            this.name = name;
            this.field = field;
            this.serialized = serialized;
            this.deserialized = deserialized;
        }

        abstract boolean writeField(Object value) throws IOException, IllegalAccessException;

        abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;

        abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
    }

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final ObjectConstructor<T> constructor;
        private final Map<String, BoundField> boundFields;
        //用于检测json没有给出应用类型字段
        private final Vector<String> boundFieldNames;

        Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
            // DES: 这里复制了原来的Map集合 清理后不能序列化对象了 WTF?? 所以只取名字集合了
            this.boundFieldNames = new Vector<>();
        }

        @Override
        public T read(JsonReader in) throws IOException {
            JsonErrorHandler.checkJsonToken(in, JsonToken.BEGIN_OBJECT);

            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                //成员变量给null时候这个地方帮助生成了变量
//                return constructor.construct();
                return null;
            }
            //增加判断是错误的ARRAY的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.BEGIN_ARRAY) {
//                Utils.readArray(in);
                in.skipValue();
                return constructor.construct();
            }
            //增加判断是错误的NUMBER的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.NUMBER) {
//                in.nextDouble();
                in.skipValue();
                return constructor.construct();
            }
            //增加判断是错误的String的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.STRING) {
//                in.nextString();
                in.skipValue();
                return constructor.construct();
            }
            //增加判断是错误的name的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.NAME) {
//                in.nextName();
                in.skipValue();
                return constructor.construct();
            }
            //增加判断是错误的bookean的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.BOOLEAN) {
                in.nextBoolean();
                return constructor.construct();
            }

            T instance = constructor.construct();
            try {
                // DES: 如果同一个类之前缺失，后面又给回来这里就出问题了坑爹
                boundFieldNames.addAll(boundFields.keySet());
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = boundFields.get(name);
                    if (field == null || !field.deserialized) {
                        in.skipValue();
                    } else {
                        field.read(in, instance);
                    }
                    boundFieldNames.remove(name);
                }
                // DES: 来一波注解处理
                if (!boundFieldNames.isEmpty()) {
                    NonField annotation = instance.getClass().getAnnotation(NonField.class);
                    //注解为空全部都解析
                    if (annotation != null) {
                        //这些包含字段忽略赋值
                        String[] ignoreAll = annotation.value();
                        if (ignoreAll.length == 0) {
                            boundFieldNames.clear();
                        } else {
                            for (String ignore : ignoreAll) {
                                boundFieldNames.remove(ignore);
                            }
                        }
                    }
                }
                // DES: 剩余的 boundFieldNames 就是json没有对应字段
                if (!boundFieldNames.isEmpty()) {
                    JsonErrorHandler.onJsonTokenParseException(in,
                            new Exception("class: " + instance.getClass().getName() +
                                    " lose field: " + boundFieldNames.toString()));

                    for (String otherKey : boundFieldNames) {

                        BoundField otherField = boundFields.get(otherKey);
                        if (otherField == null || !otherField.serialized || !otherField.deserialized) {
//                        不序列话跳过这个字段;
                            Log.w("json", "json exception no field:" + otherKey);
                        } else {
                            // DES: 常见类型反射出空的值但不是null
                            Class<?> type = otherField.field.getType();
                            if (Number.class.isAssignableFrom(type)) {
                                JsonReader jsonReader = EmptyAdapter.getJsonReader(EmptyAdapter.EMPTY_NUM);
                                otherField.read(jsonReader, instance);
                            } else if (CharSequence.class.isAssignableFrom(type)) {
                                JsonReader jsonReader = EmptyAdapter.getJsonReader(EmptyAdapter.EMPTY_STRING);
                                otherField.read(jsonReader, instance);
                            } else if (Collection.class.isAssignableFrom(type)) {
                                JsonReader jsonReader = EmptyAdapter.getJsonReader(EmptyAdapter.EMPTY_ARRAY);
                                otherField.read(jsonReader, instance);
                            } else if (type.isArray()) {
                                JsonReader jsonReader = EmptyAdapter.getJsonReader(EmptyAdapter.EMPTY_ARRAY);
                                otherField.read(jsonReader, instance);
                            }
//                        else if (Object.class.isAssignableFrom(type)
//                                && !instance.getClass().isAssignableFrom(type)) {
//                            // DES: 对象可能出现无限循环  自己内部持有自己变量 出现无限创建
//                            JsonReader jsonReader = EmptyApdater.getJsonReader(EmptyApdater.EMPTY_OBJ);
//                            otherField.read(jsonReader, instance);
//                        }
                        }
                    }
                    boundFieldNames.clear();
                }
            } catch (IllegalStateException e) {
                JsonErrorHandler.onJsonTokenParseException(in, e);
                throw new JsonSyntaxException(e);
            } catch (IllegalAccessException e) {
                JsonErrorHandler.onJsonTokenParseException(in, e);
                throw new AssertionError(e);
            }
            in.endObject();
            return instance;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            try {
                for (BoundField boundField : boundFields.values()) {
                    if (boundField.writeField(value)) {
                        out.name(boundField.name);
                        boundField.write(out, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            out.endObject();
        }
    }

    //com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory
    @SuppressWarnings({"unchecked", "rawtypes"})
    private TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
                                          TypeToken<?> type, JsonAdapter annotation) {
        Object instance = constructorConstructor.get(TypeToken.get(annotation.value())).construct();

        TypeAdapter<?> typeAdapter;
        if (instance instanceof TypeAdapter) {
            typeAdapter = (TypeAdapter<?>) instance;
        } else if (instance instanceof TypeAdapterFactory) {
            typeAdapter = ((TypeAdapterFactory) instance).create(gson, type);
        } else if (instance instanceof JsonSerializer || instance instanceof JsonDeserializer) {
            JsonSerializer<?> serializer = instance instanceof JsonSerializer
                    ? (JsonSerializer) instance : null;
            JsonDeserializer<?> deserializer = instance instanceof JsonDeserializer
                    ? (JsonDeserializer) instance : null;
            typeAdapter = new TreeTypeAdapter(serializer, deserializer, gson, type, null);
        } else {
            throw new IllegalArgumentException("Invalid attempt to bind an instance of "
                    + instance.getClass().getName() + " as a @JsonAdapter for " + type.toString()
                    + ". @JsonAdapter value must be a TypeAdapter, TypeAdapterFactory,"
                    + " JsonSerializer or JsonDeserializer.");
        }

        if (typeAdapter != null && annotation.nullSafe()) {
            typeAdapter = typeAdapter.nullSafe();
        }
        return typeAdapter;
    }
}
