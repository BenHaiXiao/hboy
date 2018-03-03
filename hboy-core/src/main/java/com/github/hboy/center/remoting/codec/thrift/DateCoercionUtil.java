package com.github.hboy.center.remoting.codec.thrift;

import java.util.Date;

import com.facebook.swift.codec.internal.coercion.FromThrift;
import com.facebook.swift.codec.internal.coercion.ToThrift;
/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 20:57
 */
public class DateCoercionUtil {

    @ToThrift
    public static long dateToLong(Date d) {
        if (null == d) {
            return 0;
        }
        return d.getTime();
    }

    @FromThrift
    public static Date longToDate(long time) {
        if(0 == time){
            return null;
        }
        return new Date(time);
    }
}
