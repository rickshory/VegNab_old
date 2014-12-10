/**
 * 
 */
package com.vegnab.vegnab.database;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * @author rshory
 *
 */
public class VegNabDbHelper extends SQLiteAssetHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "VegNab.db";
	
	public VegNabDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
/*super(context, name, factory, version, errorHandler);*/
		
	}
	
	public Cursor getProjects() {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String[] projectionIn = {"_id", "ProjCode"};
		String inTables = "Projects";
		qb.setTables(inTables);
		Cursor c = qb.query(db, projectionIn, null, null, null, null, null);
		c.moveToFirst();
		return c;
	}



}
