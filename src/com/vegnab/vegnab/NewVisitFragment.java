package com.vegnab.vegnab;

import java.util.List;
import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VegNabDbHelper;
import android.app.Activity;
import android.content.ContentResolver;
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
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.internal.widget.AdapterViewCompat.OnItemSelectedListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class NewVisitFragment extends Fragment implements OnClickListener,
		android.widget.AdapterView.OnItemSelectedListener,
		LoaderManager.LoaderCallbacks<Cursor>{
//	, OnItemSelectedListener
	private static final String LOG_TAG = "NewVisitFragment";
	public static final String PREF_DEFAULT_PROJECT_ID = "Default_Project_Id";
	public static final String PREF_DEFAULT_PLOTTYPE_ID = "Default_PlotType_Id";
	public static final int TEST_SQL_LOADER = 0; // test loading from raw SQL
	public static final int LOADER_FOR_PROJECTS = 1; // Loader Id for Projects
	public static final int LOADER_FOR_PLOTTYPES = 2; // Loader Id for Plot Types
	long projectId;
	long plotTypeId;
	final static String ARG_SUBPLOT = "subplot";
	int mCurrentSubplot = -1;
	VegNabDbHelper DbHelper;
	Spinner projSpinner, plotTypeSpinner;
	SimpleCursorAdapter mProjAdapter, mPlotTypeAdapter; // to link the spinners' data
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onNewVisitGoButtonClicked();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// first time, set default preferences
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		// database comes pre-loaded with one Project record that has _id = 1
		// default ProjCode = "MyProject', but may be renamed
		projectId = sharedPref.getLong(PREF_DEFAULT_PROJECT_ID, 1);
		if (!sharedPref.contains(PREF_DEFAULT_PROJECT_ID)) {
			Toast.makeText(this.getActivity(), 
					"Prefs key '" + PREF_DEFAULT_PROJECT_ID + "' does not exist yet.", 
					Toast.LENGTH_LONG).show();
			Log.v(LOG_TAG, "Prefs key '" + PREF_DEFAULT_PROJECT_ID + "' does not exist yet.");
			// update the create time in the database
			
		} else {
			Toast.makeText(this.getActivity(), 
					"Prefs key '" + PREF_DEFAULT_PROJECT_ID + "' = " + projectId, 
					Toast.LENGTH_LONG).show();
			Log.v(LOG_TAG, "Prefs key '" + PREF_DEFAULT_PROJECT_ID + "' = " + projectId);
		}
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous screen, remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			mCurrentSubplot = savedInstanceState.getInt(ARG_SUBPLOT);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_new_visit, container, false);
		// set click listener for the button in the view
		Button b = (Button) rootView.findViewById(R.id.new_visit_go_button);
		b.setOnClickListener(this);
		// if more, loop through all the child items of the ViewGroup rootView and 
		// set the onclicklistener for all the Button instances found
		// Create an empty adapter we will use to display the list of Projects
		projSpinner = (Spinner) rootView.findViewById(R.id.sel_project_spinner);
		projSpinner.setEnabled(false); // will enable when data ready
		mProjAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null,
				new String[] {"ProjCode"},
				new int[] {android.R.id.text1}, 0);
		mProjAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		projSpinner.setAdapter(mProjAdapter);
		projSpinner.setOnItemSelectedListener(this);
		// Prepare the loader. Either re-connect with an existing one or start a new one
		getLoaderManager().initLoader(LOADER_FOR_PROJECTS, null, this);
		// If there in no Loader yet, this will call
		// Loader<Cursor> onCreateLoader and pass it a first parameter of LOADER_FOR_PROJECTS
		plotTypeSpinner = (Spinner) rootView.findViewById(R.id.sel_plot_type_spinner);
		plotTypeSpinner.setEnabled(false); // will enable when data ready
		plotTypeSpinner.setAdapter(mPlotTypeAdapter); // for testing, don't load it yet
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
		DbHelper = new VegNabDbHelper(activity);
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

	public void saveDefaultProjectId(long id) {
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putLong(PREF_DEFAULT_PROJECT_ID, id);
		prefEditor.commit();
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
		case R.id.new_visit_go_button:
			Toast.makeText(this.getActivity(), 
					"Selected Project position: " + projSpinner.getSelectedItemPosition() 
					+ ", Id: " + projSpinner.getSelectedItemId() , 
					Toast.LENGTH_LONG).show();

			Toast.makeText(this.getActivity(), 
					"Selected PlotType position: " + plotTypeSpinner.getSelectedItemPosition() 
					+ ", Id: " + plotTypeSpinner.getSelectedItemId() , 
					Toast.LENGTH_LONG).show();
			
			if (projSpinner.getSelectedItemPosition() == -1) {
				Toast.makeText(this.getActivity(),
						"" + getResources().getString(R.string.missing_project),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (plotTypeSpinner.getSelectedItemPosition() == -1) {
				Toast.makeText(this.getActivity(),
						"" + getResources().getString(R.string.missing_plottype),
						Toast.LENGTH_SHORT).show();
				return;
			}

			mButtonCallback.onNewVisitGoButtonClicked();
			break;
		}
	}

	// define the columns we will retrieve from the Projects table
	static final String[] PROJECTS_PROJCODES = new String[] {
		"_id", "ProjCode",
	};

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case LOADER_FOR_PROJECTS:
			// First, create the base URI
			// could test here, based on e.g. filters
			baseUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), baseUri,
					PROJECTS_PROJCODES, select, null, null);
			break;
		case TEST_SQL_LOADER:
			baseUri = ContentProvider_VegNab.CONTENT_URI;
			select = "SELECT StartDate FROM Projects WHERE _id = 1;";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;
//		case LOADER_FOR_PLOTTYPES:
			// still to be written
//			break;
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor finishedCursor) {
		// there will be various loaders, switch them out here
		switch (loader.getId()) {
		case LOADER_FOR_PROJECTS:
			// Swap the new cursor in.
			// The framework will take care of closing the old cursor once we return.
			mProjAdapter.swapCursor(finishedCursor);
			int rowCt = finishedCursor.getCount();
			if (rowCt > 0) {
				projSpinner.setEnabled(true);
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
		case LOADER_FOR_PLOTTYPES:
			// still to be written
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
		case LOADER_FOR_PROJECTS:
			mProjAdapter.swapCursor(null);
			break;
		case LOADER_FOR_PLOTTYPES:
			// still to be written
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
		//Cursor cur = (Cursor)mProjAdapter.getItem(position);
		//String strSel = cur.getString(cur.getColumnIndex("ProjCode"));
		//Log.v(LOG_TAG, strSel);
		// if spinner is filled by Content Provider, can't get text by:
		//String strSel = parent.getItemAtPosition(position).toString();
		// that returns something like below, which there is no way to get text out of:
		// "android.content.ContentResolver$CursorWrapperInner@42041b40"
		
		// sort out the spinners
		// can't use switch because not constants
		if (parent.getId() == projSpinner.getId()) {
			projectId = id;
			// save in app Preferences as the default Project
			saveDefaultProjectId(projectId);
/*			
			Toast.makeText(parent.getContext(),
					"Selected Project position: " + position
					+ ", Id: " + id, 
					Toast.LENGTH_LONG).show();
			Cursor cur = (Cursor)mProjAdapter.getItem(position);
			String strSel = cur.getString(cur.getColumnIndex("ProjCode"));
			Toast.makeText(parent.getContext(), "Project selected: " + strSel, Toast.LENGTH_LONG).show();
*/
		}
		// write code for other spinner(s) here
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
}
