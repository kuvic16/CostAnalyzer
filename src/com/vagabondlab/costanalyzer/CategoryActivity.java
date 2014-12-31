package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.internal.widget.AdapterViewCompat.OnItemClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

public class CategoryActivity extends ActionBarActivity {
	
	private DatabaseHelper databaseHelper = null;
	private CategoryService categoryService;
	
	private EditText mCategoryName;
	private RadioButton mProductive;
	private RadioButton mWastage;
	private Button mCategorySaveButton;
	private TextView mCategoryStatus;
	
	private List<Map<String, String>> mCategoryListdata = new ArrayList<Map<String, String>>();
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					View idChild = ((ViewGroup) v).getChildAt(1);
					String idChildValue = ((TextView) idChild).getText().toString();
					ViewUtil.showMessage(getApplicationContext(), idChildValue);
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);
		setTitle(getString(R.string.cost_category));
		
		try {
			categoryService = new CategoryService(getHelper().getCategoryDao());
			mCategorySaveButton = (Button) findViewById(R.id.button_add_category);
			mCategorySaveButton.setOnClickListener(mSaveCategoryButtonListener);
			
			mCategoryName = (EditText)findViewById(R.id.editText_category_name);
			mProductive = (RadioButton)findViewById(R.id.radio_productive);
			mWastage = (RadioButton)findViewById(R.id.radio_wastage);
			mCategoryStatus = (TextView)findViewById(R.id.textView_category_status);
			
			loadCategoryList();
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public Button.OnClickListener mSaveCategoryButtonListener = new Button.OnClickListener() {
	    public void onClick(View v) {
	    	if(IUtil.isNotBlank(mCategoryName.getText())){
	    		String categoryName = mCategoryName.getText().toString().toLowerCase();
	    		String categoryType = getString(R.string.productive);
	    		if(mWastage.isChecked()){
	    			categoryType = getString(R.string.wastage);
	    		}else if(mProductive.isChecked()){
	    			categoryType = getString(R.string.productive);
	    		}
	    		
	    		Category category= new Category();
	    		category.setName(categoryName);
	    		category.setType(categoryType);
	    		category.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
	    		category.setCreated_by_name("");
	    		int sucess = categoryService.createCategory(category);
	    		if(sucess > 0){
	    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_success, categoryName));
	    			loadCategoryList();
	    		}else{
	    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_category_failed));
	    		}
	    	}else{
	    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.category_name_missing));
	    	}
	    }
	};
	
	
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
}
