package com.vagabondlab.costanalyzer.database.service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.database.sqlite.SQLiteConstraintException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.vagabondlab.costanalyzer.database.entity.Transaction;
import com.vagabondlab.costanalyzer.utilities.IConstant;

public class TransactionService{
	
	private Dao<Transaction, Integer> em;

	public TransactionService(Dao<Transaction, Integer> _em) {
		this.em = _em;
	}


	public void refreash(Transaction transaction){
		try {
			em.refresh(transaction);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int createTransaction(Transaction transaction){
		try {
			return em.create(transaction);
		} catch (SQLException e) { 
			if(e.getCause().getCause() instanceof SQLiteConstraintException){
				return -1;
			}
			e.printStackTrace();
		}
		return 0;
	}
	
	public void createTransactions(final List<Transaction> transactions){
		try {
			em.callBatchTasks(new Callable<Void>() {
			    public Void call() throws Exception {
			        for (Transaction transaction : transactions) {
			            em.create(transaction);
			        }
					return null;
			    }
			});
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Transaction getTransactionById(int id){
		try {
			return em.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Transaction getTransactionByName(String name){
		try {
			QueryBuilder<Transaction, Integer> builder = em.queryBuilder();
			Where<Transaction, Integer> where = builder.where();
			SelectArg selectArg = new SelectArg();
			where.eq(IConstant.TRANSACTION_NAME, selectArg);
			selectArg.setValue(name);
			List<Transaction> transactionList = em.query(builder.prepare());
			if(transactionList != null && transactionList.size() > 0){
				return transactionList.get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Transaction> getAllTransaction(){
		try {
			QueryBuilder<Transaction, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.LAST_MODIFIED_DATE, false);
			builder.limit(300L);
			List<Transaction> transactionList = em.query(builder.prepare());
			return transactionList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long countTransaction(){
		try {
			QueryBuilder<Transaction, Integer> builder = em.queryBuilder();
			return builder.countOf();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int updateTransaction(Transaction transaction){
		try {
			return em.update(transaction);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int deleteTransactionById(int id){
		try {
			return em.deleteById(id);			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public List<String[]> getSummaryTransaction(){
		GenericRawResults<String[]> rawResults;
		try {
			StringBuilder jql = new StringBuilder();
			jql.append("select sum(lend_amount), sum(borrow_amount) from mtransaction");
			rawResults = em.queryRaw(jql.toString());
			List<String[]> results = rawResults.getResults();
			return results;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
