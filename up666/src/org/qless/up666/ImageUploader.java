/**
 * 
 */
package org.qless.up666;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * @author quattro
 * 
 */
public class ImageUploader {

	/**
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static URL upload(String filename) throws Exception {
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


			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"MAX_FILE_SIZE\""
							+ lineEnd);
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(maxSize + lineEnd);

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"submit\""
							+ lineEnd);
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(" Speichern " + lineEnd);

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"f\"; filename=\""
							+ pathToOurFile + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			File file = new File(filename);

			// Get the number of bytes in the file
			long length = file.length();

			if (length > maxSize) {
				ImageProcessor.process(filename,((double)length)/maxSize , outputStream);
			} else {
				FileInputStream fileInputStream = new FileInputStream(new File(
						pathToOurFile));

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				fileInputStream.close();

			}
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();

			Log.i("ImageUpload", serverResponseCode + serverResponseMessage);
			InputStream in = new BufferedInputStream(
					connection.getInputStream());

			Scanner sc = new Scanner(in);
			String content = sc.useDelimiter("\\Z").next();
			sc.close();

			imageURL = new URL(parseResultPage(content));

			outputStream.flush();
			outputStream.close();
			Log.d("ImageUpload", imageURL.toString());
		return imageURL;
	}

	/**
	 * @param page
	 * @return
	 */
	private static String parseResultPage(String page) {
		Pattern p = Pattern.compile("(\\Qhttp://666kb.com/i/\\E.*?)\"");
		Matcher m = p.matcher(page); // get a matcher object
		if (m.find()) {
			return m.group(1);
		} else {
			Log.i("content", page);
			return null;
		}
	}
}