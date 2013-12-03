package com.subdigit.result;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import play.api.templates.Html;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Result;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.subdigit.broker.RequestResponseBroker;
import com.subdigit.data.json.helpers.BaseModelModule;
import com.subdigit.data.result.ModelTransactionResult.KeyTransactionResult;
// import org.apache.http.HttpStatus; from Apache HttpComponents via http://stackoverflow.com/a/730341/223362


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public class ServiceResult<ResultType> extends BasicResult<String, Object>
{
	public static final MediaType DEFAULT_MEDIATYPE		= MediaType.JSON_UTF_8;
	public static final boolean DEFAULT_AUTOMATICCOUNT	= true;
	public static final boolean DEFAULT_REDIRECT		= false;
	public static final boolean DEFAULT_RENDER			= false;
	
	public static final String KEY_FORMAT				= "format";

	@JsonProperty("results")
	protected LinkedList<ResultType> resultList;

//	@JsonProperty("status")
//	LinkedList<Status> _statusArchive;

	protected String version;
	protected int count;
	@JsonProperty("statuscode")
	protected int statusCode;
	protected Action action;
	protected Map<String, KeyTransactionResult> transactions;

	private RequestResponseBroker<Request,Response> _broker;
	private boolean _redirect;
	private Call _redirectTarget;
	private boolean _render;
	private Html _renderTarget;
	private MediaType _mediaType;
	private boolean _automaticCount;


	// This is to kill any use of this without passing in a valid request.
	@SuppressWarnings("unused")
	private ServiceResult(){}


	public ServiceResult(RequestResponseBroker<Request,Response> broker, Action value)
	{
		initialize(broker, value);
	}

	
	public boolean initialize(RequestResponseBroker<Request,Response> broker, Action value)
	{
		boolean initialized = super.initialize();

		if(broker == null) throw new NullPointerException();

		resultList		= new LinkedList<ResultType>();
		version			= "1";
		count			= 0;
		statusCode		= HttpStatus.SC_OK;
		action			= value;
		transactions	= null;
		_broker			= broker;
		_redirect		= DEFAULT_REDIRECT;
		_redirectTarget	= null;
		_render			= DEFAULT_RENDER;
		_renderTarget	= null;
		_mediaType		= DEFAULT_MEDIATYPE;
		_automaticCount	= DEFAULT_AUTOMATICCOUNT;

		addData(_broker.extractRequestData());
		setMediaType(extractMediaType());

		setInitialized(initialized);
		return initialized;
	}


	public List<? extends ResultType> getResults(){ return resultList; }
	public void setResults(List<? extends ResultType> value)
	{
		if(resultList != null) resultList.clear();
		addResults(value);
	}
	public void addResults(List<? extends ResultType> value)
	{
		if(value == null);
		if(resultList == null) resultList = new LinkedList<ResultType>();

		resultList.addAll(value);
	}
	public void addResult(ResultType value)
	{
		if(value == null) return;
		if(resultList == null) resultList = new LinkedList<ResultType>();

		resultList.push(value);
	}
	public int getResultsCount()
	{
		if(resultList == null) return 0;
		else return resultList.size();
	}
	
	
	public void addStatus(int code, String message, StatusLevel statusLevel, JsonNode details){ addStatus(code, message, null, statusLevel, details); }
	public void addStatus(int code, String message, Exception e, StatusLevel statusLevel, JsonNode details)
	{
		ServiceStatus status = new ServiceStatus();
		status.setCode(code);
		status.setMessage(message);
		status.setException(e);
		status.setLevel(statusLevel);
		status.setDetails(details);

		addStatus(status);
	}


	public String getVersion(){ return version; }
	public void setVersion(String value){ version = value; }

	public int getCount(){ return count; }
	public void setCount(int value){ count = value; }

	public int getStatusCode(){ return statusCode; }
	public void setStatusCode(int value){ statusCode = value; }

	public Action getAction(){ return action; }
	public void setAction(Action value){ action = value; }
	
	public Map<String, KeyTransactionResult> getTransactions(){ return transactions; }
	public void setTransactions(Map<String, KeyTransactionResult> value){ transactions = value; }
	
//	public Map<String,String> getRequestData(){ return _data; }
//	public void setRequestData(Map<String,String> value){ _data = value; }
	public String getParameter(String key){ return getData(key); }
	public boolean hasParameter(String key){ return hasData(key); }
	public Map<String,String> getParameters(){ return getDataStore(); }
	
	public boolean getRedirect(){ return _redirect; }
	public void setRedirect(boolean value){ _redirect = value; }
	public boolean redirect(){ return getRedirect(); }
	public void redirect(boolean value){ setRedirect(value); }

	public Call getRedirectTarget(){ return _redirectTarget; }
	public void setRedirectTarget(Call value){ _redirectTarget = value; }

	public boolean getRender(){ return _render; }
	public void setRender(boolean value){ _render = value; }
	public boolean render(){ return getRender(); }
	public void render(boolean value){ setRender(value); }

	public Html getRenderTarget(){ return _renderTarget; }
	public void setRenderTarget(Html value){ _renderTarget = value; }
	
	public MediaType getMediaType(){ return _mediaType; }
	public void setMediaType(MediaType value){ _mediaType = value; }
	private MediaType extractMediaType()
	{
		MediaType mediaType = DEFAULT_MEDIATYPE;
		String key = KEY_FORMAT;
		String format = null;
		Request request = _broker.getRequest();
		
		// See if we have a format override and use that first.
		if(key != null){
			format = getParameter(key);
			if(format != null){
				if(format.equalsIgnoreCase(MediaType.JSON_UTF_8.subtype())) mediaType = MediaType.JSON_UTF_8;
				else if(format.equalsIgnoreCase(MediaType.XML_UTF_8.subtype())) mediaType = MediaType.XML_UTF_8;
				else if(format.equalsIgnoreCase(MediaType.HTML_UTF_8.subtype())) mediaType = MediaType.HTML_UTF_8;
			}
		}

		// No format variable, so rely on what the client accepts, in order of our
		// preferences.
		if(mediaType == null && request != null){
			if(request.accepts(MediaType.JSON_UTF_8.withoutParameters().toString())){
				mediaType = MediaType.JSON_UTF_8;
			} else if(request.accepts(MediaType.XML_UTF_8.withoutParameters().toString())){
				mediaType = MediaType.XML_UTF_8;
			} else if(request.accepts(MediaType.HTML_UTF_8.withoutParameters().toString())){
				mediaType = MediaType.HTML_UTF_8;
			}
		}

		return mediaType;
	}

	public boolean getAutomaticCount(){ return _automaticCount; }
	public void setAutomaticCount(boolean value){ _automaticCount = value; }
	public boolean automaticCount(){ return getAutomaticCount(); }
	public void automaticCount(boolean value){ setAutomaticCount(value); }

	public static ObjectMapper getConfiguredObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.registerModule(new BaseModelModule());
		mapper.setSerializationInclusion(Include.NON_NULL);

/*
		// http://stackoverflow.com/a/7108530/223362
		// Doesnt seem to work if @JsonAutoDetect is also present.
		// And contained classes wont get serialized unless @JsonAutoDetect is set...
		// So we have to do it per class instead of globally.
		mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
			.withFieldVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
			.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
			.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
			.withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
			.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
		);
*/
		
		return mapper;
	}


	public String toJson()
	{
		ObjectMapper mapper = getConfiguredObjectMapper();
		String jsonString = null;

		try {
//			if(getParameter("pretty") != null) mapper.enable(SerializationFeature.INDENT_OUTPUT);
			jsonString = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e){
System.err.println("JsonProcessingException e: " + e);
System.err.println(printDiagnostics());
		}

System.err.println(printDiagnostics());
		
		return jsonString;
	}
	public String toXML(){ return toJson(); }
	public String toHtml(){ return toJson(); }


	public String getOutput()
	{
		MediaType mediaType = _mediaType;

		if(mediaType == null) mediaType = DEFAULT_MEDIATYPE;
		if(mediaType.is(MediaType.JSON_UTF_8)){
			return toJson();
		} else if(mediaType.is(MediaType.XML_UTF_8)){
			return toXML();
		} else if(mediaType.is(MediaType.HTML_UTF_8)){
			return toHtml();
		}
		
		return toJson();
	}


	public String getOutputMediaType()
	{
		MediaType mediaType = _mediaType;

		if(mediaType == null) mediaType = DEFAULT_MEDIATYPE;

		return mediaType.withoutParameters().toString();
	}


	public Result process()
	{
		String output = null;
		String format = null;

		// Do some last minute bookwork.
		if(automaticCount()) setCount(getResultsCount());

		output = getOutput();
		format = getOutputMediaType();

		// First, let's see if we're doing any redirects.
		// Redirects only happen if we have been specifically told to redirect
		// And we have an appropriate redirect target
		// And the format is HTML
		if(redirect() && getRedirectTarget() != null && MediaType.HTML_UTF_8.is(getMediaType())){
			return Controller.redirect(getRedirectTarget());
		}

		if(render() && getRenderTarget() != null && MediaType.HTML_UTF_8.is(getMediaType())){
			return renderViaStatus(getRenderTarget());
		}

		// No redirects or render requests, so let's output the content according to the requested format and status.
		return outputViaStatus(output, format);
	}

	
	private Result outputViaStatus(String output, String format)
	{
		switch(getStatusCode()){
		case HttpStatus.SC_OK:
			return Controller.ok(output).as(format);
		case HttpStatus.SC_CREATED:
			return Controller.created(output).as(format);
		case HttpStatus.SC_NOT_FOUND:
			return Controller.notFound(output).as(format);
		case HttpStatus.SC_BAD_REQUEST:
			return Controller.badRequest(output).as(format);
		case HttpStatus.SC_UNAUTHORIZED:
			return Controller.unauthorized(output).as(format);
		case HttpStatus.SC_FORBIDDEN:
			return Controller.forbidden(output).as(format);
		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			return Controller.internalServerError(output).as(format);
		case HttpStatus.SC_NOT_IMPLEMENTED:
			return Controller.TODO;
		default:
			return Controller.ok(output).as(format);
		}
	}

		
	private Result renderViaStatus(Html output)
	{
		switch(getStatusCode()){
		case HttpStatus.SC_OK:
			return Controller.ok(output);
		case HttpStatus.SC_CREATED:
			return Controller.created(output);
		case HttpStatus.SC_NOT_FOUND:
			return Controller.notFound(output);
		case HttpStatus.SC_BAD_REQUEST:
			return Controller.badRequest(output);
		case HttpStatus.SC_UNAUTHORIZED:
			return Controller.unauthorized(output);
		case HttpStatus.SC_FORBIDDEN:
			return Controller.forbidden(output);
		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			return Controller.internalServerError(output);
		case HttpStatus.SC_NOT_IMPLEMENTED:
			return Controller.TODO;
		default:
			return Controller.ok(output);
		}
	}


	public enum Action
	{
		GET,
		CREATE,
		UPDATE,
		DELETE,
		AUTHENTICATE,
		UNKNOWN;

		// I like my enums printed out in lower case, kthxbai.
		public String toString(){ return name().toLowerCase(); }
	}


	@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC, getterVisibility=JsonAutoDetect.Visibility.NONE)
	@JsonInclude(Include.NON_NULL)
	public class ServiceStatus extends BasicResult<String, Object>.Status
	{
		protected JsonNode details;

		public JsonNode getDetails(){ return details; }
		public ServiceStatus setDetails(JsonNode value){ details = value; return this; }
	}
}