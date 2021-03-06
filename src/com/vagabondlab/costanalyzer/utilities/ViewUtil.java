package com.vagabondlab.costanalyzer.utilities;

import com.vagabondlab.costanalyzer.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public final class ViewUtil {
	public static void showMessage(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
	
	public static TextView getTableColumn(Context context, String text, int gravity){
		TextView label = new TextView(context);
		label.setText(text);
        label.setGravity(gravity);
        label.setTextColor(Color.BLACK);
        return label;
	}
	
	public static TableRow getWeekDayTableHeader(Context context){
		TableRow header = new TableRow(context);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(context, context.getString(R.string.day), Gravity.LEFT));
		header.addView(getTableColumn(context, context.getString(R.string.total_cost), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.productive), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.wastage), Gravity.CENTER));
		return header;
	}
	
	public static TableRow getMonthTableHeader(Context context){
		TableRow header = new TableRow(context);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(context, context.getString(R.string.month), Gravity.LEFT));
		header.addView(getTableColumn(context, context.getString(R.string.total_cost), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.productive), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.wastage), Gravity.CENTER));
		return header;
	}
	
	public static TableRow getCategoryWiseCostTableHeader(Context context){
		TableRow header = new TableRow(context);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(context, context.getString(R.string.category_name), Gravity.LEFT));
		header.addView(getTableColumn(context, context.getString(R.string.total_cost), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.parcantage), Gravity.CENTER));
		return header;
	}
	
	public static TableRow getTransactionDetailsStatusTableHeader(Context context){
		TableRow header = new TableRow(context);
		header.setPadding(5, 0, 0, 0);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(context, context.getString(R.string.lend), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.borrow), Gravity.CENTER));
		header.addView(getTableColumn(context, context.getString(R.string.balance), Gravity.CENTER));
		return header;
	}
	
	@SuppressWarnings("deprecation")
	public static void createDialogWithOKButton(Context context,  String title, String message) {
		AlertDialog mAlertDialog = new AlertDialog.Builder(context).create();
		mAlertDialog.setTitle(title);
		mAlertDialog.setMessage(message);
		DialogInterface.OnClickListener dialogYesNoListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(context.getString(R.string.ok) , dialogYesNoListener);
		mAlertDialog.show();
	}
	
	public static String getIntantExtra(Intent intent, String key){
		String value = "";
		try{
			String result = intent.getExtras().getString(key);
			if(IUtil.isNotBlank(result)){
				value = result;
			}
		}catch(Throwable t){
			t.printStackTrace();
		}
		return value;
	}

}
