package com.vegnab.vegnab;

import java.util.HashSet;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddSpeciesNamerDialog extends DialogFragment 
		implements android.view.View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final String LOG_TAG = AddSpeciesNamerDialog.class.getSimpleName();
	private EditText mViewNamer;
	private String mName;
	private HashSet<String> mExistingNamers = new HashSet<String>();
	
	static AddSpeciesNamerDialog newInstance() {
		AddSpeciesNamerDialog f = new AddSpeciesNamerDialog();
		// supply arguments
		Bundle args = new Bundle();
//		args.putLong("projRecId", projRecId);
//		args.putString("projCode", projCode);
		f.setArguments(args);
		return f;
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		// fire the loader manager off ASAP, its results don't use the UI
		getLoaderManager().initLoader(Loaders.EXISTING_NAMERS, null, this);
		View view = inflater.inflate(R.layout.fragment_new_namer, root);
		mViewNamer = (EditText) view.findViewById(R.id.txt_new_namer);
		Button btnCancel = (Button) view.findViewById(R.id.btn_new_namer_cancel);
		btnCancel.setOnClickListener(this);
		Button btnSave = (Button) view.findViewById(R.id.btn_new_namer_save);
		btnSave.setOnClickListener(this);
		getDialog().setTitle(R.string.add_namer_title);
		return view;
	}	

	@Override
 	public void onClick(View v) {
		Context c = this.getActivity();
 		switch (v.getId()) {
		case R.id.btn_new_namer_cancel:
//			Toast.makeText(this.getActivity(), 
//					"'Cancel' button clicked" , 
//					Toast.LENGTH_SHORT).show();
			dismiss();
			break;
		case R.id.btn_new_namer_save:
			mName = "" + mViewNamer.getText().toString().trim();
			if (mName == "") {
				Toast.makeText(c,
					c.getResources().getString(R.string.add_namer_missing),
					Toast.LENGTH_LONG).show();
				break;
			}
			if (mExistingNamers.contains(mName)) {
				Toast.makeText(c,
					c.getResources().getString(R.string.add_namer_duplicate),
					Toast.LENGTH_LONG).show();
				break;
			}
			ContentResolver rs = c.getContentResolver();
			Uri uri, namersUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "namers");
			ContentValues values = new ContentValues();
			values.put("NamerName", mName);
			uri = rs.insert(namersUri, values);
			long namerId = Long.parseLong(uri.getLastPathSegment());
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putLong(Prefs.DEFAULT_NAMER_ID, namerId);
			prefEditor.commit();
			dismiss();
			break;
 		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = null;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case Loaders.EXISTING_NAMERS:
			// get the existing Namers, to disallow duplicates
			Uri allNamersUri = Uri.withAppendedPath(
					ContentProvider_VegNab.CONTENT_URI, "namers");
			Log.v(LOG_TAG, "allNamersUri: " + allNamersUri.toString());
			String[] projection = {"NamerName"};
			cl = new CursorLoader(getActivity(), allNamersUri,
					projection, select, null, null);
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
				mExistingNamers.add(c.getString(c.getColumnIndexOrThrow("NamerName")));
				Log.v(LOG_TAG, "Namer item added to HashMap: " + c.getString(c.getColumnIndexOrThrow("NamerName")));
			}
			break;
		}	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case Loaders.EXISTING_NAMERS:
			// nothing to do here since no adapter
			break;
		}
	}
}
