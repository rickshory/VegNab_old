package com.vegnab.vegnab;

import java.util.HashSet;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
	public interface AddNamerDialogListener {
		public void onAddNamerSaveClick(DialogFragment dialog);
	}
	AddNamerDialogListener mListener;
	private EditText mViewNamer;
	private String mName;
	private HashSet<String> mExistingNamers = new HashSet<String>();
	
	public static AddSpeciesNamerDialog newInstance() {
		AddSpeciesNamerDialog f = new AddSpeciesNamerDialog();
//		f.setTargetFragment((Fragment) listener, /*requestCode*/ 123); target fragment does not work
		// supply arguments
		Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// fire off the loader manager ASAP, its results don't use the UI
		getLoaderManager().initLoader(Loaders.EXISTING_NAMERS, null, this);
		// Verify that the host fragment implements the callback interface
        try {
        	mListener = (AddNamerDialogListener) getActivity();
        	Log.v(LOG_TAG, "(AddNamerDialogListener) getTargetFragment()");
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddNamerDialogListener interface");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	// Get the layout inflater
    	LayoutInflater inflater = getActivity().getLayoutInflater();
    	// Inflate and set the layout for the dialog
    	// Pass null as the parent view because its going in the dialog layout
    	builder.setView(inflater.inflate(R.layout.fragment_new_namer, null));
    	// Add action buttons
    	builder.setPositiveButton(R.string.action_save, null); // will set onClick after created, in onResume
    	builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			Log.v(LOG_TAG, "Cancel: onClick(DialogInterface dialog, int id); dialog='" + dialog.toString() + "', id=" + id);
    			AddSpeciesNamerDialog.this.getDialog().cancel();
    		}
    	});
    	return builder.create();
    }
    
    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog dialog = (AlertDialog)getDialog();
        // setting positiveButton onClick here allows control of dismiss
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {                  
            @Override
            public void onClick(View onClick) {
            	Context c = getActivity();
            	mViewNamer = (EditText) dialog.findViewById(R.id.txt_new_namer);
    			mName = "" + mViewNamer.getText().toString().trim();
    			if (mName == "") {
    				Toast.makeText(c,
    					c.getResources().getString(R.string.add_namer_missing),
    					Toast.LENGTH_LONG).show();
    				return;
    			}
    			if (mExistingNamers.contains(mName)) {
    				Toast.makeText(c,
    					c.getResources().getString(R.string.add_namer_duplicate),
    					Toast.LENGTH_LONG).show();
    				return;
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
            	Log.v(LOG_TAG, "onSaveButtonClick, about to callback to the main Activity");
            	mListener.onAddNamerSaveClick(AddSpeciesNamerDialog.this);
            	Log.v(LOG_TAG, "onSaveButtonClick, after callback to the main Activity");
            	dismiss(); // dismiss the dialog
            }
        });
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

	@Override
	public void onClick(View v) {
		// unused
	}
}
