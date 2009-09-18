package com.discursive.log4couch;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class CouchAppender extends AppenderSkeleton {

	private String couchDbUrl;

	public CouchAppender() {
	}

	protected void append(LoggingEvent event) {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(couchDbUrl);

		JSONObject obj = new JSONObject();

		if (event.getLevel() != null) {
			int level = event.getLevel().toInt();
			obj.put("level", new Integer(level));
		}

		if (event.locationInformationExists()) {
			LocationInfo loc = event.getLocationInformation();

			JSONObject locObj = new JSONObject();
			locObj.put("class name", loc.getClassName());
			locObj.put("file name", loc.getFileName());
			locObj.put("method name", loc.getMethodName());
			locObj.put("line number", loc.getLineNumber());
			obj.put("location", locObj);
		}

		obj.put("logger name", event.getLoggerName());

		obj.put("message", event.getRenderedMessage());

		if (!StringUtils.isEmpty(event.getNDC())) {
			obj.put("ndc", event.getNDC());
		}

		if (event.getThrowableInformation() != null) {
			ThrowableInformation thr = event.getThrowableInformation();
			Throwable t = thr.getThrowable();
			String tMessage = t.getMessage();

			JSONObject tObj = new JSONObject();
			tObj.put("class name", t.getClass().getName());
			tObj.put("message", tMessage);
			tObj.put("information", StringUtils.join(Arrays.asList(thr
					.getThrowableStrRep()), "\n"));

			obj.put("throwable", tObj);
		}

		obj.put("timestamp", new Long(event.getTimeStamp()));

		try {
			StringEntity entity = new StringEntity(obj.toString(), "UTF-8");
			post.setEntity(entity);

			client.execute(post);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void close() {
	}

	public boolean requiresLayout() {
		return false;
	}

	public String getCouchDbUrl() {
		return couchDbUrl;
	}

	public void setCouchDbUrl(String couchDbUrl) {
		this.couchDbUrl = couchDbUrl;
	}

}
