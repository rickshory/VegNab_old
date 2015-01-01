package com.vegnab.vegnab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class EditProjectDialog extends DialogFragment implements android.view.View.OnClickListener,
		android.view.View.OnFocusChangeListener //, android.view.View.OnKeyListener
		{
	long projRecId = 0; // zero default means new or not specified yet
	Uri uri, baseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "projects");
	ContentValues values = new ContentValues();
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
        	myCalendar.setTime(dateFormat.parse(s)); // use it
		} catch (java.text.ParseException e) { // otherwise
			myCalendar = Calendar.getInstance(); // use today's date
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
			uri = ContentUris.withAppendedId(baseUri, projRecId);
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
				mContext.setText(c.getString(c.getColumnIndexOrThrow("Context")));
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
			values.clear();
			switch (v.getId()) {
			case R.id.txt_projcode:
				values.put("ProjCode", mProjCode.getText().toString().trim());
				break;
			case R.id.txt_descr:
				values.put("Description", mDescription.getText().toString().trim());
				break;
			case R.id.txt_context:
				values.put("Context", mContext.getText().toString().trim());
				break;
			case R.id.txt_caveats:
				values.put("Caveats", mCaveats.getText().toString().trim());
				break;
			case R.id.txt_person:
				values.put("ContactPerson", mContactPerson.getText().toString().trim());
				break;			
			case R.id.txt_date_from: // this one is not focusable
				values.put("StartDate", mStartDate.getText().toString().trim());
				break;
			case R.id.txt_date_to: // this one is not focusable
				values.put("EndDate", mEndDate.getText().toString().trim());
				break;
			default: // save everything
				values.put("ProjCode", mProjCode.getText().toString().trim());
				values.put("Description", mDescription.getText().toString().trim());
				values.put("Context", mContext.getText().toString().trim());
				values.put("Caveats", mCaveats.getText().toString().trim());
				values.put("ContactPerson", mContactPerson.getText().toString().trim());
				values.put("StartDate", mStartDate.getText().toString().trim());
				values.put("EndDate", mEndDate.getText().toString().trim());
				}
			Log.v("EditProj", "Saving record in onFocusChange; values: " + values.toString().trim());
			int numUpdated = saveProjRecord();
			}		
		}


	@Override
	public void onCancel (DialogInterface dialog) {
		// update the project record in the database, if everything valid
//		Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "projects"), projRecId);
		Log.v("EditProj", "Saving record in onCancel; uri: " + uri.toString().trim());
		
		values.clear();
		values.put("ProjCode", mProjCode.getText().toString().trim());
		values.put("Description", mDescription.getText().toString().trim());
		values.put("Context", mContext.getText().toString().trim());
		values.put("Caveats", mCaveats.getText().toString().trim());
		values.put("ContactPerson", mContactPerson.getText().toString().trim());
		values.put("StartDate", mStartDate.getText().toString().trim());
		values.put("EndDate", mEndDate.getText().toString().trim());
		Log.v("EditProj", "Saving record in onCancel; values: " + values.toString());
		int numUpdated = saveProjRecord();
	}
	
	private int saveProjRecord () {
		if ("" + mProjCode.getText().toString().trim() == "") {
			Toast.makeText(this.getActivity(),
					"Need Project Code",
					Toast.LENGTH_LONG).show();
			return 0;
		}
		ContentResolver rs = getActivity().getContentResolver();
		if (projRecId == 0) { // new record
			uri = rs.insert(baseUri, values);
			Log.v("EditProj", "new record in saveProjRecord; returned URI: " + uri.toString());
			projRecId = Long.parseLong(uri.getLastPathSegment());
			uri = ContentUris.withAppendedId(baseUri, projRecId);
			Log.v("EditProj", "new record in saveProjRecord; URI re-parsed: " + uri.toString());
//			NewVisitFragment.saveDefaultPlotTypeId(projRecId);
			return 1;
		} else {
			int numUpdated = rs.update(uri, values, null, null);
			Log.v("EditProj", "Saved record in saveProjRecord; numUpdated: " + numUpdated);
			return numUpdated;
		}
	}
}

