package com.vegnab.vegnab;

import java.util.HashSet;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

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
import android.widget.EditText;
import android.widget.Toast;

public class EditNamerDialog extends DialogFragment implements android.view.View.OnClickListener,
		android.view.View.OnFocusChangeListener, LoaderManager.LoaderCallbacks<Cursor>
		//, android.view.View.OnKeyListener
		{
	private static final String LOG_TAG = EditNamerDialog.class.getSimpleName();
	long mNamerRecId = 0; // zero default means new or not specified yet
	Uri mUri, mNamersUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "namers");
	ContentValues mValues = new ContentValues();
	HashSet<String> mExistingNamers = new HashSet<String>();
	private EditText mEditNamerName;
	String mStringNamer;
	
	static EditNamerDialog newInstance(long mNamerId) {
		EditNamerDialog f = new EditNamerDialog();
		// supply mNamerId as an argument
		Bundle args = new Bundle();
		args.putLong("mNamerId", mNamerId);
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
		mEditNamerName = (EditText) view.findViewById(R.id.txt_edit_namer);
		mEditNamerName.setOnFocusChangeListener(this);
		getDialog().setTitle(R.string.edit_namer_title_edit);
		return view;
	}

	@Override	
	public void onClick(View v) {
		// don't need onClick here
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			mNamerRecId = args.getLong("mNamerId");
			getLoaderManager().initLoader(Loaders.EXISTING_NAMERS, null, this);
			getLoaderManager().initLoader(Loaders.NAMER_TO_EDIT, null, this);
			// will insert values into screen when cursor is finished
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) { // something lost focus
			mValues.clear();
			switch (v.getId()) {
			case R.id.txt_edit_namer:
				mValues.put("NamerName", mEditNamerName.getText().toString().trim());
				break;

			default: // save everything
				mValues.put("NamerName", mEditNamerName.getText().toString().trim());

				}
			Log.v(LOG_TAG, "Saving record in onFocusChange; mValues: " + mValues.toString().trim());
			int numUpdated = saveProjRecord();
			}		
		}


	@Override
	public void onCancel (DialogInterface dialog) {
		// update the project record in the database, if everything valid		
		mValues.clear();
		mValues.put("NamerName", mEditNamerName.getText().toString().trim());
		Log.v(LOG_TAG, "Saving record in onCancel; mValues: " + mValues.toString());
		int numUpdated = saveProjRecord();
	}
	
	private int saveProjRecord () {
		if ("" + mEditNamerName.getText().toString().trim() == "") {
			Toast.makeText(this.getActivity(),
					"Need Namer",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		if (mExistingNamers.contains("" + mEditNamerName.getText().toString().trim())) {
			Toast.makeText(this.getActivity(),
					"Duplicate Name",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		ContentResolver rs = getActivity().getContentResolver();
		if (mNamerRecId == 0) { // new record
			mUri = rs.insert(mNamersUri, mValues);
			Log.v(LOG_TAG, "new record in saveProjRecord; returned URI: " + mUri.toString());
			mNamerRecId = Long.parseLong(mUri.getLastPathSegment());
			mUri = ContentUris.withAppendedId(mNamersUri, mNamerRecId);
			Log.v(LOG_TAG, "new record in saveProjRecord; URI re-parsed: " + mUri.toString());
			// set default project; redundant with fn in NewVisitFragment; low priority fix
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.DEFAULT_PROJECT_ID, mNamerRecId);
			prefEditor.commit();
			return 1;
		} else {
			mUri = ContentUris.withAppendedId(mNamersUri, mNamerRecId);
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
		case Loaders.EXISTING_NAMERS:
			// get the existing ProjCodes, other than the current one, to disallow duplicates
			Uri allProjsUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "namers");
			String[] projection = {"NamerName"};
			select = "(_id <> " + mNamerRecId + " AND IsDeleted = 0)";
			cl = new CursorLoader(getActivity(), allProjsUri,
					projection, select, null, null);
			break;
		case Loaders.NAMER_TO_EDIT:
			// First, create the base URI
			// could test here, based on e.g. filters
//			mNamersUri = ContentProvider_VegNab.CONTENT_URI; // get the whole list
			Uri oneProjUri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "namers"), mNamerRecId);
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
		case Loaders.EXISTING_NAMERS:
			mExistingNamers.clear();
			while (c.moveToNext()) {
				Log.v(LOG_TAG, "onLoadFinished, add to HashMap: " + c.getString(c.getColumnIndexOrThrow("NamerName")));
				mExistingNamers.add(c.getString(c.getColumnIndexOrThrow("NamerName")));
			}
			break;
		case Loaders.NAMER_TO_EDIT:
			Log.v(LOG_TAG, "onLoadFinished, records: " + c.getCount());
			if (c.moveToFirst()) {
				mEditNamerName.setText(c.getString(c.getColumnIndexOrThrow("NamerName")));
			}
			break;
		}	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case Loaders.NAMER_TO_EDIT:
			// maybe nothing to do here since no adapter
			break;
		}
	}
}
