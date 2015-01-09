package com.vegnab.vegnab;

import android.os.Bundle;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.View;

public class ClickAnything extends AccessibilityDelegateCompat {
	private static final String LOG_TAG = "Accessibility";
	public boolean performAccessibilityAction(View host, int action, Bundle args) {
		if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
//			return ((MyView) host).doSomething();
			Log.v(LOG_TAG, "onClick; hostID: " + host.getId());
		}
		return super.performAccessibilityAction(host, action, args);
	}

}
