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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author quattro
 * 
 *         This activity handles the send intent for images.
 * 
 */
/**
 * @author quattro
 * 
 */
public class UploadActivity extends Activity {

	private TextView mMimeTypeTextView;
	private TextView mFilePathTextView;
	private TextView mImageURLTextView;
	private ProgressBar mProgress;
	private TextView mGreeting;
	private Button mCopyButton;
	private Button mShareButton;

	private Exception ex;
	private String mImageURL;
	private String mMimeType;
	private String mFilePath;
	private long mUploadId = -1;
	private String mComment;

	private UploadsDbAdapter mDbHelper;
	@SuppressWarnings("unused")
	private Error error;
	private EditText mCommentEditText;

	public enum Error {
		FILE_NOT_FOUND, HOST_NOT_FOUND, NETWORK, BAD_URL, BAD_INTENT
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new UploadsDbAdapter(this);
		mDbHelper.open();
		mComment = ""; // initialize, so there is no null in the db
		if (savedInstanceState != null) {
			mImageURL = savedInstanceState.getString("mImageURL");
			mFilePath = savedInstanceState.getString("mFilePath");
			mMimeType = savedInstanceState.getString("mMimeType");
			mUploadId = savedInstanceState.getLong("mUploadId");
			mComment = savedInstanceState.getString("mComment");
		}
		Intent intent = getIntent();
		setContentView(R.layout.upload);

		mGreeting = (TextView) findViewById(R.id.hello);
		mMimeTypeTextView = (TextView) findViewById(R.id.mimeType);
		mFilePathTextView = (TextView) findViewById(R.id.filePath);
		mImageURLTextView = (TextView) findViewById(R.id.imageURL);
		mProgress = (ProgressBar) findViewById(R.id.progressBarUpload);
		mCopyButton = (Button) findViewById(R.id.buttonCopy);
		mShareButton = (Button) findViewById(R.id.buttonShare);
		mCommentEditText = (EditText) findViewById(R.id.commentEditText);

		mGreeting.setText(getString(R.string.uploadAt) + " " + getString(R.string.imageHoster));

		mProgress.setVisibility(ProgressBar.INVISIBLE);
		mCopyButton.setEnabled(false);
		mShareButton.setEnabled(false);

