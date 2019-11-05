package com.fojrking.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
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
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 自定义列表解析适配器
 */
class CollectionTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final ArrayList<Class> typeTokens;

    public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
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

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        Class<? super T> rawType = typeToken.getRawType();
        //不对以下类型做适配 以Gson有参构造顺序加入
        if (typeTokens.contains(rawType)) {
            return null;
        }
        if ((typeToken.getType() instanceof GenericArrayType || typeToken.getType() instanceof Class && ((Class<?>) typeToken.getType()).isArray())) {
            return null;
        }
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }
        //结束
        Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
        TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
        ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);
        // create() doesn't define a type parameter
        @SuppressWarnings({"unchecked", "rawtypes"})
        TypeAdapter<T> result = new Adapter(gson, elementType, elementTypeAdapter, constructor);

        return result;
    }

    private static final class Adapter<E> extends TypeAdapter<Collection<E>> {

        private final TypeAdapter<E> elementTypeAdapter;
        private final ObjectConstructor<? extends Collection<E>> constructor;

        public Adapter(Gson context, Type elementType,
                       TypeAdapter<E> elementTypeAdapter,
                       ObjectConstructor<? extends Collection<E>> constructor) {
            this.elementTypeAdapter = new TypeAdapterRuntimeTypeWrapper<E>(context, elementTypeAdapter, elementType);
            this.constructor = constructor;
        }

        @Override
        public Collection<E> read(JsonReader in) throws IOException {
            JsonErrorHandler.checkJsonToken(in, JsonToken.BEGIN_ARRAY);

            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return constructor.construct();
            }
            //增加判断是错误OBJECT的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.BEGIN_OBJECT) {
                Utils.readObject(in);
                return constructor.construct();
            }
            //增加判断是错误的NUMBER的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.NUMBER) {
                in.nextDouble();
                return constructor.construct();
            }
            //增加判断是错误的String的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.STRING) {
                in.nextString();
                return constructor.construct();
            }
            //增加判断是错误的name的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.NAME) {
                in.nextName();
                return constructor.construct();
            }
            //增加判断是错误的boolean的类型（应该是object）,移动in的下标到结束，移动下标的代码在下方
            if (peek == JsonToken.BOOLEAN) {
                in.nextBoolean();
                return constructor.construct();
            }

            Collection<E> collection = constructor.construct();
            in.beginArray();
            while (in.hasNext()) {
                E instance = elementTypeAdapter.read(in);
                collection.add(instance);
            }
            in.endArray();
            return collection;
        }

        @Override
        public void write(JsonWriter out, Collection<E> collection) throws IOException {
            if (collection == null) {
                out.nullValue();
                return;
            }
            out.beginArray();
            for (E element : collection) {
                elementTypeAdapter.write(out, element);
            }
            out.endArray();
        }
    }
}
