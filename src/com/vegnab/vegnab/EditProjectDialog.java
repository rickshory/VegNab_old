package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class EditProjectDialog extends DialogFragment implements android.view.View.OnClickListener,
		android.view.View.OnFocusChangeListener //, android.view.View.OnKeyListener
		{
	long projRecId = 0; // zero default means new or not specified yet
	private EditText mProjCode, mDescription, mContext, mCaveats, mContactPerson, mStartDate, mEndDate;
	private EditText mActiveDateView;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private Calendar myCalendar = Calendar.getInstance();
	private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {

	    @Override
	    public void onDateSet(DatePicker view, int year, int monthOfYear,
	            int dayOfMonth) {
	        myCalendar.set(Calendar.YEAR, year);
	        myCalendar.set(Calendar.MONTH, monthOfYear);
	        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	        mActiveDateView.setText(dateFormat.format(myCalendar.getTime()));
	    }
	};
	
	static EditProjectDialog newInstance(long projRecId) {
		EditProjectDialog f = new EditProjectDialog();
		// supply projRecId as an argument
		Bundle args = new Bundle();
		args.putLong("projRecId", projRecId);
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
		Button btnCancel = (Button) view.findViewById(R.id.btn_editproj_cancel);
		btnCancel.setOnClickListener(this);
		Button btnSave = (Button) view.findViewById(R.id.btn_editproj_save);
		btnSave.setOnClickListener(this);

//		final Calendar myCalendar = Calendar.getInstance();
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
/*		
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener() {
	        @Override
	        public boolean onKey(View v, int keyCode, KeyEvent event) {
	        	Log.v("EditProject", "In EditProjectDialog, keyCode: " + keyCode);
	            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
	            	Log.v("EditProject", "In EditProjectDialog, caught 'onBackPressed'");
	//                getSupportFragmentManager().popBackStack(null, SupportFragmentManager.POP_BACK_STACK_INCLUSIVE);
	                return false;
	            } else {
	                return false;
	            }
	        }
	    });
*/
		getDialog().setTitle(R.string.edit_proj_title_edit);
		return view;
	}

	@Override	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_editproj_cancel:
			Toast.makeText(this.getActivity(), 
					"'Cancel' button clicked" , 
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_editproj_save:
			Toast.makeText(this.getActivity(), 
					"'Save' button clicked" , 
					Toast.LENGTH_SHORT).show();
			break;
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
        try {
        	myCalendar.setTime(dateFormat.parse(s));
		} catch (java.text.ParseException e) {
			myCalendar = Calendar.getInstance();
		}
		new DatePickerDialog(getActivity(), myDateListener,
				myCalendar.get(Calendar.YEAR),
				myCalendar.get(Calendar.MONTH),
				myCalendar.get(Calendar.DAY_OF_MONTH)).show();	
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			projRecId = args.getLong("projRecId");
			// will set up the screen based on arguments passed in
			Log.v("EditProj", "In EditProjectFragment, onStart, projRecId=" + projRecId);
			String selection = "SELECT ProjCode, Description, Context, Caveats, " + 
					"ContactPerson, StartDate, EndDate FROM Projects WHERE _id = ?;";
			String selectionArgs[] = {"" + projRecId};
			ContentResolver rs = getActivity().getContentResolver();
			// use raw SQL, may change to use CursorLoader
			Uri uri = ContentProvider_VegNab.SQL_URI;
			Cursor c = rs.query(uri, null, selection, selectionArgs, null);
			if (c.moveToFirst()) {
				mProjCode.setText(c.getString(c.getColumnIndexOrThrow("ProjCode")));
				mDescription.setText(c.getString(c.getColumnIndexOrThrow("Description")));
				mContext.setText(c.getString(c.getColumnIndexOrThrow("Description")));
				mCaveats.setText(c.getString(c.getColumnIndexOrThrow("Caveats")));
				mContactPerson.setText(c.getString(c.getColumnIndexOrThrow("ContactPerson")));
				mStartDate.setText(c.getString(c.getColumnIndexOrThrow("StartDate")));
				mEndDate.setText(c.getString(c.getColumnIndexOrThrow("EndDate")));
			}
			c.close();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) { // something lost focus
			switch (v.getId()) {
			case R.id.txt_projcode:
				Log.v("EditProj", "In EditProjectFragment, onFocusChange, 'Project Code' lost focus");
				break;
			case R.id.txt_descr:
				Log.v("EditProj", "In EditProjectFragment, onFocusChange, 'Description' lost focus");
				break;
			case R.id.txt_date_from: // this one is not focusable
				Log.v("EditProj", "In EditProjectFragment, onFocusChange, 'Start Date' lost focus");
				break;
			case R.id.txt_date_to: // this one is not focusable
				Log.v("EditProj", "In EditProjectFragment, onFocusChange, 'End Date' lost focus");
				break;
				}
			}		
		}
/*		mProjCode = (EditText) view.findViewById(R.id.txt_projcode);
		mDescription = (EditText) view.findViewById(R.id.txt_descr);
		mContext = (EditText) view.findViewById(R.id.txt_context);
		mCaveats = (EditText) view.findViewById(R.id.txt_caveats);
		mContactPerson = (EditText) view.findViewById(R.id.txt_person);
		mStartDate = (EditText) view.findViewById(R.id.txt_date_from);
		mEndDate = (EditText) view.findViewById(R.id.txt_date_to);
*/
/*
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
*/	
	@Override
	public void onCancel (DialogInterface dialog) {
		Log.v("EditProj", "In EditProjectFragment, onCancel");
	}
	
}

