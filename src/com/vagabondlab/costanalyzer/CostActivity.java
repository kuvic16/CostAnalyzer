package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class CostActivity extends CActivity {
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CategoryService categoryService;
	private CostService costService;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private TextView mCostSelectedDate;
	private TextView mChangeCostDateButton;
	private TextView mAddCostCategoryButton;
	
	private Spinner mSearchCategoryName;
	private RadioButton mSearchProductive;
	private RadioButton mSearchWastage;
	private TextView mSearchStartDate;
	private TextView mSearchEndDate;
	private TextView mSearchStartDateButton;
	private TextView mSearchEndDateButton;
	
	private Button mButtonholderAddCost;
	private Button mButtonholderSearch;
	private Button mButtonholderReload;

	private TextView mCostStatus;
	
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	private String[] spinnerArray;
	ArrayAdapter<String> spinnerAdapter;
	
	private int selectedCostId;
	private String selectedCostName;
	
	private int action = 0;
	private String mCurrentDate;
	
	private Tracker gaTracker;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_cost);
		setTitle(getString(R.string.title_activity_cost));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_cost);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_cost,(DrawerLayout) findViewById(R.id.drawer_layout_cost));
		
		try { 
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			
			mButtonholderAddCost = (Button)findViewById(R.id.buttonholder_add_cost);
			mButtonholderAddCost.setOnClickListener(buttonHolderAddCostButtonClickListener);
			mButtonholderSearch = (Button)findViewById(R.id.buttonholder_search);
			mButtonholderSearch.setOnClickListener(buttonHolderSearchButtonClickListener);
			mButtonholderReload = (Button)findViewById(R.id.buttonholder_reload);
			mButtonholderReload.setOnClickListener(buttonHolderReloadButtonClickListener);
			
			loadCostList();
			mCurrentDate = IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD);
			
			//google analytics
			gaTracker = ((CostAnalyzer) getApplication()).getTracker(CostAnalyzer.TrackerName.APP_TRACKER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("InflateParams")
	private void addNewCostDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View costFormView = factory.inflate(R.layout.cost_form, null);
		
		mCategoryName = (Spinner)costFormView.findViewById(R.id.spinner_category_name);
		mCostAmount = (EditText)costFormView.findViewById(R.id.editText_cost_amount);
		mCostSelectedDate = (TextView)costFormView.findViewById(R.id.textView_selected_cost_date);
		mCostSelectedDate.setText(mCurrentDate);
		
		mChangeCostDateButton = (TextView)costFormView.findViewById(R.id.textView_change_date);
		mChangeCostDateButton.setOnClickListener(changeDateButtonTouchListener);
		
		mAddCostCategoryButton= (TextView)costFormView.findViewById(R.id.textView_add_category);
		mAddCostCategoryButton.setOnClickListener(addCategoryButtonClickListener);
		
		
		loadCategorySpinner(mCategoryName);
	
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.addnew)
		     .setTitle(R.string.add_new_cost)
		     .setView(costFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editCostDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View costFormView = factory.inflate(R.layout.cost_form, null);
		
		mCategoryName = (Spinner)costFormView.findViewById(R.id.spinner_category_name);
		mCostAmount = (EditText)costFormView.findViewById(R.id.editText_cost_amount);
		mCostSelectedDate = (TextView)costFormView.findViewById(R.id.textView_selected_cost_date);
		mChangeCostDateButton = (TextView)costFormView.findViewById(R.id.textView_change_date);
		mChangeCostDateButton.setOnClickListener(changeDateButtonTouchListener);
		
		mAddCostCategoryButton= (TextView)costFormView.findViewById(R.id.textView_add_category);
		mAddCostCategoryButton.setOnClickListener(addCategoryButtonClickListener);
		
		loadCategorySpinner(mCategoryName);
		
		Cost cost = costService.getCostById(selectedCostId);
		if(cost == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_cost));
			return;
		}
		categoryService.refreash(cost.getCategory());
		
		mCategoryName.setSelection(spinnerAdapter.getPosition(cost.getCategory().getName()));
		mCostAmount.setText(String.valueOf(cost.getAmount()));
		mCostSelectedDate.setText(cost.getDate());
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.editnew)
		     .setTitle(R.string.edit_cost)
		     .setView(costFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCostDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(R.string.delete_cost)
		     .setMessage(getString(R.string.delete_cost_are_u_sure, selectedCostName))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	
	@SuppressLint("InflateParams")
	private void searchCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categorySearchFormView = factory.inflate(R.layout.search_cost_form, null);
		mSearchCategoryName = (Spinner)categorySearchFormView.findViewById(R.id.spinner_search_category_name);
		mSearchProductive = (RadioButton)categorySearchFormView.findViewById(R.id.radio_search_productive);
		mSearchWastage = (RadioButton)categorySearchFormView.findViewById(R.id.radio_search_wastage);
		mSearchStartDate =(TextView)categorySearchFormView.findViewById(R.id.textView_selected_cost_start_date);
		mSearchEndDate =(TextView)categorySearchFormView.findViewById(R.id.textView_selected_cost_end_date);
		mSearchStartDateButton =(TextView)categorySearchFormView.findViewById(R.id.textView_change_start_date);
		mSearchEndDateButton =(TextView)categorySearchFormView.findViewById(R.id.textView_change_end_date);
		
		mSearchStartDateButton.setOnClickListener(changeSearchStartDateButtonTouchListener);
		mSearchEndDateButton.setOnClickListener(changeSearchEndDateButtonTouchListener);
		
		loadCategorySpinner(mSearchCategoryName);
		mSearchStartDate.setText(mCurrentDate);
		mSearchEndDate.setText(mCurrentDate);
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.search)
		     .setTitle(R.string.search)
		     .setView(categorySearchFormView)
		     .setPositiveButton(R.string.search, searchCostListener)
		     .setNegativeButton(R.string.cancel, searchCostListener);
		alert.show();
	}
	
	private int saveCost(){
		
		String categoryName = mCategoryName.getSelectedItem().toString();
		Category category = categoryService.getCategoryByName(categoryName);
		if(category == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.cost_category_missing));
			return 0;
		}
		
		if(!IUtil.isNotBlank(mCostAmount.getText())){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.cost_amount_missing));
			return 0;
		} 
		
		Double costAmount = Double.valueOf(mCostAmount.getText().toString());
		if(costAmount <= 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.cost_amount_zero));
			return 0;
		}
		
		String costDate = mCostSelectedDate.getText().toString();
		mCurrentDate = costDate;
		
		Cost cost = new Cost();
		if(action == IConstant.ACTION_EDIT){
			cost = costService.getCostById(selectedCostId);
		}
		cost.setCategory(category);
		cost.setAmount(costAmount);
		cost.setDate(costDate);
		cost.setLast_modified_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
		cost.setLast_modified_by_name("you");
		
		int sucess = 0;
		if(action == IConstant.ACTION_ADD){
			cost.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
			cost.setCreated_by_name("you");
			sucess = costService.createCost(cost);
			
			gaTracker.send(new HitBuilders.EventBuilder()
            .setCategory("Cost")
            .setAction("New")
            .setLabel("Added")
            .build());
		}else if(action == IConstant.ACTION_EDIT){
			sucess = costService.updateCost(cost);
		} 
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_cost_success, categoryName));
			loadCostList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_cost_failed));
		}
    	
		return 0;
	}
	
	private int deleteCost(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = costService.deleteCostById(selectedCostId);
		} 
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_cost_success, selectedCostName));
			loadCostList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_cost_failed));
		}
    	return 0;
	}
	
	private void loadCostList(){
		try {
			List<String[]> costList = costService.searchCost(null, null, null, null);
			loadUI(costList, costService.countCost()); 
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	private void loadUI(List<String[]> costList, long total) {
		try {
			mCostStatus.setText(getString(R.string.cost_status, total));
			mCostListdata = new ArrayList<Map<String,String>>();
			for (String[] cost : costList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("id", String.valueOf(cost[0]));
				
				Calendar costDate = IUtil.getCalender(cost[4], IUtil.DATE_FORMAT_YYYY_MM_DD);
				infoMap.put("cost_day", String.valueOf(costDate.get(Calendar.DAY_OF_MONTH)));
				infoMap.put("cost_month", IUtil.changeDateFormat(cost[4], IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM));
				infoMap.put("cost_category_name", cost[1]);
				
				String info = cost[2];
				if(IUtil.isNotBlank(cost[5])){
					Date date = IUtil.getDate(cost[5], IUtil.DATE_FORMAT);
					info += "\nadded on " + date;
				}
				infoMap.put("cost_category_type_and_time", info);
				//infoMap.put("cost_amount", String.valueOf(cost[3]));
				
				double _cost = 0.0;
				try{
					_cost = Double.valueOf(cost[3]);
				}catch(Throwable t){}
				infoMap.put("cost_amount", String.format("%.1f", _cost));

				mCostListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter( 
					this, 
					mCostListdata,
					R.layout.cost_list_view, 
					new String[] {"id", "cost_day","cost_month", "cost_category_name", "cost_category_type_and_time", "cost_amount" }, 
					new int[] { R.id.cost_list_row_id, R.id.cost_date_day, R.id.cost_date_month, R.id.cost_category_name, R.id.cost_type_and_time, R.id.cost_amount 
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
			loadCostCategory();
			spinnerAdapter =new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_item, spinnerArray);
			categorySpinner.setAdapter(spinnerAdapter);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadCostCategory(){
		try {
			List<Category> categoryList = categoryService.getAllCategory();
			spinnerArray = new String[categoryList.size()+1];
			
			int i = 0;
			spinnerArray[i++] = getString(R.string.select_category);
			for(Category category : categoryList){
				spinnerArray[i++] = category.getName();
			}
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void searchCost(){
		try{
    		String categoryName = mSearchCategoryName.getSelectedItem().toString();
    		if(categoryName.equalsIgnoreCase(getString(R.string.select_category))){
    			categoryName = "";
    		}
    		
    		String categoryType = "";
    		if(mSearchWastage.isChecked()){
    			categoryType = getString(R.string.wastage);
    		}else if(mSearchProductive.isChecked()){
    			categoryType = getString(R.string.productive);
    		}
    		String startDate = mSearchStartDate.getText().toString();
    		String endDate = mSearchEndDate.getText().toString();
    		
    		List<String[]> costList = costService.searchCost(categoryName, categoryType, startDate, endDate);
    		loadUI(costList, costList.size());
    	}catch(Throwable t){
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, t.getMessage()));
    	}
	}
	
	
	// 1. Listener
	DialogInterface.OnClickListener saveCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveCost()==1){
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
				if(deleteCost()==1){
					break;
				}
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	DialogInterface.OnClickListener searchCostListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				showProgressDialog();
				searchCost();
				closeProgressDialog();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	OnClickListener buttonHolderAddCostButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_ADD;
				addNewCostDialougeBox();
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
				loadCostList();
				closeProgressDialog();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener changeDateButtonTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener changeSearchStartDateButtonTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_SEARCH_START_DATE;
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener changeSearchEndDateButtonTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_SEARCH_END_DATE;
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener addCategoryButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				Intent i = new Intent(getApplicationContext(),CategoryActivity.class);
				i.putExtra(IConstant.FORM_ACTION, IConstant.ADD_CTG_ON_REQUEST);
				startActivityForResult(i, IConstant.HOME_ACTIVITY_ADD_CATEGORYREQUEST_CODE);
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
						View viewCostId = ((ViewGroup) v).getChildAt(2);
						View viewCostDetails = ((ViewGroup) v).getChildAt(1);
						View viewCostCategoryName = ((ViewGroup) viewCostDetails).getChildAt(0);
						View viewCostAmount = ((ViewGroup) viewCostDetails).getChildAt(2);
						
						selectedCostId = Integer.valueOf(((TextView) viewCostId).getText().toString());
						selectedCostName = ((TextView) viewCostCategoryName).getText().toString() + " : " + ((TextView) viewCostAmount).getText().toString();
						
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
		getMenuInflater().inflate(R.menu.cost, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IConstant.CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCostDialougeBox();
		}
			break;
		case IConstant.CONTEXT_MENU_ARCHIVE: {
			action = IConstant.ACTION_DELETE;
			deleteCostDialougeBox();
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
			addNewCostDialougeBox();
			return true;
		}else if(id == R.id.search){
			action = IConstant.ACTION_SEARCH;
			searchCategoryDialougeBox();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void returnDate(String date) {
		if(action == IConstant.ACTION_ADD || action == IConstant.ACTION_EDIT){
			mCostSelectedDate.setText(date);
		}else if(action == IConstant.ACTION_SEARCH_START_DATE){
			mSearchStartDate.setText(date);
		}else if(action == IConstant.ACTION_SEARCH_END_DATE){
			mSearchEndDate.setText(date);
		}
	}

	@Override
	public void nextView() {
	}

	@Override
	public void prevView() {
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode==IConstant.PARENT_ACTIVITY_REQUEST_CODE){
	    	firstTime = true;
	    	onNavigationDrawerItemSelected(0);
	    }else if(requestCode == IConstant.HOME_ACTIVITY_ADD_CATEGORYREQUEST_CODE){
	    	if(mCategoryName != null){
	    		loadCategorySpinner(mCategoryName);
	    		mCategoryName.setSelection(spinnerAdapter.getPosition(spinnerArray[1]));
	    	}
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(action == IConstant.ACTION_SEARCH){
				loadCostList();
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
