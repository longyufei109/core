package com.subdigit.result;

import java.util.Map;

import org.apache.http.HttpStatus;

import play.api.templates.Html;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.subdigit.data.json.helpers.BaseModelModule;
import com.subdigit.request.WebRequest;
// import org.apache.http.HttpStatus; from Apache HttpComponents via http://stackoverflow.com/a/730341/223362


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public class WebResult<ResultType> extends ServiceResult<ResultType>
{
	public static final MediaType DEFAULT_MEDIATYPE		= MediaType.JSON_UTF_8;
	public static final boolean DEFAULT_AUTOMATICCOUNT	= true;
	public static final boolean DEFAULT_REDIRECT		= false;
	public static final boolean DEFAULT_RENDER			= false;

    protected String version;
	protected int count;
    protected Map<String, String> parameters;

	private WebRequest _request;

	private boolean _redirect;
	private Call _redirectTarget;
	private boolean _render;
	private Html _renderTarget;
	private boolean _automaticCount;


	public WebResult(WebRequest value){ super(value); }


	protected boolean initialize(WebRequest value)
	{
		boolean initialized = super.initialize(value);

		count			= 0;
		_redirect		= DEFAULT_REDIRECT;
		_redirectTarget	= null;
		_render			= DEFAULT_RENDER;
		_renderTarget	= null;
		_automaticCount	= DEFAULT_AUTOMATICCOUNT;
		_request		= value;

		version = _request.getVersion();
		parameters = _request.getParameters();

		return initialized;
	}


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

	public boolean getAutomaticCount(){ return _automaticCount; }
	public void setAutomaticCount(boolean value){ _automaticCount = value; }
	public boolean automaticCount(){ return getAutomaticCount(); }
	public void automaticCount(boolean value){ setAutomaticCount(value); }

	public int getCount(){ return count; }
	public void setCount(int value){ count = value; }


	public ObjectMapper getConfiguredObjectMapper()
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
		MediaType mediaType = _request.getMediaType();

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


	public Result process()
	{
		String output = null;
		String format = null;

		// Do some last minute bookwork.
		if(automaticCount()) setCount(getResultsCount());

		output = getOutput();
		format = _request.getMediaTypeAsString();

		// First, let's see if we're doing any redirects.
		// Redirects only happen if we have been specifically told to redirect
		// And we have an appropriate redirect target
		// And the format is HTML
		if(redirect() && getRedirectTarget() != null && MediaType.HTML_UTF_8.is(_request.getMediaType())){
			return Controller.redirect(getRedirectTarget());
		}

		if(render() && getRenderTarget() != null && MediaType.HTML_UTF_8.is(_request.getMediaType())){
			return renderViaStatus(getStatusCode(), getRenderTarget());
		}

		// No redirects or render requests, so let's output the content according to the requested format and status.
		return outputViaStatus(getStatusCode(), output, format);
	}

	
	private Result outputViaStatus(int status, String output, String format)
	{
		switch(status){
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

		
	private Result renderViaStatus(int status, Html output)
	{
		switch(status){
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
}