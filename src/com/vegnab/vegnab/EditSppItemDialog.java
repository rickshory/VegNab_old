package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;
import com.vegnab.vegnab.database.VNContract.Tags;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditSppItemDialog extends DialogFragment implements android.view.View.OnClickListener,
		android.widget.AdapterView.OnItemSelectedListener,
		android.view.View.OnFocusChangeListener, LoaderManager.LoaderCallbacks<Cursor>
		//, android.view.View.OnKeyListener
		{
	private static final String LOG_TAG = EditSppItemDialog.class.getSimpleName();
	long mVegItemRecId = 0; // zero default means new or not specified yet
	long mCurVisitRecId = 0;
	int mCurSubplotRecId = -1;
	Uri mUri, mVegItemsUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "vegitems");
	ContentValues mValues = new ContentValues();
	HashMap<Long, String> mExistingVegCodes = new HashMap<Long, String>();
	private TextView mTxtSpeciesItemLabel;
	private EditText mEditSpeciesHeight, mEditSpeciesCover;
	private CheckBox mCkSpeciesIsPresent, mCkDontVerifyPresence;
	private Spinner mSpinnerSpeciesConfidence;
	SimpleCursorAdapter mCFSpinnerAdapter;
	private String mStrVegCode, mStrDescription;
	private Boolean mBoolRecHasChanged = false;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	
	static EditSppItemDialog newInstance(long vegItemRecId, long curVisitRecId, int curSubplotRecId) {
		EditSppItemDialog f = new EditSppItemDialog();
		// supply vegItemRecId as an argument
		Bundle args = new Bundle();
		args.putLong("vegItemRecId", vegItemRecId);
		args.putLong("curVisitRecId", curVisitRecId);
		args.putInt("curSubplotRecId", curSubplotRecId);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_spp_item, root);

		mTxtSpeciesItemLabel = (TextView) view.findViewById(R.id.lbl_spp_item);
		mEditSpeciesHeight = (EditText) view.findViewById(R.id.txt_spp_height);
		mEditSpeciesCover = (EditText) view.findViewById(R.id.txt_spp_cover);
		mCkSpeciesIsPresent = (CheckBox) view.findViewById(R.id.ck_spp_present);
		mCkDontVerifyPresence = (CheckBox) view.findViewById(R.id.ck_spp_present_do_not_ask);
		mSpinnerSpeciesConfidence = (Spinner) view.findViewById(R.id.spinner_spp_confidence);
		mSpinnerSpeciesConfidence.setTag(Tags.SPINNER_FIRST_USE); // flag to catch and ignore erroneous first firing
		mSpinnerSpeciesConfidence.setEnabled(false); // will enable when data ready
		mCFSpinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null,
				new String[] {"IdLevelDescr"},
				new int[] {android.R.id.text1}, 0);
		mCFSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerSpeciesConfidence.setAdapter(mCFSpinnerAdapter);
		mSpinnerSpeciesConfidence.setOnItemSelectedListener(this);
		
		mEditSpeciesHeight.setOnFocusChangeListener(this);
		mEditSpeciesCover.setOnFocusChangeListener(this);
		mCkSpeciesIsPresent.setOnFocusChangeListener(this);
		mCkDontVerifyPresence.setOnFocusChangeListener(this);
		
		// enable long-press
		registerForContextMenu(mSpinnerSpeciesConfidence); 
		registerForContextMenu(mEditSpeciesHeight);
		registerForContextMenu(mEditSpeciesCover);
		registerForContextMenu(mCkSpeciesIsPresent);
		registerForContextMenu(mCkDontVerifyPresence);
		
		getDialog().setTitle(R.string.edit_spp_item_title_add); // usually adding, will change to 'edit' if not
		return view;
	}

	@Override	
	public void onClick(View v) {
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			mVegItemRecId = args.getLong("mVegItemRecId");
			mCurVisitRecId = args.getLong("curVisitRecId");
			mCurSubplotRecId = args.getInt("curSubplotRecId");
			
			// request existing species codes ASAP, this doesn't use the UI
			getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT_VEGITEMS, null, this);
			// get these to have ready, and to adjust screen if needed
			getLoaderManager().initLoader(Loaders.VEG_ITEM_CONFIDENCE_LEVELS, null, this);
			getLoaderManager().initLoader(Loaders.VEG_ITEM_SUBPLOT, null, this);
			getLoaderManager().initLoader(Loaders.VEG_ITEM_VISIT, null, this);
			getLoaderManager().initLoader(Loaders.VEGITEM_TO_EDIT, null, this);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) { // something lost focus
			mValues.clear();
			switch (v.getId()) {
			case R.id.txt_projcode:
				mValues.put("ProjCode", mVegCode.getText().toString().trim());
				break;
			case R.id.txt_descr:
				mValues.put("Description", mDescription.getText().toString().trim());
				break;
			case R.id.txt_context:
				mValues.put("Context", mContext.getText().toString().trim());
				break;
			case R.id.txt_caveats:
				mValues.put("Caveats", mCaveats.getText().toString().trim());
				break;
			case R.id.txt_person:
				mValues.put("ContactPerson", mContactPerson.getText().toString().trim());
				break;			
//			case R.id.txt_date_from: // this one is not focusable
//				mValues.put("StartDate", mStartDate.getText().toString().trim());
//				break;
//			case R.id.txt_date_to: // this one is not focusable
//				mValues.put("EndDate", mEndDate.getText().toString().trim());
//				break;
			default: // save everything
				mValues.put("ProjCode", mVegCode.getText().toString().trim());
				mValues.put("Description", mDescription.getText().toString().trim());
				mValues.put("Context", mContext.getText().toString().trim());
				mValues.put("Caveats", mCaveats.getText().toString().trim());
				mValues.put("ContactPerson", mContactPerson.getText().toString().trim());
				mValues.put("StartDate", mStartDate.getText().toString().trim());
				mValues.put("EndDate", mEndDate.getText().toString().trim());
				}
			if (!mValues.containsKey("ProjCode")) { // assure contains required field
				mValues.put("ProjCode", mVegCode.getText().toString().trim());
			}
			Log.v(LOG_TAG, "Saving record in onFocusChange; mValues: " + mValues.toString().trim());
			int numUpdated = saveProjRecord();
			}		
		}


	@Override
	public void onCancel (DialogInterface dialog) {
		// update the project record in the database, if everything valid		
		mValues.clear();
		mValues.put("ProjCode", mVegCode.getText().toString().trim());
		mValues.put("Description", mDescription.getText().toString().trim());
		mValues.put("Context", mContext.getText().toString().trim());
		mValues.put("Caveats", mCaveats.getText().toString().trim());
		mValues.put("ContactPerson", mContactPerson.getText().toString().trim());
		mValues.put("StartDate", mStartDate.getText().toString().trim());
		mValues.put("EndDate", mEndDate.getText().toString().trim());
		Log.v(LOG_TAG, "Saving record in onCancel; mValues: " + mValues.toString());
		int numUpdated = saveProjRecord();
	}
	
	private int saveProjRecord () {
		Context c = getActivity();
		// test field for validity
		String projCodeString = mValues.getAsString("ProjCode");
		if (projCodeString.length() == 0) {
			Toast.makeText(this.getActivity(),
					c.getResources().getString(R.string.edit_proj_msg_no_proj),
					Toast.LENGTH_LONG).show();
			return 0;
		}
		if (!(projCodeString.length() >= 2)) {
			Toast.makeText(this.getActivity(),
					c.getResources().getString(R.string.err_need_2_chars),
					Toast.LENGTH_LONG).show();
			return 0;
		}
		if (mExistingVegCodes.containsValue(projCodeString)) {
			Toast.makeText(this.getActivity(),
					c.getResources().getString(R.string.edit_proj_msg_dup_proj) + " \"" + projCodeString + "\"" ,
					Toast.LENGTH_LONG).show();
			Log.v(LOG_TAG, "in saveProjRecord, canceled because of duplicate ProjCode; mVegItemRecId = " + mVegItemRecId);
			return 0;
		}
		ContentResolver rs = c.getContentResolver();
		if (mVegItemRecId == -1) {
			Log.v(LOG_TAG, "entered saveProjRecord with (mVegItemRecId == -1); canceled");
			return 0;
		}
		if (mVegItemRecId == 0) { // new record
			mUri = rs.insert(mVegItemsUri, mValues);
			Log.v(LOG_TAG, "new record in saveProjRecord; returned URI: " + mUri.toString());
			long newRecId = Long.parseLong(mUri.getLastPathSegment());
			if (newRecId < 1) { // returns -1 on error, e.g. if not valid to save because of missing required field
				Log.v(LOG_TAG, "new record in saveProjRecord has Id == " + newRecId + "); canceled");
				return 0;
			}
			mVegItemRecId = newRecId;
			getLoaderManager().restartLoader(Loaders.EXISTING_PROJCODES, null, this);
			mUri = ContentUris.withAppendedId(mVegItemsUri, mVegItemRecId);
			Log.v(LOG_TAG, "new record in saveProjRecord; URI re-parsed: " + mUri.toString());
			// set default project; redundant with fn in NewVisitFragment; low priority fix
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.DEFAULT_PROJECT_ID, mVegItemRecId);
			prefEditor.commit();
			return 1;
		} else {
			mUri = ContentUris.withAppendedId(mVegItemsUri, mVegItemRecId);
			int numUpdated = rs.update(mUri, mValues, null, null);
			Log.v(LOG_TAG, "Saved record in saveProjRecord; numUpdated: " + numUpdated);
			return numUpdated;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case Loaders.VEGITEM_TO_EDIT:
			// First, create the base URI
			// could test here, based on e.g. filters
//			mVegItemsUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			Uri oneProjUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "vegitems"), mVegItemRecId);
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), oneProjUri,
					null, select, null, null);
			break;

		case Loaders.CURRENT_SUBPLOT_VEGITEMS:
			// get the existing ProjCodes, other than the current one, to disallow duplicates
			Uri allProjsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "vegitems");
			String[] projection = {"_id", "ProjCode"};
			select = "(_id <> " + mVegItemRecId + " AND IsDeleted = 0)";
			cl = new CursorLoader(getActivity(), allProjsUri,
					projection, select, null, null);
			break;

		case Loaders.VEG_ITEM_CONFIDENCE_LEVELS:
			// First, create the base URI
			// could test here, based on e.g. filters
