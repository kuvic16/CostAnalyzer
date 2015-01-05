package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class CostActivity extends ActionBarActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CategoryService categoryService;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private DatePicker mCostDatePicker;

	private TextView mCostStatus;
	
	private List<Map<String, String>> mCategoryListdata = new ArrayList<Map<String, String>>();
	private HashMap<Integer,String> spinnerCategoryMap = new HashMap<Integer, String>();
	
	private int selectedCategoryId;
	private String selectedCategoryName;
	
	private final int CONTEXT_MENU_EDIT = 1;
	private final int CONTEXT_MENU_ARCHIVE = 2;
	private int action = 0;
	private boolean firstTime = true;

	
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					try{
						View idChild = ((ViewGroup) v).getChildAt(1);
						selectedCategoryId = Integer.valueOf(((TextView) idChild).getText().toString());
						
						View nameChild = ((ViewGroup) v).getChildAt(0);
						selectedCategoryName = ((TextView) nameChild).getText().toString();
						
						registerForContextMenu(mListView);
	                    openContextMenu(mListView);
					}catch(Throwable t){
						t.printStackTrace();
					}
				}
			});	        
	    }
	    return mListView;
	}

	protected void setListAdapter(ListAdapter adapter) {
	    getListView().setAdapter(adapter);
	}

	protected ListAdapter getListAdapter() {
	    ListAdapter adapter = getListView().getAdapter();
	    if (adapter instanceof HeaderViewListAdapter) {
	        return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
	    } else {
	        return adapter;
	    }
	}
	// end listview activity
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_cost);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_category);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_category,(DrawerLayout) findViewById(R.id.drawer_layout_category));
		
		
		try {
			categoryService = new CategoryService(getHelper().getCategoryDao());
			mCostStatus = (TextView)findViewById(R.id.textView_category_status);
			loadCategoryList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}
	
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cost, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle(selectedCategoryName);
		menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, CONTEXT_MENU_ARCHIVE, Menu.NONE, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCategoryDialougeBox();
		}
			break;
		case CONTEXT_MENU_ARCHIVE: {
			action = IConstant.ACTION_DELETE;
			deleteCategoryDialougeBox();
		}
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.add_cost) {
			action = IConstant.ACTION_ADD;
			addNewCategoryDialougeBox();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		setTitle(getString(R.string.title_activity_cost));
	}
	
	
	@SuppressLint("InflateParams")
	private void addNewCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View costFormView = factory.inflate(R.layout.cost_form, null);
		
		mCategoryName = (Spinner)costFormView.findViewById(R.id.spinner_category_name);
		mCostAmount = (EditText)costFormView.findViewById(R.id.editText_cost_amount);
		mCostDatePicker = (DatePicker)costFormView.findViewById(R.id.datePicker_cost_date);
		
		loadCategorySpinner(mCategoryName);
	
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.add)
		     .setTitle(R.string.add_new_cost)
		     .setView(costFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categoryFormView = factory.inflate(R.layout.category_form, null);
		
		
		Category category = categoryService.getCategoryById(selectedCategoryId);
		if(category == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_category));
			return;
		}
		
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.edit)
		     .setTitle(R.string.edit_category)
		     .setView(categoryFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCategoryDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.delete)
		     .setTitle(R.string.delete_category)
		     .setMessage(getString(R.string.delete_category_are_u_sure, selectedCategoryName))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	
	DialogInterface.OnClickListener saveCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveCategory()==1){
					break;
				}
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	DialogInterface.OnClickListener deleteCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(deleteCategory()==1){
					break;
				}
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	private int saveCategory(){
//		if(IUtil.isNotBlank(mCategoryName.getText())){
//    		String categoryName = mCategoryName.getText().toString().toLowerCase();
//    		String categoryType = getString(R.string.productive);
//    		Category category= new Category();
//    		if(action == IConstant.ACTION_EDIT){
//    			category= categoryService.getCategoryById(selectedCategoryId);
//    		}
//    		category.setName(categoryName);
//    		category.setType(categoryType);
//    		category.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
//    		category.setCreated_by_name("");
//    		
//    		int sucess = 0;
//    		if(action == IConstant.ACTION_ADD){
//    			sucess = categoryService.createCategory(category);
//    		}else if(action == IConstant.ACTION_EDIT){
//    			sucess = categoryService.updateCategory(category);
//    		} 
//    		
//    		if(sucess > 0){
//    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_success, categoryName));
//    			loadCategoryList();
//    			return 1;
//    		}else{
//    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_failed));
//    		}
//    	}else{
//    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.category_name_missing));
//    	}
		return 0;
	}
	
	private int deleteCategory(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = categoryService.deleteCategoryById(selectedCategoryId);
		} 
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_category_success, selectedCategoryName));
			loadCategoryList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_category_failed));
		}
    	return 0;
	}
	
	private void loadCategoryList(){
		try {
			List<Category> categoryList = categoryService.getAllCategory();
			loadUI(categoryList, categoryService.countCategory());
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	private void loadUI(List<Category> categoryList, long total) {
		try {
			mCostStatus.setText(getString(R.string.category_status, total));
			mCategoryListdata = new ArrayList<Map<String,String>>();
			for (Category category : categoryList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("name", category.getName());
				infoMap.put("id", String.valueOf(category.getId()));
				String info = category.getType();
				if(IUtil.isNotBlank(category.getCreated_date())){
					Date date = IUtil.getDate(category.getCreated_date(), IUtil.DATE_FORMAT);				
					info += ", " + date;
				}
				infoMap.put("info", info);
				mCategoryListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter( 
					this, 
					mCategoryListdata,
					R.layout.two_item, 
					new String[] {"name","info", "id" }, new int[] { R.id.text1, R.id.text2,R.id.text3 
			});
			setListAdapter(adapter);
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			getListView().setItemsCanFocus(false);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadCategorySpinner(Spinner categorySpinner){
		try {
			List<Category> categoryList = categoryService.getAllCategory();
			String[] spinnerArray = new String[categoryList.size()];
			
			int i = 0;
			for(Category category : categoryList){
				spinnerCategoryMap.put(category.getId(),category.getName());
				spinnerArray[i++] = category.getName();
			}
			
			ArrayAdapter<String> adapter =new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, spinnerArray);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			categorySpinner.setAdapter(adapter);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
		
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if(firstTime){
			firstTime = false;
			return;
		}
		
		switch (position) {
		case 0:
			Intent i = new Intent(getApplicationContext(),CategoryActivity.class);
			startActivity(i);
			break;
		case 1:
			i = new Intent(getApplicationContext(),CostActivity.class);
			startActivity(i);
			break;
		case 2:
			break;
		}
	}	
	
}
