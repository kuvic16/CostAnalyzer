package com.vagabondlab.costanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("ClickableViewAccessibility")
public class BackupActivity extends CActivity{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CategoryService categoryService;
	private CostService costService;
	
	private TextView mTotalCostEntry;
	private TextView mTotalCategoryEntry;
	
	private Button mButtonClear;
	private Button mButtonBackup;
	private Button mButtonRestore;
	
	private AlertDialog.Builder mFolderAlert;
	private View mFolderView; 
	private String parentpath;
	private List<Map<String, Object>> mFolderListdata = new ArrayList<Map<String, Object>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		setTitle(getString(R.string.title_backup_screen));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			
			mTotalCategoryEntry = (TextView)findViewById(R.id.textView_total_category_entry);
			mTotalCategoryEntry.setText(String.valueOf(categoryService.countCategory()));
			
			mTotalCostEntry = (TextView)findViewById(R.id.textView_total_cost_entry);
			mTotalCostEntry.setText(String.valueOf(costService.countCost()));
			
			mButtonClear = (Button)findViewById(R.id.button_clear);
			mButtonBackup = (Button)findViewById(R.id.button_backup);
			mButtonRestore = (Button)findViewById(R.id.button_restore);
			
			mButtonClear.setOnClickListener(buttonCleanClickListener);
			mButtonBackup.setOnClickListener(buttonBackupClickListener);
			mButtonRestore.setOnClickListener(buttonRestoreClickListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	private void cleanDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(R.string.backup_screen_clear_title)
		     .setMessage(getString(R.string.backup_screen_clear_details))
		     .setPositiveButton(R.string.clean, cleanCancelListener)
		     .setNegativeButton(R.string.cancel, cleanCancelListener);
		alert.show();
	}
	
	private void backupDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.backup)
		     .setTitle(R.string.backup_screen_backup_title)
		     .setMessage(getString(R.string.backup_screen_backup_details))
		     .setPositiveButton(R.string.backup, backupCancelListener)
		     .setNegativeButton(R.string.cancel, backupCancelListener);
		alert.show();
	}
	
	private void restoreDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.restore)
		     .setTitle(R.string.backup_screen_restore_title)
		     .setMessage(getString(R.string.backup_screen_restore_details))
		     .setPositiveButton(R.string.restore, restoreCancelListener)
		     .setNegativeButton(R.string.cancel, restoreCancelListener);
		alert.show();
	}
	
	private void openFolderDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		mFolderView = factory.inflate(R.layout.browse_folder_form, null);
		loadFolderUI(File.separator);
		mFolderAlert = new AlertDialog.Builder(this);
		mFolderAlert.setIcon(R.drawable.db)
		     .setTitle(R.string.select_db_file)
		     .setView(mFolderView)
		     //.setPositiveButton(R.string.select, null)
		     .setNegativeButton(R.string.cancel, null);
		mFolderAlert.show();
	}
	
	private void loadFolderUI(String _path){
		parentpath = _path;
		if (getIntent().hasExtra("path")) {
			_path = getIntent().getStringExtra("path");
	    }
		
	    mFolderListdata = new ArrayList<Map<String,Object>>();
	    if(!_path.equalsIgnoreCase(File.separator)){
	    	//back button
	    	Map<String, Object> infoMap = new HashMap<String, Object>(3);
			infoMap.put("file_name", getString(R.string.parent_folder));
			infoMap.put("file_image", R.drawable.back);
			mFolderListdata.add(infoMap);
	    }
	    
	    File dir = new File(_path);
	    File subdir = null;
	    if (!dir.canRead()) {
	      setTitle(getTitle() + " (inaccessible)");
	    }
	    String[] list = dir.list();
		if (list != null) {
			for (String file : list) {
				if (!file.startsWith(".")) {
					Map<String, Object> infoMap = new HashMap<String, Object>(3);
					infoMap.put("file_name", file);
					if (dir.getPath().endsWith(File.separator)) {
						subdir = new File(dir.getPath() + file + File.separator);
					} else {
						subdir = new File(dir.getPath() + File.separator + file + File.separator);
					}

					if (subdir.isDirectory()) {
						infoMap.put("file_image", R.drawable.folder);
					} else {
						if(isDBFile(subdir.getPath())){
							infoMap.put("file_image", R.drawable.db);
						}else{
							infoMap.put("file_image", R.drawable.file);
						}
					}

					mFolderListdata.add(infoMap);
				}
			}
		}

	    SimpleAdapter adapter = new SimpleAdapter( 
				this, 
				mFolderListdata,
				R.layout.folder_list_view, 
				new String[] {"file_name", "file_image" }, 
				new int[] { R.id.file_name , R.id.file_image }
		);
	    setListAdapter(adapter);
	}
	
	
	private void cleanAll(){
		
	}
	
	private void backupDatabase() {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//com.vagabondlab.costanalyzer//databases//costassistant.db";
				String backupDBPath = "CostAnalyzer_" + IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD) + ".db";
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB).getChannel();
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
					ViewUtil.createDialogWithOKButton(this, getString(R.string.backup_successful_message_title), getString(R.string.backup_successful_message_details, backupDB.getPath()));
				}else{
					ViewUtil.showMessage(getApplicationContext(), getString(R.string.system_db_not_found));
				}
			}
		} catch (Exception e) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.backup_failed));
		}
	}
	
	private void restoreDatabase(){
		
	}
	
	
	private boolean isDBFile(String filename){
		String extension = "";
		int i = filename.lastIndexOf('.');
		if (i > 0) {
		    extension = filename.substring(i+1);
		}
		if(extension.equalsIgnoreCase("db")){
			return true;
		}
		return false;
	}
	
	// 1. Listener
	DialogInterface.OnClickListener cleanCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				cleanAll();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	DialogInterface.OnClickListener backupCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				backupDatabase();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	DialogInterface.OnClickListener restoreCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
//				restoreDatabase();
				openFolderDialougeBox();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	
	OnClickListener buttonCleanClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				cleanDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener buttonBackupClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				backupDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener buttonRestoreClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				restoreDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	
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
	        mListView = (ListView) mFolderView.findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
	            	View viewFileName = ((ViewGroup) v).getChildAt(1);
					String filename = ((TextView) viewFileName).getText().toString();
					if(filename.equalsIgnoreCase(getString(R.string.parent_folder))){
						filename = parentpath;
						loadFolderUI(new File(filename).getParent());
					}else{
		            	if (parentpath.endsWith(File.separator)) {
		                  filename = parentpath + filename + File.separator;
		                } else {
		                  filename = parentpath + File.separator + filename + File.separator;
		                }
		            	File newfile = new File(filename); 
						if (newfile.isDirectory()) {
							loadFolderUI(filename);
						} else {
							if(isDBFile(newfile.getPath())){
								
							}else{
								ViewUtil.showMessage(getApplicationContext(), getString(R.string.wrong_file));
							}
						}
					}
	                
	                
	            }
	        });
	    }
	    return mListView;
	}
}
