package com.subdigit.result.play;

import org.apache.http.HttpStatus;

import play.api.templates.Html;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.common.net.MediaType;
import com.subdigit.request.BasicRequest;
import com.subdigit.request.play.PlayRequest;
import com.subdigit.result.WebResult;


public class PlayResult<ReturnObjectType> extends WebResult<ReturnObjectType, Result, Html, Call>
{
	private PlayRequest _request;

	
	public PlayResult(PlayRequest value){ super(value); }


	protected boolean initialize(BasicRequest<?> value)
	{
		boolean initialized = super.initialize(value);

		_request		= (PlayRequest) value;

		return initialized;
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