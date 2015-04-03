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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vagabondlab.costanalyzer.database.entity.Transaction;
import com.vagabondlab.costanalyzer.database.service.TransactionService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class TransactionActivity extends CActivity{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private TransactionService transactionService;
	
	private EditText mName;
	private RadioButton mTypeLend;
	private RadioButton mTypeBorrow;
	private EditText mAmount;
	private TextView mTransactionSelectedDate;
	private TextView mChangeTransactionDateButton;
	
//	private EditText mLendAmount;
//	private EditText mBorrowAmount;
	
	private TextView mTransactionStatus;
	private Button mButtonholderAddTransaction;
	private Button mButtonholderSearch;
	
	private List<Map<String, String>> mTransactionListdata = new ArrayList<Map<String, String>>();
	private int selectedTransactionId;
	private String selectedTransactionName;
	
	private int action = 0;
	private String mCurrentDate;
	
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

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_transaction);
		setTitle(getString(R.string.title_activity_transaction));
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_transaction);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_transaction,(DrawerLayout) findViewById(R.id.drawer_layout_transaction));
		
		try {
			transactionService = new TransactionService(getHelper().getTransactionDao());
			mTransactionStatus = (TextView)findViewById(R.id.textView_transaction_status);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			mSummaryStatusView.setText(getString(R.string.transaction_summary_status));
			
			mBorrowAmountView = (TextView)findViewById(R.id.textView_summary_total_cost);
			mBorrowAmountViewLabel = (TextView)findViewById(R.id.textView_summary_total_cost_status);
			mBorrowAmountViewLabel.setText(getString(R.string.borrow));
			
			mLendAmountView = (TextView)findViewById(R.id.textView_summary_effective_cost);
			mLendAmountViewLabel = (TextView)findViewById(R.id.textView_summary_effective_cost_status);
			mLendAmountViewLabel.setText(getString(R.string.lend));
			
			mBalanceAmountView = (TextView)findViewById(R.id.textView_summary_wastage_cost);
			mBalanceAmountViewLabel = (TextView)findViewById(R.id.textView_summary_wastage_cost_status);
			mBalanceAmountViewLabel.setText(getString(R.string.balance));
			
			mButtonholderAddTransaction = (Button)findViewById(R.id.buttonholder_add_transaction);
			mButtonholderAddTransaction.setOnClickListener(buttonHolderAddTransactionButtonClickListener);
			mButtonholderSearch = (Button)findViewById(R.id.buttonholder_search);
			mButtonholderSearch.setOnClickListener(buttonHolderSearchButtonClickListener);
			
			// temporary
			//getHelper().onTransactionUpgrade(getHelper().getWritableDatabase(),getHelper().getConnectionSource(), 1, 2);
			
			mCurrentDate = IUtil.getCurrentDateTime(IUtil.DATE_FORMAT_YYYY_MM_DD);
			loadTransactionList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("InflateParams")
	private void addNewTransactionDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View transactionFormView = factory.inflate(R.layout.form_transaction, null);
		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
		mTypeLend = (RadioButton)transactionFormView.findViewById(R.id.radio_lend);
		mTypeBorrow = (RadioButton)transactionFormView.findViewById(R.id.radio_borrow);
		mAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_amount);
		
		mTransactionSelectedDate = (TextView)transactionFormView.findViewById(R.id.textView_selected_transaction_date);
		mTransactionSelectedDate.setText(mCurrentDate);
		mChangeTransactionDateButton = (TextView)transactionFormView.findViewById(R.id.textView_change_date);
		mChangeTransactionDateButton.setOnClickListener(changeDateButtonClickListener);
		
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
//		LayoutInflater factory = LayoutInflater.from(this);
//		final View transactionFormView = factory.inflate(R.layout.form_transaction, null);
//		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
//		mLendAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_lend_amount);
//		mBorrowAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_borrow_amount);
//		
//		TextView mTLALabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_lend_amount);
//		TextView mTBALabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_borrow_amount);
//		
//		Transaction transaction = transactionService.getTransactionById(selectedTransactionId);
//		if(transaction == null){
//			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_transaction));
//			return;
//		}
//		
//		mName.setText(transaction.getName());
//		if(transaction.getLend_amount() > 0){
//			mTBALabel.setText(getString(R.string.take_amount));
//			mLendAmount.setText(String.valueOf(transaction.getLend_amount()));
//		}
//		if(transaction.getBorrow_amount() > 0){
//			mTLALabel.setText(getString(R.string.pay_amount));
//			mBorrowAmount.setText(String.valueOf(transaction.getBorrow_amount()));
//		}
//		
//		
//		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
//		alert.setIcon(R.drawable.editnew)
//		     .setTitle(R.string.edit_transaction)
//		     .setView(transactionFormView)
//		     .setPositiveButton(R.string.save, saveCancelListener)
//		     .setNegativeButton(R.string.cancel, saveCancelListener);
//		alert.show();
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
	
