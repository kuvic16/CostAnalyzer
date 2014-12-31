package com.vagabondlab.costanalyzer.database.entity;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Category entity 
 * @author shaiful islam palash
 */
@DatabaseTable(tableName = "category")
public class Category extends AbstractEntity implements Serializable{
	
	private static final long serialVersionUID = -2665373337324604077L;
	
	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(index = true, unique=true)
	private String name;
	
	@DatabaseField(index = true)
	private String type;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
