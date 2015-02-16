package com.vegnab.vegnab;

import com.google.android.gms.location.LocationServices;
import com.vegnab.vegnab.VisitHeaderFragment.EditVisitDialogListener;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VegSubplotFragment extends ListFragment implements OnClickListener {
	final static String ARG_SUBPLOT = "subplot";
	int mCurrentSubplot = -1;
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onNextSubplotButtonClicked(int subpNum);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*        try {
        	mEditVisitListener = (EditVisitDialogListener) getActivity();
        	Log.v(LOG_TAG, "(EditVisitDialogListener) getActivity()");
        } catch (ClassCastException e) {
            throw new ClassCastException("Main Activity must implement EditVisitDialogListener interface");
        } */
	setHasOptionsMenu(true);
	}	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.veg_subplot, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		FragmentManager fm = getActivity().getSupportFragmentManager();
//		DialogFragment editProjDlg;
		switch (item.getItemId()) { // the Activity has first opportunity to handle these
		// any not handled come here to this Fragment
//		case R.id.action_app_info:
//			Toast.makeText(getActivity(), "''App Info'' of Visit Header is not implemented yet", Toast.LENGTH_SHORT).show();
//			return true;
		case R.id.action_add_item:
			Toast.makeText(getActivity(), "''Add item'' of Veg Subplot is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_veg_help:
			Toast.makeText(getActivity(), "''Help'' of Veg Subplot is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_mark_no_veg:
			Toast.makeText(getActivity(), "''Mark no-veg'' of Veg Subplot is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_go_to:
			Toast.makeText(getActivity(), "''Go to...'' of Veg Subplot is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous subplot remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			mCurrentSubplot = savedInstanceState.getInt(ARG_SUBPLOT);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_veg_subplot, container, false);
		// set click listener for the button in the view
		Button b = (Button) rootView.findViewById(R.id.subplotNextButton);
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
		// at this point, after inflate, the frag's objects are all child objects of the activity
		TextView t = (TextView)getActivity().findViewById(R.id.subplot_header_name);
		t.setText("Subplot " + mCurrentSubplot);
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
		case R.id.subplotNextButton:
			mButtonCallback.onNextSubplotButtonClicked(mCurrentSubplot);
			break;
		}
	}

}
