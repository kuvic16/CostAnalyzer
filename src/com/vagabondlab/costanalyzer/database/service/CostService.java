package com.vagabondlab.costanalyzer.database.service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.utilities.IConstant;

public class CostService{
	
	private Dao<Cost, Integer> em;

	public CostService(Dao<Cost, Integer> _em) {
		this.em = _em;
	}


	public int createCost(Cost cost){
		try {
			return em.create(cost);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void createCosts(final List<Cost> costs){
		try {
			em.callBatchTasks(new Callable<Void>() {
			    public Void call() throws Exception {
			        for (Cost cost : costs) {
			            em.create(cost);
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
	
	public Cost getCostById(int id){
		try {
			return em.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Cost> getAllCost(){
		try {
			QueryBuilder<Cost, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.COST_DATE, false);
			builder.limit(30L);
			List<Cost> costList = em.query(builder.prepare());
			return costList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long countCost(){
		try {
			QueryBuilder<Cost, Integer> builder = em.queryBuilder();
			return builder.countOf();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int updateCost(Cost cost){
		try {
			return em.update(cost);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int deleteCostById(int id){
		try {
			return em.deleteById(id);			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
