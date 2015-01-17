package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Prefs;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class EditProjectDialog extends DialogFragment implements android.view.View.OnClickListener,
		android.view.View.OnFocusChangeListener, LoaderManager.LoaderCallbacks<Cursor>
		//, android.view.View.OnKeyListener
		{
	public static final int LOADER_FOR_PROJECT_TO_EDIT = 3; // Loader Id
	public static final int LOADER_FOR_EXISTING_PROJECTS = 13;
	long mProjRecId = 0; // zero default means new or not specified yet
	Uri mUri, mBaseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "projects");
	ContentValues mValues = new ContentValues();
	HashSet<String> mExistingProjCodes = new HashSet<String>();
	private EditText mProjCode, mDescription, mContext, mCaveats, mContactPerson, mStartDate, mEndDate;
	private EditText mActiveDateView;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private Calendar mCalendar = Calendar.getInstance();
	private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {

	    @Override
	    public void onDateSet(DatePicker view, int year, int monthOfYear,
	            int dayOfMonth) {
	        mCalendar.set(Calendar.YEAR, year);
	        mCalendar.set(Calendar.MONTH, monthOfYear);
	        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	        mActiveDateView.setText(mDateFormat.format(mCalendar.getTime()));
	    }
	};
	
	static EditProjectDialog newInstance(long mProjRecId) {
		EditProjectDialog f = new EditProjectDialog();
		// supply mProjRecId as an argument
		Bundle args = new Bundle();
		args.putLong("mProjRecId", mProjRecId);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_project, root);

		mProjCode = (EditText) view.findViewById(R.id.txt_projcode);
		mDescription = (EditText) view.findViewById(R.id.txt_descr);
		mContext = (EditText) view.findViewById(R.id.txt_context);
		mCaveats = (EditText) view.findViewById(R.id.txt_caveats);
		mContactPerson = (EditText) view.findViewById(R.id.txt_person);
		mStartDate = (EditText) view.findViewById(R.id.txt_date_from);
		mEndDate = (EditText) view.findViewById(R.id.txt_date_to);
		
		mStartDate.setOnClickListener(this);
		mEndDate.setOnClickListener(this);
		
		mProjCode.setOnFocusChangeListener(this);
		mDescription.setOnFocusChangeListener(this);
		mContext.setOnFocusChangeListener(this);
		mCaveats.setOnFocusChangeListener(this);
		mContactPerson.setOnFocusChangeListener(this);
		mStartDate.setOnFocusChangeListener(this);
		mEndDate.setOnFocusChangeListener(this);
		
		getDialog().setTitle(R.string.edit_proj_title_edit);
		return view;
	}

	@Override	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_date_from:
			mActiveDateView = mStartDate;
			fireOffDatePicker();
			break;
		case R.id.txt_date_to:
			mActiveDateView = mEndDate;
			fireOffDatePicker();
			break;
		}
	}
		
	private void fireOffDatePicker() {
		String s = mActiveDateView.getText().toString();
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
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			mProjRecId = args.getLong("mProjRecId");
			mUri = ContentUris.withAppendedId(mBaseUri, mProjRecId);
			getLoaderManager().initLoader(LOADER_FOR_EXISTING_PROJECTS, null, this);
			getLoaderManager().initLoader(LOADER_FOR_PROJECT_TO_EDIT, null, this);
			// will insert values into screen when cursor is finished
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) { // something lost focus
			mValues.clear();
			switch (v.getId()) {
			case R.id.txt_projcode:
				mValues.put("ProjCode", mProjCode.getText().toString().trim());
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
			case R.id.txt_date_from: // this one is not focusable
				mValues.put("StartDate", mStartDate.getText().toString().trim());
				break;
			case R.id.txt_date_to: // this one is not focusable
				mValues.put("EndDate", mEndDate.getText().toString().trim());
				break;
			default: // save everything
				mValues.put("ProjCode", mProjCode.getText().toString().trim());
				mValues.put("Description", mDescription.getText().toString().trim());
				mValues.put("Context", mContext.getText().toString().trim());
				mValues.put("Caveats", mCaveats.getText().toString().trim());
				mValues.put("ContactPerson", mContactPerson.getText().toString().trim());
				mValues.put("StartDate", mStartDate.getText().toString().trim());
				mValues.put("EndDate", mEndDate.getText().toString().trim());
				}
			Log.v("EditProj", "Saving record in onFocusChange; mValues: " + mValues.toString().trim());
			int numUpdated = saveProjRecord();
			}		
		}


	@Override
	public void onCancel (DialogInterface dialog) {
		// update the project record in the database, if everything valid		
		mValues.clear();
		mValues.put("ProjCode", mProjCode.getText().toString().trim());
		mValues.put("Description", mDescription.getText().toString().trim());
		mValues.put("Context", mContext.getText().toString().trim());
		mValues.put("Caveats", mCaveats.getText().toString().trim());
		mValues.put("ContactPerson", mContactPerson.getText().toString().trim());
		mValues.put("StartDate", mStartDate.getText().toString().trim());
		mValues.put("EndDate", mEndDate.getText().toString().trim());
		Log.v("EditProj", "Saving record in onCancel; mValues: " + mValues.toString());
		int numUpdated = saveProjRecord();
	}
	
	private int saveProjRecord () {
		if ("" + mProjCode.getText().toString().trim() == "") {
			Toast.makeText(this.getActivity(),
					"Need Project Code",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		if (mExistingProjCodes.contains("" + mProjCode.getText().toString().trim())) {
			Toast.makeText(this.getActivity(),
					"Duplicate Project Code",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		ContentResolver rs = getActivity().getContentResolver();
		if (mProjRecId == 0) { // new record
			mUri = rs.insert(mBaseUri, mValues);
			Log.v("EditProj", "new record in saveProjRecord; returned URI: " + mUri.toString());
			mProjRecId = Long.parseLong(mUri.getLastPathSegment());
			mUri = ContentUris.withAppendedId(mBaseUri, mProjRecId);
			Log.v("EditProj", "new record in saveProjRecord; URI re-parsed: " + mUri.toString());
			// set default project; redundant with fn in NewVisitFragment; low priority fix
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.DEFAULT_PROJECT_ID, mProjRecId);
			prefEditor.commit();
			return 1;
		} else {
			int numUpdated = rs.update(mUri, mValues, null, null);
			Log.v("EditProj", "Saved record in saveProjRecord; numUpdated: " + numUpdated);
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
		case LOADER_FOR_EXISTING_PROJECTS:
			// get the existing ProjCodes, other than the current one, to disallow duplicates
			Uri allProjsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "projects");
			String[] projection = {"ProjCode"};
			select = "(_id <> " + mProjRecId + " AND IsDeleted = 0)";
			cl = new CursorLoader(getActivity(), allProjsUri,
					projection, select, null, null);
			break;
		case LOADER_FOR_PROJECT_TO_EDIT:
			// First, create the base URI
			// could test here, based on e.g. filters
//			mBaseUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			Uri oneProjUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "projects"), mProjRecId);
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the dataset being displayed
			// Could build a WHERE clause such as
			// String select = "(Default = true)";
			cl = new CursorLoader(getActivity(), oneProjUri,
					null, select, null, null);
			break;

		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
		case LOADER_FOR_EXISTING_PROJECTS:
			mExistingProjCodes.clear();
			while (c.moveToNext()) {
				Log.v("EditProj", "onLoadFinished, add to HashMap: " + c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mExistingProjCodes.add(c.getString(c.getColumnIndexOrThrow("ProjCode")));
			}
			break;
		case LOADER_FOR_PROJECT_TO_EDIT:
			Log.v("EditProj", "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				mProjCode.setText(c.getString(c.getColumnIndexOrThrow("ProjCode")));
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
		case LOADER_FOR_PROJECT_TO_EDIT:
			// maybe nothing to do here since no adapter
			break;
		}
	}
}
