package com.vagabondlab.costanalyzer.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.vagabondlab.costanalyzer.R;
import com.vagabondlab.costanalyzer.database.entity.Category;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * @author shaiful islam palash
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "striders.db";
	private static final int DATABASE_VERSION = 6;

	private Dao<Category, Integer> beneficiaryDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	
	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Category.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Category.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "+ newVer, e);
		}
	}

	public Dao<Category, Integer> getCategoryDao() throws SQLException {
		if (beneficiaryDao == null) {
			beneficiaryDao = getDao(Category.class);
		}
		return beneficiaryDao;
	}
	
	public void onLocationUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Location.class, true);
			TableUtils.createTable(connectionSource, Location.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "+ newVer, e);
		}
	}
	
	public void onBeneficiaryUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Category.class, true);
			TableUtils.createTable(connectionSource, Category.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "+ newVer, e);
		}
	}
}
