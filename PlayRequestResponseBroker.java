package com.subdigit.utilities;

import java.util.HashMap;
import java.util.Map;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;


public class PlayRequestResponseBroker extends AbstractRequestResponseBroker<Request,Response>
{
	private boolean _initialized;
	private Request _request;
	private Response _response;
	private Session _session;


	public PlayRequestResponseBroker(){}
	public PlayRequestResponseBroker(Request request, Response response, Session session)
	{
		_initialized = false;
		_request = null;
		_response = null;
		_session = null;
		
		initialize(request, response, session);
	}


	public boolean initialize(Request request, Response response, Session session)
	{
		boolean success = false;

		_request = request;
		_response = response;
		_session = session;

		if(_request != null && _response != null && _session != null) success = true;

		_initialized = success;

		return success;
	}
	
	
	public Request getRequest(){ return _request; }
	public RequestResponseBroker<Request,Response> setRequest(Request value){ _request = value; return this; }

	public Response getResponse(){ return _response; }
	public RequestResponseBroker<Request,Response> setResponse(Response value){ _response = value; return this; }

	public Session getSession(){ return _session; }
	public RequestResponseBroker<Request,Response> setSession(Session value){ _session = value; return this; }
	

	public boolean initialized(){ return _initialized; }

	public Object getAttribute(String key)
	{
		return Cache.get(key);

//		if(_session == null) return null;
//		else return _session.get(key);
	}


	public void setAttribute(String key, Object value)
	{
		Cache.set(key, value);
		
//		if(_session == null) return;
//		_session.put(key, "" + value);
	}


	public void removeAttribute(String key)
	{
		Cache.remove(key);

//		if(_session == null) return;
//		_session.remove(key);
	}


	public String getParameter(String key)
	{
		if(_request == null) return null;
		else return _request.getQueryString(key);
	}


	public String getPathInfo()
	{
		if(_request == null) return null;
		else return _request.path();
	}


	public boolean redirect(String url)
	{
		if(url == null || url.trim().length() == 0) return false;

		Controller.redirect(url);

		return true;
	}


	// Blatantly copied from the play.data.Form.requestData(Http.Request request) method.  It really should have been a public method...
    public Map<String,String> extractRequestData()
    {
        Map<String,String> data = new HashMap<String,String>();

        if(_request == null) return data;
    	
        Map<String,String[]> urlFormEncoded = new HashMap<String,String[]>();
        if(_request.body() != null && _request.body().asFormUrlEncoded() != null) {
            urlFormEncoded = _request.body().asFormUrlEncoded();
        }

        Map<String,String[]> multipartFormData = new HashMap<String,String[]>();
        if(_request.body() != null && _request.body().asMultipartFormData() != null) {
            multipartFormData = _request.body().asMultipartFormData().asFormUrlEncoded();
        }

        Map<String,String> jsonData = new HashMap<String,String>();
        if(_request.body() != null && _request.body().asJson() != null) {
            jsonData = play.libs.Scala.asJava(
                play.api.data.FormUtils.fromJson("", 
                    play.api.libs.json.Json.parse(
                        play.libs.Json.stringify(_request.body().asJson())
                    )
                )
            );
        }

        Map<String,String[]> queryString = _request.queryString();

        for(String key: urlFormEncoded.keySet()) {
            String[] values = urlFormEncoded.get(key);
            if(key.endsWith("[]")) {
                String k = key.substring(0, key.length() - 2);
                for(int i=0; i<values.length; i++) {
                    data.put(k + "[" + i + "]", values[i]);
                }
            } else {
                if(values.length > 0) {
                    data.put(key, values[0]);
                }
            }
        }

        for(String key: multipartFormData.keySet()) {
            String[] values = multipartFormData.get(key);
            if(key.endsWith("[]")) {
                String k = key.substring(0, key.length() - 2);
                for(int i=0; i<values.length; i++) {
                    data.put(k + "[" + i + "]", values[i]);
                }
            } else {
                if(values.length > 0) {
                    data.put(key, values[0]);
                }
            }
        }

        for(String key: jsonData.keySet()) {
            data.put(key, jsonData.get(key));
        }

        for(String key: queryString.keySet()) {
            String[] values = queryString.get(key);
            if(key.endsWith("[]")) {
                String k = key.substring(0, key.length() - 2);
                for(int i=0; i<values.length; i++) {
                    data.put(k + "[" + i + "]", values[i]);
                }
            } else {
                if(values.length > 0) {
                    data.put(key, values[0]);
                }
            }
        }

        return data;
    }
}
