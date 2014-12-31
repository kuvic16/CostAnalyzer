package com.vagabondlab.costanalyzer.database.entity;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class AbstractEntity  implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@DatabaseField
	protected String created_date;

	@DatabaseField
    protected String created_by_name;

	@DatabaseField
    protected String last_modified_date;

	@DatabaseField
    protected String last_modified_by_name;
	
	public String getCreated_date() {
		return created_date;
	}

	public void setCreated_date(String created_date) {
		this.created_date = created_date;
	}

	public String getCreated_by_name() {
		return created_by_name;
	}

	public void setCreated_by_name(String created_by_name) {
		this.created_by_name = created_by_name;
	}

	public String getLast_modified_date() {
		return last_modified_date;
	}

	public void setLast_modified_date(String last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

	public String getLast_modified_by_name() {
		return last_modified_by_name;
	}

	public void setLast_modified_by_name(String last_modified_by_name) {
		this.last_modified_by_name = last_modified_by_name;
	}	
}
