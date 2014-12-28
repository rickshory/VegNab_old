package com.vegnab.vegnab;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class MainVNActivity extends ActionBarActivity 
		implements NewVisitFragment.OnButtonListener, 
		VisitHeaderFragment.OnButtonListener, 
		VegSubplotFragment.OnButtonListener {

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
			NewVisitFragment newVisitFrag = new NewVisitFragment();
			
			// in case this activity were started with special instructions from an Intent
			// pass the Intent's Extras to the fragment as arguments
			newVisitFrag.setArguments(getIntent().getExtras());
			
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();			
			transaction.add(R.id.fragment_container, newVisitFrag);
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.new_visit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_app_info:
			Toast.makeText(getApplicationContext(), "''App Info'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_edit_proj:
			EditProjectDialog editProjDlg = new EditProjectDialog();
			FragmentManager fm = getSupportFragmentManager();
			editProjDlg.show(fm, "frg_edit_proj");
			return true;
		case R.id.action_new_proj:
			Toast.makeText(getApplicationContext(), "''New Project'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_new_plottype:
			Toast.makeText(getApplicationContext(), "''New Plot Type'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_old_visit:
			Toast.makeText(getApplicationContext(), "''Re-open Visit'' is not implemented yet", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.action_settings:
			Toast.makeText(getApplicationContext(), "''Settings'' is not implemented yet", Toast.LENGTH_SHORT).show();
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

	@Override
	public void onNewVisitGoButtonClicked() {
		VisitHeaderFragment visHdrFrag = new VisitHeaderFragment();
		Bundle args = new Bundle();
		args.putInt(VisitHeaderFragment.ARG_SUBPLOT, 0); // start with dummy value, subplot 0
		visHdrFrag.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// replace the fragment in the fragment container with this new fragment and
		// put the present fragment on the backstack so the user can navigate back to it
		transaction.replace(R.id.fragment_container, visHdrFrag);
		transaction.addToBackStack("(start visit)");
		transaction.commit();		
	}
}
