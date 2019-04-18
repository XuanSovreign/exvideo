package com.example.tang7.exvideo;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by licht on 2018/12/4.
 */

public class TimeUtil {
    public static long string2Milliseconds(String time, SimpleDateFormat format) {
        try {
            return format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
