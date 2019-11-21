### Gson++

后台开发和前端开发往往使用不通语言，导致约定的Json数据在线上经常出现脏数据，要么APP直接崩溃要么界面展示异常。如何完美监控和进行脏数据容错处理呢？

#### Gson++优劣势

google 提供了优秀的`json`解析库`Gson`，一般通过自定义`TypeAdapter`可以实现**基础数据类型**兜底，但是**集合、数组、对象**这些复杂类型如何兜底比较难；

Gson++ 优势

- 解析过程发现数据和字段类型不对应，跳过此问题，继续解析下个字段。
- 解析过程中跳过的字段，可以自动给出默认值，例如` String  s = "" ;  List l = new List() ` 兜底null
- 解析中发现字段有缺失对应数据（一般为应用类型，基础数据类型可以自带默认值），可以自动给出默认值，目前支持 `String,Array,List`不支持`Object`，原因往下看
- 支持监控`json`解析，发现数据问题后可以上报

Gson++缺点

- 开启监控后性能相比Gson较高
- 不能像原来一样使用 `new Gson()`，如果可以使用AOP插入字节码就完美了。
- 支持的数据种类不够庞大

#### 原理

`JsonReader`消费掉用无法解析的类型对应的`json`,即可跳过这个字段,向后面继续解析。

跳过的字段通过赋值默认值即可兜底，复制`Gson`字段集合检测`json`缺失字段，同样可以默认值兜底。

```
Number      0
Boolean     false
Collection  new Collection()
String      ""
```

#### 效果

```json
{
    "array":["array1","array2"],
    "bean":
    {
        //此处缺失 List<String> mString 
        "int2":20,
        "list2":["l2--2","l2--1"]
    },
    "doubleB":11.0,
    "floatF":12.001,
    "intA":10,
    "isOk":100, //此处给错 boolean ->int
    "list":["list1","list2"],
    "longL":100000,
    "map":{"map1":"map1","map2":"map2"},
    "str":"str"
}

new Gson().fromJson ：无法成功解析 isOk 期望是 boolean 实际为 Number
GsonUtils.fromJson  : 成功解析所有数据  
其中  List<String> mString  = new ArrayList<String>   isOk = false;

```

<img src="cap.png"/>



#### 支持数据类型

对以下类型可以强支持，常见字段数据**给错**或**不给**都可以容错

数字`Number:int\Integer，double\Double,float\Float,long\Long,short\Short`

逻辑 `Boolean:boolean\Boolean`

集合`Array：array,Collection` 

字符`CharSequence:String,StringBuild`

对象`Object 这个比较特殊，没有给出对应字段不能帮助兜底，会循环卡死，例如自己包含自己 自己内部字段引用包含自己等`

```java
class Test{
  int i;
  String str;
  Test demo; //不支持此种 为了防止问题
  boolean isOk;//支持数字json 解析器完成转换（1 true 非1 false）
  //用法：api定义了 0,1 做开关 {isOk:1}，我们不用 int 直接用 boolean 
}
```

#### 用法

```java
Gson gson = GsonUtils.getGson() 
GsonUtils.fromJson(str,T.class)
//还原为原生Gson
GsonUtils.setGson(new Gson())
    
//注解用法   默认使用会对所有支持字段兜底
@NonField	//所有字段均不兜底，gson默认行为
@NonField(value = {"str","list"}) //指定这些字段不兜底 其他兜底
public class TestBean {
  int i;
  String str;
  List<String> list;
}

```

#### 监控

```java
//自己实现监控接口 ，默认会以控制台日志输出
JsonErrorHandler.setListener(new JsonSyntaxErrorListener() {
	@Override
    public void onJsonSyntaxError(String exception, String invokeStack) {
        	// 上报     日志中包含错误和原json数据便于直接定位错误
            Log.e("json", "syntax exception: " + exception);
            Log.e("json", "stack exception: " + invokeStack);
    }
});
//关闭监控 关闭json性能损耗
JsonErrorHandler.setListener(null);
```

#### 性能

解析和序列化分别循环1500次

| 用法           | 解析  | 序列化 |
| ------------- | ----- | ------ |
| `new Gson()`  | 100ms | 110ms  |
| `GsonUtils.getGson()` 关闭监控 | 103ms | 122ms  |
| `GsonUtils.getGson()` 开启监控 | 105ms | 679ms |


 * `{@link JsonReader}提供了方法  {@link JsonReader#skipValue()}`
 * 通过读取跳过字段,没有进行解析性能高于 `{@link JsonReader#nextDouble()} 、{@link JsonReader#nextString()}  、{@link JsonReader#nextName()}`

普通数据性能差距并不大，但是一旦出现`JSON`没有定义字段，兜底机制需要反射获取这些字段耗费极大性能，

另外如果关闭监控，性能几乎和原生一致

#### 作者

:man: 岛主                                 	:e-mail:  fojrking@sina.com

