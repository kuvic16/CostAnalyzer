package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.vagabondlab.costanalyzer.database.DatabaseHelper;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.database.entity.Transaction;
import com.vagabondlab.costanalyzer.database.service.TransactionService;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class TransactionActivity extends ActionBarActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private DatabaseHelper databaseHelper = null;
	private TransactionService transactionService;
	
	private EditText mName;
	private EditText mLendAmount;
	private EditText mBorrowAmount;
	private TextView mTransactionStatus;
	
	private List<Map<String, String>> mTransactionListdata = new ArrayList<Map<String, String>>();
	private CharSequence mTitle;
	
	private int selectedTransactionId;
	private String selectedTransactionName;
	
	private final int CONTEXT_MENU_EDIT = 1;
	private final int CONTEXT_MENU_ARCHIVE = 2;
	private final int CONTEXT_MENU_CANCEL = 3;
	private int action = 0;
	private boolean firstTime = true;
	
	private TextView mSummaryStatusView;
	private TextView mBalanceAmountView;
	private TextView mBalanceAmountViewLabel;
	private TextView mLendAmountView;
	private TextView mLendAmountViewLabel;
	private TextView mBorrowAmountView;
	private TextView mBorrowAmountViewLabel;
	private Double lendAmount = 0.0;
	private Double borrowAmount = 0.0;
	private Double balanceAmount = 0.0;
	private ProgressDialog mProgressDialog = null;

	
	
	// for listview activity
	private ListView mListView;
	protected ListView getListView() {
	    if (mListView == null) {
	        mListView = (ListView) findViewById(android.R.id.list);
	        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
					try{
						View idChild = ((ViewGroup) v).getChildAt(0);
						selectedTransactionId = Integer.valueOf(((TextView) idChild).getText().toString());
						
						View nameChild = ((ViewGroup) v).getChildAt(1);
						selectedTransactionName = ((TextView) nameChild).getText().toString();
						
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
		setContentView(R.layout.activity_transaction);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_transaction);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_transaction,(DrawerLayout) findViewById(R.id.drawer_layout_transaction));
		setTitle(getString(R.string.title_activity_transaction));
		//getHelper().onTransactionUpgrade(getHelper().getWritableDatabase(),getHelper().getConnectionSource(), 0, 1);
		try {
			transactionService = new TransactionService(getHelper().getTransactionDao());
			mTransactionStatus = (TextView)findViewById(R.id.textView_transaction_status);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			mSummaryStatusView.setText(getString(R.string.transaction_summary_status));
			
			mBorrowAmountView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mBorrowAmountView.setOnClickListener(borrowAmountClickListener);
			mBorrowAmountViewLabel = (TextView)findViewById(R.id.textView_summary_total_cost_status);
			mBorrowAmountViewLabel.setText(getString(R.string.borrow));
			
			mLendAmountView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mLendAmountView.setOnClickListener(lendAmountClickListener);
			mLendAmountViewLabel = (TextView)findViewById(R.id.textView_summary_effective_cost_status);
			mLendAmountViewLabel.setText(getString(R.string.lend));
			
			mBalanceAmountView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			mBalanceAmountView.setOnClickListener(balanceAmountClickListener);
			mBalanceAmountViewLabel = (TextView)findViewById(R.id.textView_summary_wastage_cost_status);
			mBalanceAmountViewLabel.setText(getString(R.string.balance));
			
			loadTransactionList();
			mTitle = getTitle();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
		menu.setHeaderTitle(selectedTransactionName);
		menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, CONTEXT_MENU_ARCHIVE, Menu.NONE, R.string.delete);
		menu.add(Menu.NONE, CONTEXT_MENU_CANCEL, Menu.NONE, R.string.cancel);
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
		final View transactionFormView = factory.inflate(R.layout.form_transaction, null);
		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
		mLendAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_lend_amount);
		mBorrowAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_borrow_amount);
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.addnew)
		     .setTitle(R.string.new_transaction)
		     .setView(transactionFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editCategoryDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View transactionFormView = factory.inflate(R.layout.form_transaction, null);
		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
		mLendAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_lend_amount);
		mBorrowAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_borrow_amount);
		
		TextView mTLALabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_lend_amount);
		TextView mTBALabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_borrow_amount);
		
		Transaction transaction = transactionService.getTransactionById(selectedTransactionId);
		if(transaction == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_transaction));
			return;
		}
		
		mName.setText(transaction.getName());
		if(transaction.getLend_amount() > 0){
			mTBALabel.setText(getString(R.string.take_amount));
			mLendAmount.setText(String.valueOf(transaction.getLend_amount()));
		}
		if(transaction.getBorrow_amount() > 0){
			mTLALabel.setText(getString(R.string.pay_amount));
			mBorrowAmount.setText(String.valueOf(transaction.getBorrow_amount()));
		}
		
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.editnew)
		     .setTitle(R.string.edit_transaction)
		     .setView(transactionFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteCategoryDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(R.string.delete_transaction)
		     .setMessage(getString(R.string.delete_transaction_are_u_sure, selectedTransactionName))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	
	DialogInterface.OnClickListener saveCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveTransaction()==1){
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
				if(deleteTransaction()==1){
					break;
				}
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
			}
		}
	};
	
	private int saveTransaction(){
		if(IUtil.isNotBlank(mName.getText())){
    		String name = mName.getText().toString().toLowerCase();
    		String lendAmount = mLendAmount.getText().toString();
    		String borrowAmount = mBorrowAmount.getText().toString();
    		
    		Transaction transaction = new Transaction();
    		if(action == IConstant.ACTION_EDIT){
    			transaction = transactionService.getTransactionById(selectedTransactionId);
    		}
    		transaction.setName(name);
    		if(IUtil.isNotBlank(lendAmount)){
    			transaction.setLend_amount(Double.parseDouble(lendAmount));
    		}else{
    			transaction.setLend_amount(0.0);
    		}
    		
    		if(IUtil.isNotBlank(borrowAmount)){
    			transaction.setBorrow_amount(Double.parseDouble(borrowAmount));
    		}else{
    			transaction.setBorrow_amount(0.0);
    		}
    		
    		if(transaction.getLend_amount() > 0 && transaction.getBorrow_amount()>0){
    			Double diff = transaction.getLend_amount() -  transaction.getBorrow_amount();
    			if(diff==0){
    				transaction.setLend_amount(0.0);
    				transaction.setBorrow_amount(0.0);
    			}else if(diff>0){
    				transaction.setLend_amount(diff);
    				transaction.setBorrow_amount(0.0);
    			}else{
    				transaction.setLend_amount(0.0);
    				transaction.setBorrow_amount(diff*(-1));
    			}
    		}
    		
    		transaction.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
    		transaction.setLast_modified_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
    		
    		int sucess = 0;
    		if(action == IConstant.ACTION_ADD){
    			sucess = transactionService.createTransaction(transaction);
    		}else if(action == IConstant.ACTION_EDIT){
    			sucess = transactionService.updateTransaction(transaction);
    		}
    		
    		if(sucess > 0){
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_success, name));
    			loadTransactionList();
    			return 1;
    		}else{
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_failed));
    		}
    	}else{
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.transaction_name_missing));
    	}
		return 0;
	}
	
	private int deleteTransaction(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = transactionService.deleteTransactionById(selectedTransactionId);
		}
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_transaction_success, selectedTransactionName));
			loadTransactionList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_transaction_failed, selectedTransactionName));
		}
    	return 0;
	}
	
	private void loadTransactionList(){
		try {
			loadQuickView();
			loadListView();
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}		
	}
	
	private void loadListView(){
		List<Transaction> transactionList = transactionService.getAllTransaction();
		loadUI(transactionList, transactionService.countTransaction());
	}

	private void loadUI(List<Transaction> transactionList, long total) {
		try {
			mTransactionStatus.setText(getString(R.string.transaction_status, total));
			mTransactionListdata = new ArrayList<Map<String,String>>();
			for (Transaction transaction : transactionList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("transaction_name", transaction.getName());
				infoMap.put("transaction_row_id", String.valueOf(transaction.getId()));
				String transactionAmount = "";
				if(transaction.getLend_amount()>0){
					transactionAmount = String.valueOf(transaction.getLend_amount()) + "\n " + getString(R.string.lend);
				}else if(transaction.getBorrow_amount()>0){
					transactionAmount = "-" + String.valueOf(transaction.getBorrow_amount()) + "\n " + getString(R.string.borrow);
				}
				infoMap.put("transaction_amount", transactionAmount);
				
				String datestring = "";
				if(IUtil.isNotBlank(transaction.getCreated_date())){
					Date date = IUtil.getDate(transaction.getCreated_date(), IUtil.DATE_FORMAT);				
					datestring += date;
				}
				infoMap.put("transaction_time", datestring);
				mTransactionListdata.add(infoMap);
			}
			
			SimpleAdapter adapter = new SimpleAdapter(
					this, 
					mTransactionListdata,
					R.layout.transaction_list_view,
					new String[] {"transaction_row_id", "transaction_name", "transaction_amount", "transaction_time" }, 
					new int[] { R.id.transaction_row_id, R.id.transaction_name, R.id.transaction_amount, R.id.transaction_time
			});
			setListAdapter(adapter);
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			getListView().setItemsCanFocus(false);
		} catch (Exception ex) {
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, ex));
		}
	}
	
	private void loadQuickView(){
		try{
			lendAmount = 0.0;
			borrowAmount = 0.0;
			balanceAmount = 0.0;
			List<String[]> summaryTransaction = transactionService.getSummaryTransaction();
			if(IUtil.isNotBlank(summaryTransaction)){
				for(String[] trs : summaryTransaction){
					try{
						lendAmount = Double.valueOf(trs[0]);
						borrowAmount = Double.valueOf(trs[1]);
					}catch(Throwable t){
						t.printStackTrace();
					}
				}
			}
			balanceAmount = lendAmount - borrowAmount;
			mBalanceAmountView.setText(String.valueOf(balanceAmount.intValue()));
			mBorrowAmountView.setText(String.valueOf(borrowAmount.intValue()));
			mLendAmountView.setText(String.valueOf(lendAmount.intValue()));
		}catch(Throwable t){
			t.printStackTrace();
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
			i = new Intent(getApplicationContext(),TransactionActivity.class);
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
	
	OnClickListener borrowAmountClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(animatorListener)
					.playOn(mBorrowAmountView);
				
				//loadListView(mCurrentDate, null);
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener lendAmountClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				YoYo.with(Techniques.ZoomIn)
					.duration(500)
					.interpolate(new AccelerateDecelerateInterpolator())
					.withListener(animatorListener)
					.playOn(mLendAmountView);
				
//				loadListView(mCurrentDate, getString(R.string.productive));
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener balanceAmountClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			YoYo.with(Techniques.ZoomIn)
				.duration(500)
				.interpolate(new AccelerateDecelerateInterpolator())
				.withListener(animatorListener)
				.playOn(mBalanceAmountView);
			
//			loadListView(mCurrentDate, getString(R.string.wastage));
		}
	};
	
	AnimatorListener animatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator arg0) {
			mProgressDialog = ProgressDialog.show(TransactionActivity.this, "Please wait ...", "Loading...", true);
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
