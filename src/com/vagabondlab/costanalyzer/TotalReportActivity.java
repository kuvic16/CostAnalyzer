package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vagabondlab.costanalyzer.database.service.CostService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;


@SuppressLint({ "ClickableViewAccessibility", "DefaultLocale" })
public class TotalReportActivity  extends CActivity{

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CostService costService;
	private TextView mCostStatus;
	private List<Map<String, String>> mCostListdata = new ArrayList<Map<String, String>>();
	
	private TextView mSummaryStatusView;
	private TextView mTotalCostView;
	private TextView mProductiveCostView;
	private TextView mWastageCostView;
	private Double productiveCost = 0.0;
	private Double wastageCost = 0.0;
	private Double totalCost = 0.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_total_report);
		setTitle(getString(R.string.title_total_report));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
		
		try { 
			costService = new CostService(getHelper().getCostDao());
			mCostStatus = (TextView)findViewById(R.id.textView_cost_status);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			mTotalCostView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mProductiveCostView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mWastageCostView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			
			loadCostList(null, null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCostList(String startDate, String endDate){
		try {
			loadQuickView(startDate, endDate);
			List<String[]>  costList = costService.getTotalCostGroupByCategory(startDate, endDate);
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
			mSummaryStatusView.setText("Lifetime Report");
						
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	@Override
	public ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	    }
	    return mListView;
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
	}
	
	@Override
	public void prevView(){		
	}
	
	@Override
	public void returnDate(String date) {
	}
	
}
