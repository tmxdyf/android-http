/*
 * Copyright (C) 2012 the diamond:dogs|group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.example.http;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.data.dataobjects.WebRequest.Type;
import at.diamonddogs.service.net.HttpService;
import at.diamonddogs.service.net.HttpService.HttpServiceBinder;
import at.diamonddogs.service.processor.HeadRequestProcessor;
import at.diamonddogs.service.processor.ServiceProcessor;

/**
 * @author siyb
 * 
 */
public class HttpExampleActivity extends Activity {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpExampleActivity.class.getSimpleName());

	/**
	 * The service connection that will be used to connect with
	 * {@link HttpService}
	 */
	private HttpExampleConnection serviceConnection;

	/**
	 * An instance of {@link HttpService} that can be used to run web requests
	 */
	private HttpService httpService;

	/**
	 * Text view to display a weather string
	 */
	private TextView text;

	/**
	 * Text view to display the temperature
	 */
	private TextView temperature;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpexampleactivity);
		text = (TextView) findViewById(R.id.httpexampleactivity_text);
		temperature = (TextView) findViewById(R.id.httpexampleactivity_temperature);
	}

	/**
	 * Binds the {@link HttpService} if the {@link HttpExampleConnection} is
	 * null
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (serviceConnection == null) {
			Intent i = new Intent(this, HttpService.class);
			bindService(i, serviceConnection = new HttpExampleConnection(), BIND_AUTO_CREATE);
		}
	}

	/**
	 * Unbinds the {@link HttpService} if the {@link HttpExampleConnection} is
	 * not null
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (serviceConnection != null) {
			unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	/**
	 * Constructs and runs the weather web request
	 */
	@SuppressWarnings("unchecked")
	private void runWebRequest() {
		String weatherUrl = getWeatherUrl("Austria", "Vienna");
		LOGGER.info("URL: " + weatherUrl);
		// --- SYNC WEB REQUEST

		// sync webrequest POC, usually you should not execute synchronous web
		// requests on the main thread. The result of this call will be logged,
		// but not displayed in the UI

		WebRequest syncWebRequest = new WebRequest();
		syncWebRequest.setUrl(weatherUrl);
		syncWebRequest.setRequestType(Type.HEAD);

		// default header request processor
		syncWebRequest.setProcessorId(HeadRequestProcessor.ID);

		// required for HEAD request (yahoo specific!)
		syncWebRequest.addHeaderField("Accept-Encoding", "gzip, deflate");

		Map<String, List<String>> headers = (Map<String, List<String>>) httpService.runSynchronousWebRequest(syncWebRequest);
		if (headers != null) {
			for (String key : headers.keySet()) {
				LOGGER.error("KEY -> " + key);
				for (String value : headers.get(key)) {
					LOGGER.error("    VALUE -> " + value);
				}
			}
		} else {
			Toast.makeText(this, "Error while optaining headers", Toast.LENGTH_SHORT).show();
		}

		// --- ASYNC WEB REQUEST

		WebRequest asyncRequest = new WebRequest();
		// takes a String or URL object!
		asyncRequest.setUrl(weatherUrl);

		// The processorid tells HttpService what to do once a web reply has
		// been received.
		// You MUST set a processor id and the processor needs to be registered
		// with HttpService.
		// You may use DummyProcessor.ID to circumvent processor implementation
		asyncRequest.setProcessorId(WeatherProcessor.ID);

		// run the web request, WeatherHandler will receive a callback once the
		// web request has been finished
		httpService.runWebRequest(new WeatherHandler(), asyncRequest);
	}

	/**
	 * Formats the yahoo weather URL
	 * 
	 * @param country
	 *            the country
	 * @param city
	 *            the city
	 * @return the weather url for country & city
	 */
	private String getWeatherUrl(String country, String city) {
		Uri u = Uri.parse("http://query.yahooapis.com/v1/public/yql");
		// @formatter:off
		u = u.buildUpon()
			.appendQueryParameter("q", "select * from weather.forecast where location in (select id from weather.search where query=\""+country+","+ city +"\")")
			.appendQueryParameter("format", "xml")
			.appendQueryParameter("env", "store://datatables.org/alltableswithkeys")
		.build();
		// @formatter:on
		return u.toString();
	}

	/**
	 * A Simple {@link ServiceConnection} to be used in conjunction with
	 * {@link HttpService}
	 * 
	 * @author siyb
	 * 
	 */
	private final class HttpExampleConnection implements ServiceConnection {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof HttpServiceBinder) {
				// gaining access to the HttpService
				httpService = ((HttpServiceBinder) service).getHttpService();

				// registering the WeatherProcessor that will take care of
				// parsing the XML and returning a POJO containing weather
				// information
				if (!httpService.isProcessorRegistered(WeatherProcessor.ID)) {
					httpService.registerProcessor(new WeatherProcessor());
				}

				// registering the DummyProcessor, we need this processor for
				// our HEAD request
				if (!httpService.isProcessorRegistered(HeadRequestProcessor.ID)) {
					httpService.registerProcessor(new HeadRequestProcessor());
				}

				// run the webrequest once the processor has been registered
				runWebRequest();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	}

	/**
	 * This handler receives a callback once the web request has been processed.
	 * 
	 * @author siyb
	 * 
	 */
	private class WeatherHandler extends Handler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == WeatherProcessor.ID) {
				if (msg.arg1 == ServiceProcessor.RETURN_MESSAGE_OK) {
					Weather w = (Weather) msg.obj;
					text.setText(w.getText());
					temperature.setText(String.valueOf(w.getTemperature()));
				} else {
					Toast.makeText(HttpExampleActivity.this, "Error fetching weather", Toast.LENGTH_LONG).show();
				}
			}

		}
	}
}
