package com.vegnab.vegnab;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
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

public class VegSubplotFragment extends ListFragment 
		implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String LOG_TAG = VegSubplotFragment.class.getSimpleName();
	
	public static final String POSITION_KEY = "FragmentPositionKey";
	private int mPosition = -1;
	
	public static final String VISIT_ID = "VisitId";
	private long mVisitId = 0;

	public static final String SUBPLOT_TYPE_ID = "SubplotTypeId";
	private long mSubplotTypeId = -1;

	public static final String PRESENCE_ONLY = "PresenceOnly";
	private boolean mPresenceOnly = true;
	
	public static final String VISIT_NAME = "VisitName";
	private String mVisitName = "";

	

//	private int mSubplotTypeId = -1, mSubplot = 0;
	boolean mHasNested;
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onNewItemButtonClicked(boolean presenceOnly);
		public void onNextSubplotButtonClicked(long mSubplotTypeId);
	}
	VegItemAdapter mVegSubplotSppAdapter;
	
	static VegSubplotFragment newInstance(Bundle args) {
		VegSubplotFragment f = new VegSubplotFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//	setHasOptionsMenu(true);
		if (savedInstanceState == null) {
			Log.v(LOG_TAG, "onCreate FIRST TIME, position = " + mPosition);
		} else {
			Log.v(LOG_TAG, "onCreate SUBSEQUENT TIME, position = " + mPosition);
		}
// set up any interfaces
//		try {
//        	mEditNamerListener = (EditNamerDialogListener) getActivity();
//        	Log.v(LOG_TAG, "(EditNamerDialogListener) getActivity()");
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Main Activity must implement EditNamerDialogListener interface");
//        }
	}	

/*	
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
*/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		mPosition = getArguments().getInt(POSITION_KEY);
		mVisitId = getArguments().getLong(VISIT_ID);
		mVisitName = getArguments().getString(VISIT_NAME);
		mSubplotTypeId = getArguments().getLong(SUBPLOT_TYPE_ID);
		mPresenceOnly = getArguments().getBoolean(PRESENCE_ONLY);
		
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_veg_subplot, container, false);
		// set up the diagnostics
		TextView textview = (TextView) rootView.findViewById(R.id.txt_test_pager);
		textview.setText("Position=" + mPosition + ", VisitId=" + mVisitId +
				", Visit Name: '" + mVisitName + "'" +
				", SubplotTypeId=" + mSubplotTypeId + ", PresenceOnly=" + (mPresenceOnly ? 1 : 0));

		// set click listener for the buttons in the view
		rootView.findViewById(R.id.subplotNewItemButton).setOnClickListener(this);
		rootView.findViewById(R.id.subplotNextButton).setOnClickListener(this);
		// if more, loop through all the child items of the ViewGroup rootView and 
		// set the onclicklistener for all the Button instances found

//		if (savedInstanceState != null) {
//			mVisitId = savedInstanceState.getLong(VISIT_ID);
//			mSubplotTypeId = savedInstanceState.getInt(SUBPLOT_TYPE_ID);
//		}
		
		// use query to return 'SppLine', concatenated from code and description; more reading room
		mVegSubplotSppAdapter = new VegItemAdapter(getActivity(),
				R.layout.list_veg_item, null, 0);
		setListAdapter(mVegSubplotSppAdapter);
		Log.v(LOG_TAG, "onCreateView, position = " + mPosition);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.v(LOG_TAG, "onStart, position = " + mPosition);

