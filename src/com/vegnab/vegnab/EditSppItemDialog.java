package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;
import com.vegnab.vegnab.database.VNContract.Tags;

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
	int mIDConfidence = 1; // default 'no doubt of ID'
	boolean mPresenceOnly = true; // default is that this veg item needs only presence/absence
	Uri mUri, mVegItemsUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "vegitems");
	ContentValues mValues = new ContentValues();
	private TextView mTxtSpeciesItemLabel, mTxtHeightLabel, mTxtCoverLabel;
	private EditText mEditSpeciesHeight, mEditSpeciesCover;
	private CheckBox mCkSpeciesIsPresent, mCkDontVerifyPresence;
	private Spinner mSpinnerSpeciesConfidence;
	SimpleCursorAdapter mCFSpinnerAdapter;
	private String mStrVegCode, mStrDescription;
	private Boolean mBoolRecHasChanged = false;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	
	static EditSppItemDialog newInstance(long vegItemRecId, long curVisitRecId, int curSubplotRecId, 
			boolean presenceOnly) {
		EditSppItemDialog f = new EditSppItemDialog();
		// supply vegItemRecId as an argument
		Bundle args = new Bundle();
		args.putLong("vegItemRecId", vegItemRecId);
		args.putLong("curVisitRecId", curVisitRecId);
		args.putInt("curSubplotRecId", curSubplotRecId);
		args.putBoolean("presenceOnly", presenceOnly);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        try {
//        	mEditSppListener = (EditSppDialogListener) getActivity();
//        	Log.v(LOG_TAG, "(EditSppDialogListener) getActivity()");
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Main Activity must implement EditSppDialogListener interface");
//        }
//	setHasOptionsMenu(true);
	}
	
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.visit_header, menu);
//		super.onCreateOptionsMenu(menu, inflater);
//	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_spp_item, root);

		mTxtSpeciesItemLabel = (TextView) view.findViewById(R.id.lbl_spp_item);
		mTxtHeightLabel = (TextView) view.findViewById(R.id.lbl_spp_height);
		mEditSpeciesHeight = (EditText) view.findViewById(R.id.txt_spp_height);
		mTxtCoverLabel = (TextView) view.findViewById(R.id.lbl_spp_cover);
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
			mPresenceOnly = args.getBoolean("presenceOnly");
		}
		// request existing species codes ASAP, this doesn't use the UI
		getLoaderManager().initLoader(Loaders.CURRENT_SUBPLOT_VEGITEMS, null, this);
		// get these to have ready, and to adjust screen if needed
		getLoaderManager().initLoader(Loaders.VEG_ITEM_CONFIDENCE_LEVELS, null, this);
