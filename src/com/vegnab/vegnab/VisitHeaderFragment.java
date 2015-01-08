package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class VisitHeaderFragment extends Fragment implements OnClickListener,
		android.widget.AdapterView.OnItemSelectedListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String LOG_TAG = "VisitHeaderFragment";
	public static final int LOADER_FOR_VISIT = 5; // Loader Ids
	public static final int LOADER_FOR_NAMERS = 6;
	long visitId = 0, namerId = 0; // zero default means new or not specified yet
	Uri uri, baseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "visits");
	ContentValues values = new ContentValues();
	private EditText mVisitName, mVisitDate, mVisitScribe, mVisitLocation, mVisitNotes;
	private Spinner namerSpinner;
	SimpleCursorAdapter mVisitAdapter, mNamerAdapter;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private Calendar myCalendar = Calendar.getInstance();
	private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
	    @Override
	    public void onDateSet(DatePicker view, int year, int monthOfYear,
	            int dayOfMonth) {
	        myCalendar.set(Calendar.YEAR, year);
	        myCalendar.set(Calendar.MONTH, monthOfYear);
	        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	        mVisitDate.setText(dateFormat.format(myCalendar.getTime()));
	    }
	};
	int rowCt;
	final static String ARG_SUBPLOT = "subplot";
	int mCurrentSubplot = -1;
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onVisitHeaderGoButtonClicked();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous screen, remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			mCurrentSubplot = savedInstanceState.getInt(ARG_SUBPLOT);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_visit_header, container, false);
		// set click listener for the button in the view
		Button b = (Button) rootView.findViewById(R.id.visit_header_go_button);
		b.setOnClickListener(this);
		// if more, loop through all the child items of the ViewGroup rootView and 
		// set the onclicklistener for all the Button instances found

		namerSpinner = (Spinner) rootView.findViewById(R.id.sel_spp_namer_spinner);
		namerSpinner.setEnabled(false); // will enable when data ready		
		mNamerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null,
				new String[] {"NamerName"},
				new int[] {android.R.id.text1}, 0);		

		mNamerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		namerSpinner.setAdapter(mNamerAdapter);
		namerSpinner.setOnItemSelectedListener(this);
		// Prepare the loader. Either re-connect with an existing one or start a new one
		getLoaderManager().initLoader(LOADER_FOR_NAMERS, null, this);

		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		if (args != null) {
			// set up subplot based on arguments passed in
			updateSubplotViews(args.getInt(ARG_SUBPLOT));
		} else if (mCurrentSubplot != -1) {
			// set up subplot based on saved instance state defined in onCreateView
			updateSubplotViews(mCurrentSubplot);
		} else {
			updateSubplotViews(-1); // figure out what to do for default state 
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// assure the container activity has implemented the callback interface
		try {
			mButtonCallback = (OnButtonListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException (activity.toString() + " must implement OnButtonListener");
		}
	}
	
	public void updateSubplotViews(int subplotNum) {
		// don't do anything yet
		// figure out how to deal with default of -1
		mCurrentSubplot = subplotNum;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current subplot arguments in case we need to re-create the fragment
		outState.putInt(ARG_SUBPLOT, mCurrentSubplot);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.visit_header_go_button:
			mButtonCallback.onVisitHeaderGoButtonClicked();
			break;
		}
	}
	
	public void saveDefaultNamerId(long id) {
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putLong(Prefs.DEFAULT_NAMER_ID, id);
		prefEditor.commit();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {

/*		case LOADER_FOR_VISIT:
			// First, create the base URI
			// could test here, based on e.g. filters
			baseUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), Uri.parse(baseUri + "/projects"),
					PROJECTS_PROJCODES, select, null, null);
			break; */
		case LOADER_FOR_NAMERS:
			baseUri = ContentProvider_VegNab.SQL_URI;
			select = "SELECT _id, NamerName FROM Namers "
					+ "UNION SELECT 0, '(add new)' "
					+ "ORDER BY _id;";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor finishedCursor) {
		// there will be various loaders, switch them out here
		rowCt = finishedCursor.getCount();
		switch (loader.getId()) {
		/*
		case LOADER_FOR_VISIT:
			// Swap the new cursor in.
			// The framework will take care of closing the old cursor once we return.
			mVisitAdapter.swapCursor(finishedCursor);
			if (rowCt > 0) {
				projSpinner.setEnabled(true);
				// get default Project from app Preferences, to set spinner
				// this must wait till the spinner is populated
				SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
				// database comes pre-loaded with one Project record that has _id = 1
				// default ProjCode = "MyProject', but may be renamed
				projectId = sharedPref.getLong(Prefs.DEFAULT_PROJECT_ID, 1);
				if (!sharedPref.contains(Prefs.DEFAULT_PROJECT_ID)) {
					// this will only happen once, when the app is first installed
//					Toast.makeText(this.getActivity(), 
//							"Prefs key '" + PREF_DEFAULT_PROJECT_ID + "' does not exist yet.", 
//							Toast.LENGTH_LONG).show();
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_PROJECT_ID + "' does not exist yet.");
					// update the create time in the database from when the DB file was created to 'now'
					String sql = "UPDATE Projects SET StartDate = DATETIME('now') WHERE _id = 1;";
					ContentResolver resolver = getActivity().getContentResolver();
					// use raw SQL, to make use of SQLite internal "DATETIME('now')"
					Uri uri = ContentProvider_VegNab.SQL_URI;
					int numUpdated = resolver.update(uri, null, sql, null);
					saveDefaultProjectId(projectId);
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_PROJECT_ID + "' set for the first time."); 
				} else {
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_PROJECT_ID + "' = " + projectId);
				}
				// set the default Project to show in its spinner
				// for a generalized fn, try: projSpinner.getAdapter().getCount()
				for (int i=0; i<rowCt; i++) {
					Log.v(LOG_TAG, "Setting projSpinner default; testing index " + i);
					if (projSpinner.getItemIdAtPosition(i) == projectId) {
						Log.v(LOG_TAG, "Setting projSpinner default; found matching index " + i);
						projSpinner.setSelection(i);
						break;
					}
				}
			} else {
				projSpinner.setEnabled(false);
			}
			break;
			*/
		case LOADER_FOR_NAMERS:
			// Swap the new cursor in.
			// The framework will take care of closing the old cursor once we return.
			mNamerAdapter.swapCursor(finishedCursor);
			if (rowCt > 0) {
				namerSpinner.setEnabled(true);
				// get default Namer from app Preferences, to set spinner
				// this must wait till the spinner is populated
				SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
				// if none yet, use _id = 0, generated in query as '(add new)'
				namerId = sharedPref.getLong(Prefs.DEFAULT_NAMER_ID, 0);
				if (!sharedPref.contains(Prefs.DEFAULT_NAMER_ID)) {
					// this will only happen once, when the app is first installed
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' does not exist yet.");
					saveDefaultNamerId(namerId);
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' set for the first time."); 
				} else {
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' = " + namerId);
				}
				// set the default Namer to show in its spinner
				// for a generalized fn, try: mySpinner.getAdapter().getCount()
				for (int i=0; i<rowCt; i++) {
					Log.v(LOG_TAG, "Setting namerSpinner default; testing index " + i);
					if (namerSpinner.getItemIdAtPosition(i) == namerId) {
						Log.v(LOG_TAG, "Setting namerSpinner default; found matching index " + i);
						namerSpinner.setSelection(i);
						break;
					}
				}
			} else {
				namerSpinner.setEnabled(false);
			}
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
/*		case LOADER_FOR_VISIT:
			mVisitAdapter.swapCursor(null);
			break;
			*/
		case LOADER_FOR_NAMERS:
			mNamerAdapter.swapCursor(null);
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// 'parent' is the spinner
		// 'view' is one of the internal Android constants (e.g. text1=16908307, text2=16908308)
		//    in the item layout, unless set up otherwise
		// 'position' is the zero-based index in the list
		// 'id' is the (one-based) database record '_id' of the item
		// get the text by:
		//Cursor cur = (Cursor)mNamerAdapter.getItem(position);
		//String strSel = cur.getString(cur.getColumnIndex("NamerName"));
		//Log.v(LOG_TAG, strSel);
		// if spinner is filled by Content Provider, can't get text by:
		//String strSel = parent.getItemAtPosition(position).toString();
		// that returns something like below, which there is no way to get text out of:
		// "android.content.ContentResolver$CursorWrapperInner@42041b40"
		
		// sort out the spinners
		// can't use switch because not constants
		if (parent.getId() == namerSpinner.getId()) {
			namerId = id;
			if (namerId == 0) { // picked '(add new)'
				Log.v(LOG_TAG, "Starting 'add new' for Namer");
			}
			if (namerId != 0) {
				// save in app Preferences as the default Project
				saveDefaultNamerId(namerId);
			}
		}
		// write code for any other spinner(s) here
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
