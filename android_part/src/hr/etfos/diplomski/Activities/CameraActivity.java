package hr.etfos.diplomski.Activities;

import hr.etfos.diplomski.CarController;
import hr.etfos.diplomski.StaticVariables;
import hr.etfos.diplomski.ImageProcessing.*;
import hr.etfos.diplomski.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.FpsMeter;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Toast;


/* KREŠA SE KAD SE STISNE TIPKA NAZAD - NESTO KOD CAMERAbRIDGEvIEWA NE VALJA */
public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private final String    TAG = "CameraActivity";

    /* OpenCv variables */
    private Mat mRgba;
    
    private boolean mIsColorSelected = false;
    private boolean contourLost = true;

    private double mInitialContureArea;
    
    private CarController mCarController;
    private ColorBlobDetector mDetector;    
    private FpsMeter meter;
    
    private int debugMode = 1;
    private MenuItem mPrewievRoi;
    private MenuItem mPrewievNothing;
    private MenuItem mPrewievBounds;

    
    /* OpenCV */
    //private CameraBridgeViewBase mOpenCvCameraView;
    private CameraClass mOpenCvCameraView;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Load native library after(!) OpenCV initialization */
                    System.loadLibrary("dipl_lib"); //biblioteka dipl_lib.so

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setCvCameraViewListener(CameraActivity.this);
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                    mOpenCvCameraView.setResolution(480, 720); //prvo postavi na 1280-720 pa prebaci na ovu rezoluciju
                    

                	mDetector = new ColorBlobDetector();    // treba biti u onCameraStart
                            
                    meter  = new FpsMeter();
                    meter.init();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_activity);
 
        mOpenCvCameraView = (CameraClass) findViewById(R.id.camera);
        mCarController = new CarController(this);
    }

    /**
     * Initialize Variables when camera is started*/
    public void onCameraViewStarted(int width, int height) {

    	mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    /**
     * Release all mat variables*/
    private void matRelease()
    {
        mRgba.release();
    }
    
    /**
     * Release Variables when camera is stopped*/
    public void onCameraViewStopped() {
    	matRelease();
    }

    /**
     * Do Calculation on every camera frame*/
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	mRgba = inputFrame.rgba();
    	
    	if(mIsColorSelected && !contourLost)
    	{
    		//trazi najvecu konturu na roi-u
    		double mContureArea = mDetector.findMaxContour(mDetector.getRoi());
    		
    		/*Prewiev ROI*/
    		if(debugMode == 2)
    		{
    			Mat roi = mDetector.getRoi();
                Mat roiLabel = mRgba.submat(4, 4 + roi.rows(), 4, 4 + roi.cols());
                roi.copyTo(roiLabel);
    		}

    		if(mContureArea != -1 && mCarController.isSocketReady())
    		{
    			mDetector.updateRoi(mRgba); //pronalazi roi na prosirenom roiu i onda apdejta globalni roi	
    			mCarController.updateBound(mDetector.getRect());  			
    			mCarController.moveCar(mDetector.getCentroid(), mContureArea);
    				
    			/*Preview Bounds*/
    				if(debugMode == 1)
    				{
    				Core.circle(mRgba, mDetector.getCentroid(), 10, new Scalar(255, 0, 0)); 
        			Core.line(mRgba, new Point(mCarController.getLeftBound(), mRgba.rows()/2 ), new Point(mCarController.getRightBound(), mRgba.rows()/2), new Scalar(255, 0, 0) );
        			
        			meter.measure();
        	    	Core.putText(mRgba, meter.getFPSString(), new Point(4, 35), 2, 1, new Scalar(255, 0, 0, 255), 1);
    				}
    			}
    		else{
    			
    			mCarController.stopCar();		
    			contourLost = true;
    		}
    	}
    	else if(contourLost && mIsColorSelected && mCarController.isSocketReady()) // ako je odreðen roi i ako je izgubljena kontura, pretrazuje se cijeli frame
    	{
    		double area = mDetector.findMaxContour(mRgba);
    		
    		if(area != -1)
			{
    			mDetector.initRoi(mRgba);
    			contourLost = false;
			}
    	}
    	
    	
        return mRgba;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

        if(mCarController != null)
        	if(!mCarController.openConnectionIfClosed())
        	{
        		setResult(StaticVariables.BLUETOOTH_NULL);
            	finish();
        	}
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if(!mIsColorSelected)
		{
			//nije najbolji nacin pretvaranja koordinata
			int cols = mRgba.cols();
			int rows = mRgba.rows();  

		    int xpos = (int) (event.getX() * cols) / mOpenCvCameraView.getWidth();
		    int ypos = (int) (event.getY() * rows) / mOpenCvCameraView.getHeight();
			
			if ((xpos < 0) || (ypos < 0) || (xpos > cols) || (ypos > rows)) return false;
			//
			
            mCarController.setCentreOfWidth(mRgba.cols()/2); // treba biti u onCameraStart
			
			//kvadrat 8*8
			Rect roiRect = new Rect();
			roiRect.x = (xpos>4) ? xpos-4 : 0;
			roiRect.y = (ypos>4) ? ypos-4 : 0;
			
			//da ne uzima roi izvan slike
			roiRect.width = (xpos+4 < cols) ? xpos + 4 - roiRect.x : cols - roiRect.x;
			roiRect.height = (ypos+4 < rows) ? ypos + 4 - roiRect.y : rows - roiRect.y;
			
			mDetector.setHsvColor(mRgba, roiRect);
			
			//pronalazi najvecu konturu na clijeom frameu
			mInitialContureArea = mDetector.findMaxContour(mRgba);
			
			if(mInitialContureArea != -1)
			{
				mCarController.setInitialContourArea(mInitialContureArea);
				mDetector.initRoi(mRgba); //pronalazi pocetni roi, koji se u odnosu sa koordinatama cijelog framea	
								
				mIsColorSelected = true;
				contourLost = false;
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Conture Area can't be calculated!", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			mIsColorSelected = false;
			mCarController.stopCar();
		}
		
		return false;
	}

    /* Others */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mPrewievRoi = menu.add("Prewiev Roi");
    	mPrewievNothing = menu.add("No Debug Mode");
    	
    	mPrewievBounds = menu.add("Prewiev Bounds");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == mPrewievNothing)
    	{
    		debugMode = 0;
    	}
    	else if(item == mPrewievRoi)
    	{
    		debugMode = 2;
    		Toast.makeText(getApplicationContext(), "Debug mode " + debugMode, Toast.LENGTH_SHORT).show();
    	}
    	else if(item == mPrewievBounds)
    	{
    		debugMode = 1;
    		Toast.makeText(getApplicationContext(), "Debug mode " + debugMode, Toast.LENGTH_SHORT).show();
    	}
        return true;
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        mIsColorSelected = false;
        mCarController.stopCar();
        mCarController.closeConnection();
    }

    public void onDestroy() 
    {
    	super.onDestroy();
    	
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        mCarController.release();
        mDetector.release();
    }
    
}