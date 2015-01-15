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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AccelerateDecelerateInterpolator;

public class DailyReportActivity extends ActionBarActivity implements OnGestureListener, NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CategoryService categoryService;
	private CostService costService;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private DatePicker mCostDatePicker;

	private TextView mCostStatus;
	
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	private String[] spinnerArray;
	private ArrayAdapter<String> spinnerAdapter;
	
	private int selectedCostId;
	private String selectedCostName;

	private CharSequence mTitle;
	private boolean firstTime = true;
	private int action = 0;
	private GestureDetector mGestureDetector;
	private RelativeLayout mRLShortSummary;
	private String mCurrentDate;
	private ProgressDialog mProgressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setTitle(getString(R.string.title_home_screen));

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		//getHelper().onUpgrade(getHelper().getWritableDatabase(),getHelper().getConnectionSource(), 1, 2);
		
		try { 
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			mGestureDetector = new GestureDetector(this);
			mRLShortSummary = (RelativeLayout)findViewById(R.id.relative_layout_summary);
			mRLShortSummary.setOnTouchListener(shortSummarySwipeListener);
			
			
			
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
			Intent i = new Intent(getApplicationContext(),DailyReportActivity.class);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle(selectedCostName);
		menu.add(Menu.NONE, IConstant.CONTEXT_MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, IConstant.CONTEXT_MENU_ARCHIVE, Menu.NONE, R.string.delete);
		menu.add(Menu.NONE, IConstant.CONTEXT_MENU_CANCEL, Menu.NONE, R.string.cancel);
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
	    }
	}
	
	@SuppressLint("InflateParams")
	private void addNewCostDialougeBox(){
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
	private void editCostDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View costFormView = factory.inflate(R.layout.cost_form, null);
		
		mCategoryName = (Spinner)costFormView.findViewById(R.id.spinner_category_name);
		mCostAmount = (EditText)costFormView.findViewById(R.id.editText_cost_amount);
		mCostDatePicker = (DatePicker)costFormView.findViewById(R.id.datePicker_cost_date);		
		loadCategorySpinner(mCategoryName);
		
		Cost cost = costService.getCostById(selectedCostId);
		if(cost == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_cost));
			return;
		}
		categoryService.refreash(cost.getCategory());
		
		mCategoryName.setSelection(spinnerAdapter.getPosition(cost.getCategory().getName()));
		mCostAmount.setText(String.valueOf(cost.getAmount()));
		Calendar calender = IUtil.getCalender(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD);
		mCostDatePicker.init(calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH), null);
		
		
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
		String costDate = IUtil.getDateFromDatePicker(mCostDatePicker, IUtil.DATE_FORMAT_YYYY_MM_DD);
		
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
			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
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
			loadCostList(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD));
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
			List<Cost> costList = costService.searchCost(date);
			loadUI(costList, costList.size()); 
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	private void loadUI(List<Cost> costList, long total) {
		try {
			mCostStatus.setText(getString(R.string.cost_status, total));
			mCostListdata = new ArrayList<Map<String,String>>();
			for (Cost cost : costList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("id", String.valueOf(cost.getId()));
				
				categoryService.refreash(cost.getCategory());
				Calendar costDate = IUtil.getCalender(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD);
				infoMap.put("cost_day", String.valueOf(costDate.get(Calendar.DAY_OF_MONTH)));
				infoMap.put("cost_month", IUtil.changeDateFormat(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_MMM) + " " + String.valueOf(costDate.get(Calendar.YEAR)));
				infoMap.put("cost_category_name", cost.getCategory().getName());
				
				String info = cost.getCategory().getType();
				if(IUtil.isNotBlank(cost.getCreated_date())){
					Date date = IUtil.getDate(cost.getCreated_date(), IUtil.DATE_FORMAT);
					info += "\nadded on " + date;
				}
				infoMap.put("cost_category_type_and_time", info);
				Double costAmount = cost.getAmount();
				infoMap.put("cost_amount", String.valueOf(costAmount.intValue()));
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
	
	private void loadQuickView(String date){
		try{
			String today = IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD);
			Double productiveCost = 0.0;
			Double wastageCost = 0.0;
			
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
			
//			Double productiveCost = costService.getTotalCost(getString(R.string.productive), date);
//			Double wastageCost = costService.getTotalCost(getString(R.string.wastage), date);
			Double totalCost = productiveCost + wastageCost;
			
			TextView textViewTotalCost = (TextView)findViewById(R.id.textView_summary_total_cost);
			textViewTotalCost.setText(String.valueOf(totalCost.intValue()));
			
			TextView textViewProductiveCost = (TextView)findViewById(R.id.textView_summary_effective_cost);
			textViewProductiveCost.setText(String.valueOf(productiveCost.intValue()));
//			if(totalCost != 0 && productiveCost != 0){
//				TextView textViewProductiveCostStatus = (TextView)findViewById(R.id.textView_summary_effective_cost_status);
//				int productivePercantage = (productiveCost.intValue() * 100)/totalCost.intValue();
//				textViewProductiveCostStatus.setText(getString(R.string.productive) + "(" + String.valueOf(productivePercantage) + "%)");
//			}
			
			TextView textViewWastageCost = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			textViewWastageCost.setText(String.valueOf(wastageCost.intValue()));
//			if(totalCost != 0 && wastageCost != 0){
//				TextView textViewWastageCostStatus = (TextView)findViewById(R.id.textView_summary_wastage_cost_status);
//				int wastagePercantage = (wastageCost.intValue() * 100)/totalCost.intValue();
//				textViewWastageCostStatus.setText(getString(R.string.wastage) + "(" + String.valueOf(wastagePercantage) + "%)");
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
	
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent mv) {
//		boolean handled = mGestureDetector.onTouchEvent(mv);
//		if (!handled) {
//			return super.dispatchTouchEvent(mv);
//		}
//		return handled; // this is always true
//	}
	
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
						//ViewUtil.showMessage(getApplicationContext(), "next view");
						nextView();
					} else {
						//ViewUtil.showMessage(getApplicationContext(), "prev view");
						prevView();
					}
				} else {
					if (e1.getX() < e2.getX()) {
						//ViewUtil.showMessage(getApplicationContext(), "prev view");
						prevView();
					} else {
						//ViewUtil.showMessage(getApplicationContext(), "next view");
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
	
}
