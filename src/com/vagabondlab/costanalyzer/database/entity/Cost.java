package com.vagabondlab.costanalyzer.database.entity;

import java.io.Serializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Cost entity 
 * @author shaiful islam palash
 */
@DatabaseTable(tableName = "cost")
public class Cost extends AbstractEntity implements Serializable{
	
	private static final long serialVersionUID = -2665373337324604077L;
	
	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(index = true)
	private int category_id;
	
	@DatabaseField(dataType =  DataType.DOUBLE)
	private double amount;
	
	@DatabaseField(index = true)
	private String date ;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}	
}
