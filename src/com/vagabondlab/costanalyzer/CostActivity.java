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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class CostActivity extends ActionBarActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CategoryService categoryService;
	private CostService costService;
	
	private Spinner mCategoryName;
	private EditText mCostAmount;
	private DatePicker mCostDatePicker;

	private TextView mCostStatus;
	
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	private HashMap<Integer,String[]> spinnerCategoryMap = new HashMap<Integer, String[]>();
	private String[] spinnerArray;
	
	private int selectedCostId;
	private String selectedCostName;
	
	private final int CONTEXT_MENU_EDIT = 1;
	private final int CONTEXT_MENU_ARCHIVE = 2;
	private int action = 0;
	private boolean firstTime = true;

	
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					try{
						View idChild = ((ViewGroup) v).getChildAt(1);
						selectedCostId = Integer.valueOf(((TextView) idChild).getText().toString());
						
						View nameChild = ((ViewGroup) v).getChildAt(0);
						selectedCostName = ((TextView) nameChild).getText().toString();
						
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_cost);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_cost);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_cost,(DrawerLayout) findViewById(R.id.drawer_layout_cost));
		try { 
			//getHelper().onUpgrade(getHelper().getWritableDatabase(),getHelper().getConnectionSource(), 0, 1);
			categoryService = new CategoryService(getHelper().getCategoryDao());
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			loadCostCategory();
			loadCostList();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}
	
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cost, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle(selectedCostName);
		menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, CONTEXT_MENU_ARCHIVE, Menu.NONE, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCostDialougeBox();
		}
			break;
		case CONTEXT_MENU_ARCHIVE: {
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		setTitle(getString(R.string.title_activity_cost));
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
		
//		mCategoryName.set
		mCostAmount.setText(String.valueOf(cost.getAmount()));
		//Date date = IUtil.getDate(cost.getDate(), IUtil.DATE_FORMAT);
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
		Integer categoryId = IUtil.getKeyFromValue(spinnerCategoryMap, categoryName);
		if(categoryId == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.cost_category_missing));
			return 0;
		}
		
		if(!IUtil.isNotBlank(mCostAmount.getText())){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.cost_amount_missing));
			return 0;
		} 
		
		Double costAmount = Double.valueOf(mCostAmount.getText().toString());
		String costDate = IUtil.getDateFromDatePicker(mCostDatePicker, IUtil.DATE_FORMAT_YYYY_MM_DD);
		
		String i= categoryId + "-" + costAmount + "-" + costDate;
		ViewUtil.showMessage(getApplicationContext(), i);

		Cost cost = new Cost();
		if(action == IConstant.ACTION_EDIT){
			cost = costService.getCostById(selectedCostId);
		}
		cost.setCategory_id(categoryId);
		cost.setAmount(costAmount);
		cost.setDate(costDate);
		cost.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
		cost.setCreated_by_name("");
		
		int sucess = 0;
		if(action == IConstant.ACTION_ADD){
			sucess = costService.createCost(cost);
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
			List<Cost> costList = costService.getAllCost();
			loadUI(costList, costService.countCost()); 
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
				String[] cnt = (String[])spinnerCategoryMap.get(cost.getCategory_id());
				infoMap.put("name", cnt[0] + " - " + String.valueOf(cost.getAmount()));
				infoMap.put("id", String.valueOf(cost.getId()));
				String info = cnt[1];
				if(IUtil.isNotBlank(cost.getDate())){
					String date = IUtil.changeDateFormat(cost.getDate(), IUtil.DATE_FORMAT_YYYY_MM_DD, IUtil.DATE_FORMAT_EEE_D_MMM_YYYY);
					
					info += ", " + date;
				}
				infoMap.put("info", info);
				mCostListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter( 
					this, 
					mCostListdata,
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
	
	private void loadCategorySpinner(Spinner categorySpinner){
		try {
			ArrayAdapter<String> adapter =new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, spinnerArray);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			categorySpinner.setAdapter(adapter);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadCostCategory(){
		try {
			List<Category> categoryList = categoryService.getAllCategory();
			spinnerCategoryMap = new HashMap<Integer, String[]>();
			spinnerArray = new String[categoryList.size()+1];
			
			int i = 0;
			spinnerArray[i++] = getString(R.string.select_category);
			for(Category category : categoryList){
				String [] cnt = {category.getName(), category.getType()};
				spinnerCategoryMap.put(category.getId(),cnt);
				spinnerArray[i++] = category.getName();
			}
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
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
			Intent i = new Intent(getApplicationContext(),CategoryActivity.class);
			startActivity(i);
			break;
		case 1:
			i = new Intent(getApplicationContext(),CostActivity.class);
			startActivity(i);
			break;
		case 2:
			break;
		}
	}	
	
}
