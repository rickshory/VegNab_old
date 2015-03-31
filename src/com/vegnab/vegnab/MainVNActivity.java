package com.vegnab.vegnab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.vegnab.vegnab.database.VNContract.Prefs;
import com.vegnab.vegnab.database.VNContract.Tags;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Build;

public class MainVNActivity extends ActionBarActivity 
		implements NewVisitFragment.OnButtonListener, 
		NewVisitFragment.OnVisitClickListener,
		VisitHeaderFragment.OnButtonListener, 
		VisitHeaderFragment.EditVisitDialogListener,
		VegSubplotFragment.OnButtonListener,
		EditNamerDialog.EditNamerDialogListener,
		ConfirmDelNamerDialog.EditNamerDialogListener,
		SelectSpeciesFragment.OnSppResultClickListener,
		EditSppItemDialog.EditSppItemDialogListener {
	
	private static final String LOG_TAG = MainVNActivity.class.getSimpleName();
	static String mUniqueDeviceId, mDeviceIdSource;
	long mRowCt, mVisitId = 0, mSubplotTypeId = 0;
	
	final static String ARG_SUBPLOT_TYPE_ID = "subplotTypeId";
	final static String ARG_VISIT_ID = "visitId";
	
	ViewPager viewPager = null;

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
		// user will probably turn this one off each session, but turn it on on each restart
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putBoolean(Prefs.VERIFY_VEG_ITEMS_PRESENCE, true);
		prefEditor.commit();
		
		setContentView(R.layout.activity_vn_main);
//		viewPager = (ViewPager) findViewById(R.id.data_entry_pager);
//		FragmentManager fm = getSupportFragmentManager();
//		viewPager.setAdapter(new dataPagerAdapter(fm));
		
		/* put conditions to test below
		 * such as whether the container even exists in this layout
		 * e.g. if (findViewById(R.id.fragment_container) != null)
		 * */
		if (true) {
			if (savedInstanceState != null) {
				mVisitId = savedInstanceState.getLong(ARG_VISIT_ID);
				mSubplotTypeId = savedInstanceState.getLong(ARG_SUBPLOT_TYPE_ID, 0);
				// if restoring from a previous state, do not create view
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

/* comment out onBackPressed()
	@Override
	public void onBackPressed() {
		Log.v(LOG_TAG, "Caught 'onBackPressed'");
		FragmentManager fm = getSupportFragmentManager();
		try {
			// try to pop the data screens container fragment
			if (fm.popBackStackImmediate (Tags.DATA_SCREENS_CONTAINER, FragmentManager.POP_BACK_STACK_INCLUSIVE)) {
				Log.v(LOG_TAG, "DATA_SCREENS_CONTAINER fragment popped from backstack");
//				wasRemoved = true;
			}
		} catch (Exception e) {
			Log.v(LOG_TAG, "stack pop exception: " + e.getMessage());
		}

//		Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//		if (currentFragment.getTag() == Tags.DATA_SCREENS_CONTAINER) {
//			finish();
//			return;
//		}
		super.onBackPressed();
	return;
	}
*/
	
	

	public void onVisitHeaderGoButtonClicked(long visitId) {
		mVisitId = visitId;
		// swap DataEntryContainerFragment in place of existing fragment
		Log.e(LOG_TAG, "About to go to DataEntryContainer");
		FragmentManager fm = getSupportFragmentManager();
		Bundle args = new Bundle();
		Log.e(LOG_TAG, "In onVisitHeaderGoButtonClicked, about to putLong mVisitId=" +  mVisitId);
		args.putLong(DataEntryContainerFragment.VISIT_ID, mVisitId);
		DataEntryContainerFragment dataEntryFrag = DataEntryContainerFragment.newInstance(args);
		FragmentTransaction transaction = fm.beginTransaction();
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, dataEntryFrag, Tags.DATA_SCREENS_CONTAINER);
		transaction.addToBackStack(null);
		transaction.commit();
		Log.e(LOG_TAG, "Call to DataEntryContainer complete");

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current subplot arguments in case we need to re-create the activity
		outState.putLong(ARG_SUBPLOT_TYPE_ID, mSubplotTypeId);
		outState.putLong(ARG_VISIT_ID, mVisitId);
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

	@Override
	public void onNewItemButtonClicked(int screenToReturnTo, long visitId,
			long subplotId, boolean presenceOnly) {
		// presenceOnly is not used by Species Select, but passed along to Edit Species
		SelectSpeciesFragment selSppFrag = new SelectSpeciesFragment();
		Bundle args = new Bundle();
		// args available to send to the fragment
		// ARG_VISIT_ID = "visId";
		// ARG_SUBPLOT_TYPE_ID = "sbpId";
		// ARG_SEARCH_TEXT = "search_text";
		// ARG_SQL_TEXT = "sql_text";
		// ARG_USE_REGIONAL_LIST = "regional_list";
		// ARG_USE_FULLTEXT_SEARCH = "fulltext_search";

		// screenTag can serve both as this fn's switch and the tag name of the fragment instance
		// args.putString(SelectSpeciesFragment.ARG_TAG_ID, screenTag);
		
		// provide Visit and Subplot IDs, so selector can check for duplicate codes
		args.putLong(SelectSpeciesFragment.ARG_VISIT_ID, visitId);
		args.putLong(SelectSpeciesFragment.ARG_SUBPLOT_TYPE_ID, subplotId);
		// following not used by Spp Select, but passed along to Edit Spp
		args.putBoolean(SelectSpeciesFragment.ARG_PRESENCE_ONLY_SUBPLOT, presenceOnly); 

		selSppFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		// the tag is for the fragment now being added, not the one replaced
		transaction.replace(R.id.fragment_container, selSppFrag, Tags.SELECT_SPECIES);
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
		mVisitId = visitId;
		goToVisitHeaderScreen(visitId);
	}
	
	@Override
	public void onEditVegItemComplete(DialogFragment dialog) {
		Log.v(LOG_TAG, "onEditSppComplete(DialogFragment dialog)");
		DataEntryContainerFragment dataScreensFrag = (DataEntryContainerFragment)
				getSupportFragmentManager().findFragmentByTag(Tags.DATA_SCREENS_CONTAINER);
		if (dataScreensFrag == null) {
			Log.v(LOG_TAG, "dataScreensFrag == null");
		} else {
			Log.v(LOG_TAG, "dataScreensFrag: " + dataScreensFrag.toString());
			try {
				// Sometimes screen-rotates while EditSppItemDialog is displayed destroy some of
				// the following objects, in which case the app would crash with null pointer exceptions
				// In this case, skip the data screen refresh and allow it to happen when 
				// fragments are re-instantiated
				int index = dataScreensFrag.mDataScreenPager.getCurrentItem();
	//			int index = dataScreensFrag.mScreenToShow;
				DataEntryContainerFragment.dataPagerAdapter adapter = 
						((DataEntryContainerFragment.dataPagerAdapter)dataScreensFrag.mDataScreenPager.getAdapter());
				VegSubplotFragment vegSubpFragment = (VegSubplotFragment) adapter.getFragment(index);
				if (vegSubpFragment == null) {
					Log.v(LOG_TAG, "vegSubpFragment == null");
				} else {
					Log.v(LOG_TAG, "vegSubpFragment: " + vegSubpFragment.toString());
					Log.v(LOG_TAG, "About to do 'refreshSppList' for data page " + index);
					vegSubpFragment.refreshSppList();
					Log.v(LOG_TAG, "Completed 'refreshSppList' for data page " + index);
	//				dataScreensFrag.mDataScreenPager.setCurrentItem(index);
				}
			} catch (Exception e) {
				Log.v(LOG_TAG, "exception: " + e.getMessage());
			} 
		}
		
//		VegSubplotFragment vegSubpFragment = (VegSubplotFragment) 
//				getSupportFragmentManager().findFragmentByTag(Tags.VEG_SUBPLOT);
//		if (vegSubpFragment == null) {
//			Log.v(LOG_TAG, "vegSubpFragment == null");
//		} else {
//			Log.v(LOG_TAG, "vegSubpFragment: " + vegSubpFragment.toString());
//			vegSubpFragment.refreshSppList();
//		}
		Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment.getTag() == Tags.SELECT_SPECIES) {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onSppMatchListClicked(int sourceId, long recId, String sppCode, String sppDescr, 
			String vegGenus, String vegSpecies, String vegSubsppVar, String vegVernacular) {
		Log.v(LOG_TAG, "OnSppResultClickListener, sourceId=" + sourceId + ", recId=" + recId 
				+ ", sppCode: '" + sppCode +"', sppDescr: '" + sppDescr + "'");
		// if from Regional List, save in Found List, change Source and substitute that new record's ID
		// for testing, try to save via dialog
	}	
/*
	@Override
	public void onSelSppDone() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment vegSubpFragment = fm.findFragmentByTag(Tags.VEG_SUBPLOT);
		if (vegSubpFragment == null) {
			Log.v(LOG_TAG, "vegSubpFragment == null");
		} else {
			
			FragmentTransaction transaction = fm.beginTransaction();
			// replace the fragment in the fragment container with the stored Veg Subplot fragment
			transaction.replace(R.id.fragment_container, vegSubpFragment);
			// spp search starts fresh each time, so do not put the present fragment on the backstack
			transaction.commit();
		}
	}	
	*/
/*	
		@Override
	public void onEditSppComplete(VisitHeaderFragment visitHeaderFragment) {
		Log.v(LOG_TAG, "onEditVisitComplete(VisitHeaderFragment visitHeaderFragment)");
		NewVisitFragment newVisFragment = (NewVisitFragment) 
				getSupportFragmentManager().findFragmentByTag(Tags.NEW_VISIT);
		newVisFragment.refreshVisitsList();
	}

*/	
	
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
		ConfigurableMsgDialog flexErrDlg = new ConfigurableMsgDialog();
		try {
			copyFile(from, to);
			Log.v(LOG_TAG, "DB backed up to: " + to.getPath());
			flexErrDlg = ConfigurableMsgDialog.newInstance("DB backed up to: ", to.getPath().toString());
			flexErrDlg.show(getSupportFragmentManager(), "frg_db_copy_ok");
			return true;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error backuping up database: " + e.getMessage(), e);
			flexErrDlg = ConfigurableMsgDialog.newInstance("Error backuping up database: ", e.getMessage().toString());
			flexErrDlg.show(getSupportFragmentManager(), "frg_db_copy_ok");
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

}
