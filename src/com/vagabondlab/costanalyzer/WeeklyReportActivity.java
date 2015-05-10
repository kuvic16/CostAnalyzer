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
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
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
public class WeeklyReportActivity extends CActivity {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CostService costService;
	
	private List<Map<String, String>> mWeekListdata = new ArrayList<Map<String, String>>();
	private List<List<String>> weeks = new ArrayList<List<String>>();
	private int currentWeek;
	
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private DateTime mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
	private ScrollView mSvTable;
	private RelativeLayout mRLWeekDetailsSection;
	private RelativeLayout mRLRoot;
	
	
	
	private TextView mWeekStatusView;
	private TextView mSummaryStatusView;
	private TextView mTotalCostView;
	private TextView mProductiveCostView;
	private TextView mWastageCostView;
	private Double productiveCost = 0.0;
	private Double wastageCost = 0.0;
	private Double totalCost = 0.0;
	private int action = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weekly_report);
		setTitle(getString(R.string.title_weekly_report));
		mTitle = getTitle();
		

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			costService = new CostService(getHelper().getCostDao());
			mGestureDetector = new GestureDetector(this);
			
			mSvTable = (ScrollView)findViewById(R.id.scroll_view_table);
			mSvTable.setOnTouchListener(shortSummarySwipeListener);
			
			mRLRoot = (RelativeLayout)findViewById(R.id.relative_layout_root);
			mRLRoot.setOnTouchListener(shortSummarySwipeListener);
			
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			mRLWeekDetailsSection = (RelativeLayout)findViewById(R.id.relative_layout_week_details_section);
			mRLWeekDetailsSection.setOnTouchListener(shortSummarySwipeListener);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mTotalCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mProductiveCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			mWastageCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mWeekStatusView = (TextView)findViewById(R.id.textView_week_status);
		
			Calendar calendar = Calendar.getInstance();
			loadWeekReport(calendar, new DateTime());
			
			//google analytics
			((CostAnalyzer) getApplication()).getTracker(CostAnalyzer.TrackerName.APP_TRACKER);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadWeekReport(Calendar calendar, DateTime date){
		try{
			mCurrentDate =  date;
			mWeekStatusView.setText(IUtil.theMonth(calendar.get(Calendar.MONTH) + 1));
			weeks = IUtil.getNumberOfWeeks(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
			
			String dateString = IUtil.getDateFromCalender(calendar, IUtil.DATE_FORMAT_YYYY_MM_DD);
			currentWeek =  IUtil.getCurrentWeek(weeks, dateString);
			
			loadWeekListViewUI();
			loadCostList(currentWeek);
		}catch(Throwable t){
			
		}
	}
	
	private void loadWeekListViewUI() {
		try {
			mWeekListdata = new ArrayList<Map<String,String>>();
			int position = 0;
			for (List<String> week : weeks) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("week_number", String.valueOf(position + 1));
				infoMap.put("week_name", getString(R.string.week) + ": " + (position + 1));
				
				String info = IUtil.changeDateFormat(week.get(0), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY);
				info += "\n to \n" + IUtil.changeDateFormat(week.get(1), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY);
				infoMap.put("week_start_end_date", info);
				mWeekListdata.add(infoMap);
				position++;
			}
			
			SimpleAdapter adapter = new SimpleAdapter( 
					this, 
					mWeekListdata,
					R.layout.week_list_view, 
					new String[] {"week_number", "week_name", "week_start_end_date"}, 
					new int[] { R.id.week_number, R.id.week_name, R.id.week_start_end_date}
			);
			setListAdapter(adapter);
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			getListView().setItemsCanFocus(false);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	
	private void loadWeekDayCostListViewUI(List<String[]> costList) {
		try {
			TableLayout table = (TableLayout)findViewById(R.id.weekTable);
			table.removeAllViews();
			table.addView(getWeekDayTableHeader());
			for(String[] cost : costList){
				TableRow tr = new TableRow(this);
				String day = IUtil.changeDateFormat(cost[0], IUtil.DATE_FORMAT_YYYY_MM_DD, "E");
				tr.addView(getTableColumn(day, Gravity.LEFT));
				tr.addView(getTableColumn(String.format("%.1f", Double.valueOf(cost[3])), Gravity.CENTER));
				tr.addView(getTableColumn(String.format("%.1f", Double.valueOf(cost[1])), Gravity.CENTER));
				tr.addView(getTableColumn(String.format("%.1f", Double.valueOf(cost[2])), Gravity.CENTER));
				table.addView(tr);
				table.addView(ViewUtil.getDividerView(getApplicationContext()));
			}
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadCategoryWiseCostListViewUI(List<String[]> costList) {
		try {
			TableLayout table = (TableLayout)findViewById(R.id.categoryWiseCostTable);
			table.removeAllViews();
			table.addView(getCategoryWiseCostTableHeader());
			for(String[] cost : costList){
				TableRow tr = new TableRow(this);
				tr.addView(getTableColumn(cost[0] + "(" + cost[1] + ")", Gravity.LEFT));
//				tr.addView(getTableColumn(cost[2], Gravity.CENTER));
				
				Double ccost = Double.valueOf(cost[2]);
				tr.addView(getTableColumn(String.format("%.1f", ccost), Gravity.CENTER));
				
				Double costPercantage = 0.0;
				if (totalCost != 0 && ccost != 0) {
					costPercantage = (ccost * 100)/ totalCost;					
				}
				tr.addView(getTableColumn(String.format("%.1f", costPercantage) + "%", Gravity.CENTER));
				table.addView(tr);
				table.addView(ViewUtil.getDividerView(getApplicationContext()));
			}
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private TextView getTableColumn(String text, int gravity){
		TextView label = new TextView(getApplicationContext());
		label.setPadding(1, 3, 1, 3);
        label.setText(text);
        label.setGravity(gravity);
        label.setTextColor(Color.BLACK);
        return label;
	}
	
	private TableRow getWeekDayTableHeader(){
		TableRow header = new TableRow(this);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(getString(R.string.report_day), Gravity.LEFT));
		header.addView(getTableColumn(getString(R.string.report_total), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.report_productive), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.report_wastage), Gravity.CENTER));
		return header;
	}
	
	private TableRow getCategoryWiseCostTableHeader(){
		TableRow header = new TableRow(this);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(getString(R.string.report_category), Gravity.LEFT));
		header.addView(getTableColumn(getString(R.string.report_total_cost), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.report_percentage), Gravity.RIGHT));
		return header;
	}
		
	private void loadCostList(int weekNumber){
		try { 
			String startDate = weeks.get(currentWeek-1).get(0);
			String endDate = weeks.get(currentWeek-1).get(1);
			loadQuickView(startDate, endDate);
			List<String[]>  costList = costService.getCostListGroupByDate(startDate, endDate, getString(R.string.productive), getString(R.string.wastage));
			loadWeekDayCostListViewUI(costList);
			
			costList = costService.getTotalCostGroupByCategory(startDate, endDate);
			loadCategoryWiseCostListViewUI(costList);
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
			
			String weekStatus = getString(R.string.week) + " " + currentWeek + " ( ";			
			weekStatus += IUtil.changeDateFormat(startDate, IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY);
			weekStatus += " - " + IUtil.changeDateFormat(endDate, IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY) + " )";
			mSummaryStatusView.setText(weekStatus);
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
			mProgressDialog = ProgressDialog.show(WeeklyReportActivity.this, "Please wait ...", "Loading...", true);
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
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					try{
						//RelativeLayout rl = (RelativeLayout)v;
						//rl.setBackgroundColor(Color.RED);
						
						View viewWeekNumber = ((ViewGroup) v).getChildAt(2);
						currentWeek = Integer.valueOf(((TextView) viewWeekNumber).getText().toString());
						loadCostList(currentWeek);
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
		DateTimeFormatter fmt = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String newDate = fmt.print(prevDate);
		Calendar calender = IUtil.getCalender(newDate, IUtil.DATE_FORMAT_YYYY_MM_DD);
		loadWeekReport(calender, prevDate);
	}
	
	@Override
	public void prevView(){
		YoYo.with(Techniques.SlideInLeft)
			.duration(500)
			.interpolate(new AccelerateDecelerateInterpolator())
			.withListener(animatorListener)
			.playOn(findViewById(R.id.relative_layout_root));
		
		DateTime nextDate = mCurrentDate.plusMonths(1);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(IUtil.DATE_FORMAT_YYYY_MM_DD);
		String newDate = fmt.print(nextDate);
		Calendar calender = IUtil.getCalender(newDate, IUtil.DATE_FORMAT_YYYY_MM_DD);
		loadWeekReport(calender, nextDate);
	}
	
	@Override
	public void returnDate(String dateString) {
		action = IConstant.ACTION_SEARCH;
		closeProgressDialog();
		
		YoYo.with(Techniques.SlideInDown)
		.duration(100)
		.interpolate(new AccelerateDecelerateInterpolator())
		.withListener(animatorListener)
		.playOn(findViewById(R.id.relative_layout_root));
	
		Calendar calender = IUtil.getCalender(dateString, IUtil.DATE_FORMAT_YYYY_MM_DD);
		Date date = IUtil.getDate(dateString, IUtil.DATE_FORMAT_YYYY_MM_DD);
		DateTime dateTime = new DateTime(date);
		loadWeekReport(calender, dateTime);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(action == IConstant.ACTION_SEARCH){
				Calendar calendar = Calendar.getInstance();
				loadWeekReport(calendar, new DateTime());
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
