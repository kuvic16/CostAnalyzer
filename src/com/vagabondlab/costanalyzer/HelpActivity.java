package com.vagabondlab.costanalyzer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import com.vagabondlab.costanalyzer.utilities.IConstant;

@SuppressLint("ClickableViewAccessibility")
public class HelpActivity extends CActivity {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		setTitle(getString(R.string.title_help_screen));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));		
	}
	
	// 1. Listener
	// 2. Override methods
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode==IConstant.PARENT_ACTIVITY_REQUEST_CODE){
	    	firstTime = true;
	    	onNavigationDrawerItemSelected(0);
	    }
	}
	
	@Override
	public void nextView(){		
	}
	
	@Override
	public void prevView(){		
	}

	@Override
	public ListView getListView() {
	    return null;
	}
	
}