/*		
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		if (args != null) {
			mVisitId = args.getLong(VISIT_ID);
			mSubplotTypeId = args.getInt(SUBPLOT_TYPE_ID);

//			// set up subplot based on arguments passed in
//			updateSubplotViews(args.getInt(SUBPLOT_TYPE_ID));
//		} else if (mSubplotTypeId != -1) {
//			// set up subplot based on saved instance state defined in onCreateView
//			updateSubplotViews(mSubplotTypeId);
//		} else {
//			updateSubplotViews(-1); // figure out what to do for default state 
		}
*/	
		getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT, null, this);
		getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT_SPP, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.v(LOG_TAG, "onAttach, position = " + mPosition);
		// assure the container activity has implemented the callback interface
		try {
			mButtonCallback = (OnButtonListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException (activity.toString() + " must implement OnButtonListener");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v(LOG_TAG, "onSaveInstanceState, position = " + mPosition);
		// save the current subplot arguments in case we need to re-create the fragment
		outState.putLong(VISIT_ID, mVisitId);
		outState.putLong(SUBPLOT_TYPE_ID, mSubplotTypeId);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(LOG_TAG, "onStop, position = " + mPosition);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(LOG_TAG, "onDestroy, position = " + mPosition);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(LOG_TAG, "onDestroyView, position = " + mPosition);
	}	
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.v(LOG_TAG, "onDetach, position = " + mPosition);
	}	
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(LOG_TAG, "onPause, position = " + mPosition);
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(LOG_TAG, "onResume, position = " + mPosition);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(LOG_TAG, "onActivityCreated, position = " + mPosition);
	}
	
	/*
	public void updateSubplotViews(int subplotNum) {

		mSubplotTypeId = subplotNum;
		if (mSubplotTypeId < 1) { // invalid subplot type, should never happen
			// fill in something to help with diagnostics
			((TextView) getActivity().findViewById(R.id.subplot_header_name)).setText("Subplot " + mSubplotTypeId);
		} else {
			getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT, null, this);
//			getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT_SPP, null, this);
		}
		// at this point, after inflate, the frag's objects are all child objects of the activity
	}
	*/

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subplotNewItemButton:
			mButtonCallback.onNewItemButtonClicked(mPresenceOnly);
			break;
		case R.id.subplotNextButton:
			mButtonCallback.onNextSubplotButtonClicked(mSubplotTypeId);
			break;
		}
	}
	
	public void refreshSppList() {
		// when the referred Loader callback returns, will refresh the currently used species
		getLoaderManager().restartLoader(Loaders.CURRENT_SUBPLOT_SPP, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case Loaders.CURRENT_SUBPLOT:
			// retrieve any needed header info
			baseUri = ContentProvider_VegNab.SQL_URI;
			select = "SELECT SubplotDescription, PresenceOnly, HasNested " 
					+ "FROM SubplotTypes WHERE _id = " + mSubplotTypeId + ";";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;
		case Loaders.CURRENT_SUBPLOT_SPP:
			baseUri = ContentProvider_VegNab.SQL_URI;
			// get any species entries for this subplot of this visit
			select = "SELECT VegItems._id, VegItems.OrigCode, VegItems.OrigDescr, "
					+ "VegItems.OrigCode || ': ' || VegItems.OrigDescr AS SppLine , "
					+ "VegItems.Height, VegItems.Cover, VegItems.Presence, "
					+ "IdLevels.IdLevelDescr, IdLevels.IdLevelLetterCode "
					+ "FROM VegItems LEFT JOIN IdLevels ON VegItems.IdLevelID = IdLevels._id "
					+ "WHERE (((VegItems.VisitID)=?) AND ((VegItems.SubPlotID)=?)) "
					+ "ORDER BY VegItems.TimeLastChanged DESC;";
			Log.v(LOG_TAG, "onCreateLoader CURRENT_SUBPLOT_SPP, mVisitId=" + mVisitId 
					+ ", mSubplotTypeId=" + mSubplotTypeId);
			String[] sppSelectionArgs = { "" + mVisitId, "" + mSubplotTypeId };
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, sppSelectionArgs, null);
			break;		
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		// there will be various loaders, switch them out here
//		mRowCt = finishedCursor.getCount();
		switch (loader.getId()) {
		case Loaders.CURRENT_SUBPLOT:
			// fill in any header info
			// SubplotDescription, PresenceOnly, HasNested
			int rowCt = c.getCount();
			Log.v(LOG_TAG, "onLoadFinished CURRENT_SUBPLOT, number of rows returned: " + rowCt);
			if (c.moveToNext()) {
				((TextView) getActivity().findViewById(R.id.subplot_header_name))
					.setText(c.getString(c.getColumnIndexOrThrow("SubplotDescription")));
				mPresenceOnly = ((c.getInt(c.getColumnIndexOrThrow("PresenceOnly")) == 1) ? true : false);
			}
			
			break;
		case Loaders.CURRENT_SUBPLOT_SPP:
			Log.v(LOG_TAG, "onLoadFinished CURRENT_SUBPLOT_SPP, number of rows returned: " + c.getCount());
			mVegSubplotSppAdapter.swapCursor(c);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
		case Loaders.CURRENT_SUBPLOT:
			// no adapter, nothing to do
			break;
		case Loaders.CURRENT_SUBPLOT_SPP:
			mVegSubplotSppAdapter.swapCursor(null);
			break;
		}
	}
}
