package com.vegnab.vegnab;

import java.util.List;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VegNabDbHelper;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class DelProjectDialog extends DialogFragment implements android.view.View.OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	public static final int LOADER_FOR_VALID_DEL_PROJECTS = 4; // Loader Id for the list of Projects valid to delete
	VegNabDbHelper mDbHelper;
	ListView mValidProjList;
	SimpleCursorAdapter mListAdapter; // to link the list's data

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_del_project, root);
		mValidProjList = (ListView) view.findViewById(R.id.list_del_projects_valid);
		
		String[] fromColumns = {"ProjCode"};
		int[] toViews = {android.R.id.text1};
		mListAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_1, null,
				fromColumns, toViews, 0);
		mValidProjList.setAdapter(mListAdapter);
		getLoaderManager().initLoader(LOADER_FOR_VALID_DEL_PROJECTS, null, this);
		mValidProjList.setOnItemClickListener(new OnItemClickListener () {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				Cursor cr = ((SimpleCursorAdapter) mValidProjList.getAdapter()).getCursor();
				cr.moveToPosition(position);
				String projCd = cr.getString(cr.getColumnIndexOrThrow("ProjCode"));
				Toast.makeText(getActivity(), 
						"projCd: " + projCd, 
						Toast.LENGTH_LONG).show();
				FragmentManager fm = getChildFragmentManager();
				ConfirmDelProjDialog  confDelProjDlg = ConfirmDelProjDialog.newInstance(id, projCd);
				confDelProjDlg.show(fm, "frg_conf_del_proj");
//				dismiss();
			}
		});

		getDialog().setTitle(R.string.action_delete_proj);
		return view;
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
