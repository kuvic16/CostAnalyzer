package com.vagabondlab.costanalyzer.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

@SuppressLint("SimpleDateFormat")
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
	DateSetListener listener;

	public interface DateSetListener{
	    public void returnDate(String date);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		listener = (DateSetListener) getActivity(); 
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
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
