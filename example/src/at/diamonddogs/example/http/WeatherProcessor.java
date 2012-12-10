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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import at.diamonddogs.data.adapter.ReplyAdapter;
import at.diamonddogs.data.adapter.ReplyAdapter.Status;
import at.diamonddogs.data.dataobjects.Request;
import at.diamonddogs.data.dataobjects.WebReply;
import at.diamonddogs.data.dataobjects.WebRequest;
import at.diamonddogs.service.processor.ServiceProcessor;
import at.diamonddogs.service.processor.XMLProcessor;
import at.diamonddogs.util.CacheManager.CachedObject;

/**
 * All methods in this class, except getProcessorID(), will be executed in a
 * worker thread.
 * 
 * @author siyb
 * 
 */
public class WeatherProcessor extends XMLProcessor<Weather> {

	public static final int ID = 124345724;

	/**
	 * Callback method that will be called once a {@link Document} has been
	 * created from the byte data contained in the r.eply
	 */
	@Override
	protected Weather parse(Document inputObject) {
		Weather ret = new Weather();
		Element root = inputObject.getDocumentElement();
		NodeList condition = root.getElementsByTagNameNS("http://xml.weather.yahoo.com/ns/rss/1.0", "condition");
		Node weatherNode = condition.item(0);
		NamedNodeMap weatherAttributes = weatherNode.getAttributes();
		ret.setTemperature(Float.parseFloat(weatherAttributes.getNamedItem("temp").getNodeValue()));
		ret.setText(weatherAttributes.getNamedItem("text").getNodeValue());
		return ret;
	}

	/**
	 * Created a return {@link Message} that can be sent to the callback
	 * handler.
	 */
	@Override
	protected Message createReturnMessage(Weather data) {
		Message m = new Message();
		m.what = ID;
		m.arg1 = ServiceProcessor.RETURN_MESSAGE_OK;
		m.obj = data;
		return m;
	}

	/**
	 * This method has to inform the callback handler
	 */
	@Override
	public void processWebReply(Context c, ReplyAdapter r, Handler handler) {
		try {
			if (r.getStatus() == Status.FAILED) {
				handler.sendMessage(createErrorMessage(ID, (WebRequest) r.getRequest()));
			} else {
				// processData uses parse(...) and createReturnMessage(...) to
				// create pData
				ProcessingData<Weather> pData = processData(((WebReply) r.getReply()).getData());
				handler.sendMessage(pData.returnMessage);
			}
		} catch (Throwable tr) {
			handler.sendMessage(createErrorMessage(ID, tr, (WebRequest) r.getRequest()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processCachedObject(CachedObject cachedObject, Handler handler, Request request) {
		// no caching!
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProcessorID() {
		return ID;
	}

}
