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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
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
public class TotalReportActivity extends ActionBarActivity implements OnGestureListener, NavigationDrawerFragment.NavigationDrawerCallbacks, DateSetListener
																		{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CostService costService;
	private TextView mCostStatus;
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	
	private CharSequence mTitle;
	private boolean firstTime = true;
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private String mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
	private Double totalCost = 0.0;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_total_report);
		setTitle(getString(R.string.title_daily_report));

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			mGestureDetector = new GestureDetector(this);
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			ListView mList = (ListView)findViewById(android.R.id.list);
			mList.setOnTouchListener(shortSummarySwipeListener);
			
			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
		} catch (SQLException e) {
			e.printStackTrace();
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
			i = new Intent(getApplicationContext(),TotalReportActivity.class);
			startActivityForResult(i, IConstant.PARENT_ACTIVITY_REQUEST_CODE);
			break;
		}
	}
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
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
	
//	private void showCalenderDialog(){
//		final Calendar c = Calendar.getInstance();
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int day = c.get(Calendar.DAY_OF_MONTH);
//
//        // Create a new instance of DatePickerDialog and return it
//        return new DatePickerDialog(getActivity(), this, year, month, day);
//	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode==IConstant.PARENT_ACTIVITY_REQUEST_CODE){
	    	firstTime = true;
	    	onNavigationDrawerItemSelected(0);
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
			Double productiveCost = 0.0;
			Double wastageCost = 0.0;
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
			TextView textViewTotalCost = (TextView)findViewById(R.id.textView_summary_total_cost);
			textViewTotalCost.setText(String.valueOf(totalCost.intValue()));
			
			TextView textViewProductiveCost = (TextView)findViewById(R.id.textView_summary_effective_cost);
			textViewProductiveCost.setText(String.valueOf(productiveCost.intValue()));
//			if(totalCost != 0 && productiveCost != 0){
//				TextView textViewProductiveCostStatus = (TextView)findViewById(R.id.textView_summary_effective_cost_status);
//				int productivePercantage = (productiveCost.intValue() * 100)/totalCost.intValue();
//				textViewProductiveCostStatus.setText(getString(R.string.productive) + " " + String.valueOf(productivePercantage) + "%");
//			}
			
			TextView textViewWastageCost = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			textViewWastageCost.setText(String.valueOf(wastageCost.intValue()));
//			if(totalCost != 0 && wastageCost != 0){
//				TextView textViewWastageCostStatus = (TextView)findViewById(R.id.textView_summary_wastage_cost_status);
//				int wastagePercantage = (wastageCost.intValue() * 100)/totalCost.intValue();
//				textViewWastageCostStatus.setText(getString(R.string.wastage) + " " + String.valueOf(wastagePercantage) + "%");
//			}
			
			String dateStatus = "";			
			if(date.equalsIgnoreCase(today)){
				dateStatus = getString(R.string.today_top_date_text, IUtil.changeDateFormat(date, IUtil.DATE_FORMAT_YYYY_MM_DD, "EEE, MMM d, yyyy"));
			}else{
				dateStatus = IUtil.changeDateFormat(date, IUtil.DATE_FORMAT_YYYY_MM_DD, "EEE, MMM d, yyyy");
			}
			TextView topDateText = (TextView)findViewById(R.id.textView_summary_status);
			topDateText.setText(dateStatus);
						
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
		loadCostList(newDate);
		
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
		loadCostList(newDate);
	}
	
	AnimatorListener animatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator arg0) {
			mProgressDialog = ProgressDialog.show(TotalReportActivity.this, "Please wait ...", "Loading...", true);
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
		.duration(500)
		.interpolate(new AccelerateDecelerateInterpolator())
		.withListener(animatorListener)
		.playOn(findViewById(R.id.relative_layout_root));
	
		loadCostList(date);
	}
	
}
