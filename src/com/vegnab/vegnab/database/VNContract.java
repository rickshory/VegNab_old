/**
 * 
 */
package com.vegnab.vegnab.database;

import android.provider.BaseColumns;

/**
 * @author rshory
 *
 */
public final class VNContract {
	// empty constructor to prevent accidental instantiation
	public VNContract() {}
	// inner class to define preferences
	public static abstract class Prefs {
		public static final String DEFAULT_PROJECT_ID = "Default_Project_Id";
		public static final String DEFAULT_PLOTTYPE_ID = "Default_PlotType_Id";
	}
	
	// inner classes to define tables
	public static abstract class Project implements BaseColumns {
		public static final String TABLE_NAME = "Projects";
		public static final String COLUMN_NAME_PROJCODE = "ProjCode";
		public static final int COLUMN_SIZE_PROJCODE = 10;
		public static final String COLUMN_NAME_DESCRIPTION = "DESCRIPTION";
		public static final String COLUMN_NAME_CONTEXT = "Context";
		public static final String COLUMN_NAME_CAVEATS = "Caveats";
		public static final String COLUMN_NAME_CONTACTPERSON = "ContactPerson";
		public static final String COLUMN_NAME_STARTDATE = "StartDate";
		public static final String COLUMN_NAME_ENDDATE = "EndDate";
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,"
				+ COLUMN_NAME_PROJCODE + " VARCHAR(" + COLUMN_SIZE_PROJCODE + ") NOT NULL UNIQUE,"
				+ COLUMN_NAME_DESCRIPTION + " TEXT,"
				+ COLUMN_NAME_CONTEXT + " TEXT,"
				+ COLUMN_NAME_CAVEATS + " TEXT,"
				+ COLUMN_NAME_CONTACTPERSON + " TEXT,"
				+ COLUMN_NAME_STARTDATE + " DATE DEFAULT (DATETIME('now')),"
				+ COLUMN_NAME_ENDDATE + " DATE"
				+ ");";
		public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
		public static final String SQL_ASSURE_CONTENTS = "";
		

		
	}
}
