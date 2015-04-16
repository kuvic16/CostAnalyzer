package com.vagabondlab.costanalyzer.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	public static final String DATE_FORMAT_EEE_D_MMM_YYYY = "EEE, d MMM yyyy";
	public static final String DATE_FORMAT_MMM_D_YY = "MMM d, ''yy";
	public static final String DATE_FORMAT_MMM = "MMM";
	
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
	public static String getDateFromCalender(Calendar calendar, String format){
		String dateString = "";
		try{
			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date =  calendar.getTime();
			dateString = dateFormat.format(date);
		}catch(Throwable t){
			System.out.println("Error: " + t);
		}
		return dateString;
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
	
	@SuppressLint("SimpleDateFormat")
	public static String changeDateFormat(String dateString, String oldFormat, String newFormat){
		Date date = new Date();
		try{
			DateFormat dateFormat = new SimpleDateFormat(oldFormat);
			date = dateFormat.parse(dateString);
			
			SimpleDateFormat sdf = new SimpleDateFormat(newFormat);
			return sdf.format(date); 
		}catch(Throwable t){
			System.out.println("Error: " + t);
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
	
	public static Integer getKeyFromValue(Map<Integer, String[]> map, Object value) {
		try{
			for (Object o : map.keySet()) {
				String[] cnt = (String[])map.get(o);
				if(cnt[0].equals(value)){
					return (Integer)o;
				}
			}
		}catch(Throwable t){
			t.printStackTrace();
		}
		return null;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static List<List<String>> getNumberOfWeeks(int month, int year) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
        List<List<String>> weekdates = new ArrayList<List<String>>();
        List<String> dates;
        
        Calendar c = Calendar.getInstance();
        
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, 1);
        while (c.get(Calendar.MONTH) == month) {
              dates = new ArrayList<String>();
              while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_MONTH, -1);
              }
              dates.add(format.format(c.getTime()));
              c.add(Calendar.DAY_OF_MONTH, 6);
              dates.add(format.format(c.getTime()));
              weekdates.add(dates);
              c.add(Calendar.DAY_OF_MONTH, 1);
        }
        System.out.println(weekdates);
        return weekdates;
    }
	
	public static int getCurrentWeek(List<List<String>> weeks, String date){
		int selected_week = 0;
		int position = 0;
		for(List<String> week : weeks){
			if(week.get(0).compareTo(date) <= 0 && week.get(1).compareTo(date) >= 0){
				selected_week = position + 1;
				break;
			}
			position++;
		}
		return selected_week;
	}
	
	@SuppressLint("SimpleDateFormat")
	private static int getWeek(String date){
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(format.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return calendar.get(Calendar.WEEK_OF_MONTH);
	}
	
	
	public static String theMonth(int month){
	    String[] monthNames = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	    return monthNames[month];
	}
	
	public static String firstDayOfYear(String year){
		return year + "-01-01";
	}
	
	public static String endDayOfYear(String year){
		return year + "-12-31";
	}
}
