package hr.etfos.diplomski.Activities;

import hr.etfos.diplomski.R;
import hr.etfos.diplomski.StaticVariables;
import hr.etfos.diplomski.Bluetooth.BluetoothConnection;
import hr.etfos.diplomski.Bluetooth.BluetoothCustomAdapter;
import hr.etfos.diplomski.R.id;
import hr.etfos.diplomski.R.layout;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
    private static final String    TAG = "MainActivity";
    private Button btn;
    private Button refreshBtn;
    private ListView list;
    
    /* Bluetooth variables */
    private BluetoothAdapter adapter;
    private BluetoothCustomAdapter btAdapter;
    private BroadcastReceiver mReciever;
    private BluetoothDevice device;
    final List<BluetoothDevice> listNext = new ArrayList<BluetoothDevice>();
    private BluetoothConnection mBTConnection;
    
    
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
                
        
        /* Making adapter for list */
        btAdapter = new BluetoothCustomAdapter(this, R.layout.list_item, listNext);
        list = (ListView) findViewById(R.id.deviceList);
        list.setAdapter(btAdapter);
        
        btn = (Button) findViewById(R.id.buttonId);  
        btn.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View v) {

				int rbPosition = btAdapter.getPositionOfSelectedRadioButton();
				if(rbPosition == -1)
				{
					Log.i(TAG, "Can't find device!");
					Toast.makeText(getApplicationContext(), "Device isn't selected. Can't find device!", Toast.LENGTH_SHORT).show();
				}
				else
				{
					device = listNext.get(rbPosition);
					
					Toast.makeText(getApplicationContext(), "Connecting to " + device.getName() + "! Please wait...", Toast.LENGTH_SHORT).show();
					
					cancelIfDiscovering();
			
					mBTConnection = BluetoothConnection.getInstance(device);
					if(mBTConnection != null)
					{
						Intent intent = new Intent(MainActivity.this, CameraActivity.class);
						MainActivity.this.startActivityForResult(intent, StaticVariables.SECOND_ACTIVITY);
					}
				}
			}
		});
        
        /*
        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				refreshDiscovery();
			}
		});
        */
        /* KAD NESTANE UREÐAJ IZ BLIZINE TREBA PROMJENITI LISTVIJEV  - privremeno napravljen refresh button */
         
        /* BroadcasteReciever update ListItems when find new device */
        mReciever = new BroadcastReceiver(){
        	
        	@Override
        	public void onReceive(Context context, Intent intent) {
        		
        		String intenetAction = intent.getAction(); // ACTION_FOUND, ako je pronaðen
        		
        		/* Device is found */
        		if(BluetoothDevice.ACTION_FOUND.equals(intenetAction))
        		{
        			BluetoothDevice br_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        			try{
        				if(!listNext.contains(br_device))
            			{
            				listNext.add(br_device);
            				btAdapter.notifyDataSetChanged();
            			}
        			}
        	        catch(Exception e)
        	        {
        	        	Log.i("adapter", e.getMessage());
        	        }
        		}        		
        	}
        	
        	
        };
       
        
        IntentFilter event = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReciever, event);

    }

    //PROVJERA MOGUCIH ERRORA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == StaticVariables.SECOND_ACTIVITY){
    		if(resultCode == StaticVariables.CONNECTION_LOST)
    		{
    			Toast.makeText(getApplicationContext(), "Connection is lost. Try again", Toast.LENGTH_LONG).show();
    		}
    		else if(resultCode == StaticVariables.BLUETOOTH_NULL)
    		{
    			Toast.makeText(getApplicationContext(), "Bluetooth is null. Try again", Toast.LENGTH_LONG).show();
    		}
    		
    		if(mBTConnection != null)
    			mBTConnection.deleteInstance();
    		
    		refreshDiscovery();
    	}
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        return true;
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();    	
    	cancelIfDiscovering();
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	makeBluetoothAdapter();
    	
    	cancelIfDiscovering();
    	adapter.startDiscovery();
    	
    }

    public void onDestroy() {
    	super.onDestroy();
    	
    	adapter.cancelDiscovery();
    	if(this.mReciever != null)
    		unregisterReceiver(mReciever);
    }
    
    private void cancelIfDiscovering()
    {
    	if(adapter.isDiscovering())
        {
        	adapter.cancelDiscovery();
        }
    }
    
    private void refreshDiscovery()
    {
		listNext.clear();
		btAdapter.notifyDataSetChanged();
		
		cancelIfDiscovering();
		adapter.startDiscovery();
    }
    
    private void makeBluetoothAdapter()
    {
        /* Check if bluetooth is turned on */
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
        {
        	Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        	this.finish();
        	return;
        }
        else if(!this.adapter.isEnabled())
        {
        	Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(turnBluetoothOn, 0);
        	
        	Toast.makeText(getApplicationContext(), "Turn Bluetooth on!", Toast.LENGTH_LONG).show();
        }
    }
    
}