		mCommentEditText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				// XXX do something
				mComment = s.toString();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// XXX do something
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// XXX do something
			}
		});

		mCopyButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(mImageURL);
				Context context = getApplicationContext();
				CharSequence text = getString(R.string.copyToast);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		});

		mShareButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(android.content.Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
				i.putExtra(Intent.EXTRA_TEXT, mImageURL);
				startActivity(Intent.createChooser(i, getString(R.string.share_title)));

			}

		});

		if (mImageURL == null) {
			// since there is no previously stored url, we have to upload the
			// file
			if (Intent.ACTION_SEND.equals(intent.getAction())) {
				Bundle extras = intent.getExtras();
				if (extras.containsKey(Intent.EXTRA_STREAM)) {
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
					String scheme = uri.getScheme();
					boolean ok = false;
					mMimeType = null;
					mFilePath = null;
					if (scheme.equals("content")) {
						mMimeType = intent.getType();
						ContentResolver contentResolver = getContentResolver();
						Cursor cursor = contentResolver.query(uri, null, null, null, null);
						cursor.moveToFirst();
						mFilePath = cursor.getString(cursor
								.getColumnIndexOrThrow(Images.Media.DATA));
						ok = true;
					} else if (scheme.equals("file")) {
						mMimeType = intent.getType();
						mFilePath = uri.getPath();
						ok = true;
					} else {
						Log.d("BAD_INTENT", "no content scheme, is: " + scheme);
						errorDialogue(null, Error.BAD_INTENT);
					}
					if (ok) {
						mMimeTypeTextView.setText(mMimeType);
						mFilePathTextView.setText(mFilePath);
						new ImageUploadTask().execute(mFilePath);
					}
				} else {
					Log.d("BAD_INTENT", "no EXTRA_STREAM");
					errorDialogue(null, Error.BAD_INTENT);
				}
			} else {
				Log.d("BAD_INTENT", "no ACTION_SEND");
				errorDialogue(null, Error.BAD_INTENT);
			}
		} else {
			// we already have a url, so we just update the gui and make it look
			// like expected.
			resetGUI();
		}

	}

	/**
	 * Saves what we have in the db. if the entry already exists, just the comments will be updated.
	 * otherwise an entry (with thumbnail) will be generated and stored.
	 */
	private void saveState() {
		if (-1 == mUploadId) {
			// has never been saved to the db
			Log.d("database", "stored new upload");
			byte[] thumbnail = ImageProcessor.thumbnail(mFilePath, 100);
			mUploadId = mDbHelper.createUpload(mImageURL, mFilePath, thumbnail, mComment);
		} else {
			// has been saved, just update the comment
			mDbHelper.updateCommentOnUpload(mUploadId, mComment);
			Log.d("database", "update comment");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveState();

		outState.putString("mImageURL", mImageURL);
		outState.putString("mMimeType", mMimeType);
		outState.putString("mFilePath", mFilePath);
		outState.putLong("mUploadId", mUploadId);
		outState.putString("mComment", mComment);
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
		if (mDbHelper == null) {
			mDbHelper = new UploadsDbAdapter(this);
			mDbHelper.open();
		}
		//resetGUI();
	}

	/**
	 * Displays the image URL and enables the copy/share buttons
	 * 
	 * @param url
	 */
	protected void showURL(URL url) {
		mImageURLTextView.setText(url != null ? url.toString() : "nothing!");

		if (url != null) {
			mImageURL = url.toString();

			mCopyButton.setEnabled(true);
			mShareButton.setEnabled(true);
		}
	}

	/**
	 * Displays an error dialogue with the abilits to send a log on some errors
	 * 
	 * @param ex
	 */
	protected void errorDialogue(Exception ex, Error error) {
		this.ex = ex;
		this.error = error;
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setIcon(android.R.drawable.ic_dialog_alert);

		switch (error) {
		case HOST_NOT_FOUND:
			b.setTitle(R.string.errorTitleHostNotFound);
			b.setMessage(R.string.errorMessageHostNotFound);
			break;
		case NETWORK:
			b.setTitle(R.string.errorTitleNetwork);
			b.setMessage(R.string.errorMessageNetwork);
			break;
		case FILE_NOT_FOUND:
			b.setTitle(R.string.errorTitleFileNotFound);
			b.setMessage(R.string.errorMessageFileNotFound);
			break;
		case BAD_URL:
			b.setTitle(R.string.errorTitleBadURL);
			b.setMessage(R.string.errorMessageBadURL);
			break;
		case BAD_INTENT:
			b.setTitle(R.string.errorTitleBadIntent);
			b.setMessage(R.string.errorMessageBadIntent);
			break;
		default:
			b.setTitle(R.string.errorTitle);
			b.setMessage(R.string.errorMessage);
			break;
		}

		if (ex != null) { // exception -> send error report
			b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					sendError();
				}
			});
			b.setNegativeButton(android.R.string.no, null);
		} else { // just a normal error like network problems
			// add a neutral button to the alert box and assign a click listener
			b.setNeutralButton("Ok", null);
		}
		b.show();

	}

	/**
	 * Constructs an ACTION_SEND intent to email a stacktrace
	 * 
	 */
	private void sendError() {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();

		// create an email intent to send to yourself
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "android@qless.org" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name)
				+ " " + getString(R.string.errorSubject));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stacktrace);

		// start the email activity - note you need to start it
		// with a chooser
		startActivity(Intent.createChooser(emailIntent, getString(R.string.errorSendAction)));

	}

	private void resetGUI() {
		mProgress.setVisibility(ProgressBar.INVISIBLE);
		mMimeTypeTextView.setText(mMimeType);
		mFilePathTextView.setText(mFilePath);
		mImageURLTextView.setText(mImageURL);
		mCopyButton.setEnabled(true);
		mShareButton.setEnabled(true);
	}

	/**
	 * @author quattro
	 * 
	 *         handles the resize and upload process in a background thread
	 * 
	 */
	private class ImageUploadTask extends AsyncTask<String, Integer, URL> {

		private Exception ex;
		private Error error;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(ProgressBar.VISIBLE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected URL doInBackground(String... params) {
			URL url = null;
			try {
				url = ImageUploader.upload(params[0]);
			} catch (FileNotFoundException e) {
				error = Error.FILE_NOT_FOUND;
			} catch (UnknownHostException e) {
				error = Error.HOST_NOT_FOUND;
			} catch (MalformedURLException e) {
				error = Error.BAD_URL;
				ex = e;
			} catch (ProtocolException e) {
				ex = e;
			} catch (IOException e) {
				error = Error.NETWORK;
			} catch (Exception e) {
				ex = e;
			}
			return url;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(URL result) {
			mProgress.setVisibility(ProgressBar.INVISIBLE);
			if (ex != null || error != null) {
				errorDialogue(ex, error);
			} else {
				showURL(result);
			}
		}
	}

}
