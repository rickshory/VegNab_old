package com.vegnab.vegnab.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	private static final String LOG_TAG = DataBaseHelper.class.getSimpleName();
 
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/vegnab/databases/";
 
    private static String DB_NAME = "VegNab.db";
 
    private SQLiteDatabase myDataBase; 
 
    private final Context myContext;
 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to 
     * access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {
 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	
 
  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
    	boolean dbExist = checkDataBase();
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
    		//By calling this method an empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
        	try {
    			copyDataBase();
    		} catch (IOException e) {
        		throw new Error("Error copying database");
        	}
    	}
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
    	SQLiteDatabase checkDB = null;
    	try {
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	} catch (SQLiteException e){
    		//database does't exist yet.
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     * */
    private void copyDataBase() throws IOException{
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
 
    public void openDataBase() throws SQLException {
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
 
    @Override
	public synchronized void close() {
    	if(myDataBase != null) {
    		myDataBase.close();
    	}
    	super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
 
        // Add your public helper methods to access and get content from the database.
       // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
       // for you to create adapters for your views.
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public String fillSpeciesTable(ParcelFileDescriptor fileDescr) {
		// expects the file to be text
		// first line is geographical area of the species list, such as a state like "Oregon"
		// appropriate for forming a message like, "species list for Oregon"
		// this first line is what is returned
		// after the first line,
		// each row has short 'code' and long 'description' separated by Tab character
		//
		StringBuilder lines = new StringBuilder();
		String listName = "", line, code, descr;
		String[] lineParts;
		long ct = 0;
		// clear existing codes from the table
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM RegionalSpeciesList;");
        String sSql = "INSERT OR REPLACE INTO RegionalSpeciesList ( Code, SppDescr ) VALUES ( ?, ? )";
//        db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sSql);

		try {
			InputStream is = new FileInputStream(fileDescr.getFileDescriptor()); // use getFileDescriptor to get InputStream
			// wrap InputStream with an InputStreamReader, which is wrapped by a BufferedReader, "trick" to use readLine() fn
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			while ((line = br.readLine()) != null) {
				if (ct == 0) { // the internal description of the list
					listName = line.toString();
				} else { // a species code and description separated by a tab
					// readLine gets the lines one at a time, strips the delimiters
					lineParts = line.split("\t");
					code = lineParts[0].replace('\'', '`');
					descr = lineParts[1].replace('\'', '`');
					Log.v(LOG_TAG, "Code: '" + code + "', Description: '" + descr + "'");
					lines.append("('").append(code).append("', '").append(descr).append("'), \n\r");
					stmt.bindString(1, lineParts[0]);
					stmt.bindString(2, lineParts[1]);
					stmt.execute();
					stmt.clearBindings();
				}
				ct++;
			}
			br.close();
			db.setTransactionSuccessful();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
		} catch (IOException e) {
			Log.v(LOG_TAG, "IOException " + e.getMessage());
		}
		
		db.endTransaction();
		db.close();
		return listName;
	}
}