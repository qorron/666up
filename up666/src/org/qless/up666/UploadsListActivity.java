/**
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
 * 
 */

package org.qless.up666;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class UploadsListActivity extends ListActivity {
	public static final int MENU_CAMERA_ID = Menu.FIRST;
	public static final int MENU_PREFERENCES_ID = Menu.FIRST + 1;
	public static final int MENU_ABOUT_ID = Menu.FIRST + 2;
	
	private int mUploadNumber = 1;
	private UploadsDbAdapter mDbHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploads_list);
        mDbHelper = new UploadsDbAdapter(this);
        mDbHelper.open();
        fillData();
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	
        menu.add(0, MENU_CAMERA_ID, 1, R.string.menu_camera).setIcon(android.R.drawable.ic_menu_camera);
        menu.add(0, MENU_ABOUT_ID, 2, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, MENU_PREFERENCES_ID, 3, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
    
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case MENU_CAMERA_ID:
            //createNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllUploads();
        startManagingCursor(c);

        String[] from = new String[] { UploadsDbAdapter.KEY_COMMENT, UploadsDbAdapter.KEY_UPLOAD_DATE };
        int[] to = new int[] { R.id.headlineTextView, R.id.additionalTextView };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.upload_row, c, from, to);
        setListAdapter(notes);
    }
	
}
