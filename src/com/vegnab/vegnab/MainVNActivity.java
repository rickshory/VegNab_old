package com.vegnab.vegnab;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class MainVNActivity extends ActionBarActivity 
		implements VisitHeaderFragment.OnButtonListener, VegSubplotFragment.OnButtonListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vn_main);
		/* put conditions to test below
		 * such as whether the container even exists in this layout
		 * e.g. if (findViewById(R.id.fragment_container) != null)
		 * */
		if (true) {
			if (savedInstanceState != null) {
				// if restoring from a previous state, do not create
				// could end up with overlapping views
				return;
			}
			
			// create an instance of Visit Header fragment
			VisitHeaderFragment visitHdrFrag = new VisitHeaderFragment();
			
			// in case this activity were started with special instructions from an Intent
			// pass the Intent's Extras to the fragment as arguments
			visitHdrFrag.setArguments(getIntent().getExtras());
			
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();			
			transaction.add(R.id.fragment_container, visitHdrFrag);
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_visit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onNextSubplotButtonClicked(int subpNum) {
		Toast.makeText(getApplicationContext(), "Received value " + subpNum + ", going to " + (subpNum + 1), Toast.LENGTH_SHORT).show();
		// swap new Subplot frag in place of existing one
		// can we use the same name as existing?
		VegSubplotFragment vegSbpFrag = new VegSubplotFragment();
		Bundle args = new Bundle();
		args.putInt(VegSubplotFragment.ARG_SUBPLOT, subpNum + 1); // increment subplot number
		vegSbpFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the existing Subplot fragment (in the container) with this new Subplot fragment
		// and put the existing Subplot fragment on the backstack so the user can navigate back to it
		transaction.replace(R.id.fragment_container, vegSbpFrag);
		transaction.addToBackStack("Subplot " + subpNum);
		transaction.commit();
	}

	public void onVisitHeaderGoButtonClicked() {
//		Toast.makeText(getApplicationContext(), "Main Activity received event from Visit Header", Toast.LENGTH_LONG).show();
		// swap Subplot fragment in place of Header fragment
		VegSubplotFragment vegSbpFrag = new VegSubplotFragment();
		Bundle args = new Bundle();
		args.putInt(VegSubplotFragment.ARG_SUBPLOT, 1); // start with subplot 1
		vegSbpFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		transaction.replace(R.id.fragment_container, vegSbpFrag);
		transaction.addToBackStack("(header)");
		transaction.commit();
	}


}
