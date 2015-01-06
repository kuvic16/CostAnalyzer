package com.vagabondlab.costanalyzer.utilities;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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

}
