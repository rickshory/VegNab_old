package com.vegnab.vegnab;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.net.Uri;
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
//			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, mProjRecId: " + mProjRecId);
//			Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, mProjCode: " + mProjCode);
		}
		AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		bld.setTitle(R.string.del_proj_confirm).setMessage(mProjCode)
			.setPositiveButton(R.string.action_affirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, Positive button clicked");
					Log.v("ConfirmDelProj", "In ConfirmDelProjDialog about to delete Project record id=" + mProjRecId);
					
					//"DELETE FROM Projects WHERE _id = ?;" {"" + mProjRecId}
					Uri uri = ContentUris.withAppendedId(
							Uri.withAppendedPath(
							ContentProvider_VegNab.CONTENT_URI, "projects"), mProjRecId);
					Log.v("ConfirmDelProj", "In ConfirmDelProjDialog URI: " + uri.toString());
					ContentResolver rs = getActivity().getContentResolver();
					int numDeleted = rs.delete(uri, null, null);
					Log.v("ConfirmDelProj", "In ConfirmDelProjDialog numDeleted: " + numDeleted);
				}
				
			})
			.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	Log.v("ConfirmDelProj", "In ConfirmDelProjDialog DialogFragment, onCreateDialog, Negative button clicked");
	                // User cancelled the dialog
	            }
	        });
		return bld.create();
	}
}