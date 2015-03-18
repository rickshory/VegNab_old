package com.vegnab.vegnab;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;
import com.vegnab.vegnab.database.VNContract.Loaders;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DataEntryContainerFragment extends Fragment {
	private static final String LOG_TAG = DataEntryContainerFragment.class.getSimpleName();
	public static final String TAG = DataEntryContainerFragment.class.getName();

//	long mVisitId = 0, mSubplotTypeId = -1; // defaults for new or not specified yet
//	Uri mUri, mNamersUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "namers");
//	ContentValues mValues = new ContentValues();
//	HashMap<Long, String> mExistingNamers = new HashMap<Long, String>();
//	private EditText mEditNamerName;
//	private TextView mTxtNamerMsg;
//	String mTitle;
//	int mLayoutRes;
//	View mRootview;
//	ViewPager mDataScreenPager = null;
//	FragmentStatePagerAdapter mAdapter = null;	
	
//	public static DataEntryContainerFragment newInstance(long visitId, long subplotTypeId, String stTitle) {
	public static DataEntryContainerFragment newInstance() {
		DataEntryContainerFragment f = new DataEntryContainerFragment();
		// supply arguments
//		Bundle args = new Bundle();
//		args.putLong("visitId", visitId);
//		args.putLong("subplotTypeId", subplotTypeId);
//		args.putString("stTitle", stTitle);
//		f.setArguments(args);
		return f;
	}
	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
// set up any interfaces
//		try {
//        	mEditNamerListener = (EditNamerDialogListener) getActivity();
//        	Log.v(LOG_TAG, "(EditNamerDialogListener) getActivity()");
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Main Activity must implement EditNamerDialogListener interface");
//        }
//	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(LOG_TAG, "entered 'onCreateView'");
		//View root = inflater.inflate(R.layout.fragment_parent_viewpager, container, false);
		View root = inflater.inflate(R.layout.fragment_data_entry_container, container, false);
// assign UI elements
		ViewPager viewPager = (ViewPager) root.findViewById(R.id.data_entry_pager);
        // Must use the child FragmentManager
        viewPager.setAdapter(new dataPagerAdapter(getChildFragmentManager()));
        
//		mDataScreenPager = (ViewPager) mRootview.findViewById(R.id.data_entry_pager);
//		mDataScreenPager.setOffscreenPageLimit(1);
		
//		FragmentManager fm = getChildFragmentManager();
//		mAdapter = new dataPagerAdapter(fm);
//		Log.v(LOG_TAG, "about to do 'setAdapterTask().execute()'");
//		new setAdapterTask().execute();
//		Log.v(LOG_TAG, "did 'setAdapterTask().execute()'");
//		mTxtNamerMsg = (TextView) mRootview.findViewById(R.id.lbl_namer);
//		mEditNamerName = (EditText) mRootview.findViewById(R.id.txt_edit_namer);
//		// attempt to automatically show soft keyboard
//		mEditNamerName.requestFocus();
//		getDialog().getWindow().setSoftInputMode(android.mRootview.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//		mEditNamerName.setOnFocusChangeListener(this);

//		if (mNamerRecId == 0) { // new record
//			getDialog().setTitle(R.string.add_namer_title);
//		} else { // existing record being edited
//			getDialog().setTitle(R.string.edit_namer_title_edit);
//		}
		Log.v(LOG_TAG, "about to return from 'onCreateView'");
		return root;
	}
	
/*
	private class setAdapterTask extends AsyncTask<Void,Void,Void>{
	      protected Void doInBackground(Void... params) {
	            return null;
	        }

	        @Override
	        protected void onPostExecute(Void result) {
	        	mDataScreenPager.setAdapter(mAdapter);
	        }
	}
*/
	
//	@Override	
//	public void onClick(View v) {
//		// don't need onClick here
//	}

/*
	@Override
	public void onStart() {
		super.onStart();
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
	}
*/
	
	public static class dataPagerAdapter extends FragmentStatePagerAdapter {
	
		public dataPagerAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt(TestPagerFragment.POSITION_KEY, position);
            return TestPagerFragment.newInstance(args);
        }

/*		public Fragment getItem(int dataScreenIndex) {
			Log.v(LOG_TAG, "called dataPagerAdapter 'getItem' with value " + dataScreenIndex);
			Fragment dataEntryFrag = TestPagerFragment.newInstance(0, 0, "test");
	 may need some switching later		
			switch (dataScreenIndex) {
			case 0:
				dataEntryFrag = new TestPagerFragment();
				break;		
			
			case 1:
				dataEntryFrag = new TestPagerFragment();
				break;
				
			case 2:
				dataEntryFrag = new TestPagerFragment();
				break;		
			}
			
			return dataEntryFrag;
		}
*/	
		@Override
		public int getCount() {
			Log.v(LOG_TAG, "called dataPagerAdapter 'getCount'");
			return 4; // for testing
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return "subplot " + (position + 1);
		}
	}
	
	
}

