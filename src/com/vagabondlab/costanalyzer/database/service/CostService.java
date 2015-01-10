package com.vagabondlab.costanalyzer.database.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vagabondlab.costanalyzer.database.entity.Cost;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;

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
	
	public List<Cost> getCosts(){
		GenericRawResults<String[]> rawResults;
		try {
			rawResults = em.queryRaw("select * from cost as co join category as ca on co.category_id=ca.id");
			List<String[]> results = rawResults.getResults();
			String[] resultArray = results.get(0);
			System.out.println("Account-id 10 has " + resultArray[0] + " orders");
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
	
	public List<Cost> searchCost(String costDate) {
		List<Cost> costList = new ArrayList<Cost>();
		try {
			QueryBuilder<Cost, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.CREATED_DATE, false);
			builder.limit(30L);
			Where<Cost, Integer> where = builder.where();

			boolean needAnd = false;
			if (IUtil.isNotBlank(costDate)) {
				where.like(IConstant.COST_DATE, costDate + "%");
				needAnd = true;
			}

			// if(IUtil.isNotBlank(name)){
			// if(needAnd){
			// where.and();
			// }
			// where.like(Category.MEMBER_NAME, name + "%");
			// needAnd = true;
			// }

//			if (needAnd) {
//				where.and();
//			}
			// where.eq(Category.REGISTERED,
			// true).and().isNull(Category.DEATH_DATE);

			costList = em.query(builder.prepare());
			return costList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return costList;
	}
}
