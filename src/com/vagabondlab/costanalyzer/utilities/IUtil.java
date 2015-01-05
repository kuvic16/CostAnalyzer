package com.vagabondlab.costanalyzer.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

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
	
	public static int getSeizeInDp(Context context, int size){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float dp = 378f;
		float fpixels = metrics.density * dp;
		int pixels = (int) (fpixels + 0.5f);
		return pixels;
	}
	
	public static TextView getTextView(Context context, String text){
		TextView label = new TextView(context);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
        label.setTextColor(Color.BLACK);
        label.setText(text);
        return label;
	}
	
	public static TextView getSmallTextView(Context context, String text){
		TextView label = new TextView(context);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        label.setTextColor(Color.BLACK);
        label.setText(text);
        return label;
	}
	
	@SuppressWarnings("deprecation")
	public static View getDividerView(Context context){
		View dividerView = new View(context);
		dividerView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
		dividerView.setBackgroundColor(Color.LTGRAY);
        return dividerView;
	}
	
	public static String generateUniqueID() {
		 String pt1 = Long.toString((new Date()).getTime());
		 UUID uuid = UUID.randomUUID();
		 String pt2 = uuid.toString().substring(18);
		 return pt1 + pt2;
	 }
}