//		getLoaderManager().initLoader(Loaders.VEG_ITEM_SUBPLOT, null, this);
//		getLoaderManager().initLoader(Loaders.VEG_ITEM_VISIT, null, this);
		getLoaderManager().initLoader(Loaders.VEGITEM_TO_EDIT, null, this);
		
		// adjust UI depending on whether we want Height/Cover information, or only Presence/Absence
		if (mPresenceOnly) { // hide the Height/Cover views
			mTxtHeightLabel.setVisibility(View.GONE);
			mEditSpeciesHeight.setVisibility(View.GONE);
			mTxtCoverLabel.setVisibility(View.GONE);
			mEditSpeciesCover.setVisibility(View.GONE);
		} else { // hide the Presence/Absence views
			mCkSpeciesIsPresent.setVisibility(View.GONE);
			mCkDontVerifyPresence.setVisibility(View.GONE);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		if(!hasFocus) { // something lost focus
			mValues.clear();
			switch (v.getId()) { 
			case R.id.txt_spp_height:
				mValues.put("Height", mEditSpeciesHeight.getText().toString().trim());
				break;
			case R.id.txt_spp_cover:
				mValues.put("Cover", mEditSpeciesCover.getText().toString().trim());
				break;
			case R.id.ck_spp_present:
				mValues.put("Presence", (mCkSpeciesIsPresent.isChecked() ? 1 : 0));
				break;		

			default: // save everything relevant
				if (mPresenceOnly) {
					mValues.put("Height", mEditSpeciesHeight.getText().toString().trim());
					mValues.put("Cover", mEditSpeciesCover.getText().toString().trim());
				} else {
					mValues.put("Presence", (mCkSpeciesIsPresent.isChecked() ? 1 : 0));
				}
			}
		Log.v(LOG_TAG, "Saving record in onFocusChange; mValues: " + mValues.toString().trim());
		int numUpdated = saveVegItemRecord();
		}		
	}
	

	@Override
	public void onCancel (DialogInterface dialog) {
		// update the project record in the database, if everything valid		
		mValues.clear();
		if (mPresenceOnly) {
			mValues.put("Height", mEditSpeciesHeight.getText().toString().trim());
			mValues.put("Cover", mEditSpeciesCover.getText().toString().trim());
		} else {
			mValues.put("Presence", (mCkSpeciesIsPresent.isChecked() ? 1 : 0));
		}
		Log.v(LOG_TAG, "Saving record in onCancel; mValues: " + mValues.toString());
		int numUpdated = saveVegItemRecord();
	}
	
	private int saveVegItemRecord () {
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

		ContentResolver rs = c.getContentResolver();
		if (mVegItemRecId == -1) {
			Log.v(LOG_TAG, "entered saveVegItemRecord with (mVegItemRecId == -1); canceled");
			return 0;
		}
		if (mVegItemRecId == 0) { // new record
			mUri = rs.insert(mVegItemsUri, mValues);
			Log.v(LOG_TAG, "new record in saveVegItemRecord; returned URI: " + mUri.toString());
			long newRecId = Long.parseLong(mUri.getLastPathSegment());
			if (newRecId < 1) { // returns -1 on error, e.g. if not valid to save because of missing required field
				Log.v(LOG_TAG, "new record in saveVegItemRecord has Id == " + newRecId + "); canceled");
				return 0;
			}
			mVegItemRecId = newRecId;
			getLoaderManager().restartLoader(Loaders.EXISTING_PROJCODES, null, this);
			mUri = ContentUris.withAppendedId(mVegItemsUri, mVegItemRecId);
			Log.v(LOG_TAG, "new record in saveVegItemRecord; URI re-parsed: " + mUri.toString());
			// set default project; redundant with fn in NewVisitFragment; low priority fix
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.DEFAULT_PROJECT_ID, mVegItemRecId);
			prefEditor.commit();
			return 1;
		} else {
			mUri = ContentUris.withAppendedId(mVegItemsUri, mVegItemRecId);
			int numUpdated = rs.update(mUri, mValues, null, null);
			Log.v(LOG_TAG, "Saved record in saveVegItemRecord; numUpdated: " + numUpdated);
			return numUpdated;
		}
	}
	
	/*
	SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
	SharedPreferences.Editor prefEditor = sharedPref.edit();
	prefEditor.putBoolean(Prefs.VERIFY_VEG_ITEMS_PRESENCE, false);
	prefEditor.commit();
	
	 */
	
	private boolean validateVegItemValues() {
		// validate all user-accessible items
		Context c = getActivity();
		String stringProblem;
		String errTitle = c.getResources().getString(R.string.vis_hdr_validate_generic_title);
		ConfigurableMsgDialog flexErrDlg = new ConfigurableMsgDialog();
		if (mPresenceOnly) {
			
		} else {
			
		}
		return true;
	}
	/*	private boolean validateRecordValues() {
		// validate all items on the screen the user can see
		// assure mValues contains all required fields
		if (!mValues.containsKey("VisitName")) {
			mValues.put("VisitName", mViewVisitName.getText().toString().trim());
		}
		String stringVisitName = mValues.getAsString("VisitName");
		if (stringVisitName.length() == 0) {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_name_none);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_visname_none");
					mViewVisitName.requestFocus();
				}
			}
			return false;
		}
		if (!(stringVisitName.length() >= 2)) {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_name_short);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_visname_short");
					mViewVisitName.requestFocus();
				}
			}
			return false;
		}
		if (mExistingVisitNames.containsValue(stringVisitName)) {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_name_dup);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_visname_duplicate");
					mViewVisitName.requestFocus();
				}
			}
			return false;
		}
		if (!mValues.containsKey("VisitDate")) {
			mValues.put("VisitDate", mViewVisitDate.getText().toString().trim());
		}
		String stringVisitDate = mValues.getAsString("VisitDate");
		if (stringVisitDate.length() == 0) {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_date_none);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_visdate_none");
					mViewVisitDate.requestFocus();
				}
			}
			return false;
		}
		
		if (mNamerId == 0) {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_namer_none);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_namer_none");
					mViewVisitDate.requestFocus();
				}
			}
			return false;
		}
		if (!mValues.containsKey("NamerID")) { // valid if we are to this point
			mValues.put("NamerID", mNamerId);
		}

		if (!mValues.containsKey("Scribe")) { // optional, no validation
			mValues.put("Scribe", mViewVisitScribe.getText().toString().trim());
		}
		
		if (mLocIsGood) {
			mValues.put("RefLocIsGood", 1);
		} else {
			if (mValidationLevel > VALIDATE_SILENT) {
				stringProblem = c.getResources().getString(R.string.vis_hdr_validate_loc_not_ready);
				if (mValidationLevel == VALIDATE_QUIET) {
					Toast.makeText(this.getActivity(),
							stringProblem,
							Toast.LENGTH_LONG).show();
				}
				if (mValidationLevel == VALIDATE_CRITICAL) {
					flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
					flexErrDlg.show(getFragmentManager(), "frg_err_loc_not_ready");
				}
			}
			return false;
		}

		// validate Azimuth
		String stringAz = mViewAzimuth.getText().toString().trim();
		if (stringAz.length() > 0) { // null is valid but empty string is not
			Log.v(LOG_TAG, "Azimuth is length " + stringAz.length());
			int Az = 0;
			try {
				Az = Integer.parseInt(stringAz);
				if ((Az < 0) || (Az > 360)) {
					if (mValidationLevel > VALIDATE_SILENT) {
						stringProblem = c.getResources().getString(R.string.vis_hdr_validate_azimuth_bad);
						if (mValidationLevel == VALIDATE_QUIET) {
							Toast.makeText(this.getActivity(),
									stringProblem,
									Toast.LENGTH_LONG).show();
						}
						if (mValidationLevel == VALIDATE_CRITICAL) {
							flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
							flexErrDlg.show(getFragmentManager(), "frg_err_azimuth_out_of_range");
							mViewAzimuth.requestFocus();
						}
					}
					return false;
				} else {
					mValues.put("Azimuth", Az);
				}
			} catch(NumberFormatException e) {
				if (mValidationLevel > VALIDATE_SILENT) {
					stringProblem = c.getResources().getString(R.string.vis_hdr_validate_azimuth_bad);
					if (mValidationLevel == VALIDATE_QUIET) {
						Toast.makeText(this.getActivity(),
								stringProblem,
								Toast.LENGTH_LONG).show();
					}
					if (mValidationLevel == VALIDATE_CRITICAL) {
						flexErrDlg = ConfigurableMsgDialog.newInstance(errTitle, stringProblem);
						flexErrDlg.show(getFragmentManager(), "frg_err_azimuth_bad_number");
						mViewAzimuth.requestFocus();
					}
				}
			}
		} else {
			Log.v(LOG_TAG, "Azimuth is length zero");
		}
		
		if (!mValues.containsKey("VisitNotes")) { // optional, no validation
			mValues.put("VisitNotes", mViewVisitNotes.getText().toString().trim());
		}
		return true;
	}*/
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case Loaders.VEGITEM_TO_EDIT:
			Uri oneVegItemUri = ContentUris.withAppendedId(
				Uri.withAppendedPath(
				ContentProvider_VegNab.CONTENT_URI, "vegitems"), mVegItemRecId);
			cl = new CursorLoader(getActivity(), oneVegItemUri,
					null, select, null, null);
			break;

		case Loaders.CURRENT_SUBPLOT_VEGITEMS:
			// get the existing VegCodes, other than the current one, to disallow duplicates
			Uri allSbVegItemsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "vegitems");
			String[] projection = {"_id", "OrigCode"};
			select = "(VisitID = " + mCurVisitRecId 
					+ " AND SubPlotID = " + mCurSubplotRecId 
					+ " AND _id <> " + mVegItemRecId + ")";
			cl = new CursorLoader(getActivity(), allSbVegItemsUri,
					projection, select, null, null);
			break;

		case Loaders.VEG_ITEM_CONFIDENCE_LEVELS:
			Uri allCFLevelsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "idlevels");
			select = "(_id <= 3)"; // don't offer 'not identified' here, only for Placeholders never identified
			// mIDConfidence
			cl = new CursorLoader(getActivity(), allCFLevelsUri,
					null, select, null, null);
			break;

