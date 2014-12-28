package com.vegnab.vegnab;

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
import android.widget.EditText;

public class EditProjectDialog extends DialogFragment implements OnClickListener {
	Button buttonSetDateStart;
	private EditText mEditProjCode;
	EditText mEditDateFrom;
	
	public EditProjectDialog() {
		// Empty constructor required for DialogFragment
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_project, root);
		mEditProjCode = (EditText) view.findViewById(R.id.txt_projcode);
		// txt_date_from
		mEditDateFrom = (EditText) view.findViewById(R.id.txt_date_from);
		mEditDateFrom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("EditProj", "Event caught in EditProjectFragment, anonymous onClick");
				DatePickerFragment newFragment = new DatePickerFragment();
				FragmentManager fm = getChildFragmentManager();
				newFragment.show(fm, "datePicker");			
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
