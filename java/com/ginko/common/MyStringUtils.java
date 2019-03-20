package com.ginko.common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class MyStringUtils {
	public static String clean(String str) {
		if (str == null) {
			return null;
		}
		str = str.trim();
		if (str.equalsIgnoreCase("null")) {
			return "";
		}
		return str;

	}

	public static String randomCreator(int num) {
		Random random = new Random();

		String result = "";
		for (int j = 0; j < num; j++) {
			boolean intChar = random.nextInt(100) < 60;
			int i = intChar ? random.nextInt(9) : random.nextInt(26);
			char rc = (char) (i + (intChar ? (int) '0' : (int) 'A'));
			result += rc;
		}

		return result;
	}

	public static String mapToUrlQuery(Map<String, String> map) {
		if (map==null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Iterator<Entry<String, String>> iter = map.entrySet().iterator(); iter
				.hasNext();) {
			Entry<String, String> kv = iter.next();
			sb.append(kv.getKey()).append("=");

			try {
				sb.append(URLEncoder.encode(kv.getValue(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
				sb.append(kv.getValue());
			}
			sb.append("&");
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String arrayToString(Integer[] arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		String result = "";
		for (int i = 0; i < arr.length; i++) {
			result += arr[i];
			if (i < arr.length - 1) {
				result += ",";
			}
		}
		return result;
	}
	
	public static String arrayToString(String[] arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		String result = "";
		for (int i = 0; i < arr.length; i++) {
			result += arr[i];
			if (i < arr.length - 1) {
				result += ",";
			}
		}
		return result;
	}
	
	public static Integer[] stringToArray(String arrStr){
		return stringToArray(arrStr,",");
	}
	
	public static Integer[] stringToArray(String arrStr,String split){
		if (StringUtils.isBlank(arrStr)){
			return new Integer[0];
		}
		List<Integer> result = new ArrayList<Integer>();
		String[] tempStrArr = arrStr.split(split);
		for (String s : tempStrArr) {
			s=s.trim();
			if (!NumberUtils.isNumber(s)){
				continue;
			}
			result.add(Integer.valueOf(s));
		}
		return result.toArray(new Integer[0]);
	}
	
	
	public static String[] toNames(String fullName){
		if (StringUtils.isBlank(fullName)){
			return new String[0];
		}	List<String> result = new ArrayList<String>();
		String firstName=fullName;
		String lastName="";
		int index = fullName.indexOf(" ");
		if (index>0){
			firstName = fullName.substring(0, index);
			lastName= fullName.substring(index+1);
		}
		result.add(firstName);
		result.add(lastName);
		return result.toArray(new String[0]);
	}

    public static String exceptionToString(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }
}
