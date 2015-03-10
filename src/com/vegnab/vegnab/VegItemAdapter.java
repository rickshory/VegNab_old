package com.vegnab.vegnab;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class VegItemAdapter extends ResourceCursorAdapter {
	
	private LayoutInflater mInflater;

	public VegItemAdapter(Context ctx, int layout, Cursor c, int flags) {
		super(ctx, layout, c, flags);
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public void bindView(View v, Context ctx, Cursor c) {
		if(c.getPosition()%2==1) {
//			view.setBackgroundColor(ctx.getResources().getColor(R.color.background_odd));
		} else {
//			view.setBackgroundColor(ctx.getResources().getColor(R.color.background_even));
		}
		TextView vegText = (TextView) v.findViewById(R.id.veg_descr_text);
		vegText.setText(c.getString(c.getColumnIndexOrThrow("SppLine")));
	}
}
