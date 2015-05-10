package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nineoldandroids.animation.Animator;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("ClickableViewAccessibility")
public class HomeActivity extends CActivity {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CategoryService categoryService;
	private CostService costService;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private TextView mCostSelectedDate;
	private TextView mChangeCostDateButton;
	private TextView mAddCostCategoryButton;
	private Button mButtonholderAddCost;
	private Button mButtonholderSearch;

	private TextView mCostStatus;
	
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	private String[] spinnerArray;
	private ArrayAdapter<String> spinnerAdapter;
	
	private int selectedCostId;

	private int action = 0;
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private String mCurrentDate;
	
	private TextView mTotalCostView;
	private TextView mProductiveCostView;
	private TextView mWastageCostView;
	private Double productiveCost = 0.0;
	private Double wastageCost = 0.0;
	private Double totalCost = 0.0;
	
	private Tracker gaTracker;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setTitle(getString(R.string.title_home_screen));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			mGestureDetector = new GestureDetector(this);
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mTotalCostView.setOnClickListener(totalCostTouchListener);
			mTotalCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mProductiveCostView.setOnClickListener(productiveCostTouchListener);
			mProductiveCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			mWastageCostView.setOnClickListener(wastageCostTouchListener);
			mWastageCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mButtonholderAddCost = (Button)findViewById(R.id.buttonholder_add_cost);
			mButtonholderAddCost.setOnClickListener(buttonHolderAddCostButtonClickListener);
			mButtonholderSearch = (Button)findViewById(R.id.buttonholder_search);
			mButtonholderSearch.setOnClickListener(buttonHolderSearchButtonClickListener);
			
			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
			
			//google analytics
			gaTracker = ((CostAnalyzer) getApplication()).getTracker(CostAnalyzer.TrackerName.APP_TRACKER);
			gaTracker.setScreenName(mTitle.toString());
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
		mAddCostCategoryButton= (TextView)costFormView.findViewById(R.id.textView_add_category);
		mCostSelectedDate.setText(mCurrentDate);
		mChangeCostDateButton = (TextView)costFormView.findViewById(R.id.textView_change_date);
		
		mChangeCostDateButton.setOnClickListener(changeDateButtonClickListener);
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
		mAddCostCategoryButton= (TextView)costFormView.findViewById(R.id.textView_add_category);
		
