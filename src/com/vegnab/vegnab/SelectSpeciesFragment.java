package com.vegnab.vegnab;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class SelectSpeciesFragment extends ListFragment 
		implements LoaderManager.LoaderCallbacks<Cursor>{
	private static final String LOG_TAG = SelectSpeciesFragment.class.getSimpleName();
	SimpleCursorAdapter mSppResultsAdapter;
	// declare that the container Activity must implement this interface

	public interface OnSppResultClickListener {
		// methods that must be implemented in the container Activity
		public void onSppMatchListClicked(int sourceId, long recId);
	}
	OnSppResultClickListener mListClickCallback;
	long mRowCt;
	EditText mViewSearchChars;
	TextWatcher sppCodeTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			// use this method; test length of string; e.g. 'count' of other methods does not give this length
			Log.v(LOG_TAG, "afterTextChanged, s: '" + s.toString() + "'");
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// the 'count' characters beginning at 'start' are about to be replaced by new text with length 'after'
			Log.v(LOG_TAG, "beforeTextChanged, s: '" + s.toString() 
					+ "', start: " + start + ", count: " + count + ", after: " + after);
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// the 'count' characters beginning at 'start' have just replaced old text that had length 'before'
			Log.v(LOG_TAG, "onTextChanged, s: '" + s.toString()
					+ "', start: " + start + ", before: " + before + ", count: " + count);
			
		}
	};
	
	// add option checkboxes or radio buttons, or do as menu items
	
/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.new_visit, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
*/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous screen, remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			// restore search text and any search options
//			mViewSearchChars = savedInstanceState.getString(ARG_SEARCH_CHARS);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_sel_species, container, false);
		mViewSearchChars = (EditText) rootView.findViewById(R.id.txt_search_chars);
		
		mViewSearchChars.addTextChangedListener(sppCodeTextWatcher);

		// use query to return 'MatchTxt', concatenated from code and description; more reading room
		mSppResultsAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_1, null,
				new String[] {"MatchTxt"},
				new int[] {android.R.id.text1}, 0);
		setListAdapter(mSppResultsAdapter);
		getLoaderManager().initLoader(Loaders.SPP_MATCHES, null, this);

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// assure the container activity has implemented the callback interface
		try {
			mListClickCallback = (OnSppResultClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException (activity.toString() + " must implement OnVisitClickListener");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current search text and any options
//		outState.putInt(ARG_SEARCH_TEXT, mCurrentSubplot);
	}
	
    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
//        Toast.makeText(this.getActivity(), "Clicked position " + pos + ", id " + id, Toast.LENGTH_SHORT).show();
        mListClickCallback.onSppMatchListClicked((int)id, id); // for testing, send ID for both parmams
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// switch out based on id
		CursorLoader cl = null;
		Uri baseUri;
		String select = null; // default for all-columns, unless re-assigned or overridden by raw SQL
		switch (id) {

		case Loaders.SPP_MATCHES:
			baseUri = ContentProvider_VegNab.SQL_URI;
			select = "SELECT _id, Code || ': ' || SppDescr AS MatchTxt " 
					+ "FROM RegionalSpeciesList " 
					+ "WHERE Code LIKE 'CACA%' " 
					+ "ORDER BY Code;";
			cl = new CursorLoader(getActivity(), baseUri,
					null, select, null, null);
			break;		
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor finishedCursor) {
		// there will be various loaders, switch them out here
		mRowCt = finishedCursor.getCount();
		switch (loader.getId()) {
		case Loaders.SPP_MATCHES:
			mSppResultsAdapter.swapCursor(finishedCursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// is about to be closed. Need to make sure it is no longer is use.
		switch (loader.getId()) {
		case Loaders.PREV_VISITS:
			mSppResultsAdapter.swapCursor(null);
			break;
		}
	}

/*
	// no Override
	public static void onBackPressed() {
		Log.v("NewVist", "In NewVisitFragment, caught 'onBackPressed'");
	return;
	}
*/	


}
