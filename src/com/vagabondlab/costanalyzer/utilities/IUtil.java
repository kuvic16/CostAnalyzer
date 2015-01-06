package com.vagabondlab.costanalyzer.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.DatePicker;

public final class IUtil {
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	public static final String DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy";
	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDateTime(String format){
		String currentDate = "";
		try{
			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date = new Date();
			currentDate = dateFormat.format(date);
		}catch(Throwable t){
			System.out.println("Error: " + t);
		}
		return currentDate;
	}
	
	
	@SuppressLint("SimpleDateFormat")
	public static Date getDate(String datestring, String format){
		Date date = new Date();
		try{
			DateFormat dateFormat = new SimpleDateFormat(format);
			date = dateFormat.parse(datestring);
		}catch(Throwable t){
			System.out.println("Error: " + t);
		}
		return date;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Calendar getCalender(String datestring, String format){
		Calendar calender = Calendar.getInstance();
		try{
			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date = dateFormat.parse(datestring);
			calender.setTime(date);
		}catch(Throwable t){
			System.out.println("Error: " + t);
		}
		return calender;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getDateFromDatePicker(DatePicker datePicker, String dateFormat){
		try {
			int day = datePicker.getDayOfMonth();
			int month = datePicker.getMonth();
			int year = datePicker.getYear();

			Calendar c = Calendar.getInstance();
			c.set(year, month, day, 0, 0);

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			String formatedDate = sdf.format(c.getTime());
			return formatedDate;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}
	
	public static int getIntValueFromIntent(Intent intent, String key){
		int action = 0;
		try{
			action = intent.getExtras().getInt(key);
		}catch(Throwable t){
			t.printStackTrace();
		}
		return action;
	}
	
	public static boolean isNotBlank(String value){
    	return (value != null && value.length()>0)? true : false; 
    }
	
	public static boolean isNotBlank(Object value){
    	return (value != null && value.toString().length()>0)? true : false; 
    }
	
	public static boolean isNotBlank(List<Object> list){
    	return (list != null && list.size()>0)? true : false; 
    }
	
	public static String generateUniqueID() {
		 String pt1 = Long.toString((new Date()).getTime());
		 UUID uuid = UUID.randomUUID();
		 String pt2 = uuid.toString().substring(18);
		 return pt1 + pt2;
	 }
	
	public static Integer getKeyFromValue(Map<Integer, String> map, Object value) {
		for (Object o : map.keySet()) {
			if (map.get(o).equals(value)) {
				return (Integer)o;
			}
		}
		return null;
	}
}
