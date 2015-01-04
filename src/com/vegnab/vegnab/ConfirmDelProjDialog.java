package com.vegnab.vegnab;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class ConfirmDelProjDialog extends DialogFragment {
	long mProjRecId = 0;
	String mProjCode = "";
	
	static ConfirmDelProjDialog newInstance(long projRecId, String projCode) {
		ConfirmDelProjDialog f = new ConfirmDelProjDialog();
		// supply arguments
		Bundle args = new Bundle();
		args.putLong("projRecId", projRecId);
		args.putString("projCode", projCode);
		f.setArguments(args);
		return f;
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null) {
			mProjRecId = args.getLong("projRecId");
			mProjCode = args.getString("projCode");
			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, mProjRecId: " + mProjRecId);
			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, mProjCode: " + mProjCode);
		}
		AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		bld.setTitle("Delete Project?").setMessage(mProjCode)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
				
			});
		return bld.create();
	}
}
