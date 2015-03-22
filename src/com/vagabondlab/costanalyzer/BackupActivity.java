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
public class BackupActivity extends CActivity {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CategoryService categoryService;
	private CostService costService;
	
	private TextView mTotalCostEntry;
	private TextView mTotalCategoryEntry;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private TextView mCostSelectedDate;
	private TextView mChangeCostDateButton;
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
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		setTitle(getString(R.string.title_backup_screen));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		//getHelper().onUpgrade(getHelper().getWritableDatabase(),getHelper().getConnectionSource(), 1, 2);
		
		try { 
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			
			mTotalCostEntry = (TextView)findViewById(R.id.textView_total_cost_entry);
			mTotalCostEntry.setText(String.valueOf(categoryService.countCategory()));
			
			mTotalCategoryEntry = (TextView)findViewById(R.id.textView_total_category_entry);
			mTotalCategoryEntry.setText(String.valueOf(costService.countCost()));
			
//			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
//			mGestureDetector = new GestureDetector(this);
//			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
//			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
//			
//			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
//			mTotalCostView.setOnClickListener(totalCostTouchListener);
//			mTotalCostView.setOnTouchListener(shortSummarySwipeListener);
//			
//			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
//			mProductiveCostView.setOnClickListener(productiveCostTouchListener);
//			mProductiveCostView.setOnTouchListener(shortSummarySwipeListener);
//			
//			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
//			mWastageCostView.setOnClickListener(wastageCostTouchListener);
//			mWastageCostView.setOnTouchListener(shortSummarySwipeListener);
//			
//			mButtonholderAddCost = (Button)findViewById(R.id.buttonholder_add_cost);
//			mButtonholderAddCost.setOnClickListener(buttonHolderAddCostButtonClickListener);
//			mButtonholderSearch = (Button)findViewById(R.id.buttonholder_search);
//			mButtonholderSearch.setOnClickListener(buttonHolderSearchButtonClickListener);
//			
//			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
		} catch (Exception e) {
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
	private void editCostDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View costFormView = factory.inflate(R.layout.cost_form, null);
		
		mCategoryName = (Spinner)costFormView.findViewById(R.id.spinner_category_name);
		mCostAmount = (EditText)costFormView.findViewById(R.id.editText_cost_amount);
		mCostSelectedDate = (TextView)costFormView.findViewById(R.id.textView_selected_cost_date);
		mChangeCostDateButton = (TextView)costFormView.findViewById(R.id.textView_change_date);
		mChangeCostDateButton.setOnClickListener(changeDateButtonTouchListener);
		
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
		alert.setIcon(R.drawable.edit)
		     .setTitle(R.string.edit_cost)
		     .setView(costFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCostDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.delete)
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
		String costDate = mCostSelectedDate.getText().toString();
		mCurrentDate = costDate;
		
		Cost cost = new Cost();
		if(action == IConstant.ACTION_EDIT){
			cost = costService.getCostById(selectedCostId);
		}
		cost.setCategory(category);
		cost.setAmount(costAmount);
		cost.setDate(costDate);
		cost.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
		cost.setCreated_by_name("you");
		
		int sucess = 0;
		if(action == IConstant.ACTION_ADD){
			sucess = costService.createCost(cost);
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
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(BackupActivity.this)
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
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(BackupActivity.this)
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
			YoYo.with(Techniques.ZoomIn)
				.duration(500)
				.interpolate(new AccelerateDecelerateInterpolator())
				.withListener(BackupActivity.this)
				.playOn(mWastageCostView);
			
			loadListView(mCurrentDate, getString(R.string.wastage));
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