//		case Loaders.VEG_ITEM_SUBPLOT:
//			Uri oneSbPlotUri = ContentUris.withAppendedId(
//					Uri.withAppendedPath(
//					ContentProvider_VegNab.CONTENT_URI, "subplots"), mCurSubplotRecId);
//			cl = new CursorLoader(getActivity(), oneSbPlotUri,
//					null, select, null, null);
//			break;

//		case Loaders.VEG_ITEM_VISIT:
//			Uri oneVisitUri = ContentUris.withAppendedId(
//					Uri.withAppendedPath(
//					ContentProvider_VegNab.CONTENT_URI, "visits"), mCurVisitRecId);
//			cl = new CursorLoader(getActivity(), oneVisitUri,
//					null, select, null, null);
//			break;		
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		int rowCt = c.getCount();
		switch (loader.getId()) {
			
		case Loaders.VEGITEM_TO_EDIT:
			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				String vegItemLabel = c.getString(c.getColumnIndexOrThrow("OrigCode")) + ": "
						+ c.getString(c.getColumnIndexOrThrow("OrigDescr"));
				mTxtSpeciesItemLabel.setText(vegItemLabel);
				mEditSpeciesHeight.setText(c.getString(c.getColumnIndexOrThrow("Height")));
				mEditSpeciesCover.setText(c.getString(c.getColumnIndexOrThrow("Cover")));
				int presInt = c.getInt(c.getColumnIndexOrThrow("Presence"));
				boolean present = ((presInt == 1) ? true : false);
				mCkSpeciesIsPresent.setChecked(present);
				
				// CheckBox mCkSpeciesIsPresent, mCkDontVerifyPresence;
				// set up spinner
			}
			break;

		case Loaders.VEG_ITEM_CONFIDENCE_LEVELS:
			// Swap the new cursor in
			// The framework will take care of closing the old cursor once we return
			mCFSpinnerAdapter.swapCursor(c);
			if (rowCt > 0) {
				// setNamerSpinnerSelectionFromDefaultNamer(); // internally sets mNamerId
				mSpinnerSpeciesConfidence.setEnabled(true);
			} else {
				mSpinnerSpeciesConfidence.setEnabled(false);
			}
			break;
			
