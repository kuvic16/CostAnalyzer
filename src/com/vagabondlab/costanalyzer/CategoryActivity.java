package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class CategoryActivity extends CActivity{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CategoryService categoryService;
	private CostService costService;
	
	private EditText mCategoryName;
	private RadioButton mProductive;
	private RadioButton mWastage;
	private TextView mCategoryStatus;
	private Button mButtonholderAddCategory;
	private Button mButtonholderSearch;
	private Button mButtonholderReload;
	
	private EditText mSearchCategoryName;
	private RadioButton mSearchProductive;
	private RadioButton mSearchWastage;
	
	private List<Map<String, String>> mCategoryListdata = new ArrayList<Map<String, String>>();
	
	private int selectedCategoryId;
	private String selectedCategoryName;
	private int action = 0;
	private Tracker gaTracker;
	private String request = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_category);
		setTitle(getString(R.string.cost_category));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_category);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_category,(DrawerLayout) findViewById(R.id.drawer_layout_category));
		
		try {
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			
			mCategoryStatus = (TextView)findViewById(R.id.textView_category_status);
			mButtonholderAddCategory = (Button)findViewById(R.id.buttonholder_add_category);
			mButtonholderAddCategory.setOnClickListener(buttonHolderAddCategoryButtonClickListener);
			mButtonholderSearch = (Button)findViewById(R.id.buttonholder_search);
			mButtonholderSearch.setOnClickListener(buttonHolderSearchButtonClickListener);
			mButtonholderReload = (Button)findViewById(R.id.buttonholder_reload);
			mButtonholderReload.setOnClickListener(buttonHolderReloadButtonClickListener);
			
			loadCategoryList();
			
			request = ViewUtil.getIntantExtra(getIntent(), IConstant.FORM_ACTION);
			if(request.equalsIgnoreCase(IConstant.ADD_CTG_ON_REQUEST)){
				action = IConstant.ACTION_ADD;
				addNewCategoryDialougeBox();
			}
			
			//google analytics
			gaTracker = ((CostAnalyzer) getApplication()).getTracker(CostAnalyzer.TrackerName.APP_TRACKER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("InflateParams")
	private void addNewCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categoryFormView = factory.inflate(R.layout.category_form, null);
		mCategoryName = (EditText)categoryFormView.findViewById(R.id.editText_category_name);
		mProductive = (RadioButton)categoryFormView.findViewById(R.id.radio_productive);
		mWastage = (RadioButton)categoryFormView.findViewById(R.id.radio_wastage);
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.addnew)
		     .setTitle(R.string.add_new_category)
		     .setView(categoryFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categoryFormView = factory.inflate(R.layout.category_form, null);
		mCategoryName = (EditText)categoryFormView.findViewById(R.id.editText_category_name);
		mProductive = (RadioButton)categoryFormView.findViewById(R.id.radio_productive);
		mWastage = (RadioButton)categoryFormView.findViewById(R.id.radio_wastage);
		
		Category category = categoryService.getCategoryById(selectedCategoryId);
		if(category == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_category));
			return;
		}
		
		mCategoryName.setText(category.getName());
		if(category.getType().equalsIgnoreCase(getString(R.string.productive))){
			mProductive.setChecked(true);
		}else if(category.getType().equalsIgnoreCase(getString(R.string.wastage))){
			mWastage.setChecked(true);
		}
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.editnew)
		     .setTitle(R.string.edit_category)
		     .setView(categoryFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCategoryDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(R.string.delete_category)
		     .setMessage(getString(R.string.delete_category_are_u_sure, selectedCategoryName))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void searchCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categorySearchFormView = factory.inflate(R.layout.search_category_form, null);
		mSearchCategoryName = (EditText)categorySearchFormView.findViewById(R.id.editText_search_category_name);
		mSearchProductive = (RadioButton)categorySearchFormView.findViewById(R.id.radio_search_productive);
		mSearchWastage = (RadioButton)categorySearchFormView.findViewById(R.id.radio_search_wastage);
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.search)
		     .setTitle(R.string.search)
		     .setView(categorySearchFormView)
		     .setPositiveButton(R.string.search, searchCategorylListener)
		     .setNegativeButton(R.string.cancel, searchCategorylListener);
		alert.show();
	}
	
	private int saveCategory(){
		if(IUtil.isNotBlank(mCategoryName.getText())){
    		String categoryName = mCategoryName.getText().toString().trim().toLowerCase();
    		String categoryType = getString(R.string.productive);
    		if(mWastage.isChecked()){
    			categoryType = getString(R.string.wastage);
    		}else if(mProductive.isChecked()){
    			categoryType = getString(R.string.productive);
    		}
    		
    		Category category= new Category();
    		if(action == IConstant.ACTION_EDIT){
    			category= categoryService.getCategoryById(selectedCategoryId);
    		}
    		category.setName(categoryName);
    		category.setType(categoryType);
    		category.setLast_modified_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
			category.setLast_modified_by_name("you");
			
    		int sucess = 0;
    		if(action == IConstant.ACTION_ADD){
    			category.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
        		category.setCreated_by_name("you");
    			sucess = categoryService.createCategory(category);
    			
    			gaTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Cost Category")
                .setAction("New")
                .setLabel("Added")
                .build());
    		}else if(action == IConstant.ACTION_EDIT){
    			sucess = categoryService.updateCategory(category);
    		} 
    		
    		if(sucess > 0){
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_success, categoryName));
    			loadCategoryList();
    			return 1;
    		}else{
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_failed));
    		}
    	}else{
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.category_name_missing));
    	}
		return 0;
	}
	
	private int deleteCategory(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = categoryService.deleteCategoryById(selectedCategoryId);
			costService.deleteCostByCategory(selectedCategoryId);
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
	
	private void searchCategory(){
		try{
    		String categoryName = mSearchCategoryName.getText().toString().toLowerCase();
    		String categoryType = "";
    		if(mSearchWastage.isChecked()){
    			categoryType = getString(R.string.wastage);
    		}else if(mSearchProductive.isChecked()){
    			categoryType = getString(R.string.productive);
    		}
    		
    		List<Category> categoryList = categoryService.searchCategory(categoryName, categoryType);
    		loadUI(categoryList, categoryList.size());
    	}catch(Throwable t){
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, t.getMessage()));
    	}
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
			mCategoryStatus.setText(getString(R.string.category_status, total));
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
	
	// 1. Listener
	DialogInterface.OnClickListener saveCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveCategory()==1){
					if(request.equalsIgnoreCase(IConstant.ADD_CTG_ON_REQUEST)){
						finish();
					}
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
	
	DialogInterface.OnClickListener searchCategorylListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				showProgressDialog();
				searchCategory();
				closeProgressDialog();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	OnClickListener buttonHolderAddCategoryButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_ADD;
				addNewCategoryDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener buttonHolderSearchButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_SEARCH;
				searchCategoryDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener buttonHolderReloadButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				showProgressDialog();
				loadCategoryList();
				closeProgressDialog();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	
	// 2. Override methods
	@Override
	public ListView getListView() {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { 
		getMenuInflater().inflate(R.menu.category, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IConstant.CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCategoryDialougeBox();
		}
			break;
		case IConstant.CONTEXT_MENU_ARCHIVE: {
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
		if(id == R.id.search){
			action = IConstant.ACTION_SEARCH;
			searchCategoryDialougeBox();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void nextView() {
	}

	@Override
	public void prevView() {
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(action == IConstant.ACTION_SEARCH){
				loadCategoryList();
				action = IConstant.ACTION_NONE;				
			}else{
				Intent i = new Intent(getApplicationContext(),HomeActivity.class);
				startActivity(i);
			}
			return true;
		}		
		return super.onKeyDown(keyCode, event);
	}
	

}
