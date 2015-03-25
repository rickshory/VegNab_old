package com.vegnab.vegnab;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class TestPagerFragment extends Fragment implements OnClickListener {
	private static final String LOG_TAG = TestPagerFragment.class.getSimpleName();
	
	public static final String POSITION_KEY = "FragmentPositionKey";
	private int position = -1;

	public static final String VISIT_ID = "VisitId";
	private long mVisitId = 0;

	public static final String SUBPLOT_TYPE_ID = "SubplotTypeId";
	private long mSubplotTypeID = -1;

	public static final String PRESENCE_ONLY = "PresenceOnly";
	private int mPresenceOnly = -1;
	
	public static final String VISIT_NAME = "VisitName";
	private String mVisitName = "";
	
	
//	long mVisitId = 0, mSubplotTypeId = -1; // defaults for new or not specified yet
//	Uri mUri, mNamersUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "namers");
//	ContentValues mValues = new ContentValues();
//	HashMap<Long, String> mExistingNamers = new HashMap<Long, String>();
//	private EditText mEditNamerName;
//	private TextView mTxtNamerMsg;
	String mTitle;	
	
//	static TestPagerFragment newInstance(long visitId, long subplotTypeId, String stTitle) {
	static TestPagerFragment newInstance(Bundle args) {
		TestPagerFragment f = new TestPagerFragment();
		// supply arguments
//		Bundle args = new Bundle();
//		args.putLong("visitId", visitId);
//		args.putLong("subplotTypeId", subplotTypeId);
//		args.putString("stTitle", stTitle);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			Log.v(LOG_TAG, "onCreate FIRST TIME, position = " + position);
		} else {
			Log.v(LOG_TAG, "onCreate SUBSEQUENT TIME, position = " + position);
		}
// set up any interfaces
//		try {
//        	mEditNamerListener = (EditNamerDialogListener) getActivity();
//        	Log.v(LOG_TAG, "(EditNamerDialogListener) getActivity()");
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Main Activity must implement EditNamerDialogListener interface");
//        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		position = getArguments().getInt(POSITION_KEY);
		mVisitId = getArguments().getLong(VISIT_ID);
		mVisitName = getArguments().getString(VISIT_NAME);
		mSubplotTypeID = getArguments().getLong(SUBPLOT_TYPE_ID);
		mPresenceOnly = getArguments().getInt(PRESENCE_ONLY);
		
		View root = inflater.inflate(R.layout.fragment_test_pager, container, false);
		TextView textview = (TextView) root.findViewById(R.id.txt_test_pager);
		textview.setText("Position=" + position + ", VisitId=" + mVisitId +
				", Visit Name: '" + mVisitName + "'" +
				", SubplotTypeId=" + mSubplotTypeID + ", PresenceOnly=" + mPresenceOnly);
		textview.setOnClickListener(this);
		//
// assign any UI elements		
//		mTxtNamerMsg = (TextView) view.findViewById(R.id.lbl_namer);
//		mEditNamerName = (EditText) view.findViewById(R.id.txt_edit_namer);
//		// attempt to automatically show soft keyboard
//		mEditNamerName.requestFocus();
//		getDialog().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//		mEditNamerName.setOnFocusChangeListener(this);

//		if (mNamerRecId == 0) { // new record
//			getDialog().setTitle(R.string.add_namer_title);
//		} else { // existing record being edited
//			getDialog().setTitle(R.string.edit_namer_title_edit);
//		}
		Log.v(LOG_TAG, "onCreateView, position = " + position);
		return root;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.v(LOG_TAG, "onStart, position = " + position);
/*	
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			mVisitId = args.getLong("visitId");
			mSubplotTypeId = args.getLong("subplotTypeId");
			mTitle = args.getString("stTitle");
//			// request existing Namers ASAP, this doesn't use the UI
//			getLoaderManager().initLoader(Loaders.EXISTING_NAMERS, null, this);
//			getLoaderManager().initLoader(Loaders.NAMER_TO_EDIT, null, this);
//			// will insert values into screen when cursor is finished
		}
//		if (mVisitId == 0) { // new record
//			mTxtNamerMsg.setText(R.string.add_namer_header);
//		} else { // existing record being edited
//			mTxtNamerMsg.setText(mTitle);
//		}
*/	
	}

	@Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), "Clicked Position: " + position, Toast.LENGTH_LONG).show();
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v(LOG_TAG, "onSaveInstanceState, position = " + position);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.v(LOG_TAG, "onStop, position = " + position);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(LOG_TAG, "onDestroy, position = " + position);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v(LOG_TAG, "onDestroyView, position = " + position);
	}	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.v(LOG_TAG, "onAttach, position = " + position);
	}	
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.v(LOG_TAG, "onDetach, position = " + position);
	}	
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(LOG_TAG, "onPause, position = " + position);
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(LOG_TAG, "onResume, position = " + position);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(LOG_TAG, "onActivityCreated, position = " + position);
	}
	
}