//		case Loaders.VEG_ITEM_SUBPLOT:
//			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
//			if (c.moveToFirst()) {
//				// what do we need from the Subplot, other than Presence?
//			}
//			break;
//		case Loaders.VEG_ITEM_VISIT:
//			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
//			if (c.moveToFirst()) {
//				// what do we need from the Visit?
//			}
//			break;
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
//		case Loaders.VEG_ITEM_SUBPLOT:
//			// nothing to do here since no adapter
//			break;
//		case Loaders.VEG_ITEM_VISIT:
//			// nothing to do here since no adapter
//			break;
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
			// workaround for spinner firing when first set
			if(((String)parent.getTag()).equalsIgnoreCase(Tags.SPINNER_FIRST_USE)) {
	            parent.setTag("");
	            return;
	        }
			// mIDConfidence
			mNamerId = id;
			if (mNamerId == 0) { // picked '(add new)'
				Log.v(LOG_TAG, "Starting 'add new' for Namer from onItemSelect");
//				AddSpeciesNamerDialog  addSppNamerDlg = AddSpeciesNamerDialog.newInstance();
//				FragmentManager fm = getActivity().getSupportFragmentManager();
//				addSppNamerDlg.show(fm, "sppNamerDialog_SpinnerSelect");
				EditNamerDialog newNmrDlg = EditNamerDialog.newInstance(0);
				newNmrDlg.show(getFragmentManager(), "frg_new_namer_fromSpinner");

			} else { // (mNamerId != 0) 
				// save in app Preferences as the default Namer
				saveDefaultNamerId(mNamerId);
			}
			setNamerSpinnerSelectionFromDefaultNamer(); // in either case, reset selection
			return;
		}
		// write code for any other spinner(s) here
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
	
/*
	public void setNamerSpinnerSelectionFromDefaultNamer() {
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		// if none yet, use _id = 0, generated in query as '(add new)'
		mNamerId = sharedPref.getLong(Prefs.DEFAULT_NAMER_ID, 0);
		setNamerSpinnerSelection();
		if (mNamerId == 0) {
			// user sees '(add new)', blank TextView receives click;
			mLblNewNamerSpinnerCover.bringToFront();
		} else {
			// user can operate the spinner
			mNamerSpinner.bringToFront();
		}
	}
	
	public void setNamerSpinnerSelection() {
		// set the current Namer to show in its spinner
		// mIDConfidence
		for (int i=0; i<mRowCt; i++) {
			Log.v(LOG_TAG, "Setting mNamerSpinner; testing index " + i);
			if (mNamerSpinner.getItemIdAtPosition(i) == mNamerId) {
				Log.v(LOG_TAG, "Setting mNamerSpinner; found matching index " + i);
				mNamerSpinner.setSelection(i);
				break;
			}
		}
	}

*/	
}
