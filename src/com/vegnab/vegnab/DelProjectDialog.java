package com.vegnab.vegnab;

import java.util.List;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VegNabDbHelper;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DelProjectDialog extends DialogFragment implements android.view.View.OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	public static final int LOADER_FOR_VALID_DEL_PROJECTS = 4; // Loader Id for the list of Projects valid to delete
	VegNabDbHelper mDbHelper;
	List<?> mValidProjList;
	SimpleCursorAdapter mListAdapter; // to link the list's data
/*
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.action_delete_proj)
			.setAdapter(mListAdapter, null);
		// getLoaderManager().initLoader(LOADER_FOR_PROJECTS, null, this);
		return builder.create();
	}
*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_del_project, root);
		
		getDialog().setTitle(R.string.action_delete_proj);
		return view;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cl = null;
		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {
		case LOADER_FOR_VALID_DEL_PROJECTS:
			baseUri = ContentProvider_VegNab.SQL_URI;
			select = "SELECT _id, ProjCode FROM Projects;";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor finishedCursor) {
		switch (loader.getId()) {
		case LOADER_FOR_VALID_DEL_PROJECTS:
			mListAdapter.swapCursor(finishedCursor);
			break;
		}	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_FOR_VALID_DEL_PROJECTS:
			mListAdapter.swapCursor(null);
			break;
		}
	}
}
