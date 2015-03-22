package com.vagabondlab.costanalyzer.database.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.vagabondlab.costanalyzer.database.entity.Category;
import com.vagabondlab.costanalyzer.utilities.IConstant;
import com.vagabondlab.costanalyzer.utilities.IUtil;

public class CategoryService{
	
	private Dao<Category, Integer> em;

	public CategoryService(Dao<Category, Integer> _em) {
		this.em = _em;
	}


	public void refreash(Category category){
		try {
			em.refresh(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
	
	public Category getCategoryByName(String categoryName){
		try {
			QueryBuilder<Category, Integer> builder = em.queryBuilder();
			Where<Category, Integer> where = builder.where();
			SelectArg selectArg = new SelectArg();
			where.eq(IConstant.CATEGORY_NAME, selectArg);
			selectArg.setValue(categoryName);
			List<Category> categoryList = em.query(builder.prepare());
			if(categoryList != null && categoryList.size() > 0){
				return categoryList.get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Category> getAllCategory(){
		try {
			QueryBuilder<Category, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.CREATED_DATE, false);
			builder.limit(100L);
			List<Category> categoryList = em.query(builder.prepare());
			return categoryList;
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
	
	public int updateCategory(Category category){
		try {
			return em.update(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int deleteCategoryById(int id){
		try {
			return em.deleteById(id);			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	

	public List<Category> searchCategory(String categoryName, String categoryType){
		List<Category> categoryList = new ArrayList<Category>();
		try {
			QueryBuilder<Category, Integer> builder = em.queryBuilder();
			builder.orderBy(IConstant.CATEGORY_NAME, false);
			//builder.limit(30L);
			Where<Category, Integer> where = builder.where();
			
			boolean needAnd = false;
			if(IUtil.isNotBlank(categoryName)){
				where.like(IConstant.CATEGORY_NAME, categoryName  + "%");
				needAnd = true;
			}
			
			if(IUtil.isNotBlank(categoryType)){
				if(needAnd){
					where.and();
				}
				where.eq(IConstant.CATEGORY_TYPE, categoryType);
				needAnd = true;
			}
			
			if(needAnd == false){
				return categoryList;
			}
			
			categoryList = em.query(builder.prepare());
			return categoryList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryList;
	}
}
