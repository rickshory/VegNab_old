package com.vegnab.vegnab;

import java.util.List;

import com.vegnab.vegnab.database.VegNabDbHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class NewVisitFragment extends Fragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor>{
//	, OnItemSelectedListener
	final static String ARG_SUBPLOT = "subplot";
	int mCurrentSubplot = -1;
	VegNabDbHelper DbHelper;
	Spinner projSpinner;
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onNewVisitGoButtonClicked();
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
		View rootView = inflater.inflate(R.layout.fragment_new_visit, container, false);
		projSpinner = (Spinner) rootView.findViewById(R.id.sel_project_spinner);
//		projSpinner.setOnItemSelectedListener((android.widget.AdapterView.OnItemSelectedListener) this);
		loadProjSpinnerItems();
		// set click listener for the button in the view
		Button b = (Button) rootView.findViewById(R.id.new_visit_go_button);
		b.setOnClickListener(this);
		// if more, loop through all the child items of the ViewGroup rootView and 
		// set the onclicklistener for all the Button instances found
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
			mButtonCallback.onNewVisitGoButtonClicked();
			break;
		}
	}

	private void loadProjSpinnerItems() {
		List<String> projCodes = DbHelper.getProjectsAsList();
//		Cursor prjCursor = DbHelper.getProjectsAsCursor();
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_spinner_item, projCodes);
//		String[] columns = new String[] { "Project" };
//		Int [] to = new Int[] {};
//		SimpleCursorAdapter cAdapter = new SimpleCursorAdapter(this.getActivity(), 
//				android.R.layout.simple_spinner_item, prjCursor, null, null, mCurrentSubplot);
		
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		projSpinner.setAdapter(dataAdapter);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 
	@Override
	public void onItemSelected(AdapterViewCompat<?> parent, View view, int position,
			long id) {
		String strSel = parent.getItemAtPosition(position).toString();
		Toast.makeText(parent.getContext(), "Project selected: " + strSel, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterViewCompat<?> arg0) {
		// TODO Auto-generated method stub
	}
*/
}
