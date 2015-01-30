package com.vagabondlab.costanalyzer.database.entity;

import java.io.Serializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Transaction entity 
 * @author shaiful islam palash
 */
@DatabaseTable(tableName = "mtransaction")
public class Transaction extends AbstractEntity implements Serializable{
	
	private static final long serialVersionUID = -2665373337324604077L;
	
	@DatabaseField(generatedId = true)
	int id;
	
	@DatabaseField(canBeNull = false, unique=true)
    private String name;

	@DatabaseField(dataType =  DataType.DOUBLE)
	private double lend_amount;
	
	@DatabaseField(dataType =  DataType.DOUBLE)
	private double borrow_amount;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLend_amount() {
		return lend_amount;
	}

	public void setLend_amount(double lend_amount) {
		this.lend_amount = lend_amount;
	}

	public double getBorrow_amount() {
		return borrow_amount;
	}

	public void setBorrow_amount(double borrow_amount) {
		this.borrow_amount = borrow_amount;
	}	
}
