package com.vegnab.vegnab;

import com.vegnab.vegnab.contentprovider.ContentProvider_VegNab;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddSpeciesNamerDialog extends DialogFragment
		implements android.view.View.OnClickListener {
	Uri uri, baseUri = Uri.withAppendedPath(ContentProvider_VegNab.CONTENT_URI, "namers");
	ContentValues values = new ContentValues();
	private EditText mNamer;
	String mName;
	
	static AddSpeciesNamerDialog newInstance() {
		AddSpeciesNamerDialog f = new AddSpeciesNamerDialog();
		// supply arguments
		Bundle args = new Bundle();
//		args.putLong("projRecId", projRecId);
//		args.putString("projCode", projCode);
		f.setArguments(args);
		return f;
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_new_namer, root);

		mNamer = (EditText) view.findViewById(R.id.txt_new_namer);
		Button btnCancel = (Button) view.findViewById(R.id.btn_new_namer_cancel);
		btnCancel.setOnClickListener(this);
		Button btnSave = (Button) view.findViewById(R.id.btn_new_namer_save);
		btnSave.setOnClickListener(this);
		getDialog().setTitle(R.string.add_namer_title);
		return view;
	}	

	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
		case R.id.btn_new_namer_cancel:
//			Toast.makeText(this.getActivity(), 
//					"'Cancel' button clicked" , 
//					Toast.LENGTH_SHORT).show();
			dismiss();
			break;
		case R.id.btn_new_namer_save:
			Toast.makeText(this.getActivity(), 
					"'Save' button clicked" , 
					Toast.LENGTH_SHORT).show();
			break;
 		}
	}
}
