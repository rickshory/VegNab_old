package com.vegnab.vegnab;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class ConfirmDelProjDialog extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, savedInstanceState == null");
		} else {
			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, savedInstanceState NOT null");
		}
		AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		bld.setTitle("Delete Project?").setMessage("the project")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
				
			});
		return bld.create();
	}
}
