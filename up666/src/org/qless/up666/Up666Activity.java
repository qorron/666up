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
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

/**
 * @author quattro
 * 
 * this does not much.
 * at the moment, it displays some explanation on how to use the app
 *
 */
public class Up666Activity extends Activity {

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