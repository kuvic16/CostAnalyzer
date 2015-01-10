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
	
	@DatabaseField(canBeNull = false, foreign = true)
    private Category category;

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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
