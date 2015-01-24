package com.vagabondlab.costanalyzer.utilities;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;

@SuppressLint("SimpleDateFormat")
public class MonthYearPickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
	DateSetListener listener;

	public interface DateSetListener{
	    public void returnDate(String date);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Calendar c = Calendar.getInstance();
		listener = (DateSetListener) getActivity();
		
		DatePickerDialog dlg = new DatePickerDialog(getActivity(),this, 
			    c.get(Calendar.YEAR), 
			    c.get(Calendar.MONTH), 
			    c.get(Calendar.DAY_OF_MONTH));
//			{
//			    @Override
//			    protected void onCreate(Bundle savedInstanceState)
//			    {
//			        super.onCreate(savedInstanceState);
//			        int day = getContext().getResources().getIdentifier("android:id/date", null, null);
//			        if(day != 0){
//			            View dayPicker = findViewById(day);
//			            if(dayPicker != null){
//			                dayPicker.setVisibility(View.GONE);
//			            }
//			        }
//			    }
//			};
		
			try{
			    Field[] datePickerDialogFields = dlg.getClass().getDeclaredFields();
			    for (Field datePickerDialogField : datePickerDialogFields) { 
			        if (datePickerDialogField.getName().equals("mDatePicker")) {
			            datePickerDialogField.setAccessible(true);
			            DatePicker datePicker = (DatePicker) datePickerDialogField.get(dlg);
			            Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();
			            for (Field datePickerField : datePickerFields) {
			               if ("mDayPicker".equals(datePickerField.getName())) {
			                  datePickerField.setAccessible(true);
			                  Object dayPicker = new Object();
			                  dayPicker = datePickerField.get(datePicker);
			                  ((View) dayPicker).setVisibility(View.GONE);
			               }
			            }
			         }
	
			      }
			    }catch(Exception ex){
			    }
		
		return dlg;
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);

		SimpleDateFormat sdf = new SimpleDateFormat(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String formattedDate = sdf.format(c.getTime());
		if (listener != null){
		  listener.returnDate(formattedDate); 
		}
	}

}
