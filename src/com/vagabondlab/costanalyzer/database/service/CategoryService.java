package com.vagabondlab.costanalyzer.database.service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.utilities.IConstant;

public class CategoryService{
	
	private Dao<Category, Integer> em;

	public CategoryService(Dao<Category, Integer> _em) {
		this.em = _em;
	}


	public int createCategory(Category category){
		try {
			return em.create(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void createCategorys(final List<Category> categories){
		try {
			em.callBatchTasks(new Callable<Void>() {
			    public Void call() throws Exception {
			        for (Category category : categories) {
			            em.create(category);
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
	
	public Category getCategoryById(int id){
		try {
			return em.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	public Category getBeneficiaryByBeneficiaryId(String beneficiaryId){
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			Where<Category, Integer> where = builder.where();
//			SelectArg selectArg = new SelectArg();
//			where.eq(Category.BENEFICIARY_ID, selectArg);
//			selectArg.setValue(beneficiaryId);
//			List<Category> beneficiaryList = em.query(builder.prepare());
//			if(beneficiaryList != null && beneficiaryList.size() > 0){
//				return beneficiaryList.get(0);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public List<Category> getNonRegisteredBeneficiarys(int beneficiaryType){
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			builder.orderBy(Category.CREATED_DATE, false);
//			
//			Where<Category, Integer> where = builder.where();
//			where.eq(Category.REGISTERED, false).and().eq(Category.BENEFICIARY_TYPE, beneficiaryType);
//			
//			List<Category> beneficiaryList = em.query(builder.prepare());
//			return beneficiaryList;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
	
	public List<Category> getAllCategory(){
		try {
			QueryBuilder<Category, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.CREATED_DATE, false);
			builder.limit(30L);
			List<Category> beneficiaryList = em.query(builder.prepare());
			return beneficiaryList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long countCategory(){
		try {
			QueryBuilder<Category, Integer> builder = em.queryBuilder();
			return builder.countOf();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
//	
//	public long countRegisteredBeneficiarys(){
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			Where<Category, Integer> where = builder.where();
//			where.eq(Category.REGISTERED, true).and().isNull(Category.DEATH_DATE);
//			return builder.countOf();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0;
//	}
//	
//	public long countNonRegisteredBeneficiarys(int beneficiaryType){
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			Where<Category, Integer> where = builder.where();
//			where.ne(Category.REGISTERED, true).and().eq(Category.BENEFICIARY_TYPE, beneficiaryType);
//			return builder.countOf();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0;
//	}
	
	public int updateCategory(Category category){
		try {
			return em.update(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void deleteCategoryById(int id){
		try {
			em.deleteById(id);			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
//	public void deleteRegisteredBeneficiary(){
//		try {
//			DeleteBuilder<Category, Integer> deleteBuilder = em.deleteBuilder();
//			SelectArg selectArg = new SelectArg();
//			selectArg.setValue(true);
//			deleteBuilder.where().eq(Category.REGISTERED, selectArg);
//			deleteBuilder.delete();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void deleteBeneficiaryByBeneficiaryId(String beneficiaryId){
//		try {
//			DeleteBuilder<Category, Integer> deleteBuilder = em.deleteBuilder();
//			SelectArg selectArg = new SelectArg();
//			selectArg.setValue(beneficiaryId);
//			deleteBuilder.where().eq(Category.BENEFICIARY_ID, selectArg);
//			deleteBuilder.delete();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public List<Category> searchBeneficiary(String BeneficiaryId, String name, String householdId, String householdNumber){
//		List<Category> beneficiaryList = new ArrayList<Category>();
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			builder.orderBy(Category.CREATED_DATE, false);
//			builder.limit(30L);
//			Where<Category, Integer> where = builder.where();
//			
//			boolean needAnd = false;
//			if(IUtil.isNotBlank(BeneficiaryId)){
//				where.like(Category.BENEFICIARY_ID, BeneficiaryId  + "%");
//				needAnd = true;
//			}
//			
//			if(IUtil.isNotBlank(name)){
//				if(needAnd){
//					where.and();
//				}
//				where.like(Category.MEMBER_NAME, name  + "%");
//				needAnd = true;
//			}
//			
//			if(IUtil.isNotBlank(householdId)){
//				if(needAnd){
//					where.and();
//				}
//				where.like(Category.HOUSEHOLD_ID, householdId  + "%");
//				needAnd = true;
//			}
//			
//			if(IUtil.isNotBlank(householdNumber)){
//				if(needAnd){
//					where.and();
//				}
//				where.like(Category.HOUSEHOLD_NUMBER, householdNumber  + "%");
//				needAnd = true;
//			}
//			
//			if(needAnd){
//				where.and();
//			}
//			where.eq(Category.REGISTERED, true).and().isNull(Category.DEATH_DATE);
//			
//			beneficiaryList = em.query(builder.prepare());
//			return beneficiaryList;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return beneficiaryList;
//	}
//	
//	public List<Category> searchBeneficiary(String searchKey){
//		try {
//			QueryBuilder<Category, Integer> builder = em.queryBuilder();
//			builder.orderBy(Category.CREATED_DATE, false);
//			builder.limit(30L);
//			Where<Category, Integer> where = builder.where();
//			where.like(Category.HOUSEHOLD_ID, searchKey  + "%").or().like(Category.MEMBER_NAME, searchKey  + "%");
//			where.or().like(Category.BENEFICIARY_ID, searchKey  + "%").or().like(Category.HOUSEHOLD_NUMBER,  searchKey  + "%").and().eq(Category.REGISTERED, true).and().isNull(Category.DEATH_DATE);
//			List<Category> beneficiaryList = em.query(builder.prepare());
//			return beneficiaryList;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
}
