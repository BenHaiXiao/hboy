package com.github.hboy.common.config.annotation.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 22:22
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMethod {
    String value();
}
