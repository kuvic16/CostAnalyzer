package com.vagabondlab.costanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vagabondlab.costanalyzer.utilities.IConstant;

@SuppressLint("ClickableViewAccessibility")
public class AboutActivity extends CActivity {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	public ListView mListView;
	private String path;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setTitle(getString(R.string.title_backup_screen));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		path = "/";
	    if (getIntent().hasExtra("path")) {
	      path = getIntent().getStringExtra("path");
	    }
	    setTitle(path);

	    // Read all files sorted into the values-array
	    List values = new ArrayList<>();
	    File dir = new File(path);
	    if (!dir.canRead()) {
	      setTitle(getTitle() + " (inaccessible)");
	    }
	    String[] list = dir.list();
	    if (list != null) {
	      for (String file : list) {
	        if (!file.startsWith(".")) {
	          values.add(file);
	        }
	      }
	    }
	    Collections.sort(values);

	    // Put the data into the list
	    ArrayAdapter adapter = new ArrayAdapter(this,
	        android.R.layout.simple_list_item_2, android.R.id.text1, values);
	    setListAdapter(adapter);
	    
	    
	    
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
		if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
	            public boolean onItemLongClick(AdapterView<?> parent, View v,int position, long id) {
	            	String filename = (String) getListAdapter().getItem(position);
	                if (path.endsWith(File.separator)) {
	                  filename = path + filename;
	                } else {
	                  filename = path + File.separator + filename;
	                }
	                if (new File(filename).isDirectory()) {
	                  Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
	                  intent.putExtra("path", filename);
	                  startActivity(intent);
	                } else {
	                  Toast.makeText(getApplicationContext(), filename + " is not a directory", Toast.LENGTH_LONG).show();
	                }
	                return true;
	            }
	        });
	    }
	    return mListView;
	}
	
}
