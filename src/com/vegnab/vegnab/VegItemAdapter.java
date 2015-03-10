package com.vegnab.vegnab;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;

public class VegItemAdapter extends ResourceCursorAdapter {
	
	private LayoutInflater mInflater;

	public VegItemAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public void bindView(View v, Context ctx, Cursor c) {
		// TODO Auto-generated method stub
		
	}

}
