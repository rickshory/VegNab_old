package com.vegnab.vegnab;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;

public class VegItemAdapter extends ResourceCursorAdapter {

	public VegItemAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		// TODO Auto-generated method stub
		
	}

}
