/**
 * 
 */
package org.qless.up666;

import java.io.DataOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

/**
 * 
 * @author quattro
 * 
 */

public class ImageProcessor {

	/**
	 * 
	 */
	static int maxPixel = 1200;

	/**
	 * @param imagePath
	 * @param httpPostOutputStream
	 */
	public static void process(String imagePath, double oversizeFactor,
			DataOutputStream httpPostOutputStream) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // to get the size without actually
											// loading the image into the
											// memory.
		Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath, options);

		int originalHeight = options.outHeight;
		int originalWidth = options.outWidth;

		int originalPixels = originalHeight * originalWidth;
		// Note: change this to long once gigapixel cameras have arrived

		int maxPixels = maxPixel * maxPixel;
		int maxPixelsForLoading = maxPixels; // multiply by 4 to leave some
													// room and scale down the
													// rest in memory

		double baseInSampleSize = 1;
		Log.i("ImageScale", "maxPixelsForLoading: " + maxPixelsForLoading
				+ " originalPixels: " + originalPixels + " baseInSampleSize: "
				+ baseInSampleSize);

		for (; originalPixels > maxPixelsForLoading; originalPixels /= 4) {
			baseInSampleSize *=2;
		}


		// outMimeType

		options.inJustDecodeBounds = false;
		options.inSampleSize = (int) baseInSampleSize;

		Log.i("ImageScale", "maxPixelsForLoading: " + maxPixelsForLoading
				+ " originalPixels: " + originalPixels + " baseInSampleSize: "
				+ baseInSampleSize);

		bitmapOrg = BitmapFactory.decodeFile(imagePath, options);

		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// calculate the scale 
		float scaleWidth = 1;
		float scaleHeight = 1;
		scaleWidth = scaleHeight = ((float) maxPixels) / originalPixels;
		
		Log.i("ImageScale", "width: " + width + " height: "
				+ height + "scale: " + scaleWidth);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		// matrix.postRotate(45);

		bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90,httpPostOutputStream);

		// recreate the new Bitmap
//		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
//				height, matrix, true);
//
//		resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90,
//				httpPostOutputStream);

	}
}
