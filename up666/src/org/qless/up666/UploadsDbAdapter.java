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
 * 
 */

package org.qless.up666;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author quattro
 * 
 *         Simple uploads database access helper class. Defines the basic CRUD operations for the
 *         666kb uploads, and gives the ability to list all uploads as well as retrieve or modify a
 *         specific upload.
 * 
 *         This class is derived from the notepad example
 * 
 */

public class UploadsDbAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_URL = "url";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_MIMETYPE = "mimetype";
	public static final String KEY_THUMBNAIL = "thumbnail";
	public static final String KEY_UPLOAD_DATE = "upload_date";
	public static final String KEY_COMMENT = "comment";

	private static final String TAG = "UploadsDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table uploads (_id integer primary key autoincrement, "
			+ "url text not null, filename text not null, thumbnail blob, "
			+ "upload_date timestamp not null DEFAULT current_timestamp, "
			+ "comment text not null, mimetype text);";

	private static final String DATABASE_NAME = "666ups";
	private static final String DATABASE_TABLE = "uploads";
	private static final int DATABASE_VERSION = 3;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will NOT destroy all old data");
			if (oldVersion < 3) {
				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD mimetype text");
			}
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public UploadsDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the uploads database. If it cannot be opened, try to create a new instance of the
	 * database. If it cannot be created, throw an exception to signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public UploadsDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * close the database
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new upload entry using the data provided. If the upload is successfully created
	 * return the new rowId for that instance, otherwise return a -1 to indicate failure.
	 * 
	 * @param url
	 *            the url of the uploaded image
	 * @param filename
	 *            the original filename including the full path
	 * @param mimetype
	 *            the mime type of the file given
	 * @param thumbnail
	 *            a thumbnail of the image
	 * @param comment
	 *            user generated information about the image
	 * @return rowId or -1 if failed
	 */
	public long createUpload(String url, String filename, String mimetype, byte[] thumbnail,
			String comment) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_URL, url);
		initialValues.put(KEY_FILENAME, filename);
		initialValues.put(KEY_MIMETYPE, mimetype);
		initialValues.put(KEY_THUMBNAIL, thumbnail);
		initialValues.put(KEY_COMMENT, comment);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the upload with the given rowId
	 * 
	 * @param rowId
	 *            id of upload to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteUpload(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllUploads() {

		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_URL, KEY_FILENAME,
				KEY_MIMETYPE, KEY_THUMBNAIL, KEY_UPLOAD_DATE, KEY_COMMENT }, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor positioned at the upload that matches the given rowId
	 * 
	 * @param rowId
	 *            id of upload to retrieve
	 * @return Cursor positioned to matching upload, if found
	 * @throws SQLException
	 *             if upload could not be found/retrieved
	 */
	public Cursor fetchUpload(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_URL,
				KEY_FILENAME, KEY_MIMETYPE, KEY_THUMBNAIL, KEY_UPLOAD_DATE, KEY_COMMENT },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Return url and comment for the upload that matches the given rowId
	 * 
	 * @param rowId
	 *            id of upload to retrieve
	 * @return the url and the comment, null if not found
	 */
	public String[] fetchUploadUrlAndComment(long rowId) {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_URL,
				KEY_COMMENT }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
			return new String[] { mCursor.getString(mCursor.getColumnIndex(KEY_URL)),
					mCursor.getString(mCursor.getColumnIndex(KEY_COMMENT)) };
		}
		return null;

	}

	/**
	 * Update the upload using the comment provided. The upload to be updated is specified using the
	 * rowId, and it is altered to use the comment value passed in
	 * 
	 * @param rowId
	 *            id of upload to update
	 * @param comment
	 *            value to set upload comment to
	 * @return true if the upload was successfully updated, false otherwise
	 */
	public boolean updateCommentOnUpload(long rowId, String comment) {
		ContentValues args = new ContentValues();
		args.put(KEY_COMMENT, comment);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

}
