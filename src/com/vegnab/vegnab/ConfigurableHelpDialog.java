package com.vegnab.vegnab;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConfigurableHelpDialog extends DialogFragment 	
{
	private static final String LOG_TAG = ConfigurableHelpDialog.class.getSimpleName();

	private TextView mTxtHelpMsg;
	String mStringHelpTitle, mStringHelpMessage;
	
	static ConfigurableHelpDialog newInstance(String helpTitle, String helpMessage) {
		ConfigurableHelpDialog f = new ConfigurableHelpDialog();
		// supply title and message as arguments
		Bundle args = new Bundle();
		args.putString("helpTitle", helpTitle);
		args.putString("helpMessage", helpMessage);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help_configurable, root);
		mTxtHelpMsg = (TextView) view.findViewById(R.id.lbl_help_text);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// during startup, check if arguments are passed to the fragment
		// this is where to do this because the layout has been applied
		// to the fragment
		Bundle args = getArguments();
		
		if (args != null) {
			mStringHelpTitle = args.getString("helpTitle"); 
			mStringHelpMessage = args.getString("helpMessage");
//			getDialog().setTitle(R.string.help_generic_text);
//			mTxtHelpMsg.setText(R.string.help_generic_text);
			getDialog().setTitle(mStringHelpTitle);
			mTxtHelpMsg.setText(mStringHelpMessage);
		}
	}
}
