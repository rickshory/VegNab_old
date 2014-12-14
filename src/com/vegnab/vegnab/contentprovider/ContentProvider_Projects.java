package com.vegnab.vegnab.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.vegnab.vegnab.database.VegNabDbHelper;
//import com.vegnab.vegnab.database.VegNabDbContract;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ContentProvider_Projects extends ContentProvider {
	
	// database
	private VegNabDbHelper database;
	
	// used for the UriMatcher
	private static final int PROJECTS = 10;
	private static final int PROJECT_ID = 20;
	
	private static final String AUTHORITY = "com.vegnab.provider";
	private static final String BASE_PATH = "projects";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	private static final String CONTENT_SUBTYPE = "vnd.vegnab.projects";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_SUBTYPE;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_SUBTYPE;
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, PROJECTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PROJECT_ID);
	}
	
	@Override
	public boolean onCreate() {
		database = new VegNabDbHelper(getContext());
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// use SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// check if the caller has requested a column that does not exist
		checkColumns(projection);
		// assign the table
		queryBuilder.setTables("Projects");
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PROJECTS:
			break;
		case PROJECT_ID:
			// add the ID to the original query
			queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);		
		}
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
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
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case PROJECTS:
			id = sqlDB.insert("Projects", null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case PROJECTS:
			rowsDeleted = sqlDB.delete("Projects", selection, selectionArgs);
			break;
		case PROJECT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete("Projects", "_id=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete("Projects", "_id=" + id, selectionArgs);
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
		switch (uriType) {
		case PROJECTS:
			rowsUpdated = sqlDB.update("Projects", values, selection, selectionArgs);
			break;
		case PROJECT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update("Projects", values, "_id=" + id, null);
			} else {
				rowsUpdated = sqlDB.update("Projects", values, "_id=" + id, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI:" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	private void checkColumns(String[] projection) {
		// figure out how to get these directly from the database
		String[] available = {
				"_id",
				"ProjCode",
				"Description",
				"Context",
				"Caveats",
				"ContactPerson",
				"StartDate",
				"EndDate"
		};
		// (

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// check if all columns requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

}
