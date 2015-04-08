package com.vagabondlab.costanalyzer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.vagabondlab.costanalyzer.database.entity.Transaction;
import com.vagabondlab.costanalyzer.database.service.TransactionService;
import com.vagabondlab.costanalyzer.utilities.DatePickerFragment;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;
import com.vagabondlab.costanalyzer.utilities.ViewUtil;

@SuppressLint("DefaultLocale")
public class TransactionDetailsActivity extends CActivity{
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private TransactionService transactionService;
	
	private TextView mNameLabel;
	private EditText mName;
	private RadioButton mTypeLend;
	private RadioButton mTypeBorrow;
	private EditText mAmount;
	private TextView mTransactionSelectedDate;
	private TextView mChangeTransactionDateButton;
	
	private EditText mSearchTransactionName;
	
	private TextView mTransactionStatus;
	private Button mButtonholderAddTransaction;
	private Button mButtonholderDeleteTransaction;
	
	private List<Map<String, String>> mTransactionListdata = new ArrayList<Map<String, String>>();
	private int selectedTransactionId;
	
	private int action = 0;
	private String mCurrentDate;
	private String mSelectedTransactionName;
	
	private TextView mSummaryStatusView;
	private Double lendAmount = 0.0;
	private Double borrowAmount = 0.0;
	private Double balanceAmount = 0.0;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_transaction_details);
		
		
		mSelectedTransactionName = getIntent().getExtras().getString(IConstant.PARAM_TRANSACTION_NAME);
		setTitle(mSelectedTransactionName);
		mTitle = getTitle();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_transaction);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer_transaction,(DrawerLayout) findViewById(R.id.drawer_layout_transaction));
		
		try {
			transactionService = new TransactionService(getHelper().getTransactionDao());
			mTransactionStatus = (TextView)findViewById(R.id.textView_transaction_status);
			
			mSummaryStatusView = (TextView)findViewById(R.id.textView_summary_status);
			mSummaryStatusView.setText(getString(R.string.transaction_details_summary_status, mSelectedTransactionName));
			
			mButtonholderAddTransaction = (Button)findViewById(R.id.buttonholder_add_transaction);
			mButtonholderAddTransaction.setOnClickListener(buttonHolderAddTransactionButtonClickListener);
			
			mButtonholderDeleteTransaction = (Button)findViewById(R.id.buttonholder_delete_transaction);
			mButtonholderDeleteTransaction.setOnClickListener(buttonHolderDeleteTransactionButtonClickListener);
			
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
		
		mNameLabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_name);
		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
		mTypeLend = (RadioButton)transactionFormView.findViewById(R.id.radio_lend);
		mTypeBorrow = (RadioButton)transactionFormView.findViewById(R.id.radio_borrow);
		mAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_amount);
		
		mName.setVisibility(View.GONE);
		mNameLabel.setText(getString(R.string.transaction_name_with, mSelectedTransactionName));
		
		mTransactionSelectedDate = (TextView)transactionFormView.findViewById(R.id.textView_selected_transaction_date);
		mTransactionSelectedDate.setText(mCurrentDate);
		mChangeTransactionDateButton = (TextView)transactionFormView.findViewById(R.id.textView_change_date);
		mChangeTransactionDateButton.setOnClickListener(changeDateButtonClickListener);
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.addnew)
		     .setTitle(R.string.add_transaction)
		     .setView(transactionFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	@SuppressLint("InflateParams")
	private void editTransactionDialougeBox(){
		LayoutInflater factory = LayoutInflater.from(this);
		final View transactionFormView = factory.inflate(R.layout.form_transaction, null);
		
		mNameLabel = (TextView)transactionFormView.findViewById(R.id.textView_transaction_name);
		mName = (EditText)transactionFormView.findViewById(R.id.editText_transaction_name);
		mTypeLend = (RadioButton)transactionFormView.findViewById(R.id.radio_lend);
		mTypeBorrow = (RadioButton)transactionFormView.findViewById(R.id.radio_borrow);
		mAmount = (EditText)transactionFormView.findViewById(R.id.editText_transaction_amount);
		
		mName.setVisibility(View.GONE);
		mNameLabel.setText(getString(R.string.transaction_name_with, mSelectedTransactionName));
		
		mTransactionSelectedDate = (TextView)transactionFormView.findViewById(R.id.textView_selected_transaction_date);
		mTransactionSelectedDate.setText(mCurrentDate);
		mChangeTransactionDateButton = (TextView)transactionFormView.findViewById(R.id.textView_change_date);
		mChangeTransactionDateButton.setOnClickListener(changeDateButtonClickListener);
		
		Transaction transaction = transactionService.getTransactionById(selectedTransactionId);
		if(transaction == null){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.not_found_transaction));
			return;
		}
		
		Double amount = 0.0;
		if(transaction.getLend_amount() > 0){
			mTypeLend.setChecked(true);
			amount = transaction.getLend_amount();
		}else if(transaction.getBorrow_amount() > 0){
			mTypeBorrow.setChecked(true);
			amount = transaction.getBorrow_amount();
		}
		mAmount.setText(String.valueOf(amount));
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.editnew)
		     .setTitle(R.string.edit_transaction)
		     .setView(transactionFormView)
		     .setPositiveButton(R.string.save, saveCancelListener)
		     .setNegativeButton(R.string.cancel, saveCancelListener);
		alert.show();
	}
	
	private void deleteTransactionDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(R.string.delete_transaction)
		     .setMessage(getString(R.string.delete_transaction_confirm))
		     .setPositiveButton(R.string.delete, deleteCancelListener)
		     .setNegativeButton(R.string.cancel, deleteCancelListener);
		alert.show();
	}
	
	private void deleteTransactionsByNameDialougeBox(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.clean)
		     .setTitle(getString(R.string.delete_transaction_by_name, mSelectedTransactionName))
		     .setMessage(getString(R.string.delete_all_transaction_confirm, mSelectedTransactionName))
		     .setPositiveButton(R.string.delete, deleteAllCancelListener)
		     .setNegativeButton(R.string.cancel, deleteAllCancelListener);
		alert.show();
	}
	
	private int saveTransaction(){
		try{
			if(IUtil.isNotBlank(mAmount.getText().toString())){
	    		String name = mSelectedTransactionName.toLowerCase();
	    		Double lendAmount = 0.0;
	    		Double borrowAmount = 0.0;
	    		Double amount = 0.0;
	    		String transactionDate = mTransactionSelectedDate.getText().toString();
	    		
	    		amount = Double.parseDouble(mAmount.getText().toString());
	    		
	    		if(mTypeLend.isChecked()){
	    			lendAmount = amount;
	    		}else if(mTypeBorrow.isChecked()){
	    			borrowAmount = amount;
	    		}
	    		
	    		Transaction transaction = new Transaction();
	    		if(action == IConstant.ACTION_ADD){
	    			transaction.setCreated_date(IUtil.getCurrentDateTime(IUtil.DATE_FORMAT));	    			
	    		}else if(action == IConstant.ACTION_EDIT){
	    			transaction = transactionService.getTransactionById(selectedTransactionId);	    			
	    		}
	    		
	    		transaction.setName(name);
	    		transaction.setLend_amount(lendAmount);
	    		transaction.setBorrow_amount(borrowAmount);
	    		transaction.setDate(transactionDate);
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
	    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.amount_missing));
	    	}
		}catch(Throwable t){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.please_try_again));
		}
		return 0;
	}
	
	private int deleteTransaction(){
		int sucess = 0;
		if(action == IConstant.ACTION_DELETE){
			sucess = transactionService.deleteTransactionById(selectedTransactionId);
		}
		
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_transaction_success_short));
			loadTransactionList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_transaction_failed_short));
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
		List<Transaction> transactionList = transactionService.getTransactionsByName(mSelectedTransactionName);
		loadUI(transactionList, transactionList.size());
	}

	private void loadUI(List<Transaction> transactionList, long total) {
		try {
			mTransactionStatus.setText(getString(R.string.transaction_status, total));
			mTransactionListdata = new ArrayList<Map<String,String>>();
			for (Transaction transaction : transactionList) {
				Map<String, String> infoMap = new HashMap<String, String>(3);
				infoMap.put("transaction_row_id", String.valueOf(transaction.getId()));
				String transactionAmount = "";
				if(transaction.getLend_amount()>0){
					transactionAmount = String.valueOf(transaction.getLend_amount());
					infoMap.put("transaction_name", getString(R.string.lend));					
				}else if(transaction.getBorrow_amount()>0){
					transactionAmount = "-" + String.valueOf(transaction.getBorrow_amount());
					infoMap.put("transaction_name", getString(R.string.borrow));
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
			List<String[]> summaryTransaction = transactionService.getSummaryTransactionByName(mSelectedTransactionName);
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
			TableLayout table = (TableLayout)findViewById(R.id.tdTable);
			table.removeAllViews();
			table.addView(ViewUtil.getTransactionDetailsStatusTableHeader(this));
			
			TableRow tr = new TableRow(this);
			tr.setPadding(5, 0, 0, 0);
			tr.addView(ViewUtil.getTableColumn(this, String.valueOf(lendAmount.intValue()), Gravity.CENTER));
			tr.addView(ViewUtil.getTableColumn(this, String.valueOf(borrowAmount.intValue()), Gravity.CENTER));
			tr.addView(ViewUtil.getTableColumn(this, String.valueOf(balanceAmount.intValue()), Gravity.CENTER));
			table.addView(tr);
			table.addView(ViewUtil.getDividerView(getApplicationContext()));
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	private void searchTransaction(){
		try{
    		String transactionName = mSearchTransactionName.getText().toString().toLowerCase();
    		
    		List<Transaction> transactionList = transactionService.searchTransaction(transactionName);
    		loadUI(transactionList, transactionList.size());
    	}catch(Throwable t){
    		ViewUtil.showMessage(getApplicationContext(), getString(R.string.error, t.getMessage()));
    	}
	}
	
	private int deleteAllTransactionByName(){
		int sucess = transactionService.deleteTransactionsByName(mSelectedTransactionName);
		if(sucess > 0){
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_all_transaction_success_short, mSelectedTransactionName));
			loadTransactionList();
			return 1;
		}else{
			ViewUtil.showMessage(getApplicationContext(), getString(R.string.delete_all_transaction_failed_short, mSelectedTransactionName));
		}
    	return 0;
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
	
	DialogInterface.OnClickListener deleteAllCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				if(deleteAllTransactionByName()==1){
					finish();					
				}
				break;
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
	
	OnClickListener buttonHolderDeleteTransactionButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				action = IConstant.ACTION_DELETE;
				deleteTransactionsByNameDialougeBox();
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
	
	DialogInterface.OnClickListener searchTransactionListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			switch (i) {
			case DialogInterface.BUTTON_POSITIVE:
				searchTransaction();
				break;
			case DialogInterface.BUTTON_NEGATIVE: 
				break;
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
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.category, menu);
//		restoreActionBar();
//		return true;
//	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IConstant.CONTEXT_MENU_EDIT: {
			action = IConstant.ACTION_EDIT;
			editTransactionDialougeBox();
		}
			break;
		case IConstant.CONTEXT_MENU_ARCHIVE: {
			action = IConstant.ACTION_DELETE;
			deleteTransactionDialougeBox();
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
