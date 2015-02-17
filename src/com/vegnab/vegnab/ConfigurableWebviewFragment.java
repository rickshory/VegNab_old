package com.vegnab.vegnab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ConfigurableWebviewFragment extends Fragment {
	final static String ARG_TAG_ID = "tagId";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		// if the activity was re-created (e.g. from a screen rotate)
		// restore the previous subplot remembered by onSaveInstanceState()
		// This is mostly needed in fixed-pane layouts
		if (savedInstanceState != null) {
			//mCurrentSubplot = savedInstanceState.getInt(ARG_SUBPLOT);
		}
		// inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_test_webview, container, false);
		WebView myWebView = (WebView) rootView.findViewById(R.id.webview);
		myWebView.loadUrl("http://www.vegnab.com");
		return rootView;
	}

}
