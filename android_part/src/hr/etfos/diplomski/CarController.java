package hr.etfos.diplomski;

import hr.etfos.diplomski.Bluetooth.BluetoothConnection;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class CarController {
	private int halfOfCols;
	
	private double mInitialContureArea;
	private int centreBound = 75;
    private double mPrecentOfArea = 0.2;
    private double upperAreaB; 
	private double lowerAreaB;
	private int rightBound; // maxRightBound = width
	private int leftBound; // maxLeftBound = 0
	
	private int speed = 225;
	private int speedlr = 255;
	
	private BluetoothConnection mBTConnection;
	private boolean mConnection = true;
	private boolean mSocketReady = false;
	
	private boolean mMoveRL = false;
	private boolean mDataSend2 = false;
	
	private boolean idle = true;
	
	private Activity activity;

    /**Constructor, cols and rows are for calculating centre of frame
     * @param cols number of cols in image
     * @param rows number of rows
     * @param initArea initial size of contoure
     * @param thiz is Activitiy for showing Toasts, etc.*/
    public CarController(Activity thiz)
    {
    	activity = thiz;	
 
    	//SVE POTREBNO ZA BLUETOOTH KONEKCIJU
    	mBTConnection = BluetoothConnection.getInstance();
    	if(mBTConnection == null)
    	{
    		activity.setResult(StaticVariables.BLUETOOTH_NULL);
    		activity.finish();
    	}
    		
    	//ovo zamjeniti sa obicnom niti!
		if(!mBTConnection.openSocket())
    	{
    		mSocketReady = false;
    		Toast.makeText(activity.getApplicationContext(), "Can't open Socket. Try again!", Toast.LENGTH_LONG).show();
    		
    		activity.setResult(StaticVariables.BSOCKET_PROBLEM_CLOSE);
    		activity.finish();
    	}
		else{
			mSocketReady = true;
			Toast.makeText(activity.getApplicationContext(), "Socket is ready", Toast.LENGTH_SHORT).show(); 
			
			/* Pocni slusati dolazni stream sa arduina */
	    	//mBTConnection.start();
	    	Toast.makeText(activity.getApplicationContext(), "Connected to "+ mBTConnection.getBluetoothName(), Toast.LENGTH_LONG).show();
	    	}
		}
    
    /**Function that move car depends on Moments and size of Conture
     * @param position za kretanje lijevo-densno
     * @param mContureArea za kretanje napriejd nazad*/
    public void moveCar(Point position, double mContureArea)
    {    	
    	/*
    	 * prioritet ima okretanje lijevo-desno, nakon toga se krece naprijed nazad*/
    	if(position.x > leftBound && position.x < rightBound)
    	{
    		//u jednome trenutku predmet se nalazi unutar grancia, ali je udaljen
    		//autic ne zeli ici naprijed sve dok se on ne priblizi dovoljno blizu
    		//predpostavljam da se mDataSend2 zadrzi u false
    		if( mContureArea < upperAreaB && mContureArea > lowerAreaB && mDataSend2)
            {    	
        		mConnection = mBTConnection.write("S:");
            	mDataSend2 = false;
            	
            	idle = true;
            }
    		else if( mContureArea > upperAreaB && !mDataSend2)
            {
        		mConnection = mBTConnection.write("S:B:"+speed+":");
        		
            	mDataSend2 = true;
            	idle = false;
     
            }
            else if( mContureArea < lowerAreaB && !mDataSend2)
            {
        		mConnection = mBTConnection.write("S:F:"+speed+":");
        		
            	mDataSend2 = true;
            	idle = false;
            }
    		
    	}
    	else if(position.x > rightBound)
    	{

    		if(mDataSend2)
    		{
    			mConnection = mBTConnection.write("S:");
    		}
    		
    		if(!idle)
    			mConnection = mBTConnection.write("TL:"+speedlr+":10:"); //motorici su zamjenjeni na auticu
    		else 
    			mConnection = mBTConnection.write("RL:"+speedlr+":10:");	
    		
			mDataSend2 = false;
    	}
    	else if(position.x < leftBound)
    	{
    		if(mDataSend2)
    		{
    			mConnection = mBTConnection.write("S:");
    		}
    		
    		if(!idle)
    			mConnection = mBTConnection.write("TR:"+speedlr+":10:");
    		else 
    			mConnection = mBTConnection.write("RR:"+speedlr+":10:");
    		
			mDataSend2 = false;
    	}
  	
    	if(!mConnection)
        {
        	activity.setResult(StaticVariables.CONNECTION_LOST);
        	activity.finish();
        }
    }
       
    /**Function that open Bluetooth connection if it is closed*/
    public boolean openConnectionIfClosed()
    { 
        if(!mBTConnection.isSocketOpen())
        {
        	if(!mBTConnection.openSocket())
        	{
        		mSocketReady = false;
        		Toast.makeText(activity.getApplicationContext(), "Can't open Socket. Try again!", Toast.LENGTH_LONG).show();
        	
        	}else
        	{
        		mSocketReady = true;
        	}
        }

    	
    	Toast.makeText(activity.getApplicationContext(), "Socket is ready", Toast.LENGTH_SHORT).show();    
    	return mSocketReady;
    }
    
    /**Function that close socket and make mSocketReady false*/
    public void closeConnection()
    {
        
        if(!mBTConnection.closeSocket())
        {
        	//throws exception if there is an error
        	activity.setResult(StaticVariables.BSOCKET_PROBLEM_CLOSE);
        	activity.finish();
        }
        else
        {
        	mSocketReady = false;
        }
    }
    
    /**Function that release Bluetooth object*/
    public void release()
    {
    	if(!mBTConnection.write("S:"))
        {
        	activity.setResult(StaticVariables.CONNECTION_LOST);
        	activity.finish();
        }
    	closeConnection();
    	mBTConnection.deleteInstance(); 
    }
    
    public boolean isSocketReady()
    {
    	return mSocketReady;
    }
 
    /**Sets centre of frame width and initial bound for right and left movment*/
    public void setCentreOfWidth(int cols){
    	this.halfOfCols = cols;
    	
    	rightBound = halfOfCols + centreBound ; // maxRightBound = width
    	leftBound = halfOfCols - centreBound; // maxLeftBound = 0
    }
    
    public void updateBound(Rect rect)
    {
    	centreBound = (rect.width) > halfOfCols ? halfOfCols : (rect.width); //ne vece od pola framea
    	centreBound = centreBound < 75 ? 75 : centreBound; //ne manje od 50
    	
    	rightBound = halfOfCols + centreBound ; // maxRightBound = width
    	leftBound = halfOfCols - centreBound; // maxLeftBound = 0
    	

    	Log.i("MSG", centreBound + " " + rect.width);
    }
    
    /**Doraditi!*/
    public void updateSpeed(double newArea)
    {
    	
    	if( newArea < upperAreaB && newArea > lowerAreaB)
        {    	
    		speed = 225;
        }
		else if( newArea > upperAreaB || newArea < lowerAreaB)
        {
    		speed = speed < 255 ? speed++ : 255;
        }
    }
    
    /** Sets initial Area Value and inital bound for Forward and backward movment*/
    public void setInitialContourArea(double initArea){

    	mInitialContureArea = initArea;

    	upperAreaB = mInitialContureArea*(1 + mPrecentOfArea);
    	lowerAreaB = mInitialContureArea*(1 - mPrecentOfArea);
    }

    public int getRightBound()
    {
    	return rightBound;
    }
    
    public int getLeftBound()
    {
    	return leftBound;
    }
    
    public double getUpperAreaB()
    {
    	return upperAreaB;
    }
    
    public double getLowerAreaB()
    {
    	return lowerAreaB;
    }
        
    public void stopCar()
    {
    	if(!mBTConnection.write("S:"))
        {
        	activity.setResult(StaticVariables.CONNECTION_LOST);
        	activity.finish();
        }
    }
}