//	private int saveTransaction(){
//		if(IUtil.isNotBlank(mName.getText())){
//    		String name = mName.getText().toString().toLowerCase();
//    		String lendAmount = mLendAmount.getText().toString();
//    		String borrowAmount = mBorrowAmount.getText().toString();
//    		
//    		Transaction transaction = new Transaction();
//    		if(action == IConstant.ACTION_EDIT){
//    			transaction = transactionService.getTransactionById(selectedTransactionId);
//    		}
//    		transaction.setName(name);
//    		if(IUtil.isNotBlank(lendAmount)){
//    			transaction.setLend_amount(Double.parseDouble(lendAmount));
//    		}else{
//    			transaction.setLend_amount(0.0);
//    		}
//    		
//    		if(IUtil.isNotBlank(borrowAmount)){
//    			transaction.setBorrow_amount(Double.parseDouble(borrowAmount));
//    		}else{
//    			transaction.setBorrow_amount(0.0);
//    		}
//    		
//    		if(transaction.getLend_amount() > 0 && transaction.getBorrow_amount()>0){
//    			Double diff = transaction.getLend_amount() -  transaction.getBorrow_amount();
//    			if(diff==0){
//    				transaction.setLend_amount(0.0);
//    				transaction.setBorrow_amount(0.0);
//    			}else if(diff>0){
//    				transaction.setLend_amount(diff);
//    				transaction.setBorrow_amount(0.0);
//    			}else{
//    				transaction.setLend_amount(0.0);
//    				transaction.setBorrow_amount(diff*(-1));
//    			}
//    		}
//    		
//    		transaction.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
//    		transaction.setLast_modified_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
//    		
//    		int sucess = 0;
//    		if(action == IConstant.ACTION_ADD){
//    			sucess = transactionService.createTransaction(transaction);
//    		}else if(action == IConstant.ACTION_EDIT){
//    			sucess = transactionService.updateTransaction(transaction);
//    		}
//    		
//    		if(sucess > 0){
//    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_success, name));
//    			loadTransactionList();
//    			return 1;
//    		}else{
//    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_failed));
//    		}
//    	}else{
//    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.transaction_name_missing));
//    	}
//		return 0;
//	}
	
	private int saveTransaction(){
		if(IUtil.isNotBlank(mName.getText())){
    		String name = mName.getText().toString().toLowerCase();
    		Double lendAmount = 0.0;
    		Double borrowAmount = 0.0;
    		Double amount = 0.0;
    		String transactionDate = mTransactionSelectedDate.getText().toString();
    		
    		if(IUtil.isNotBlank(mAmount.getText().toString())){
    			amount = Double.parseDouble(mAmount.getText().toString());
    		}
    		
    		if(mTypeLend.isChecked()){
    			lendAmount = amount;
    		}else if(mTypeBorrow.isChecked()){
    			borrowAmount = amount;
    		}
    		
    		Transaction transaction = transactionService.getTransactionByName(name);
    		if(transaction != null){
    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_failed_duplicate));
    		}else{
	    		transaction = new Transaction();
	    		transaction.setName(name);
	    		transaction.setLend_amount(lendAmount);
	    		transaction.setBorrow_amount(borrowAmount);
	    		transaction.setDate(transactionDate);
	    		transaction.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));
	    		
	    		int sucess = transactionService.createTransaction(transaction);
	    		if(sucess > 0){
	    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_success, name));
	    			loadTransactionList();
	    			return 1;
	    		}else{
	    			ViewUtil.showMessage(getApplicationContext(), getString(R.string.save_transaction_failed));
	    		}
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

	// 1. Listener
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
	
	OnClickListener buttonHolderAddTransactionButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_ADD;
				addNewTransactionDialougeBox();
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener buttonHolderSearchButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_SEARCH;
//				DialogFragment newFragment = new DatePickerFragment();
//			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	};
	
	OnClickListener changeDateButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}catch(Throwable t){
				t.printStackTrace();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.category, menu);
		restoreActionBar();
		return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IConstant.CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editCategoryDialougeBox();
		}
			break;
		case IConstant.CONTEXT_MENU_ARCHIVE: {
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
		if (id == R.id.search) {
			action = IConstant.ACTION_SEARCH;
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void nextView() {
	}

	@Override
	public void prevView() {
	}
	
	@Override
	public void returnDate(String date) {
		if(action == IConstant.ACTION_ADD || action == IConstant.ACTION_EDIT){
			mTransactionSelectedDate.setText(date);
		}
	}
}
