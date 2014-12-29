package com.vegnab.vegnab;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class EditProjectDialog extends DialogFragment implements OnClickListener {
	int projRecId = 0; // zero default means new or not specified yet
//	Button buttonSetDateStart;
	private EditText mEditProjCode;
	EditText mEditDateFrom;
	
	static EditProjectDialog newInstance(int projRecId) {
		EditProjectDialog f = new EditProjectDialog();
		// supply projRecId as an argument
		Bundle args = new Bundle();
		args.putInt("projRecId", projRecId);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_project, root);
		mEditProjCode = (EditText) view.findViewById(R.id.txt_projcode);
		// txt_date_from
		final Calendar myCalendar = Calendar.getInstance();
		mEditDateFrom = (EditText) view.findViewById(R.id.txt_date_from);
		
		final DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {

		    @Override
		    public void onDateSet(DatePicker view, int year, int monthOfYear,
		            int dayOfMonth) {
		        Log.v("EditProj", "Event caught in EditProjectDialog, anonymous onDateSet");
		        myCalendar.set(Calendar.YEAR, year);
		        myCalendar.set(Calendar.MONTH, monthOfYear);
		        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//		        updateLabel();
		        mEditDateFrom.setText("" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
		    }

		};
		
/*		mEditDateFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("EditProj", "Event caught in EditProjectDialog, anonymous onClick");
				DatePickerFragment newFragment = new DatePickerFragment();
				FragmentManager fm = getChildFragmentManager();
				newFragment.show(fm, "datePicker");			
			}
		}); */
		mEditDateFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("EditProj", "Event caught in EditProjectFragment, anonymous onClick");
				new DatePickerDialog(getActivity(), myDateListener,
						myCalendar.get(Calendar.YEAR),
						myCalendar.get(Calendar.MONTH),
						myCalendar.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		getDialog().setTitle(R.string.edit_proj_title_edit);
		return view;
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		Log.v("EditProj", "Event caught in EditProjectFragment, DialogInterface onClick");
		
	}

}
