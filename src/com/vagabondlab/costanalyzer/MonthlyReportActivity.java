package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.List;

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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
public class MonthlyReportActivity extends CActivity{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CostService costService;
	private TextView mCategoryWiseCostStatus;
	private TextView mDayWiseCostStatus;
	
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private DateTime mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
	private ScrollView mSvTable;
	
	private TextView mSummaryStatusView;
	private TextView mTotalCostView;
	private TextView mProductiveCostView;
	private TextView mWastageCostView;
	private Double productiveCost = 0.0;
	private Double wastageCost = 0.0;
	private Double totalCost = 0.0;
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
	private int action = 0;
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monthly_report);
		setTitle(getString(R.string.title_monthly_report));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		try { 
			costService = new CostService(getHelper().getCostDao());
			mGestureDetector = new GestureDetector(this);
			
			mCategoryWiseCostStatus = (TextView)findViewById(R.id.textView_category_wise_cost_status);
			mDayWiseCostStatus = (TextView)findViewById(R.id.textView_day_wise_cost_status);
			
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			mSvTable = (ScrollView)findViewById(R.id.scroll_view_table);
			mSvTable.setOnTouchListener(shortSummarySwipeListener);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mTotalCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mProductiveCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			mWastageCostView.setOnTouchListener(shortSummarySwipeListener);
		
			DateTime date = new DateTime();
			loadCostList(date);
			
			//google analytics
			((CostAnalyzer) getApplication()).getTracker(CostAnalyzer.TrackerName.APP_TRACKER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCostList(DateTime date){
		try {
			mCurrentDate =  date;
			String startDate = dateFormatter.print(date.dayOfMonth().withMinimumValue());
			String endDate = dateFormatter.print(date.dayOfMonth().withMaximumValue());
			loadQuickView(startDate, endDate);
			
			List<String[]>  costList = costService.getTotalCostGroupByCategory(startDate, endDate);
			loadCategoryWiseCostListViewUI(costList);
			
			costList = costService.getCostListGroupByDate(startDate, endDate, getString(R.string.productive), getString(R.string.wastage));
			loadDayWiseCostListViewUI(costList);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	
	private void loadQuickView(String startDate, String endDate){
		try{
			productiveCost = 0.0;
			wastageCost = 0.0;
			totalCost = 0.0;
			List<String[]> costListGroupByType = costService.getTotalCostGroupByType(startDate, endDate);
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
			mTotalCostView.setText(String.valueOf(totalCost.intValue()));
			mProductiveCostView.setText(String.valueOf(productiveCost.intValue()));
			mWastageCostView.setText(String.valueOf(wastageCost.intValue()));
			mSummaryStatusView.setText(IUtil.theMonth(mCurrentDate.getMonthOfYear()) + " " + mCurrentDate.getYear());
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	private void loadCategoryWiseCostListViewUI(List<String[]> costList) {
		try {
			TableLayout table = (TableLayout)findViewById(R.id.categoryWiseCostTable);
			table.removeAllViews();
			table.addView(ViewUtil.getCategoryWiseCostTableHeader(this));
			for(String[] cost : costList){
				TableRow tr = new TableRow(this);
				tr.addView(ViewUtil.getTableColumn(this, cost[0] + "(" + cost[1] + ")", Gravity.LEFT));
//				tr.addView(ViewUtil.getTableColumn(this, cost[2], Gravity.CENTER));
				
				Double ccost = Double.valueOf(cost[2]);
				tr.addView(ViewUtil.getTableColumn(this, String.format("%.1f", ccost), Gravity.CENTER));
				
				Double costPercantage = 0.0;
				if (totalCost != 0 && ccost != 0) {
					costPercantage = (ccost * 100)/ totalCost;					
				}
				tr.addView(ViewUtil.getTableColumn(this, String.format("%.1f", costPercantage) + "%", Gravity.CENTER));
				table.addView(tr);
				table.addView(ViewUtil.getDividerView(getApplicationContext()));
			}
			mCategoryWiseCostStatus.setText(getString(R.string.category_wise_cost_status, costList.size()));
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadDayWiseCostListViewUI(List<String[]> costList) {
		try {
			TableLayout table = (TableLayout)findViewById(R.id.dayWiseCostTable);
			table.removeAllViews();
			table.addView(ViewUtil.getWeekDayTableHeader(this));
			for(String[] cost : costList){
				TableRow tr = new TableRow(this);
				//String day = IUtil.changeDateFormat(cost[0], IUtil.DATE_FORMAT_YYYY_MM_DD, "E");
				tr.addView(ViewUtil.getTableColumn(this, cost[0], Gravity.LEFT));
				tr.addView(ViewUtil.getTableColumn(this, String.format("%.1f", Double.valueOf(cost[3])), Gravity.CENTER));
				tr.addView(ViewUtil.getTableColumn(this, String.format("%.1f", Double.valueOf(cost[1])), Gravity.CENTER));
				tr.addView(ViewUtil.getTableColumn(this, String.format("%.1f", Double.valueOf(cost[2])), Gravity.CENTER));
				table.addView(tr);
				table.addView(ViewUtil.getDividerView(getApplicationContext()));
			}
			mDayWiseCostStatus.setText(getString(R.string.day_wise_cost_status, costList.size()));
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
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
			mProgressDialog = ProgressDialog.show(MonthlyReportActivity.this, "Please wait ...", "Loading...", true);
			mProgressDialog.setCancelable(true);
		}
		
		@Override
		public void onAnimationRepeat(Animator arg0) {
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

	// 2. override
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
		
		DateTime prevDate = mCurrentDate.plusMonths(-1);
		loadCostList(prevDate);
		
	}
	
	@Override
	public void prevView(){
		YoYo.with(Techniques.SlideInLeft)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(animatorListener)
			.playOn(findViewById(R.id.relative_layout_root));
		
		DateTime nextDate = mCurrentDate.plusMonths(1);
		loadCostList(nextDate);
	}
		
	@Override
	public void returnDate(String date) {
		action = IConstant.ACTION_SEARCH;
		closeProgressDialog();
		
		YoYo.with(Techniques.SlideInDown)
		.duration(500)
		.interpolate(new AccelerateDecelerateInterpolator())
		.withListener(animatorListener)
		.playOn(findViewById(R.id.relative_layout_root));
		
		loadCostList(dateFormatter.parseDateTime(date));
	}

	@Override
	public ListView getListView() {
		return null;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(action == IConstant.ACTION_SEARCH){
				showProgressDialog();
				DateTime date = new DateTime();
				loadCostList(date);
				action = IConstant.ACTION_NONE;
				closeProgressDialog();
			}else{
				Intent i = new Intent(getApplicationContext(),HomeActivity.class);
				startActivity(i);				
			}
			return true;
		}		
		return super.onKeyDown(keyCode, event);
	}
	
}
