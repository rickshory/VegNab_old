package com.vegnab.vegnab;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;

import com.google.android.gms.location.LocationRequest;
import com.vegnab.vegnab.database.VNContract.Loaders;
import com.vegnab.vegnab.database.VNContract.Prefs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TestDownloadFragment extends Fragment 
		implements OnClickListener {
	 
	 private DownloadManager downloadManager;
	 private long downloadReference;
	 Button mBtnStartDownload, mBtnDisplayDownload, mBtnCheckStatus, mBtnCancelDownload;
	 TextView mTxtVwShowSpp;
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, 
				Bundle savedInstanceState) {
			// if the activity was re-created (e.g. from a screen rotate)
			// restore the previous screen, remembered by onSaveInstanceState()
			// This is mostly needed in fixed-pane layouts
			if (savedInstanceState != null) {
			}
			// inflate the layout for this fragment
			View rootView = inflater.inflate(R.layout.fragment_test_download, container, false);
			//start download button
			mBtnStartDownload = (Button) rootView.findViewById(R.id.startDownload);
			mBtnStartDownload.setOnClickListener(this);
			
			//display all download button
			mBtnDisplayDownload = (Button) rootView.findViewById(R.id.displayDownload);
			mBtnDisplayDownload.setOnClickListener(this);
			
			//check download status button
			mBtnCheckStatus = (Button) rootView.findViewById(R.id.checkStatus);
			mBtnCheckStatus.setOnClickListener(this);
			mBtnCheckStatus.setEnabled(false);
			
			//cancel download button
			mBtnCancelDownload = (Button) rootView.findViewById(R.id.cancelDownload);
			mBtnCancelDownload.setOnClickListener(this);
			mBtnCancelDownload.setEnabled(false);
			
			mTxtVwShowSpp = (TextView) rootView.findViewById(R.id.spp_data);
			
			//set filter to only when download is complete and register broadcast receiver
			IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			getActivity().registerReceiver(downloadReceiver, filter);

			return rootView;
		}
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setHasOptionsMenu(true);
	 }
	 
	 public void onClick(View v) {
	 
	  switch (v.getId()) {
	 
	  //start the download process
	  case R.id.startDownload:
	 
	   downloadManager = (DownloadManager)getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
	   Uri Download_Uri = Uri.parse("http://www.vegnab.com/specieslists/AFewSpp.txt");
	   DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
	    
	   //Restrict the types of networks over which this download may proceed.
	   request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
	   //Set whether this download may proceed over a roaming connection.
	   request.setAllowedOverRoaming(false);
	   //Set the title of this download, to be displayed in notifications (if enabled).
	   request.setTitle("My Data Download");
	   //Set a description of this download, to be displayed in notifications (if enabled)
	   request.setDescription("Android Data download using DownloadManager.");
	   //Set the local destination for the downloaded file to a path within the application's external files directory
	   request.setDestinationInExternalFilesDir(getActivity(),Environment.DIRECTORY_DOWNLOADS,"CountryList.json");
	 
	   //Enqueue a new download and same the referenceId
	   downloadReference = downloadManager.enqueue(request);
	   
	   mTxtVwShowSpp.setText("Getting data from Server, Please WAIT...");
	    
	   mBtnCheckStatus.setEnabled(true);
	   mBtnCancelDownload.setEnabled(true);
	   break;
	 
	  //display all downloads 
	  case R.id.displayDownload: 
	 
	   Intent intent = new Intent();
	   intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
	   startActivity(intent);
	   break;
	 
	  //check the status of a download 
	  case R.id.checkStatus: 
	 
	   Query myDownloadQuery = new Query();
	   //set the query filter to our previously Enqueued download 
	   myDownloadQuery.setFilterById(downloadReference);
	 
	   //Query the download manager about downloads that have been requested.
	   Cursor cursor = downloadManager.query(myDownloadQuery);
	   if(cursor.moveToFirst()){
	    checkStatus(cursor);
	   }
	   break;
	   
	  //cancel the ongoing download 
	  case R.id.cancelDownload: 
	 
	   downloadManager.remove(downloadReference);
	   mBtnCheckStatus.setEnabled(false);
	   mTxtVwShowSpp.setText("Download of the file cancelled...");
	    
	   break; 
	 
	   // More buttons go here (if any) ...
	 
	  }
	 }
	 
	 
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.test_download, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}	 
	 
	 
	 private void checkStatus(Cursor cursor){
	   
	  //column for status
	  int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
	  int status = cursor.getInt(columnIndex);
	  //column for reason code if the download failed or paused
	  int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
	  int reason = cursor.getInt(columnReason);
	  //get the download filename
	  int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
	  String filename = cursor.getString(filenameIndex);
	 
	  String statusText = "";
	  String reasonText = "";
	 
	  switch(status){
	  case DownloadManager.STATUS_FAILED:
	   statusText = "STATUS_FAILED";
	   switch(reason){
	   case DownloadManager.ERROR_CANNOT_RESUME:
	    reasonText = "ERROR_CANNOT_RESUME";
	    break;
	   case DownloadManager.ERROR_DEVICE_NOT_FOUND:
	    reasonText = "ERROR_DEVICE_NOT_FOUND";
	    break;
	   case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
	    reasonText = "ERROR_FILE_ALREADY_EXISTS";
	    break;
	   case DownloadManager.ERROR_FILE_ERROR:
	    reasonText = "ERROR_FILE_ERROR";
	    break;
	   case DownloadManager.ERROR_HTTP_DATA_ERROR:
	    reasonText = "ERROR_HTTP_DATA_ERROR";
	    break;
	   case DownloadManager.ERROR_INSUFFICIENT_SPACE:
	    reasonText = "ERROR_INSUFFICIENT_SPACE";
	    break;
	   case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
	    reasonText = "ERROR_TOO_MANY_REDIRECTS";
	    break;
	   case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
	    reasonText = "ERROR_UNHANDLED_HTTP_CODE";
	    break;
	   case DownloadManager.ERROR_UNKNOWN:
	    reasonText = "ERROR_UNKNOWN";
	    break;
	   }
	   break;
	  case DownloadManager.STATUS_PAUSED:
	   statusText = "STATUS_PAUSED";
	   switch(reason){
	   case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
	    reasonText = "PAUSED_QUEUED_FOR_WIFI";
	    break;
	   case DownloadManager.PAUSED_UNKNOWN:
	    reasonText = "PAUSED_UNKNOWN";
	    break;
	   case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
	    reasonText = "PAUSED_WAITING_FOR_NETWORK";
	    break;
	   case DownloadManager.PAUSED_WAITING_TO_RETRY:
	    reasonText = "PAUSED_WAITING_TO_RETRY";
	    break;
	   }
	   break;
	  case DownloadManager.STATUS_PENDING:
	   statusText = "STATUS_PENDING";
	   break;
	  case DownloadManager.STATUS_RUNNING:
	   statusText = "STATUS_RUNNING";
	   break;
	  case DownloadManager.STATUS_SUCCESSFUL:
	   statusText = "STATUS_SUCCESSFUL";
	   reasonText = "Filename:\n" + filename;
	   break;
	  }
	 
	 
	  Toast toast = Toast.makeText(getActivity(), 
	    statusText + "\n" + 
	    reasonText, 
	    Toast.LENGTH_LONG);
	  toast.setGravity(Gravity.TOP, 25, 400);
	  toast.show();
	 
	 }
	 
	 private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
	 
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    
	   //check if the broadcast message is for our Enqueued download
	   long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
	   if(downloadReference == referenceId){
	    
	    mBtnCancelDownload.setEnabled(false);

	    ParcelFileDescriptor file;
	    //parse the data
	    try {
	     file = downloadManager.openDownloadedFile(downloadReference);
	     // 'file' here is a pointer, cannot directly read InputStream
	     InputStream is = new FileInputStream(file.getFileDescriptor()); // use getFileDescriptor to get InputStream
	     // wrap InputStream with an InputStreamReader, which is wrapped by a BufferedReader, "trick" to use readLine() fn
	     BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	     StringBuilder lines = new StringBuilder();
	     String line;
	     while ((line = br.readLine()) != null) {
	    	 // gets the lines one at a time, strips the delimiters
	    	 lines.append(line); // mashes all the lines together, with no delimiters
	     }
	     br.close();

	     mTxtVwShowSpp.setText(lines.toString()); // lines are all combined, will parse them into DB in final version
	      
	     Toast toast = Toast.makeText(getActivity(), 
	       "Downloading of data just finished", Toast.LENGTH_LONG);
	     toast.setGravity(Gravity.TOP, 25, 400);
	     toast.show();
	      
	    } catch (FileNotFoundException e) {
	     e.printStackTrace();
	    } catch (IOException e) {
	     e.printStackTrace();
//	    } catch (JSONException e) {
//	     e.printStackTrace();
	    }
	 
	   }
	  }
 };
}
