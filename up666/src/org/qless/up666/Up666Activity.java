/*
 * Copyright 2011 qorron
 * Contact: https://github.com/qorron
 * 
 * This file is part of 666up!
 * 
 * 666up! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 666up! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 666up!.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.qless.up666;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author quattro
 * 
 *         this does not much. at the moment, it displays some explanation on
 *         how to use the app
 * 
 */
public class Up666Activity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	private TextView mIntruductionBodyView;
	private Button mButtonCamera;
	private Uri imageUri;

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			imageUri = savedInstanceState.getParcelable("uri");
		}
		Log.d("ullilog", "start_intruduction");
		setContentView(R.layout.main);
		mIntruductionBodyView = (TextView) findViewById(R.id.introductionBody);
		Linkify.addLinks(mIntruductionBodyView, Linkify.ALL);

		// check, if we have a camera
		PackageManager pm = this.getPackageManager();
		mButtonCamera = (Button) findViewById(R.id.buttonCam);
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// we do have a camera
			mButtonCamera.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					startCameraIntent();
				}
			});

		} else {
			// we don't have a camera
			mButtonCamera.setVisibility(View.GONE);
		}
	}

	private void startCameraIntent() {
		// define the file-name to save photo taken by Camera activity
		String fileName = "new-photo-name.jpg";
		// create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION,
				"Image capture by camera");
		// imageUri is the current activity attribute, define and save it for
		// later usage (also in onSaveInstanceState)
		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {

				if (imageUri == null) {
					Log.d("imageUri", "null!");
				} else {

					// use imageUri here to access the image
					Bundle b = new Bundle();
					b.putParcelable(Intent.EXTRA_STREAM, imageUri);
					Intent i = new Intent(this, UploadActivity.class);
					i.putExtras(b);
					i.setAction(Intent.ACTION_SEND);
					startActivity(i);
				}

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT);
			}
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("uri", imageUri);
	}
}