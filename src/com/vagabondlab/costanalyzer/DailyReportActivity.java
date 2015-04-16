package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;


@SuppressLint({ "ClickableViewAccessibility", "DefaultLocale" })
public class DailyReportActivity extends CActivity{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CostService costService;
	private TextView mCostStatus;
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private String mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
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
		setContentView(R.layout.activity_daily_report);
		setTitle(getString(R.string.title_daily_report));
		mTitle = getTitle();
		
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		try { 
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			mGestureDetector = new GestureDetector(this);
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			
			ListView mList = (ListView)findViewById(android.R.id.list);
			mList.setOnTouchListener(shortSummarySwipeListener);
			
			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCostList(String date){
		try {
			mCurrentDate = date;
			loadQuickView(date);
			List<String[]>  costList = costService.getTotalCostGroupByCategory(mCurrentDate);
			loadUI(costList, costList.size()); 
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	private void loadUI(List<String[]> costList, long total) {
		try {
			mCostStatus.setText(getString(R.string.category_wise_cost_status, total));
			mCostListdata = new ArrayList<Map<String,String>>();
			for (String[] costs : costList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("cost_category_name", costs[0]);
				
				String info = costs[3] + "\n" + costs[1] + " time happened";
				infoMap.put("cost_category_type_and_time", info);
//				infoMap.put("cost_amount", String.format("%.1f", costs[2]));
				infoMap.put("cost_amount", costs[2]);
				Double cost = Double.valueOf(costs[2]);
				Double costPercantage = 0.0;
				if (totalCost != 0 && cost != 0) {
					costPercantage = (cost * 100)/ totalCost;					
				}
				String result = String.format("%.1f", costPercantage); 
				infoMap.put("cost_percantage", result + "%");
				mCostListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter( 
					this, 
					mCostListdata,
					R.layout.category_wise_cost_list_view, 
					new String[] {"cost_category_name", "cost_category_type_and_time", "cost_amount", "cost_percantage" }, 
					new int[] { R.id.cost_category_name, R.id.cost_type_and_time, R.id.cost_amount, R.id.cost_amount_percent 
			});
			setListAdapter(adapter);
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
	
AnimatorListener animatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator arg0) {
			mProgressDialog = ProgressDialog.show(DailyReportActivity.this, "Please wait ...", "Loading...", true);
			mProgressDialog.setCancelable(true);
		}
		
		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator arg0) {
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
			}
		}
		
		@Override
		public void onAnimationCancel(Animator arg0) {
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
			}
		}
	};

	
	// 2. Override methods
	@Override
	public ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	    }
	    return mListView;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.report, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.search) {
			DialogFragment newFragment = new DatePickerFragment();
		    newFragment.show(getSupportFragmentManager(), "datePicker");
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode==IConstant.PARENT_ACTIVITY_REQUEST_CODE){
	    	firstTime = true;
	    	onNavigationDrawerItemSelected(0);
	    }
	}
	
	@Override	
	public void nextView(){
		YoYo.with(Techniques.SlideInRight)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(animatorListener)
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
			.withListener(animatorListener)
			.playOn(findViewById(R.id.relative_layout_root));
		
		Date date = IUtil.getDate(mCurrentDate, IUtil.DATE_FORMAT_YYYY_MM_DD);
		DateTime dateTime = new DateTime(date);
		dateTime = dateTime.plusDays(1);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String newDate = fmt.print(dateTime);
		loadCostList(newDate);
	}
	
	@Override
	public void returnDate(String date) {
		closeProgressDialog();
		
		YoYo.with(Techniques.SlideInDown)
		.duration(500)
		.interpolate(new AccelerateDecelerateInterpolator())
		.withListener(animatorListener)
		.playOn(findViewById(R.id.relative_layout_root));
	
		loadCostList(date);
	}	
}
