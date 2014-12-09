/**
 * 
 */
package com.vegnab.vegnab.database;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author rshory
 *
 */
public class VegNabDbHelper extends SQLiteAssetHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "VegNab.db";
	
	public VegNabDbHelper(Context context) {
/*	public VegNabDbHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
*/
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
/*super(context, name, factory, version, errorHandler);*/
		
	}

//	@Override
//	public void onCreate(SQLiteDatabase db) {
//		db.execSQL(VNContract.Project.SQL_CREATE_TABLE);
//	}

//	@Override
//	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		// during development & testing, just delete old and re-create
//		db.execSQL(VNContract.Project.SQL_DROP_TABLE);
//		onCreate(db);
//	}

//    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//    	// during development & testing, just delete old and re-create
//        onUpgrade(db, oldVersion, newVersion);
//    }

}
