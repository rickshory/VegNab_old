package com.vegnab.vegnab.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.vegnab.vegnab.database.VegNabDbHelper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ContentProvider_VegNab extends ContentProvider {
	private static final String LOG_TAG = ContentProvider_VegNab.class.getSimpleName();
	
	// database
	private VegNabDbHelper database; // = null; // to initialize ?
	
	// used for the UriMatcher
	private static final int RAW_SQL = 1;
	private static final int PROJECTS = 10;
	private static final int PROJECT_ID = 20;
	private static final int VISITS = 30;
	private static final int VISIT_ID = 40;
	
	private static final String AUTHORITY = "com.vegnab.provider"; // must match in app Manifest
	private static final String BASE_PATH = "data";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public static final Uri SQL_URI = Uri.parse("content://" + AUTHORITY
			+ "/sql");
	private static final String CONTENT_SUBTYPE = "vnd.vegnab.data";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_SUBTYPE;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_SUBTYPE;
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, "sql", RAW_SQL);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/projects", PROJECTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/projects/#", PROJECT_ID);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/visits", VISITS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/visits/#", VISIT_ID);
	}
	HashSet<String> mFields_Projects = new HashSet<String>();
	
	@Override
	public boolean onCreate() {
		database = new VegNabDbHelper(getContext());
		// get the list of fields from the table 'Projects' and populate a HashSet
		String s = "pragma table_info(Projects);";
		Cursor c = database.getReadableDatabase().rawQuery(s, null);
		while (c.moveToNext()) {
//			Log.v(LOG_TAG, "Project field added to HashMap: " + c.getString(c.getColumnIndexOrThrow("name")));
			mFields_Projects.add(c.getString(c.getColumnIndexOrThrow("name")));
		}
		// could extend this to other tables, but is there any point?
		// used below to check if a query is requesting non-existent fields but if so it
		// causes an unrecoverable error.
		// we would find these bugs during development; after that the queries would always
		// work unless the DB structure changed, which would require a rewrite anyway
		
		// can get list of tables by this query:
		// SELECT tbl_name FROM sqlite_master WHERE (type='table') AND (sql LIKE '%_id%') AND (tbl_name <> 'android_metadata');
		
		c.close();

		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		Cursor cursor = null;
		int uriType = sURIMatcher.match(uri);
		if (uriType == RAW_SQL) { // use rawQuery
			// the full SQL statement is in 'selection' and any needed parameters in 'selectionArgs'
			cursor = database.getReadableDatabase().rawQuery(selection, selectionArgs);
		} else {
			
			switch (uriType) {
			case PROJECTS:
				// fix up the following fn to work with all tables
				// check if the caller has requested a field that does not exist
				checkFields("Projects", projection);
				// assign the table
				queryBuilder.setTables("Projects");
				break;
			case PROJECT_ID:
				checkFields("Projects", projection);
				queryBuilder.setTables("Projects");
				// add the ID to the original query
				queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
				break;

			case VISIT_ID:
				queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
				// note, no break, so drops through
			case VISITS:
				queryBuilder.setTables("Visits");
				break;
			
			
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);		
			}
			SQLiteDatabase db = database.getReadableDatabase();
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		}
		// assure potential listeners are notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		Uri uriToReturn;
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case PROJECTS:
			id = sqlDB.insert("Projects", null, values);
			uriToReturn = Uri.parse(BASE_PATH + "/projects/" + id);
			break;
		case VISITS:
			id = sqlDB.insert("Visits", null, values);
			uriToReturn = Uri.parse(BASE_PATH + "/visits/" + id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return uriToReturn;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		String id;
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case PROJECTS:
			rowsDeleted = sqlDB.delete("Projects", selection, selectionArgs);
			break;
		case PROJECT_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete("Projects", "_id=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete("Projects", "_id=" + id, selectionArgs);
			}
			break;

		case VISITS:
			rowsDeleted = sqlDB.delete("Visits", selection, selectionArgs);
			break;
		case VISIT_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete("Visits", "_id=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete("Visits", "_id=" + id, selectionArgs);
			}
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		String id;
		switch (uriType) {
		case RAW_SQL:
			// SQL to run is in 'selection', any parameters in 'selectionArgs'
			sqlDB.execSQL(selection); // run SQL that creates no cursor and returns no results
			// then use SQLite internal 'Changes' fn to retrieve number of rows changed
			Cursor cur = sqlDB.rawQuery("SELECT Changes() AS C;", null);
			cur.moveToFirst();
			rowsUpdated = cur.getInt(0);
			break;
		case PROJECTS:
			rowsUpdated = sqlDB.update("Projects", values, selection, selectionArgs);
			break;
		case PROJECT_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update("Projects", values, "_id=" + id, null);
			} else {
				rowsUpdated = sqlDB.update("Projects", values, "_id=" + id, selectionArgs);
			}
			break;

		case VISITS:
			rowsUpdated = sqlDB.update("Visits", values, selection, selectionArgs);
			break;
		case VISIT_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update("Visits", values, "_id=" + id, null);
			} else {
				rowsUpdated = sqlDB.update("Visits", values, "_id=" + id, selectionArgs);
			}
			break;
		
		
		default:
			throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	private void checkFields(String tableName, String[] projection) {
		if (projection != null) {
			HashSet<String> requestedFields = new HashSet<String>(Arrays.asList(projection));
			// check if all columns requested are available
			switch (tableName) {
			case "Projects":
				// mFields_Projects is populated in onCreate
				if (!mFields_Projects.containsAll(requestedFields)) {
					throw new IllegalArgumentException("Unknown fields in projection");
				}
				break;
			default:
				break; // for now, let all other cases go by
			}
		}
	}

}
