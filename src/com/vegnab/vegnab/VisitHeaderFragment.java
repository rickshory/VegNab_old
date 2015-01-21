package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.internal.widget.AdapterViewCompat.AdapterContextMenuInfo;
import android.support.v7.internal.widget.AdapterViewCompat.OnItemSelectedListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class VisitHeaderFragment extends Fragment implements OnClickListener,
		android.widget.AdapterView.OnItemSelectedListener,
		android.view.View.OnFocusChangeListener,
		LoaderManager.LoaderCallbacks<Cursor>,
		ConnectionCallbacks, OnConnectionFailedListener, 
        LocationListener{
	/*
	private class VisitRecord {
		private String mStartTime = mTimeFormat.format(new Date());
		private String mLastChanged = mTimeFormat.format(new Date());
		private long mId = 0;
		long getId() {
			return mId;
		}
		void setId (long iD) {
			mId = iD;
			mLastChanged = mTimeFormat.format(new Date()); // maybe move this to saveToDb
		}
		private String mVisName;
		String getVisitName() {
			return mVisName;
		}
		void setVisitName(String visName) {
			mVisName = visName;
			mLastChanged = mTimeFormat.format(new Date()); // maybe move this to saveToDb
		}
		private long mProjID = 0;
		private long mPlotTypeID = 0;
		private long mNamerID = 0;
		private String mScribe;
		private long mRefLocID = 0;
		private boolean mRefLocIsGood = false;
		private int mAzimuth;
		private String mVisitNotes;
		private int mDeviceType = 1;
		private String mDeviceID;
		private boolean mIsComplete = false;
		private boolean mShowOnMobile = true;
		private boolean mInclude = true;
		private boolean mIsDeleted = false;
		private int mNumAdditionalLocations = 0;
		private int mAdditionalLocationsType = 1;			
		
	}

		
	CREATE TABLE "Visits" (
"_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
"VisitName" VARCHAR(16) NOT NULL,
"VisitDate" TIMESTAMP NOT NULL, -- visible to user & can be manually changed
"ProjID" INTEGER NOT NULL,
"PlotTypeID" INTEGER NOT NULL,
"StartTime" TIMESTAMP NOT NULL DEFAULT (DATETIME('now')), -- maintained automatically
"LastChanged" TIMESTAMP NOT NULL DEFAULT (DATETIME('now')), -- maintained automatically
"NamerID" INTEGER NOT NULL,
"Scribe" VARCHAR(20), -- optional, if someone besides the Namer is entering data
"RefLocID" INTEGER, -- can not be required for valid record, but app will keep bugging user to get this
"RefLocIsGood" BOOL NOT NULL DEFAULT 0, -- allow to accept as good, even if accuracy is poor
"Azimuth" INTEGER -- if it applies to this plot type
CHECK ((Azimuth IS NULL) OR ((CAST(Azimuth AS INTEGER) == Azimuth) 
AND (Azimuth >= 0) AND (Azimuth <= 360))), 
"VisitNotes" VARCHAR(255), -- limit the length; some users would write a thesis
"DeviceType" INTEGER DEFAULT 1, -- 1=Unknown, 2=Android
"DeviceID" VARCHAR(20) NOT NULL,

"DeviceID" should be a unique identifier for the device this Visit is entered on.
It would be either from TelephonyManager.getDeviceId() (IMEI, MEID, ESN, etc.), or 
else ANDROID_ID.
ESNs are either 11-digit decimal numbers or 8-digit hexadecimal numbers
MEIDs are 56 bits long, the same length as the IMEI
MEID allows hexadecimal digits while IMEI allows only decimal digits
IMEI length 17
MEID length 14
ANDROID_ID is a 64-bit number as a hex string, therefore length 16
Use 20 to be sure the field is long enough.

"IsComplete" BOOL NOT NULL DEFAULT 0, -- flag to sync to cloud storage, if subscribed; option to automatically set following flag to 0 after sync
"ShowOnMobile" BOOL NOT NULL DEFAULT 1, -- allow masking out, to reduce clutter
"Include" BOOL NOT NULL DEFAULT 1, -- include in analysis, not used on mobile but here for completeness
"IsDeleted" BOOL NOT NULL DEFAULT 0, -- don't allow user to actually delete a visit, just flag it; this by hard experience
"NumAdditionalLocations" INTEGER NOT NULL DEFAULT 0, -- if additional locations are mapped, maintain the count
"AdditionalLocationsType" INTEGER NOT NULL DEFAULT 1 -- 1=points, 2=line, 3=polygon
CHECK ((AdditionalLocationsType >= 1) AND (AdditionalLocationsType <= 3)),
"AdditionalLocationSelected" INTEGER, -- the currently active additional location for this visit, if one is selected
FOREIGN KEY("ProjID") REFERENCES Projects("_id"),
FOREIGN KEY("PlotTypeID") REFERENCES PlotTypes("_id"),
FOREIGN KEY("NamerID") REFERENCES Namers("_id"),
FOREIGN KEY("RefLocID") REFERENCES Locations("_id"),
FOREIGN KEY("AdditionalLocationSelected") REFERENCES Locations("_id"),
FOREIGN KEY("AdditionalLocationsType") REFERENCES LocationTypes("_id")
*/
	
	private class VNLocation extends Location {
		public VNLocation(Location l) {
			super(l);
		}
		
		private long mId;
		public long getId() {
			return mId;
		}
		public void setId (long iD) {
			mId = iD;
		}
		
		private String mLocName;
		public String getLocName() {
			return mLocName;
		}
		public void setLocName(String locName) {
			mLocName = locName;
		}

		private long mVisitId;
		public long getVisitId() {
			return mVisitId;
		}
		public void setVisitId (long iD) {
			mVisitId = iD;
		}
		
		private long mSubplotId;
		public long getSubploId() {
			return mSubplotId;
		}
		public void setSubploId (long iD) {
			mSubplotId = iD;
		}
		
		private int mListingOrder;
		public int getListingOrder() {
			return mListingOrder;
		}
		public void setListingOrder (int i) {
			mListingOrder = i;
		}
		
		public String getTimeString() {
			long n = super.getTime();
			return mTimeFormat.format(new Date(n));
		}
	}
	/*CREATE TABLE Locations (
"_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
"LocName" VARCHAR(30) NOT NULL,
"VisitID" INTEGER NOT NULL,
"SubplotID" INTEGER,
"ListingOrder" INTEGER DEFAULT 0,
"Latitude" FLOAT NOT NULL 
CHECK ((CAST(Latitude AS FLOAT) == Latitude) 
AND (Latitude >= -90) AND (Latitude <= 90)),
"Longitude" FLOAT NOT NULL 
CHECK ((CAST(Longitude AS FLOAT) == Longitude) 
AND (Longitude >= -180) AND (Longitude <= 180)),
"TimeStamp" TIMESTAMP NOT NULL,
"Accuracy" FLOAT,
"Altitude" FLOAT,
FOREIGN KEY("VisitID") REFERENCES Visits("_id"),
FOREIGN KEY("SubplotID") REFERENCES SubplotTypes("_id")
)*/
	private static final String LOG_TAG = VisitHeaderFragment.class.getSimpleName();
	private static final String TAG_SPINNER_FIRST_USE = "FirstTime";
	private static final int MENU_HELP = 0;
	private static final int MENU_ADD = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_DELETE = 3;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double mLatitude, mLongitude;
//    private VisitRecord mVis;
    private boolean mLocIsGood = false; // default until retrieved or established true
    private float mAccuracy, mAccuracyTargetForVisitLoc;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	long mVisitId = 0, mNamerId = 0; // zero default means new or not specified yet
	Uri mUri, mBaseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "visits");
	ContentValues mValues = new ContentValues();
	private EditText mViewVisitName, mViewVisitDate, mViewVisitScribe, mViewVisitLocation, mViewAzimuth, mViewVisitNotes;
	private Spinner mNamerSpinner;
	private TextView mLblNewNamerSpinnerCover;
	SimpleCursorAdapter mVisitAdapter, mNamerAdapter;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
	private Calendar mCalendar = Calendar.getInstance();
	private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
	    @Override
	    public void onDateSet(DatePicker view, int year, int monthOfYear,
	            int dayOfMonth) {
	        mCalendar.set(Calendar.YEAR, year);
	        mCalendar.set(Calendar.MONTH, monthOfYear);
	        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	        mViewVisitDate.setText(mDateFormat.format(mCalendar.getTime()));
	    }
	};
	int mRowCt;
	final static String ARG_VISIT_ID = "visitId";
	final static String ARG_SUBPLOT = "subplot";
	final static String ARG_LOC_GOOD_FLAG = "locGood";
	int mCurrentSubplot = -1;
	OnButtonListener mButtonCallback; // declare the interface
	// declare that the container Activity must implement this interface
	public interface OnButtonListener {
		// methods that must be implemented in the container Activity
		public void onVisitHeaderGoButtonClicked();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.visit_header, menu);
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
		case R.id.action_app_info:
			Toast.makeText(getActivity(), "''App Info'' of Visit Header is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_visit_info:
			Toast.makeText(getActivity(), "''Visit Details'' of Visit Header is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_delete_visit:
			Toast.makeText(getActivity(), "''Delete Visit'' is not fully implemented yet", Toast.LENGTH_SHORT).show();
			Fragment newVisFragment = fm.findFragmentByTag("new_visit");
			if (newVisFragment == null) {
				Log.v(LOG_TAG, "newVisFragment == null");
			} else {
				Log.v(LOG_TAG, "newVisFragment: " + newVisFragment.toString());
				FragmentTransaction transaction = fm.beginTransaction();
				// replace the fragment in the fragment container with the stored New Visit fragment
				transaction.replace(R.id.fragment_container, newVisFragment);
				// we are deleting this record, so do not put the present fragment on the backstack
				transaction.commit();		
			}

//			DelProjectDialog delProjDlg = new DelProjectDialog();
//			delProjDlg.show(fm, "frg_del_proj");
			return true;
		case R.id.action_visit_help:
			Toast.makeText(getActivity(), "''Visit Help'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_settings:
			Toast.makeText(getActivity(), "''Settings'' of Visit Header is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous screen, remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			mCurrentSubplot = savedInstanceState.getInt(ARG_SUBPLOT, 0);
			mVisitId = savedInstanceState.getLong(ARG_VISIT_ID, 0);
			mLocIsGood = savedInstanceState.getBoolean(ARG_LOC_GOOD_FLAG, false);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_visit_header, container, false);
		mViewVisitName = (EditText) rootView.findViewById(R.id.txt_visit_name);
		mViewVisitName.setOnFocusChangeListener(this);
		registerForContextMenu(mViewVisitName); // enable long-press
		mViewVisitDate = (EditText) rootView.findViewById(R.id.txt_visit_date);
		mViewVisitDate.setText(mDateFormat.format(mCalendar.getTime()));
		mViewVisitDate.setOnClickListener(this);
		mNamerSpinner = (Spinner) rootView.findViewById(R.id.sel_spp_namer_spinner);
		mNamerSpinner.setTag(TAG_SPINNER_FIRST_USE); // flag to catch and ignore erroneous first firing
		mNamerSpinner.setEnabled(false); // will enable when data ready		
		mNamerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null,
				new String[] {"NamerName"},
				new int[] {android.R.id.text1}, 0);		
		mNamerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mNamerSpinner.setAdapter(mNamerAdapter);
		mNamerSpinner.setOnItemSelectedListener(this);
		registerForContextMenu(mNamerSpinner); // enable long-press
		// also need click, if no names & therefore selection cannot be changed
//		mNamerSpinner.setOnFocusChangeListener(this); // does not work
		// use a TextView on top of the spinner, named "lbl_spp_namer_spinner_cover"
		mLblNewNamerSpinnerCover = (TextView) rootView.findViewById(R.id.lbl_spp_namer_spinner_cover);
		mLblNewNamerSpinnerCover.setOnClickListener(this);
		// Prepare the loader. Either re-connect with an existing one or start a new one
		getLoaderManager().initLoader(Loaders.NAMERS, null, this);
		// in layout, TextView is in front of Spinner and takes precedence
		// for testing context menu, bring spinner to front so it receives clicks
//		mNamerSpinner.bringToFront();		
		mViewVisitScribe = (EditText) rootView.findViewById(R.id.txt_visit_scribe);
		mViewVisitScribe.setOnFocusChangeListener(this);
		registerForContextMenu(mViewVisitScribe); // enable long-press
		// set up the visit Location
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		mAccuracyTargetForVisitLoc = sharedPref.getFloat(Prefs.TARGET_ACCURACY_OF_VISIT_LOCATIONS, 7.0f);
		mViewVisitLocation = (EditText) rootView.findViewById(R.id.txt_visit_location);
		mViewVisitLocation.setOnFocusChangeListener(this);
		registerForContextMenu(mViewVisitLocation); // enable long-press
        // should the following go in onCreate() ?
        buildGoogleApiClient();
    	mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
            .setFastestInterval(1 * 1000); // 1 second, in milliseconds

		mViewAzimuth = (EditText) rootView.findViewById(R.id.txt_visit_azimuth);
		mViewAzimuth.setOnFocusChangeListener(this);
		registerForContextMenu(mViewAzimuth); // enable long-press
		mViewVisitNotes = (EditText) rootView.findViewById(R.id.txt_visit_notes);
		mViewVisitNotes.setOnFocusChangeListener(this);
		registerForContextMenu(mViewVisitNotes); // enable long-press
		// set click listener for the button in the view
		Button b = (Button) rootView.findViewById(R.id.visit_header_go_button);
		b.setOnClickListener(this);
		// if more, loop through all the child items of the ViewGroup rootView and 
		// set the onclicklistener for all the Button instances found
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
//        mGoogleApiClient.connect();
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
	public void onResume() {
	    super.onResume();
//	    do other setup here if needed
	    if (!mLocIsGood) {
	    	mGoogleApiClient.connect();
	    }
	    
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    if (mGoogleApiClient.isConnected()) {
	    	LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	        mGoogleApiClient.disconnect();
	    }
	}

    @Override
    public void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
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
		outState.putLong(ARG_VISIT_ID, mVisitId);
		outState.putBoolean(ARG_LOC_GOOD_FLAG, mLocIsGood);
	}

	@Override
	public void onClick(View v) {
		AddSpeciesNamerDialog  addSppNamerDlg = AddSpeciesNamerDialog.newInstance();
		FragmentManager fm = getActivity().getSupportFragmentManager();
		switch (v.getId()) {
		case R.id.txt_visit_date:
			fireOffDatePicker();
			break;
		case R.id.sel_spp_namer_spinner:
			Log.v(LOG_TAG, "Starting 'add new' for Namer from onClick");
			addSppNamerDlg.show(fm, "");
			break;
		case R.id.lbl_spp_namer_spinner_cover:
			Log.v(LOG_TAG, "Starting 'add new' for Namer from onClick of 'lbl_spp_namer_spinner_cover'");
			addSppNamerDlg.show(fm, "");
			break;
		case R.id.visit_header_go_button:
			mButtonCallback.onVisitHeaderGoButtonClicked();
			break;
		}
	}
	
	private void fireOffDatePicker() {
		String s = mViewVisitDate.getText().toString();
	    try { // if the EditText view contains a valid date
	    	mCalendar.setTime(mDateFormat.parse(s)); // use it
		} catch (java.text.ParseException e) { // otherwise
			mCalendar = Calendar.getInstance(); // use today's date
		}
		new DatePickerDialog(getActivity(), myDateListener,
				mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH)).show();	
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

		case Loaders.VISIT_TO_EDIT:
			Uri oneVisUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "visits"), mVisitId);
			cl = new CursorLoader(getActivity(), oneVisUri,
					null, select, null, null);
			break;
		case Loaders.NAMERS:
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
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		// there will be various loaders, switch them out here
		mRowCt = c.getCount();
		switch (loader.getId()) {
		case Loaders.VISIT_TO_EDIT:
			Log.v(LOG_TAG, "onLoadFinished, VISIT_TO_EDIT, records: " + c.getCount());
			if (c.moveToFirst()) {
				mViewVisitName.setText(c.getString(c.getColumnIndexOrThrow("VisitName")));
				mViewVisitDate.setText(c.getString(c.getColumnIndexOrThrow("VisitDate")));
				mNamerId = c.getLong(c.getColumnIndexOrThrow("NamerID"));
				setNamerSpinnerSelection();
				mViewVisitScribe.setText(c.getString(c.getColumnIndexOrThrow("Scribe")));
				// write code to save/retrieve Locations
				mLocIsGood = (c.getInt(c.getColumnIndexOrThrow("RefLocIsGood")) == 0) ? false : true;
				
//		mViewVisitLocation = (EditText) rootView.findViewById(R.id.txt_visit_location);
				mViewAzimuth.setText("" + c.getInt(c.getColumnIndexOrThrow("Azimuth")));
				mViewVisitNotes.setText(c.getString(c.getColumnIndexOrThrow("VisitNotes")));
			}

			break;
		
		case Loaders.NAMERS:
			// Swap the new cursor in.
			// The framework will take care of closing the old cursor once we return.
			mNamerAdapter.swapCursor(c);
			if (mRowCt > 0) {
				// get default Namer from app Preferences, to set spinner
				// this must wait till the spinner is populated
				SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
				// if none yet, use _id = 0, generated in query as '(add new)'
				mNamerId = sharedPref.getLong(Prefs.DEFAULT_NAMER_ID, 0);
				if (!sharedPref.contains(Prefs.DEFAULT_NAMER_ID)) {
					// this will only happen once, when the app is first installed
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' does not exist yet.");
					saveDefaultNamerId(mNamerId);
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' set for the first time."); 
				} else {
					Log.v(LOG_TAG, "Prefs key '" + Prefs.DEFAULT_NAMER_ID + "' = " + mNamerId);
				}
				setNamerSpinnerSelection();
				mNamerSpinner.setEnabled(true);
			} else {
				mNamerSpinner.setEnabled(false);
			}
			if (mNamerId == 0) {
				// user sees '(add new)', blank TextView receives click;
				mLblNewNamerSpinnerCover.bringToFront();
			} else {
				// user can operate the spinner
				mNamerSpinner.bringToFront();
			}
			break;
		}
	}

	private void setNamerSpinnerSelection() {
		// set the current Namer to show in its spinner
		for (int i=0; i<mRowCt; i++) {
			Log.v(LOG_TAG, "Setting mNamerSpinner; testing index " + i);
			if (mNamerSpinner.getItemIdAtPosition(i) == mNamerId) {
				Log.v(LOG_TAG, "Setting mNamerSpinner; found matching index " + i);
				mNamerSpinner.setSelection(i);
				break;
			}
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
		case Loaders.VISIT_TO_EDIT:
			Log.v(LOG_TAG, "onLoaderReset, VISIT_TO_EDIT.");
//			don't need to do anything here, no cursor adapter
			break;
			
		case Loaders.NAMERS:
			mNamerAdapter.swapCursor(null);
			break;
		}
	}

	
	private int saveVisitRecord () {
		// if anything invalid, don't save record
		if ("" + mViewVisitName.getText().toString().trim() == "") {
			Toast.makeText(this.getActivity(),
					"Need Visit Name",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		// test and warn if duplicate name, but allow
//		if (mExistingProjCodes.contains("" + mProjCode.getText().toString().trim())) {
//			Toast.makeText(this.getActivity(),
//					"Duplicate Project Code",
//					Toast.LENGTH_LONG).show();
//			return 0;
//		}

		if ("" + mViewVisitDate.getText().toString().trim() == "") {
			Toast.makeText(this.getActivity(),
					"Need Visit Date",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		if (mNamerId == 0) {
			Toast.makeText(this.getActivity(),
					"Need a Namer for species",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		
		if (!mLocIsGood) {
			Toast.makeText(this.getActivity(),
					"Wait till Location is valid, or long-press for options.",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		// mVisitId = 0, mNamerId = 0;
		// mLocIsGood
		// , mViewVisitLocation
		
// none of these are required: mViewVisitScribe, mViewAzimuth, mViewVisitNotes;
//		private Spinner mNamerSpinner;

		ContentResolver rs = getActivity().getContentResolver();
		if (mVisitId == 0) { // new record
			// fill in fields the user never sees
//			mValues.put("ProjID", mEndDate.getText().toString().trim()); // how to put a long?
//			mValues.put("PlotTypeID", mEndDate.getText().toString().trim());
			mValues.put("StartTime", mTimeFormat.format(new Date()));
			mValues.put("LastChanged", mTimeFormat.format(new Date()));
//			mValues.put("NamerID", mEndDate.getText().toString().trim()); // maybe do this one in Switch?
//			mValues.put("RefLocID", mEndDate.getText().toString().trim()); // save the Location to get this ID
//			mValues.put("RefLocIsGood", mEndDate.getText().toString().trim());
//			mValues.put("DeviceType", mEndDate.getText().toString().trim()); // get this earlier and have it ready
//			mValues.put("DeviceID", mEndDate.getText().toString().trim()); // get this earlier and have it ready
			// don't actually need the following 6 as the fields have default values
			// check if this is the right way to do a number
			mValues.put("IsComplete", "0"); // flag to sync to cloud storage, if subscribed; option to automatically set following flag to 0 after sync
			mValues.put("ShowOnMobile", "1"); // allow masking out, to reduce clutter
			mValues.put("Include", "1"); // include in analysis, not used on mobile but here for completeness
			mValues.put("IsDeleted", "0"); // don't allow user to actually delete a visit, just flag it; this by hard experience
			mValues.put("NumAdditionalLocations", "0"); // if additional locations are mapped, maintain the count
			mValues.put("AdditionalLocationsType", "1"); // 1=points, 2=line, 3=polygon
			
//			mProjID = 0;
			/*	
			CREATE TABLE "Visits" (
		"_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
		"VisitName" VARCHAR(16) NOT NULL,
		"VisitDate" TIMESTAMP NOT NULL, -- visible to user & can be manually changed
		"ProjID" INTEGER NOT NULL,
		"PlotTypeID" INTEGER NOT NULL,
		"StartTime" TIMESTAMP NOT NULL DEFAULT (DATETIME('now')), -- maintained automatically
		"LastChanged" TIMESTAMP NOT NULL DEFAULT (DATETIME('now')), -- maintained automatically
		"NamerID" INTEGER NOT NULL,
		"Scribe" VARCHAR(20), -- optional, if someone besides the Namer is entering data
		"RefLocID" INTEGER, -- can not be required for valid record, but app will keep bugging user to get this
		"RefLocIsGood" BOOL NOT NULL DEFAULT 0, -- allow to accept as good, even if accuracy is poor
		"Azimuth" INTEGER -- if it applies to this plot type
		CHECK ((Azimuth IS NULL) OR ((CAST(Azimuth AS INTEGER) == Azimuth) 
		AND (Azimuth >= 0) AND (Azimuth <= 360))), 
		"VisitNotes" VARCHAR(255), -- limit the length; some users would write a thesis
		"DeviceType" INTEGER DEFAULT 1, -- 1=Unknown, 2=Android
		"DeviceID" VARCHAR(20) NOT NULL,

		"DeviceID" should be a unique identifier for the device this Visit is entered on.
		It would be either from TelephonyManager.getDeviceId() (IMEI, MEID, ESN, etc.), or 
		else ANDROID_ID.
		ESNs are either 11-digit decimal numbers or 8-digit hexadecimal numbers
		MEIDs are 56 bits long, the same length as the IMEI
		MEID allows hexadecimal digits while IMEI allows only decimal digits
		IMEI length 17
		MEID length 14
		ANDROID_ID is a 64-bit number as a hex string, therefore length 16
		Use 20 to be sure the field is long enough.

		*/
			
			mUri = rs.insert(mBaseUri, mValues);
			Log.v(LOG_TAG, "new record in saveVisitRecord; returned URI: " + mUri.toString());
			mVisitId = Long.parseLong(mUri.getLastPathSegment());
			mUri = ContentUris.withAppendedId(mBaseUri, mVisitId);
			Log.v(LOG_TAG, "new record in saveVisitRecord; URI re-parsed: " + mUri.toString());
			// set default project; redundant with fn in NewVisitFragment; low priority fix
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.CURRENT_VISIT_ID, mVisitId);
			prefEditor.commit();
			return 1;
		} else { // update the existing record
			mValues.put("LastChanged", mTimeFormat.format(new Date())); // update the last-changed time
			int numUpdated = rs.update(mUri, mValues, null, null);
			Log.v(LOG_TAG, "Saved record in saveVisitRecord; numUpdated: " + numUpdated);
			return numUpdated;
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
		if (parent.getId() == mNamerSpinner.getId()) {
			if(((String)parent.getTag()).equalsIgnoreCase(TAG_SPINNER_FIRST_USE)) {
	            parent.setTag("");
	            return;
	        }
			mNamerId = id;
			if (mNamerId == 0) { // picked '(add new)'
				Log.v(LOG_TAG, "Starting 'add new' for Namer from onItemSelect");
				AddSpeciesNamerDialog  addSppNamerDlg = AddSpeciesNamerDialog.newInstance();
				FragmentManager fm = getActivity().getSupportFragmentManager();
				addSppNamerDlg.show(fm, "");

			}
			if (mNamerId != 0) {
				// save in app Preferences as the default Project
				saveDefaultNamerId(mNamerId);
			}
		}
		// write code for any other spinner(s) here
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.sel_spp_namer_spinner:
			if (hasFocus) {
				Log.v(LOG_TAG, "Starting 'add new' for Namer from onFocusChange");
				AddSpeciesNamerDialog  addSppNamerDlg = AddSpeciesNamerDialog.newInstance();
				FragmentManager fm = getActivity().getSupportFragmentManager();
				addSppNamerDlg.show(fm, "");
			}
			

			break;
//		case R.id.txt_descr:
//			values.put("Description", mDescription.getText().toString().trim());
//			break;
		}
	}
	// create context menus
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, 
	   ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		switch (v.getId()) {
		case R.id.txt_visit_name:
			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		case R.id.sel_spp_namer_spinner:
			menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, "Edit");
			menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");
			break;
		case R.id.lbl_spp_namer_spinner_cover:
			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		case R.id.txt_visit_scribe:
			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		case R.id.txt_visit_location:
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_visit_header_location, menu);
//			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		case R.id.txt_visit_azimuth:
			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		case R.id.txt_visit_notes:
			menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
			break;
		}
	}

	// This is executed when the user selects an option
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	if (info == null) {
		Log.v(LOG_TAG, "onContextItemSelected info is null");
	} else {
		Log.v(LOG_TAG, "onContextItemSelected info: " + info.toString());
	}
	UnderConstrDialog msgDlg = new UnderConstrDialog();
	switch (item.getItemId()) {
	case R.id.vis_hdr_loc_restore_prev:
		Log.v(LOG_TAG, "'Restore Previous' selected");
		// re-acquire location
		msgDlg.show(getFragmentManager(), null);
		return true;
	case R.id.vis_hdr_loc_reacquire:
		Log.v(LOG_TAG, "'Re-acquire' selected");
		// re-acquire location
		msgDlg.show(getFragmentManager(), null);
		return true;
	case R.id.vis_hdr_loc_accept:
		Log.v(LOG_TAG, "'Accept accuracy' selected");
		// accept location even with poor accuracy
		msgDlg.show(getFragmentManager(), null);
		return true;
	case R.id.vis_hdr_loc_manual:
		Log.v(LOG_TAG, "'Enter manually' selected");
		// enter location manually
		msgDlg.show(getFragmentManager(), null);
		return true;
	case R.id.vis_hdr_loc_details:
		Log.v(LOG_TAG, "'Details' selected");
		// show location details
		msgDlg.show(getFragmentManager(), null);
		return true;
	case R.id.vis_hdr_loc_help:
		Log.v(LOG_TAG, "'Help' selected");
		// show help on locations
		msgDlg.show(getFragmentManager(), null);
		return true;
	case MENU_EDIT:
		Log.v(LOG_TAG, "MENU_EDIT selected");
//	        mark_item(info.id);
		return true;
	case MENU_DELETE:
	    Log.v(LOG_TAG, "MENU_DELETE selected");
//	        delete_item(info.id);
	    return true;
	case MENU_HELP:
		Log.v(LOG_TAG, "MENU_HELP selected");
		HelpUnderConstrDialog hlpDlg = new HelpUnderConstrDialog();
		hlpDlg.show(getFragmentManager(), null);
		return true;
    default:
    	return super.onContextItemSelected(item);
	   }
	}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(LOG_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
            mViewVisitLocation.setText("Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    // Runs when a GoogleApiClient object successfully connects.
    @Override
    public void onConnected(Bundle connectionHint) {
    	if (!mLocIsGood) {
    		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    	}
    }


    // Called by Google Play services if the connection to GoogleApiClient drops because of an error.

    public void onDisconnected() {
        Log.v(LOG_TAG, "Disconnected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.v(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    // Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
    // documented under FusedLocationProviderApi
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

	private boolean servicesAvailable() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
	
	    if (ConnectionResult.SUCCESS == resultCode) {
	        return true;
	    }
	    else {
	        GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0).show();
	        return false;
	    }
	}

	@Override
	public void onLocationChanged(Location newLoc) {
		handleLocation(newLoc);
	}
	
	public void handleLocation(Location loc) {
		String s;
		mLatitude = loc.getLatitude();
		mLongitude = loc.getLongitude();
		mAccuracy = loc.getAccuracy();
		s = "" + mLatitude + ", " + mLongitude
				+ "\naccuracy " + mAccuracy + "m"
				+ "\ntarget accuracy " + mAccuracyTargetForVisitLoc + "m"
				+ "\ncontinuing to acquire";
		mViewVisitLocation.setText(s);
		if (mAccuracy <= mAccuracyTargetForVisitLoc) {
			mLocIsGood = true;
			if (mGoogleApiClient.isConnected()) {
		        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		        mGoogleApiClient.disconnect();
		        // overwrite the message
				s = "" + mLatitude + ", " + mLongitude
						+ "\naccuracy " + mAccuracy + "m";
				mViewVisitLocation.setText(s);
				// write to database here, or just flag?
		    }
		}
	}
	
	// if Google Play Services not available, would Location Services be?
	// requestSingleUpdate
}