package hr.etfos.diplomski.Bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * This class allows you to insecure connect to bluetooth module.
 * @author Antonio.Loncar.
 *
 */
public class BluetoothConnection extends Thread{
    private static final String    TAG = "BluetoothConnection";
    
    private static BluetoothConnection instance = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
   
    /* Bluetooth variables */
    private BluetoothDevice device;
    private BluetoothSocket socket;    
    private OutputStream os = null;
    private InputStream is = null;
    
    private String adress;
    private String name;
    
	private BluetoothConnection(BluetoothDevice device)
	{
		this.adress = device.getAddress();
		this.name = device.getName();
	}
	
	/**
	 * Method that make only one instnace of class. */
	public static BluetoothConnection getInstance(BluetoothDevice device)
	{
		if(instance == null)
			instance = new BluetoothConnection(device);

		return instance;
	}
	
	/* NIJE SIGURNO!! */
	/**
	 * Use only when you already instanced object.*/
	public static BluetoothConnection getInstance()
	{
		return instance;
	}
	
	/**
	 * Method opens socket 
	 * @return false if .invoke() or .connection throws exception*/
	public boolean openSocket()
	{
    	try {
    		device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(adress);
    		
			/*
			 * socket = device.createRfcommSocketToServiceRecord(MY_UUID);
			 * socket = (BluetoothSocket) m.invoke(device, MY_UUID);
			 * m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });*/
    		
			 Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
			 socket = (BluetoothSocket) m.invoke(device, 1);
    		
    		
			Log.i(TAG, "Socket is opening!");
		} catch (Exception e) {
			Log.i("Error", e.getMessage());
			return false;
		}
    	
    	OutputStream tmpos = null;
    	InputStream tmpis = null;

    	try {
			socket.connect();
			
			tmpos = socket.getOutputStream();
			tmpis = socket.getInputStream();
			
		} catch (IOException e) {
			Log.i("Error", e.getMessage());
			return false;
		}
    	
    	is = tmpis;
    	os = tmpos;
    	
    	return true;
	}

	/**
	 * Method closes socket.
	 * @return false if .close() function throws exception */
	public boolean closeSocket()
	{
		try {
			socket.close();

			Log.i(TAG, "Socket is closed!");
		} catch (IOException e) {
			Log.i("Error", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Method sends string throug socket
	 * @param str String that will be sent. 
	 * @return false if .write() or .flush() throws exception*/
	public boolean write(String msg){
		
		byte[] b = msg.getBytes();
		try {
			os.write(b);
			os.flush();
			
		} catch (IOException e) {
			Log.i("Error", e.getMessage());
			return false;
		}

		return true;
	}
	
	/** NAPRAVITI DO KRAJA */
	@Override
	public void run() {
		
		byte[] buffer = new byte[256];
		int bytes;
		while(true)
		{
			try {
				bytes = is.read(buffer);
				String s = new String(buffer, 0, bytes);
				Log.i(TAG, "Read: "+ s);
				
			} catch (IOException e) {
				Log.i("Error", e.getMessage());
				break;
			}
			
		}
	}
	
	public String getDeviceAdress()
	{
		return this.adress;
	}
	
	public String getBluetoothName()
	{
		return this.name;
	}
	
	public void deleteInstance()
	{
		//instance.closeSocket();
		instance = null;
	}
	
	@SuppressLint("NewApi") public boolean isSocketOpen()
	{
		return socket.isConnected() == true ? true : false;
	}
}

