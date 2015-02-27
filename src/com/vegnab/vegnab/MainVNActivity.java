package com.vegnab.vegnab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;
import com.vegnab.vegnab.database.VNContract.Tags;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Build;

public class MainVNActivity extends FragmentActivity 
		implements LoaderManager.LoaderCallbacks<Cursor>, 
		NewVisitFragment.OnButtonListener, 
		NewVisitFragment.OnVisitClickListener,
		VisitHeaderFragment.OnButtonListener, 
		VisitHeaderFragment.EditVisitDialogListener,
		VegSubplotFragment.OnButtonListener,
		EditNamerDialog.EditNamerDialogListener,
		ConfirmDelNamerDialog.EditNamerDialogListener,
		SelectSpeciesFragment.OnSppResultClickListener {
	
	private static final String LOG_TAG = MainVNActivity.class.getSimpleName();
	static String mUniqueDeviceId, mDeviceIdSource;
	long mSubplotNum, mRowCt, mNumSubplots;
	boolean mDispatcherStructNewlySetUp = false;
	private ArrayList<Long> mSubPlotNumbersList = new ArrayList();
	//  maybe use JSONArray to include aux data screens?
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set up some default Preferences
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		if (!sharedPref.contains(Prefs.TARGET_ACCURACY_OF_VISIT_LOCATIONS)) {
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putFloat(Prefs.TARGET_ACCURACY_OF_VISIT_LOCATIONS, (float) 7.0);
			prefEditor.commit();
		}
		if (!sharedPref.contains(Prefs.TARGET_ACCURACY_OF_MAPPED_LOCATIONS)) {
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putFloat(Prefs.TARGET_ACCURACY_OF_MAPPED_LOCATIONS, (float) 7.0);
			prefEditor.commit();
		}
		if (!sharedPref.contains(Prefs.UNIQUE_DEVICE_ID)) {
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			getUniqueDeviceId(this); // generate the ID and the source
			prefEditor.putString(Prefs.DEVICE_ID_SOURCE, mDeviceIdSource);
			prefEditor.putString(Prefs.UNIQUE_DEVICE_ID, mUniqueDeviceId);
			prefEditor.commit();
		}
		
		//getDeviceId
		setContentView(R.layout.activity_vn_main);
		/* put conditions to test below
		 * such as whether the container even exists in this layout
		 * e.g. if (findViewById(R.id.fragment_container) != null)
		 * */
		if (true) {
			if (savedInstanceState != null) {
				// if restoring from a previous state, do not create
				// could end up with overlapping views
				return;
			}
			
			// create an instance of New Visit fragment
			NewVisitFragment newVisitFrag = new NewVisitFragment();
			
			// in case this activity were started with special instructions from an Intent
			// pass the Intent's Extras to the fragment as arguments
			newVisitFrag.setArguments(getIntent().getExtras());
			
			// the tag is for the fragment now being added
			// it will stay with this fragment when put on the backstack
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();			
			transaction.add(R.id.fragment_container, newVisitFrag, Tags.NEW_VISIT);
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vn_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		FragmentManager fm = getSupportFragmentManager();
		DialogFragment editProjDlg;
		switch (item.getItemId()) { // some of these are from Fragments, but handled here in the Activity
		case R.id.action_app_info:
			Toast.makeText(getApplicationContext(), "''App Info'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_edit_proj:
//			EditProjectDialog editProjDlg = new EditProjectDialog();
			/*
			fm.executePendingTransactions(); // assure all are done
			NewVisitFragment newVis = (NewVisitFragment) fm.findFragmentByTag("NewVisitScreen");
			if (newVis == null) {
				Toast.makeText(getApplicationContext(), "Can't get New Visit Screen fragment", Toast.LENGTH_SHORT).show();
				return true;
			}
			// wait, we don't need to regenerate the default project Id, it's stored in Preferences*/
			SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
			long defaultProjId = sharedPref.getLong(Prefs.DEFAULT_PROJECT_ID, 1);
			editProjDlg = EditProjectDialog.newInstance(defaultProjId);
			editProjDlg.show(fm, "frg_edit_proj");
			return true;
		case R.id.action_new_proj:
			editProjDlg = EditProjectDialog.newInstance(0);
			editProjDlg.show(fm, "frg_new_proj");
			return true;
		case R.id.action_del_proj:
			DelProjectDialog delProjDlg = new DelProjectDialog();
			delProjDlg.show(fm, "frg_del_proj");
			return true;
		case R.id.action_new_plottype:
//			Toast.makeText(getApplicationContext(), "''New Plot Type'' is still under construction", Toast.LENGTH_SHORT).show();
			showWebViewScreen(Tags.WEBVIEW_PLOT_TYPES);
			/*		public static final String WEBVIEW_TUTORIAL = "WebviewTutorial";
			public static final String WEBVIEW_PLOT_TYPES = "WebviewPlotTypes";
			public static final String WEBVIEW_REGIONAL_LISTS = "WebviewSppLists";
	*/
			return true;
		case R.id.action_get_species:
//			Toast toast = Toast.makeText(getApplicationContext(),
//					"''Get species'' is not implemented yet", Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.TOP, 25, 400);
//			toast.show();
			TestDownloadFragment tstDnLdFrag = new TestDownloadFragment();
//			Bundle args = new Bundle();
			// screenTag serves both as this fn's switch and the tag name of the fragment instance
//			args.putString(ConfigurableWebviewFragment.ARG_TAG_ID, screenTag);
//			tstDnLdFrag.setArguments(args);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			// replace the fragment in the fragment container with this new fragment and
			// put the present fragment on the backstack so the user can navigate back to it
			// the tag is for the fragment now being added, not the one replaced
			transaction.replace(R.id.fragment_container, tstDnLdFrag, "frg_tst_download");
//			transaction.replace(R.id.fragment_container, webVwFrag, screenTag);
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
			
		case R.id.action_export_db:
			exportDB();
			return true;
			
		case R.id.action_unhide_visits:
			Toast.makeText(getApplicationContext(), "''Un-hide Visits'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
			
		case R.id.action_settings:
			Toast.makeText(getApplicationContext(), "''Settings'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

/*	
	@Override
	public void onBackPressed() {
		Log.v("Main", "In MainActivity, caught 'onBackPressed'");
		super.onBackPressed();
	return;
	}
*/	
	public void onNextSubplotButtonClicked(int subpNum) {
		Toast.makeText(getApplicationContext(), "Received value " + subpNum + ", going to next", Toast.LENGTH_SHORT).show();
		// swap new Subplot frag in place of existing one
		// can we use the same name as existing?
		VegSubplotFragment vegSbpFrag = new VegSubplotFragment();
		Bundle args = new Bundle();
		args.putInt(VegSubplotFragment.ARG_SUBPLOT, subpNum + 1); // increment subplot number
		vegSbpFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the existing Subplot fragment (in the container) with this new Subplot fragment
		// and put the existing Subplot fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, vegSbpFrag, "Subplot " + subpNum);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void onVisitHeaderGoButtonClicked() {
		Log.e(LOG_TAG, "About to call LoaderManager.initLoader CURRENT_SUBPLOTS");
		getSupportLoaderManager().initLoader(Loaders.CURRENT_SUBPLOTS, null, this);
		Log.e(LOG_TAG, "Called LoaderManager.initLoader CURRENT_SUBPLOTS");
//		goToSubplotScreen();

//		Toast.makeText(getApplicationContext(), 
//				"Testing Species Select screen", 
//				Toast.LENGTH_LONG).show();
//		showSppSelectScreen();
	}
	
	public void dispatchDataEntryScreen() {
		// for testing, just go to the first veg subplot screen
		// ultimately will dispatch to relevent veg or aux screen
		// mSubPlotNumbersList
		mNumSubplots = mSubPlotNumbersList.size();
		// test for no subplots?
		mSubplotNum = mSubPlotNumbersList.get(0);
		Log.v(LOG_TAG, "About to call 'goToSubplotScreen', mSubplotNum=" + mSubplotNum);
		goToSubplotScreen();
		Log.v(LOG_TAG, "Called 'goToSubplotScreen', mSubplotNum=" + mSubplotNum);
		goToSubplotScreen();
	}
	
	@Override
	public void onNewVisitGoButtonClicked() {
		goToVisitHeaderScreen(0);
	}
	
	public void goToVisitHeaderScreen(long visitID) {
		VisitHeaderFragment visHdrFrag = new VisitHeaderFragment();
		Bundle args = new Bundle();
		// visitID = 0 means new visit, not assigned or created yet
		args.putLong(VisitHeaderFragment.ARG_VISIT_ID, visitID);
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putLong(Prefs.CURRENT_VISIT_ID, visitID);
		prefEditor.commit();
		args.putInt(VisitHeaderFragment.ARG_SUBPLOT, 0); // start with dummy value, subplot 0
		visHdrFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, visHdrFrag, Tags.VISIT_HEADER);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void goToSubplotScreen() {
		// swap Subplot fragment in place of existing fragment
		FragmentManager fm = getSupportFragmentManager();
		VegSubplotFragment vegSbpFrag = new VegSubplotFragment();
		Bundle args = new Bundle();
		args.putLong(VegSubplotFragment.ARG_SUBPLOT, mSubplotNum);
		vegSbpFrag.setArguments(args);
		FragmentTransaction transaction = fm.beginTransaction();
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, vegSbpFrag, Tags.VEG_SUBPLOT);
		transaction.addToBackStack(null);
		transaction.commit();
		// only allow one Subplot fragment on the the backstack, multiple could interfere with each other's species lists
		boolean wasRemoved = false;
		try {
			// this pops all fragments back to and including the last veg subplot
			if (fm.popBackStackImmediate (Tags.VEG_SUBPLOT, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
				wasRemoved = true;
			}
		} catch (Exception e) {
			Log.v(LOG_TAG, "stack pop exception: " + e.getMessage());
		}
	}
	
	public void showWebViewScreen(String screenTag) {
		ConfigurableWebviewFragment webVwFrag = new ConfigurableWebviewFragment();
		Bundle args = new Bundle();
		// screenTag serves both as this fn's switch and the tag name of the fragment instance
		args.putString(ConfigurableWebviewFragment.ARG_TAG_ID, screenTag);
		webVwFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, webVwFrag, screenTag);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void showSppSelectScreen() {
		SelectSpeciesFragment selSppFrag = new SelectSpeciesFragment();
		Bundle args = new Bundle();
		// don't presently need to send anything to the fragment
		// screenTag can serve both as this fn's switch and the tag name of the fragment instance
		// args.putString(SelectSpeciesFragment.ARG_TAG_ID, screenTag);
		selSppFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, selSppFrag, "frg_sel_spp");
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void getUniqueDeviceId(Context context) {
		// this is used to get a unique identifier for the device this app is being run on
		// it is primarily used to warn the user if the Visit has been downloaded onto
		// a different device and the Visit is about to be edited; 
		// This simple fn is not entirely robust for various reasons, but it is adequate since
		// it is rare for Visits to be edited, and even more rare to be downloaded before editing
		// this ID may be useful in sorting out field work chaos, to tell where the data came from
	    String deviceId;
	    try {
	    	TelephonyManager tMgr = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
	    	deviceId = tMgr.getDeviceId();
		    if (deviceId != null) { // won't have this if device is not a phone, and
		    						//not always reliable to read even if it is a phone
		    	mDeviceIdSource = "Phone";
		    	mUniqueDeviceId = deviceId;
		        return;
		    } else { // try to get the Android ID
		    	deviceId = android.os.Build.SERIAL; // generated on first boot, so may change on system reset
		    	// only guaranteed available from API 9 and up
		    	// since Gingerbread (Android 2.3) android.os.Build.SERIAL must exist on any device that doesn't provide IMEI
		    	if (deviceId != null) {
		    		// some Froyo 2.2 builds give the same serial number "9774d56d682e549c" for
		    		// all, but these are rare and dying out (fixed ~December 2010.
		    		// 4.2+, different profiles on the same device may give different IDs
			    	mDeviceIdSource = "Android serial number";
			    	mUniqueDeviceId = deviceId;
		    		return;
		    	} else { 
		    		// generate a random number
			    	mDeviceIdSource = "random UUID";
			    	mUniqueDeviceId = UUID.randomUUID().toString();
		    		return;
		    	}
		    }	
	    } catch (Exception e) {
    		// generate a random number
	    	mDeviceIdSource = "random UUID";
	    	mUniqueDeviceId = UUID.randomUUID().toString();
    		return;	    	
	    }
	}

	@Override
	public void onEditNamerComplete(DialogFragment dialog) {
		Log.v(LOG_TAG, "onEditNamerComplete(DialogFragment dialog)");
		VisitHeaderFragment visHdrFragment = (VisitHeaderFragment) 
				getSupportFragmentManager().findFragmentByTag(Tags.VISIT_HEADER);
		visHdrFragment.refreshNamerSpinner();				
	}

	@Override
	public void onEditVisitComplete(VisitHeaderFragment visitHeaderFragment) {
		Log.v(LOG_TAG, "onEditVisitComplete(VisitHeaderFragment visitHeaderFragment)");
		NewVisitFragment newVisFragment = (NewVisitFragment) 
				getSupportFragmentManager().findFragmentByTag(Tags.NEW_VISIT);
		newVisFragment.refreshVisitsList();
	}

	@Override
	public void onExistingVisitListClicked(long visitId) {
		goToVisitHeaderScreen(visitId);
	}

	@Override
	public void onSppMatchListClicked(int sourceId, long recId) {
		Log.v(LOG_TAG, "OnSppResultClickListener, sourceId=" + sourceId + ", recId=" + recId);
	}	
	
	private static final String DATABASE_NAME = "VegNab.db";

	public File getBackupDatabaseFile() {
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-", Locale.US);
		String uniqueTime = timeFormat.format(new Date()).toString();
		Log.v(LOG_TAG, "uniqueTime: " + uniqueTime);
		String dbBkupName = uniqueTime + DATABASE_NAME;
		Log.v(LOG_TAG, "dbBkupName: " + dbBkupName);

		File sdCard = Environment.getExternalStorageDirectory();
		// create the folder
		File vnFolder = new File(sdCard.getAbsolutePath() + "/VegNab");
		vnFolder.mkdirs();
		Log.v(LOG_TAG, "folder created 'VegNab'");
		File backupDB = new File(vnFolder, dbBkupName);

		return backupDB;
	}
	public final boolean exportDB() {
		File from = getApplicationContext().getDatabasePath(DATABASE_NAME);
		File to = this.getBackupDatabaseFile();
		try {
			copyFile(from, to);
			Log.v(LOG_TAG, "DB backed up to: " + to.getPath());
			return true;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error backuping up database: " + e.getMessage(), e);
		}
		return false;
	}

	public void copyFile(File src, File dst) throws IOException {
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst);
		FileChannel fromChannel = null, toChannel = null;
		try {
			fromChannel = in.getChannel();
			toChannel = out.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel); 
		} finally {
			if (fromChannel != null) 
				fromChannel.close();
			if (toChannel != null) 
				toChannel.close();
		}
		in.close();
		out.close();
		// must do following or file is not visible externally
		MediaScannerConnection.scanFile(getApplicationContext(), new String[] { dst.getAbsolutePath() }, null, null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
//		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {

		case Loaders.CURRENT_SUBPLOTS:
			// current plot type is the default plot type stored in Preferences
			SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
			long plotTypeId = sharedPref.getLong(Prefs.DEFAULT_PLOTTYPE_ID, 0);
			Uri curSbpsUri = ContentUris.withAppendedId(
					Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "subplottypes"), plotTypeId);
			cl = new CursorLoader(this, curSbpsUri, null, select, null, null);
			break;		
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor finishedCursor) {
		// there will be various loaders, switch them out here
		mRowCt = finishedCursor.getCount();
		switch (loader.getId()) {
		case Loaders.CURRENT_SUBPLOTS:
			// store the list of subplots and then go to the first one
			mSubPlotNumbersList.clear();
			while (finishedCursor.moveToNext()) {
				mSubPlotNumbersList.add(finishedCursor.getLong(finishedCursor.getColumnIndexOrThrow("_id")));
			}
			mDispatcherStructNewlySetUp = true;
			// use a callback to continue program flow outside this fn, where direct calls to 
			// 'goToSubplotScreen' are not legal
			Log.v(LOG_TAG, "In 'onLoadFinished', about to create message");
			Message msgSupbNumbersDone = Message.obtain();
			Log.v(LOG_TAG, "In 'onLoadFinished', about to create callback");
			Callback cbkSbsDone = new Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					// execute this during callback
					Log.v(LOG_TAG, "In 'cbkSbsDone' callback, about to call 'goToSubplotScreen'");
					goToSubplotScreen();
					Log.v(LOG_TAG, "In 'cbkSbsDone' callback, after call to 'goToSubplotScreen'");
					return false;
				}
			};
			Log.v(LOG_TAG, "In 'onLoadFinished', about to create handler");
			Handler hnd = new Handler(cbkSbsDone);
			Log.v(LOG_TAG, "In 'onLoadFinished', about to send message");
			hnd.sendMessage(msgSupbNumbersDone);
			Log.v(LOG_TAG, "In 'onLoadFinished', message sent");
			break;
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
		case Loaders.CURRENT_SUBPLOTS:
			// no adapter, nothing to do
			break;
		}
	}


}
