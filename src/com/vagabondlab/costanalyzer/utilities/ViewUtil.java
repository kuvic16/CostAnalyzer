package com.vagabondlab.costanalyzer.utilities;

import android.content.Context;
import android.widget.Toast;

public final class ViewUtil {
	public static void showMessage(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

}
