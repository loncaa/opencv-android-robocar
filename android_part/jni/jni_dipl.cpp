#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>

using namespace std;
using namespace cv;

Mat dst, hsvImg, mask;
vector<vector<Point> > contours;
vector<Point> biggestContour;
Moments m;

extern "C" {
	JNIEXPORT jintArray JNICALL Java_hr_etfos_diplomski_ColorBlobDetector_process
	  (JNIEnv *env, jobject, jlong mRgbaAdress, jintArray low, jintArray high);

	JNIEXPORT jintArray JNICALL Java_hr_etfos_diplomski_ColorBlobDetector_process
	  (JNIEnv *env, jobject, jlong mRgbaAdress, jintArray low, jintArray high)
	{
		jintArray result;
		result = env->NewIntArray(2); //pravi niz za vracanje

		jint* lowArray = env->GetIntArrayElements(low, 0);
		jint* highArray = env->GetIntArrayElements(high, 0);

		jint centroidCoordinates[2];
		Mat& mRgba = *(Mat*) mRgbaAdress;

		pyrDown(mRgba, dst, Size( mRgba.cols/2, mRgba.rows/2 ));
		pyrDown(dst, dst, Size( dst.cols/2, dst.rows/2 ));
		cvtColor(dst, hsvImg, CV_RGB2HSV_FULL);

		inRange(hsvImg, Scalar(lowArray[0], lowArray[1], lowArray[2]), Scalar(highArray[0], highArray[1], highArray[2]), mask);
		dilate(mask, mask, Mat());

		findContours(mask, contours, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

		if(contours.size() > 0)
		{
			double max = contourArea(contours[1], false);
			for(int i = 1; i < contours.size(); ++i)
			{

				double area = contourArea(contours[i], false);
				if(area > max)
				{
					max = area;
					biggestContour = contours[i];
				}
			}

			multiply(biggestContour, Scalar(4,4), biggestContour);
			m = moments(biggestContour, false);

			centroidCoordinates[0] = m.m10 / m.m00; // x koordinata
			centroidCoordinates[1] = m.m01 / m.m00; // y koordinata

			circle(mRgba, Point(centroidCoordinates[0], centroidCoordinates[1]), 10, Scalar(255,0,0));
		}
		else
		{
			centroidCoordinates[0] = -1; // x koordinata
			centroidCoordinates[1] = -1; // y koordinata
		}


		env->ReleaseIntArrayElements(low, lowArray, 0);
		env->ReleaseIntArrayElements(high, highArray, 0);
		env->SetIntArrayRegion(result, 0, 2, centroidCoordinates); //
		return result;
	}


}
