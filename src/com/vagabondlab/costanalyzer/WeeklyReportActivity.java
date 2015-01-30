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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment.DateSetListener;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;


@SuppressLint({ "ClickableViewAccessibility", "DefaultLocale" })
public class WeeklyReportActivity extends ActionBarActivity implements OnGestureListener, NavigationDrawerFragment.NavigationDrawerCallbacks, DateSetListener
																		{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CostService costService;
	private TextView mCostStatus;
	
	private List<Map<String, String>> mWeekListdata = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	private List<List<String>> weeks = new ArrayList<List<String>>();
	private int currentWeek;
	
	private CharSequence mTitle;
	private boolean firstTime = true;
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private String mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
	private TextView mSummaryStatusView;
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
		setContentView(R.layout.activity_weekly_report);
		setTitle(getString(R.string.title_weekly_report));

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			costService = new CostService(getHelper().getCostDao());
			//mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			mGestureDetector = new GestureDetector(this);
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			//mTotalCostView.setOnClickListener(totalCostTouchListener);
			mTotalCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			//mProductiveCostView.setOnClickListener(productiveCostTouchListener);
			mProductiveCostView.setOnTouchListener(shortSummarySwipeListener);
			
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			//mWastageCostView.setOnClickListener(wastageCostTouchListener);
			mWastageCostView.setOnTouchListener(shortSummarySwipeListener);
		
			Calendar calendar = Calendar.getInstance();
			loadWeekReport(calendar);
//	        weeks = IUtil.getNumberOfWeeks(c.get(Calendar.MONTH), c.get(Calendar.YEAR));
//			currentWeek = IUtil.getCurrentWeek();
//			loadWeekListViewUI();
//			loadCostList(currentWeek);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadWeekReport(Calendar calendar){
		try{
			weeks = IUtil.getNumberOfWeeks(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
			currentWeek = calendar.get(Calendar.WEEK_OF_MONTH);
			loadWeekListViewUI();
			loadCostList(currentWeek);
		}catch(Throwable t){
			
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
			Intent i = new Intent(getApplicationContext(),HomeActivity.class);
			startActivity(i);
			break;
		case 1:
			i = new Intent(getApplicationContext(),CategoryActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 2:
			i = new Intent(getApplicationContext(),CostActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 3:
			i = new Intent(getApplicationContext(),DailyReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 4:
			i = new Intent(getApplicationContext(),WeeklyReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 5:
			i = new Intent(getApplicationContext(),MonthlyReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 6:
			i = new Intent(getApplicationContext(),YearlyReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 7:
			i = new Intent(getApplicationContext(),TotalReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		case 8:
			i = new Intent(getApplicationContext(),TransactionActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		}
	}
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					try{
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
	protected void onDestroy() {
		setResult(IConstant.PARENT_ACTIVITY_REQUEST_CODE);
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}
	
	@Override
	protected void onStop() {
	    setResult(IConstant.PARENT_ACTIVITY_REQUEST_CODE);
	    super.onStop();
	}
	
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
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
	
	private void loadWeekListViewUI() {
		try {
			mWeekListdata = new ArrayList<Map<String,String>>();
			int position = 0;
			for (List<String> week : weeks) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("week_number", String.valueOf(position + 1));
				infoMap.put("week_name", getString(R.string.week) + " " + (position + 1));
				
				String info = IUtil.changeDateFormat(week.get(0), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY);
				info += " - " + IUtil.changeDateFormat(week.get(1), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM_D_YY);
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
				tr.addView(getTableColumn(cost[1], Gravity.CENTER));
				tr.addView(getTableColumn(cost[2], Gravity.CENTER));
				tr.addView(getTableColumn(cost[3], Gravity.CENTER));
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
				tr.addView(getTableColumn(cost[2], Gravity.CENTER));
				
				Double ccost = Double.valueOf(cost[2]);
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
		header.addView(getTableColumn(getString(R.string.day), Gravity.LEFT));
		header.addView(getTableColumn(getString(R.string.productive), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.wastage), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.total_cost), Gravity.RIGHT));
		return header;
	}
	
	private TableRow getCategoryWiseCostTableHeader(){
		TableRow header = new TableRow(this);
		header.setBackgroundColor(Color.LTGRAY);
		header.addView(getTableColumn(getString(R.string.category_name), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.total_cost), Gravity.CENTER));
		header.addView(getTableColumn(getString(R.string.parcantage), Gravity.LEFT));
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

	private void loadUI(List<String[]> costList, long total) {
		try {
			mCostStatus.setText(getString(R.string.category_wise_cost_status, total));
			mCostListdata = new ArrayList<Map<String,String>>();
			for (String[] costs : costList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("cost_category_name", costs[0]);
				
				String info = costs[3] + "\n" + costs[1] + " time happened";
				infoMap.put("cost_category_type_and_time", info);
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

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int xPixelLimit = (int) (dm.xdpi * .25);
			int yPixelLimit = (int) (dm.ydpi * .25);

		
			if ((Math.abs(e1.getX() - e2.getX()) > xPixelLimit && Math.abs(e1
					.getY() - e2.getY()) < yPixelLimit)
					|| Math.abs(e1.getX() - e2.getX()) > xPixelLimit * 2) {
				if (velocityX > 0) {
					if (e1.getX() > e2.getX()) {
						nextView();
					} else {
						prevView();
					}
				} else {
					if (e1.getX() < e2.getX()) {
						prevView();
					} else {
						nextView();
					}
				}
				return true;
			}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}
	
	private void nextView(){
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
		//loadCostList(newDate);
		
	}
	
	private void prevView(){
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
		//loadCostList(newDate);
	}
	
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

	@Override
	public void returnDate(String date) {
		YoYo.with(Techniques.SlideInDown)
		.duration(100)
		.interpolate(new AccelerateDecelerateInterpolator())
		.withListener(animatorListener)
		.playOn(findViewById(R.id.relative_layout_root));
	
		//loadCostList(date);
		Calendar calender = IUtil.getCalender(date, IUtil.DATE_FORMAT_YYYY_MM_DD);
		loadWeekReport(calender);
//		weeks = IUtil.getNumberOfWeeks(calender.get(Calendar.MONTH), calender.get(Calendar.YEAR));
//		currentWeek = calender.get(Calendar.WEEK_OF_MONTH);
//		loadWeekListViewUI();
//		loadCostList(currentWeek);
	}
	
}
