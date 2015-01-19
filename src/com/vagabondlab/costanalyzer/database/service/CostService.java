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
	
	public List<String[]> getTotalCostGroupByType(String costDate){
		GenericRawResults<String[]> rawResults;
		try {
			StringBuilder jql = new StringBuilder();
			jql.append("select ca.type, sum(co.amount) from cost as co join category ca on co.category_id=ca.id ");
			
			if(IUtil.isNotBlank(costDate)){
				jql.append(" where ");
				jql.append(" co.date = '").append(costDate).append("'");
			}
			jql.append(" group by ca.type");
			
			rawResults = em.queryRaw(jql.toString());
			List<String[]> results = rawResults.getResults();
			return results;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String[]> getTotalCostGroupByCategory(String costDate){
		GenericRawResults<String[]> rawResults;
		try {
			StringBuilder jql = new StringBuilder();
			jql.append("select ca.name, count(*), sum(co.amount) as total,ca.type from cost as co join category ca on co.category_id=ca.id ");
			
			if(IUtil.isNotBlank(costDate)){
				jql.append(" where ");
				jql.append(" co.date = '").append(costDate).append("'");
			}
			jql.append(" group by co.category_id order by total desc");
			
			rawResults = em.queryRaw(jql.toString());
			List<String[]> results = rawResults.getResults();
			return results;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Deprecated
	public double getTotalCost(String categoryType, String costDate){
		double totalCost = 0.0;
		GenericRawResults<String[]> rawResults;
		try {
			StringBuilder jql = new StringBuilder();
			jql.append("select sum(co.amount) from cost as co join category ca on co.category_id=ca.id ");
			
			boolean andRequired = false;
			if(IUtil.isNotBlank(categoryType)){
				jql.append(" where ");
				jql.append(" ca.type = '").append(categoryType).append("'");
				andRequired = true;
			}
			
			if(IUtil.isNotBlank(costDate)){
				if(andRequired){
					jql.append(" and ");
				}else{
					jql.append(" where ");
				}
				jql.append(" co.date = '").append(costDate).append("'");
			}
			
			rawResults = em.queryRaw(jql.toString());
			List<String[]> results = rawResults.getResults();
			String[] resultArray = results.get(0);
			if(IUtil.isNotBlank(resultArray[0])){
				totalCost = Double.parseDouble(resultArray[0]);
			}
			System.out.println("Total cost: " + totalCost);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return totalCost;
	}
	
	public List<Cost> searchCost(String costDate) {
		List<Cost> costList = new ArrayList<Cost>();
		try {
			QueryBuilder<Cost, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.CREATED_DATE, false);
			builder.limit(30L);
			Where<Cost, Integer> where = builder.where();

			//boolean needAnd = false;
			if (IUtil.isNotBlank(costDate)) {
				where.like(IConstant.COST_DATE, costDate + "%");
				//needAnd = true;
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
