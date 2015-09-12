package com.yuncore.bdsync.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static String formatTime(long time) {
		final SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(time));
	}

	public static long current_time_ss() {
		return System.currentTimeMillis() / 1000;
	}

}
