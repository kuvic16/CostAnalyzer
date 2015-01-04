package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.database.service.CategoryService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class CategoryActivity extends ActionBarActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private CategoryService categoryService;
	
	private EditText mCategoryName;
	private RadioButton mProductive;
	private RadioButton mWastage;
	private TextView mCategoryStatus;
	
	private List<Map<String, String>> mCategoryListdata = new ArrayList<Map<String, String>>();
	private CharSequence mTitle;
	
	private int selectedCategoryId;
	private String selectedCategoryName;
	
	private final int CONTEXT_MENU_EDIT = 1;
	private final int CONTEXT_MENU_ARCHIVE = 2;
	private int action = 0;

	
	
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
						selectedCategoryId = Integer.valueOf(((TextView) idChild).getText().toString());
						
						View nameChild = ((ViewGroup) v).getChildAt(0);
						selectedCategoryName = ((TextView) nameChild).getText().toString();
						
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
		setContentView(R.layout.activity_category);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_category);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_category,(DrawerLayout) findViewById(R.id.drawer_layout_category));
		setTitle(getString(R.string.cost_category));
		
		try {
			categoryService = new CategoryService(getHelper().getCategoryDao());
			mCategoryStatus = (TextView)findViewById(R.id.textView_category_status);
			loadCategoryList();
			mTitle = getTitle();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.category, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle(selectedCategoryName);
		menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, CONTEXT_MENU_ARCHIVE, Menu.NONE, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCategoryDialougeBox();
		}
			break;
		case CONTEXT_MENU_ARCHIVE: {
			action = IConstant.ACTION_DELETE;
			deleteCategoryDialougeBox();
		}
			break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.add_category) {
			action = IConstant.ACTION_ADD;
			addNewCategoryDialougeBox();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}
	
	
	@SuppressLint("InflateParams")
	private void addNewCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categoryFormView = factory.inflate(R.layout.category_form, null);
		mCategoryName = (EditText)categoryFormView.findViewById(R.id.editText_category_name);
		mProductive = (RadioButton)categoryFormView.findViewById(R.id.radio_productive);
		mWastage = (RadioButton)categoryFormView.findViewById(R.id.radio_wastage);
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.add)
		     .setTitle(R.string.add_new_category)
		     .setView(categoryFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View categoryFormView = factory.inflate(R.layout.category_form, null);
		mCategoryName = (EditText)categoryFormView.findViewById(R.id.editText_category_name);
		mProductive = (RadioButton)categoryFormView.findViewById(R.id.radio_productive);
		mWastage = (RadioButton)categoryFormView.findViewById(R.id.radio_wastage);
		
		Category category = categoryService.getCategoryById(selectedCategoryId);
		if(category == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_category));
			return;
		}
		
		mCategoryName.setText(category.getName());
		if(category.getType().equalsIgnoreCase(getString(R.string.productive))){
			mProductive.setChecked(true);
		}else if(category.getType().equalsIgnoreCase(getString(R.string.wastage))){
			mWastage.setChecked(true);
		}
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.edit)
		     .setTitle(R.string.edit_category)
		     .setView(categoryFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCategoryDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.delete)
		     .setTitle(R.string.delete_category)
		     .setMessage(getString(R.string.delete_category_are_u_sure, selectedCategoryName))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	
	DialogInterface.OnClickListener saveCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveCategory()==1){
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
				if(deleteCategory()==1){
					break;
				}
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	private int saveCategory(){
		if(IUtil.isNotBlank(mCategoryName.getText())){
    		String categoryName = mCategoryName.getText().toString().toLowerCase();
    		String categoryType = getString(R.string.productive);
    		if(mWastage.isChecked()){
    			categoryType = getString(R.string.wastage);
    		}else if(mProductive.isChecked()){
    			categoryType = getString(R.string.productive);
    		}
    		
    		Category category= new Category();
    		if(action == IConstant.ACTION_EDIT){
    			category= categoryService.getCategoryById(selectedCategoryId);
    		}
    		category.setName(categoryName);
    		category.setType(categoryType);
    		category.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
    		category.setCreated_by_name("");
    		
    		int sucess = 0;
    		if(action == IConstant.ACTION_ADD){
    			sucess = categoryService.createCategory(category);
    		}else if(action == IConstant.ACTION_EDIT){
    			sucess = categoryService.updateCategory(category);
    		} 
    		
    		if(sucess > 0){
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_success, categoryName));
    			loadCategoryList();
    			return 1;
    		}else{
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_failed));
    		}
    	}else{
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.category_name_missing));
    	}
		return 0;
	}
	
	private int deleteCategory(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = categoryService.deleteCategoryById(selectedCategoryId);
		} 
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_category_success, selectedCategoryName));
			loadCategoryList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_category_failed));
		}
    	return 0;
	}
	
	private void loadCategoryList(){
		try {
			List<Category> categoryList = categoryService.getAllCategory();
			loadUI(categoryList, categoryService.countCategory());
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}

	private void loadUI(List<Category> categoryList, long total) {
		try {
			mCategoryStatus.setText(getString(R.string.category_status, total));
			mCategoryListdata = new ArrayList<Map<String,String>>();
			for (Category category : categoryList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("name", category.getName());
				infoMap.put("id", String.valueOf(category.getId()));
				String info = category.getType();
				if(IUtil.isNotBlank(category.getCreated_date())){
					Date date = IUtil.getDate(category.getCreated_date(), IUtil.DATE_FORMAT);				
					info += ", " + date;
				}
				infoMap.put("info", info);
				mCategoryListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter(
					this, 
					mCategoryListdata,
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

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container_category,PlaceholderFragment.newInstance(position + 1)).commit();
		
		ViewUtil.showMessage(getApplicationContext(), String.valueOf(position));
		switch (position) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		}
	}

	public void onSectionAttached(int number) {
		ViewUtil.showMessage(getApplicationContext(), String.valueOf(number));
		switch (number) {
		case 1:
			mTitle = getString(R.string.main_menu1);
			break;
		case 2:
			mTitle = getString(R.string.main_menu2);
			break;
		case 3:
			mTitle = getString(R.string.main_menu3);
			break;
		}
	}
	
	public static class PlaceholderFragment extends Fragment {
		private static final String ARG_SECTION_NUMBER = "section_number";

		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_home, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			//((CategoryActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}
}
