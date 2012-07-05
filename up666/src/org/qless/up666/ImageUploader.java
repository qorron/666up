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

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.util.Log;

/**
 * @author quattro
 * 
 *         uploads the image and calls the {@link ImageProcessor} should the file be to large
 * 
 */
public class ImageUploader {

	/**
	 * handles the image upload
	 * 
	 * @param filename
	 *            the path to the image
	 * @return the {@link URL} of the uploaded image or null if the upload failed
	 * 
	 * @throws MalformedURLException
	 *             if the upload failed
	 * @throws IOException
	 *             on a network error
	 * @throws ProtocolException
	 *             the server responded unexpectedly
	 * @throws FileNotFoundException
	 *             the suggested file was not found
	 */
	public static URL upload(String filename) throws MalformedURLException, IOException,
			ProtocolException, FileNotFoundException {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		String pathToOurFile = filename;
		String urlServer = "http://666kb.com/u.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		final long maxSize = 681984;

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		URL imageURL = null;

		File file = new File(filename);
		// Get the number of bytes in the file
		long length = file.length();

		Bitmap resizedImage = null;

		if (length > maxSize || needsRotation(filename)) {

			resizedImage = ImageProcessor.process(filename);

			// .compress(Bitmap.CompressFormat.JPEG, 90, outputStream) ;

		}

		URL url = new URL(urlServer); // theoretically possible MalformedURLException
		Log.d("ImageUpload", "connecting..");
		connection = (HttpURLConnection) url.openConnection(); // IOException -> network error

		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		Log.d("ImageUpload", "setting req property");
		// Enable POST method
		connection.setRequestMethod("POST"); // ProtocolException ->

		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		outputStream = new DataOutputStream(connection.getOutputStream()); // IOException -> network
																			// error
		Log.d("ImageUpload", "writing to stream");

		outputStream.writeBytes(twoHyphens + boundary + lineEnd); // IOException -> network error
		outputStream.writeBytes("Content-Disposition: form-data; name=\"MAX_FILE_SIZE\"" + lineEnd);
		outputStream.writeBytes(lineEnd); // IOException -> network error
		outputStream.writeBytes(maxSize + lineEnd); // IOException -> network error

		outputStream.writeBytes(twoHyphens + boundary + lineEnd); // ...
		outputStream.writeBytes("Content-Disposition: form-data; name=\"submit\"" + lineEnd);
		outputStream.writeBytes(lineEnd);
		outputStream.writeBytes(" Speichern " + lineEnd);

		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"f\"; filename=\""
				+ pathToOurFile + "\"" + lineEnd);
		outputStream.writeBytes(lineEnd);

		if (resizedImage != null) {
			Log.d("ImageUpload", "compressing");
			resizedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
			Log.d("ImageUpload", "done");
		} else {
			// FileNotFoundException -> most likely the filename got escaped and needs to be
			// unescaped (known to happen on froyo+ and sending from a
			// filemanager)
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

			// IOException (file) -> should not really happen unless sdcard is broken?
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize); // IOException (file)

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize); // IOException -> network error
				bytesAvailable = fileInputStream.available(); // IOException (file)
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize); // IOException (file)
			}
			fileInputStream.close(); // IOException (file)

		}
		Log.d("ImageUpload", "upload done");
		outputStream.writeBytes(lineEnd); // IOException -> network error
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		outputStream.flush(); // IOException -> network error
		outputStream.close(); // ..
		Log.d("ImageUpload", "upload close, flush");
		// Responses from the server (code and message)
		int serverResponseCode = connection.getResponseCode(); // IOException -> network error
		String serverResponseMessage = connection.getResponseMessage(); // IOException -> network
																		// error

		Log.i("ImageUpload", "response: " + serverResponseCode + serverResponseMessage);
		InputStream in = new BufferedInputStream(connection.getInputStream()); // IOException ->
																				// network error

		Scanner sc = new Scanner(in);
		String content = sc.useDelimiter("\\Z").next();
		sc.close();

		imageURL = new URL(parseResultPage(content)); // MalformedURLException -> if
														// parseResultPage(String page) fails

		Log.d("ImageUpload", imageURL.toString());
		return imageURL;
	}

	/**
	 * Takes the whole webpage as a String and tries to parse the image url
	 * 
	 * @param page
	 *            the HTML document
	 * @return the URL of the uploaded image or null if it was not found
	 */
	private static String parseResultPage(String page) {
		Pattern p = Pattern.compile("(\\Qhttp://666kb.com/i/\\E.*?)\"");
		Matcher m = p.matcher(page); // get a matcher object
		if (m.find()) {
			return m.group(1);
		} else {
			Log.d("content", page);
			return null;
		}
	}

	private static boolean needsRotation(String filename) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(filename);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			if (orientation == ExifInterface.ORIENTATION_NORMAL
					|| orientation == ExifInterface.ORIENTATION_UNDEFINED) {
				return false;
			}
		} catch (IOException e) {
			// this should not occur under normal circumstances.
			// if it does, it doesn't matter.
		}
		return true;
	}
}