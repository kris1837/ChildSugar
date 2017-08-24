package com.example.childsugar.nfc_app_2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by yth on 17/03/2016.
 */
public class MultipartUtility {
	private static final String TAG = "MultipartUtility";

	public static class Response {
		int httpStatus = 0;
		String response = null;
	}

	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;

	private static final int READ_TIMEOUT = 10000;
	private static final int CONNECT_TIMEOUT = 15000;


	public interface Progress {
		int progressCallback(int percentage);
	}

	private Progress progress;

	/**
	 * This constructor initializes a new HTTP POST request with content type
	 * is set to multipart/form-data
	 *
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 */
	public MultipartUtility(String requestURL, String charset, Progress progress) throws IOException {
		this.charset = charset;

		this.progress = progress;

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";

		URL url = new URL(requestURL);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		// httpConn.setReadTimeout(READ_TIMEOUT);
		httpConn.setConnectTimeout(CONNECT_TIMEOUT);
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "Favor");
		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
				true);
	}

	/**
	 * Adds a form field to the request
	 *
	 * @param name  field name
	 * @param value field value
	 */
	public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
				.append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(
				LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Adds a upload file section to the request
	 *
	 * @param fieldName  name attribute in <input type="file" name="..." />
	 * @param uploadFile a File to be uploaded
	 * @throws IOException
	 */
	public long addFilePart(String fieldName, File uploadFile) throws IOException {
		String fileName = uploadFile.getName();

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
				"Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"; size=" + uploadFile.length())
				.append(LINE_FEED);

		String contentType = URLConnection.guessContentTypeFromName(fileName);
		writer.append("Content-Type: " + contentType).append(LINE_FEED);

		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[16 * 1024];

		int bytesRead = -1;
		long totalWritten = 0;
		int prevProgress = 0;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
			totalWritten += bytesRead;

			Log.d(TAG, "addFilePart: " + totalWritten + " bytes written");

			if (progress != null) {
				int percentage = (int) (((float) totalWritten / (float) uploadFile.length()) * 100);
				if (prevProgress != percentage) {
					this.progress.progressCallback(percentage);
					prevProgress = percentage;
				}
			}
		}
		outputStream.flush();
		inputStream.close();

		// Get rid extra carriage-return, linefeed (YTH, 9 Aug 2016)
		// writer.append(LINE_FEED);
		writer.flush();

		return totalWritten;
	}

	/**
	 * Adds a upload file section to the request
	 *
	 * @param fieldName name attribute in <input type="file" name="..." />
	 * @param is        InputStream from which to read
	 * @throws IOException
	 */
	public long addFilePart(String fieldName, String filePath, InputStream is) throws IOException {
		int fileLength = is.available();

		String fileName = filePath;
		int index = filePath.lastIndexOf('/');
		if (index >= 0) {
			fileName = filePath.substring(index + 1);
		}

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
				"Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"; size=" + fileLength)
				.append(LINE_FEED);

		String contentType = URLConnection.guessContentTypeFromName(fileName);
		writer.append("Content-Type: " + contentType).append(LINE_FEED);

		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		byte[] buffer = new byte[16 * 1024];

		int bytesRead = -1;
		long totalWritten = 0;
		int prevProgress = 0;
		while ((bytesRead = is.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
			totalWritten += bytesRead;

			Log.d(TAG, "addFilePart: " + totalWritten + " bytes written");

			if (progress != null) {
				int percentage = (int) (((float) totalWritten / (float) fileLength) * 100);
				if (prevProgress != percentage) {
					this.progress.progressCallback(percentage);
					prevProgress = percentage;
				}
			}
		}
		outputStream.flush();

		// Get rid extra carriage-return, linefeed (YTH, 9 Aug 2016)
		// writer.append(LINE_FEED);
		writer.flush();

		return totalWritten;
	}

	/**
	 * Adds a header field to the request.
	 *
	 * @param name  - name of the header field
	 * @param value - value of the header field
	 */
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Completes the request and receives response from the server.
	 *
	 * @return a list of Strings as response in case the server returned
	 * status OK, otherwise an exception is thrown.
	 * @throws IOException
	 */
	/*
	public List<String> finish() throws IOException {
		List<String> response = new ArrayList<>();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response.add(line);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return response;
	}
	*/

	public Response finish() throws IOException {
		Response response = new Response();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		response.httpStatus = httpConn.getResponseCode();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append("\r\n");
		}
		reader.close();
		response.response = sb.toString();

		return response;
	}
}

