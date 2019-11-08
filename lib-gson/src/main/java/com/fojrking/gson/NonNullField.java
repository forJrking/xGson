package com.fojrking.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 用于是否对类字段兜底，或者忽略的字段
 * @Author: 岛主
 * @Copyright: 浙江集商优选电子商务有限公司
 * @CreateDate: 2019/11/7 0007 下午 5:24
 * @Version: 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NonNullField {

    /*** @return 不需要兜底的字段名称数组*/
    String[] value() default {};
}
