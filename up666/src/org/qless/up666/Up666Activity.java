package org.qless.up666;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

/**
 * @author quattro
 *
 */
public class Up666Activity extends Activity {

	/**
	 * 
	 */
	private TextView mIntruductionBodyView;

	/** Called when the activity is first created. */
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ullilog","start_intruduction");
		setContentView(R.layout.main);
		mIntruductionBodyView = (TextView) findViewById(R.id.introductionBody);
		Linkify.addLinks(mIntruductionBodyView, Linkify.ALL);
	}
}