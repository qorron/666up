package org.qless.up666;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

	private String imageURL;
	
	private Exception ex;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ullilog", "start");
		Intent intent = getIntent();
		Log.d("ullilog", "got intent");
		setContentView(R.layout.upload);

		mGreeting = (TextView) findViewById(R.id.hello);
		mMimeTypeTextView = (TextView) findViewById(R.id.mimeType);
		mFilePathTextView = (TextView) findViewById(R.id.filePath);
		mImageURLTextView = (TextView) findViewById(R.id.imageURL);
		mProgress = (ProgressBar) findViewById(R.id.progressBarUpload);
		mCopyButton = (Button) findViewById(R.id.buttonCopy);
		mShareButton = (Button) findViewById(R.id.buttonShare);

		mGreeting.setText(getString(R.string.uploadAt) + " "
				+ getString(R.string.imageHoster));

		mProgress.setVisibility(ProgressBar.INVISIBLE);
		mCopyButton.setEnabled(false);
		mShareButton.setEnabled(false);

		mCopyButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(imageURL);
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
				i.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.share_subject));
				i.putExtra(Intent.EXTRA_TEXT, imageURL);
				startActivity(Intent.createChooser(i,
						getString(R.string.share_title)));

			}

		});

		Log.d("ullilog", "about to proccess intent");

		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				String scheme = uri.getScheme();
				Log.d("ullilog", "content scheme is: " + scheme);
				boolean ok = false;
				String mimeType = null;
				String filePath = null;
				if (scheme.equals("content")) {
					mimeType = intent.getType();
					ContentResolver contentResolver = getContentResolver();
					Cursor cursor = contentResolver.query(uri, null, null,
							null, null);
					cursor.moveToFirst();
					filePath = cursor.getString(cursor
							.getColumnIndexOrThrow(Images.Media.DATA));
					ok = true;
				} else if (scheme.equals("file")) {
					mimeType = intent.getType();
					filePath = uri.toString().substring("file://".length());
					ok = true;
				} else {
					Log.d("ullilog", "no content scheme, is: " + scheme);
					Context context = getApplicationContext();
					CharSequence text = "no content scheme";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
				if (ok) {
					mMimeTypeTextView.setText(mimeType);
					mFilePathTextView.setText(filePath);
					new ImageUploadTask().execute(filePath);
				}
			} else {
				Log.d("ullilog", "no EXTRA_STREAM");
				Context context = getApplicationContext();
				CharSequence text = "no EXTRA_STREAM";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		} else {
			Log.d("ullilog", "no ACTION_SEND");
			Context context = getApplicationContext();
			CharSequence text = "no ACTION_SEND";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	/**
	 * @param url
	 */
	protected void showURL(URL url) {
		mImageURLTextView.setText(url != null ? url.toString() : "nothing!");

		if (url != null) {
			imageURL = url.toString();

			mCopyButton.setEnabled(true);
			mShareButton.setEnabled(true);
		}
	}

	/**
	 * @param ex
	 */
	protected void errorDialogue(Exception ex) {
		this.ex = ex;
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setIcon(android.R.drawable.ic_dialog_alert);
		b.setTitle(R.string.errorTitle);
		b.setMessage(R.string.errorMessage);
		b.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						sendError();
					}
				});
		b.setNegativeButton(android.R.string.no, null);
		b.show();

	}
	
	/**
	 * 
	 */
	private void sendError(){
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();

		// create an email intent to send to yourself
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent
				.putExtra(
						android.content.Intent.EXTRA_EMAIL,
						new String[] { "android@qless.org" });
		emailIntent.putExtra(
				android.content.Intent.EXTRA_SUBJECT,
				getString(R.string.app_name) + " " + getString(R.string.errorSubject));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				stacktrace);

		// start the email activity - note you need to start it
		// with a chooser
		startActivity(Intent.createChooser(emailIntent,
				getString(R.string.errorSendAction)));
		
	}

	/**
	 * @author quattro
	 *
	 */
	private class ImageUploadTask extends AsyncTask<String, Integer, URL> {

		private Exception ex;

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(ProgressBar.VISIBLE);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected URL doInBackground(String... params) {
			URL url = null;
			try {
				url = ImageUploader.upload(params[0]);
			} catch (Exception e) {
				ex = e;
			}
			return url;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(URL result) {
			mProgress.setVisibility(ProgressBar.INVISIBLE);
			if (ex != null) {
				errorDialogue(ex);
			} else {
				showURL(result);
			}
		}

	}
}
