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
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * @author quattro
 * 
 */
public class UploadEdit extends Activity {
	private Long mRowId;
	private UploadsDbAdapter mDbHelper;
	private EditText mCommentText;
	private ImageView mThumbnailView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new UploadsDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.upload_edit);
		setTitle(R.string.app_name);

		mCommentText = (EditText) findViewById(R.id.editComment);
		mThumbnailView = (ImageView) findViewById(R.id.largeThumbnailView);
		
		Button confirmButton = (Button) findViewById(R.id.save);

		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState
				.getSerializable(UploadsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(UploadsDbAdapter.KEY_ROWID) : null;
		}

		populateFields();

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor upload = mDbHelper.fetchUpload(mRowId);
			startManagingCursor(upload);
			mCommentText.setText(upload.getString(upload
					.getColumnIndexOrThrow(UploadsDbAdapter.KEY_COMMENT)));
			
			byte[] image = upload.getBlob(upload
					.getColumnIndex(UploadsDbAdapter.KEY_THUMBNAIL));
			if (image != null) {
				mThumbnailView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0,
						image.length));
			} else {
				mThumbnailView.setImageResource(android.R.drawable.ic_menu_gallery);
			}
			
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(UploadsDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
		if (mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
		if (mDbHelper == null) {
			mDbHelper = new UploadsDbAdapter(this);
			mDbHelper.open();
		}
	}

	private void saveState() {
		String comment = mCommentText.getText().toString();

		if (mRowId == null) {
			/*
			long id = mDbHelper.createNote(title, body);
			if (id > 0) {
				mRowId = id;
			}
			*/
		} else {
			mDbHelper.updateCommentOnUpload(mRowId, comment);
		}
	}

}
