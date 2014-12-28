package com.vegnab.vegnab;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

public class DatePickerFragment extends DialogFragment 
		implements DatePickerDialog.OnDateSetListener {
	
	private FragmentActivity myContext;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Toast.makeText(this.getActivity(), 
				"Year/Month/Day: " + year + "/" + month + "/" + day , 
				Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onAttach(Activity activity) {
	    myContext=(FragmentActivity) activity;
	    super.onAttach(activity);
	}
	
	public void showDatePickerDialog(View v) {
		DatePickerFragment newFragment = new DatePickerFragment();
	    FragmentManager fm = myContext.getSupportFragmentManager();
	    newFragment.show(fm, "datePicker");
	}	
	/*			EditProjectDialog editProjDlg = new EditProjectDialog();
			FragmentManager fm = getSupportFragmentManager();
			editProjDlg.show(fm, "frg_edit_proj");
*/
}