		mChangeCostDateButton.setOnClickListener(changeDateButtonClickListener);
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
		}else if(action == IConstant.ACTION_ADD){
			cost.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
			cost.setCreated_by_name("you");
		}
		cost.setLast_modified_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
		cost.setLast_modified_by_name("you");
		cost.setCategory(category);
		cost.setAmount(costAmount);
		cost.setDate(costDate);
		
		int sucess = 0;
		if(action == IConstant.ACTION_ADD){
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
			loadCostList(mCurrentDate);
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
			loadCostList(mCurrentDate);
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_cost_failed));
		}
    	return 0;
	}
	
	private void loadCostList(String date){
		try {
			mCurrentDate = date;
			loadQuickView(date);
			loadListView(date, null); 
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadListView(String costDate, String costType){
		List<Cost> costList = costService.searchCost(costDate);
		loadUI(costList, costList.size(), costType);
	}

	private void loadUI(List<Cost> costList, long total, String costType) {
		try {
			mCostListdata = new ArrayList<Map<String,String>>();
			for (Cost cost : costList) {
				categoryService.refreash(cost.getCategory());
				if(costType == null || costType.equalsIgnoreCase(cost.getCategory().getType())){
				
					Map<String, String> infoMap = new HashMap<String, String>(3);
					infoMap.put("id", String.valueOf(cost.getId()));
					
					Calendar costDate = IUtil.getCalender(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD);
					infoMap.put("cost_day", String.valueOf(costDate.get(Calendar.DAY_OF_MONTH)));
					infoMap.put("cost_month", IUtil.changeDateFormat(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM));
					infoMap.put("cost_category_name", cost.getCategory().getName());
					 
					String info = cost.getCategory().getType();
					if(IUtil.isNotBlank(cost.getCreated_date())){
						Date date = IUtil.getDate(cost.getCreated_date(), IUtil.DATE_FORMAT);
						info += "\nadded on " + date;
					}
					infoMap.put("cost_category_type_and_time", info);
					infoMap.put("cost_amount", String.format("%.1f", cost.getAmount()));
					mCostListdata.add(infoMap);
				}
			}
			if(costType == null){
				mCostStatus.setText(getString(R.string.cost_status, mCostListdata.size()));
			}else if(costType.equalsIgnoreCase(getString(R.string.productive))){
				mCostStatus.setText(getString(R.string.productive_cost_status, mCostListdata.size()));
			}else if(costType.equalsIgnoreCase(getString(R.string.wastage))){
				mCostStatus.setText(getString(R.string.wastage_cost_status, mCostListdata.size()));
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
	
	private void loadQuickView(String date){
		try{
			String today = IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD);
			productiveCost = 0.0;
			wastageCost = 0.0;
			totalCost = 0.0;
			
			List<String[]> costListGroupByType = costService.getTotalCostGroupByType(date);
			if(IUtil.isNotBlank(costListGroupByType)){
				for(String[] costs : costListGroupByType){
					try{
						if(costs[0].equalsIgnoreCase(getString(R.string.productive))){
							productiveCost = Double.valueOf(costs[1]);
						}else if(costs[0].equalsIgnoreCase(getString(R.string.wastage))){
							wastageCost = Double.valueOf(costs[1]);
						}
					}catch(Throwable t){
						t.printStackTrace();
					}
				}
			}
			
			totalCost = productiveCost + wastageCost;
			mTotalCostView.setText(String.format("%.1f", totalCost));
			mProductiveCostView.setText(String.format("%.1f", productiveCost));
			mWastageCostView.setText(String.format("%.1f", wastageCost));
			
			String dateStatus = "";			
			if(date.equalsIgnoreCase(today)){
				dateStatus = getString(R.string.page_top_date_text, IUtil.changeDateFormat(date, IUtil.DATE_FORMAT_YYYY_MM_DD, "EEE, MMM d, yyyy"));
			}else{
				dateStatus = IUtil.changeDateFormat(date, IUtil.DATE_FORMAT_YYYY_MM_DD, "EEE, MMM d, yyyy");
			}
			TextView topDateText = (TextView)findViewById(R.id.textView_summary_status);
			topDateText.setText(dateStatus);
						
		}catch(Throwable t){
			t.printStackTrace();
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
	
	
	
	OnTouchListener shortSummarySwipeListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
			}
			if (mGestureDetector.onTouchEvent(event)) {
				return false;
			} else {
				return false;
			}
		}
	};
	
	OnClickListener totalCostTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				closeProgressDialog();
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(HomeActivity.this)
					.playOn(mTotalCostView);
				
				loadListView(mCurrentDate, null);
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener productiveCostTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				closeProgressDialog();
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(HomeActivity.this)
					.playOn(mProductiveCostView);
				
				loadListView(mCurrentDate, getString(R.string.productive));
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener wastageCostTouchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			closeProgressDialog();
			YoYo.with(Techniques.ZoomIn)
				.duration(500)
				.interpolate(new AccelerateDecelerateInterpolator())
				.withListener(HomeActivity.this)
				.playOn(mWastageCostView);
			
			loadListView(mCurrentDate, getString(R.string.wastage));
		}
	};
	
	OnClickListener changeDateButtonClickListener = new OnClickListener() {
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
	
	Spinner.OnItemSelectedListener categoryItemSelectListener = new Spinner.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		
	};
	
	
	// 2. Override methods
	@Override
	public ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnTouchListener(shortSummarySwipeListener);
	        mListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
	            public boolean onItemLongClick(AdapterView<?> parent, View v,int position, long id) {
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
	            		return true;
	            }
	        });
	    }
	    return mListView;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.home, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
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
			DialogFragment newFragment = new DatePickerFragment();
		    newFragment.show(getSupportFragmentManager(), "datePicker");
		}
		return super.onOptionsItemSelected(item);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode==IConstant.PARENT_ACTIVITY_REQUEST_CODE){
	    	firstTime = true;
	    	onNavigationDrawerItemSelected(0);
	    	loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
	    }else if(requestCode == IConstant.HOME_ACTIVITY_ADD_CATEGORYREQUEST_CODE){
	    	if(mCategoryName != null){
	    		loadCategorySpinner(mCategoryName);
	    		mCategoryName.setSelection(spinnerAdapter.getPosition(spinnerArray[1]));
	    	}
	    }
	}
	
	@Override
	public void nextView(){
		YoYo.with(Techniques.SlideInRight)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(this)
			.playOn(findViewById(R.id.relative_layout_root));
		
		Date date = IUtil.getDate(mCurrentDate, IUtil.DATE_FORMAT_YYYY_MM_DD);
		DateTime dateTime = new DateTime(date);
		dateTime = dateTime.plusDays(-1);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String newDate = fmt.print(dateTime);
		loadCostList(newDate);		
	}
	
	@Override
	public void prevView(){
		YoYo.with(Techniques.SlideInLeft)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(this)
			.playOn(findViewById(R.id.relative_layout_root));
		
		Date date = IUtil.getDate(mCurrentDate, IUtil.DATE_FORMAT_YYYY_MM_DD);
		DateTime dateTime = new DateTime(date);
		dateTime = dateTime.plusDays(1);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String newDate = fmt.print(dateTime);
		loadCostList(newDate);
	}
	
	@Override
	public void onAnimationStart(Animator arg0) {
		mProgressDialog = ProgressDialog.show(HomeActivity.this, "Please wait ...", "Loading...", true);
		mProgressDialog.setCancelable(true);
	}
	
	@Override
	public void returnDate(String date) {
		if(action == IConstant.ACTION_ADD || action == IConstant.ACTION_EDIT){
			mCostSelectedDate.setText(date);
		}else if(action == IConstant.ACTION_SEARCH){
			closeProgressDialog();
			
			YoYo.with(Techniques.SlideInDown)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(this)
			.playOn(findViewById(R.id.relative_layout_root));
			loadCostList(date);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(action == IConstant.ACTION_SEARCH){
				loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
				action = IConstant.ACTION_NONE;
				return true;
			}else{
				System.exit(0);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
