package com.ginko.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyDataUtils {
	/*
	 * 
	 */
	public static String convert(String sourceStr){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat targetFormat = new SimpleDateFormat("MMM dd, yyyy");
		Date date = new Date();
		try {
			 date = format.parse(sourceStr);
		} catch (ParseException e) {
			Logger.error(e);
		}
		return targetFormat.format(date);
	}

	public static String convertFullMonthString(String sourceStr){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM dd, yyyy");
		Date date = new Date();
		try {
			date = format.parse(sourceStr);
		} catch (ParseException e) {
			Logger.error(e);
		}
		return targetFormat.format(date);
	}

	public static String format(Date date){
	     SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     return mDateFormat.format(date);
	}

	public static String chatTimeFormat(Date date){
		//SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");
		SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
		return mDateFormat.format(date);
	}

    public static String amPmFormat(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");
        return format.format(date);
    }

	public static Date convertUTCTimeToLocalTime(String strTime)
	{
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date time = simpleDateFormat.parse(strTime);
			return time;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return Calendar.getInstance().getTime();
	}


    public static Date parse(String datestr){
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return mDateFormat.parse(datestr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

	public static Date parseOnlyDate(String datestr){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat targetFormat = new SimpleDateFormat("MMM dd, yyyy");
		Date date = new Date();
		try {
			date = format.parse(datestr);
			datestr = targetFormat.format(date);
			date = targetFormat.parse(datestr);
		} catch (ParseException e) {
			Logger.error(e);
		}
		return date;

	}
}
