package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
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
	public static final int LOADER_FOR_VISIT = 5; // Loader Ids
	public static final int LOADER_FOR_NAMERS = 6;
	long visitId = 0; // zero default means new or not specified yet
	Uri uri, baseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "visits");
	ContentValues values = new ContentValues();
	private EditText mVisitName, mVisitDate, mVisitScribe, mVisitLocation, mVisitNotes;
	private Spinner namerSpinner;
	SimpleCursorAdapter mNamerAdapter;
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
					+ "UNION SELECT 0, '(add new)'' "
					+ "ORDER BY _id;";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