//			mVegItemsUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			Uri oneProjUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "vegitems"), mVegItemRecId);
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), oneProjUri,
					null, select, null, null);
			break;

		case Loaders.VEG_ITEM_SUBPLOT:
			// First, create the base URI
			// could test here, based on e.g. filters
//			mVegItemsUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			Uri oneProjUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "vegitems"), mVegItemRecId);
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), oneProjUri,
					null, select, null, null);
			break;

		case Loaders.VEG_ITEM_VISIT:
			// get the existing ProjCodes, other than the current one, to disallow duplicates
			Uri allProjsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "vegitems");
			String[] projection = {"_id", "ProjCode"};
			select = "(_id <> " + mVegItemRecId + " AND IsDeleted = 0)";
			cl = new CursorLoader(getActivity(), allProjsUri,
					projection, select, null, null);
			break;

			
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
			
		case Loaders.VEGITEM_TO_EDIT:
			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				mVegCode.setText(c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mDescription.setText(c.getString(c.getColumnIndexOrThrow("Description")));
				mContext.setText(c.getString(c.getColumnIndexOrThrow("Context")));
				mCaveats.setText(c.getString(c.getColumnIndexOrThrow("Caveats")));
				mContactPerson.setText(c.getString(c.getColumnIndexOrThrow("ContactPerson")));
				mStartDate.setText(c.getString(c.getColumnIndexOrThrow("StartDate")));
				mEndDate.setText(c.getString(c.getColumnIndexOrThrow("EndDate")));
			}
			break;
			
		case Loaders.CURRENT_SUBPLOT_VEGITEMS:
			mExistingVegCodes.clear();
			while (c.moveToNext()) {
				Log.v(LOG_TAG, "onLoadFinished, add to HashMap: " + c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mExistingVegCodes.put(c.getLong(c.getColumnIndexOrThrow("_id")), 
						c.getString(c.getColumnIndexOrThrow("ProjCode")));
			}
			Log.v(LOG_TAG, "onLoadFinished, number of items in mExistingVegCodes: " + mExistingVegCodes.size());
			Log.v(LOG_TAG, "onLoadFinished, items in mExistingVegCodes: " + mExistingVegCodes.toString());
			break;

		case Loaders.VEG_ITEM_CONFIDENCE_LEVELS:
			mExistingVegCodes.clear();
			while (c.moveToNext()) {
				Log.v(LOG_TAG, "onLoadFinished, add to HashMap: " + c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mExistingVegCodes.put(c.getLong(c.getColumnIndexOrThrow("_id")), 
						c.getString(c.getColumnIndexOrThrow("ProjCode")));
			}
			Log.v(LOG_TAG, "onLoadFinished, number of items in mExistingVegCodes: " + mExistingVegCodes.size());
			Log.v(LOG_TAG, "onLoadFinished, items in mExistingVegCodes: " + mExistingVegCodes.toString());
			break;
			
		case Loaders.VEG_ITEM_SUBPLOT:
			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				mVegCode.setText(c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mDescription.setText(c.getString(c.getColumnIndexOrThrow("Description")));
				mContext.setText(c.getString(c.getColumnIndexOrThrow("Context")));
				mCaveats.setText(c.getString(c.getColumnIndexOrThrow("Caveats")));
				mContactPerson.setText(c.getString(c.getColumnIndexOrThrow("ContactPerson")));
				mStartDate.setText(c.getString(c.getColumnIndexOrThrow("StartDate")));
				mEndDate.setText(c.getString(c.getColumnIndexOrThrow("EndDate")));
			}
			break;
		case Loaders.VEG_ITEM_VISIT:
			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				mVegCode.setText(c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mDescription.setText(c.getString(c.getColumnIndexOrThrow("Description")));
				mContext.setText(c.getString(c.getColumnIndexOrThrow("Context")));
				mCaveats.setText(c.getString(c.getColumnIndexOrThrow("Caveats")));
				mContactPerson.setText(c.getString(c.getColumnIndexOrThrow("ContactPerson")));
				mStartDate.setText(c.getString(c.getColumnIndexOrThrow("StartDate")));
				mEndDate.setText(c.getString(c.getColumnIndexOrThrow("EndDate")));
			}
			break;

		}	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case Loaders.VEGITEM_TO_EDIT:
			// nothing to do here since no adapter
			break;
		case Loaders.CURRENT_SUBPLOT_VEGITEMS:
			// nothing to do here since no adapter
			break;
		case Loaders.VEG_ITEM_CONFIDENCE_LEVELS:
			mCFSpinnerAdapter.swapCursor(null);
			break;
		case Loaders.VEG_ITEM_SUBPLOT:
			// nothing to do here since no adapter
			break;
		case Loaders.VEG_ITEM_VISIT:
			// nothing to do here since no adapter
			break;
		}
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
