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

/**
 * A Log4J appender that creates a document in CouchDB.  This appender
 * is used to create a document in a CouchDB database, and is
 * recommended to use as a place to post errors messages and
 * exceptions.  This appender is not optimized in any way for
 * performance, and will simply print a stack trace to Standard Out if
 * CouchDB is not available.  To configure this appender in a
 * log4j.properties, add the following lines:
 *
 * log4j.appender.COUCH=com.discursive.log4couch.CouchAppender
 * log4j.appender.COUCH.CouchDbUrl=http://localhost:5984/error-log
 * log4j.appender.COUCH.threshold=ERROR
 *
 * The appender is going to POST a JSON document that represents a
 * logging event to the URL provided.  If you have configured this
 * properly, this will result in a new document in the specified
 * database.  In the example shown above, this will result in a new
 * document being stored to the error-log database for every logging
 * event with a level of ERROR or higher.
 *
 * Note: The threshold line is especially important as the appender hasn't
 * been optimized for performance.  You do not want this appender to
 * be involved with every logging statement (it will make your program
 * crawl to a halt).  This appender was created as a way to record and
 * log exceptions in a production environment in CouchDB for offline
 * analysis.
 *
 */
  public class CouchAppender extends AppenderSkeleton {

	private String couchDbUrl;
	private HttpClient client;
	private String host = "DEFAULT_HOST";
	private String process = "DEFAULT_PROCESS";

	public CouchAppender() {
	}

	protected void append(LoggingEvent event) {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(couchDbUrl);

		JSONObject obj = new JSONObject();

		obj.put( "host", host );
		obj.put( "process", process);
		
		if (event.getLevel() != null) {
			int level = event.getLevel().toInt();
			obj.put("level", new Integer(level));
		}

		if (event.locationInformationExists()) {
			LocationInfo loc = event.getLocationInformation();

			JSONObject locObj = new JSONObject();
			locObj.put("className", loc.getClassName());
			locObj.put("fileName", loc.getFileName());
			locObj.put("methodName", loc.getMethodName());
			locObj.put("lineNumber", loc.getLineNumber());
			obj.put("location", locObj);
		}

		obj.put("loggerName", event.getLoggerName());

		obj.put("message", event.getRenderedMessage());

		if (!StringUtils.isEmpty(event.getNDC())) {
			obj.put("ndc", event.getNDC());
		}

		if (event.getThrowableInformation() != null) {
			ThrowableInformation thr = event.getThrowableInformation();
			Throwable t = thr.getThrowable();
			String tMessage = t.getMessage();

			JSONObject tObj = new JSONObject();
			tObj.put("className", t.getClass().getName());
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}
	
	
	
}
