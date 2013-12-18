package com.subdigit.data.play;

import play.api.templates.Html;
import play.mvc.Call;
import play.mvc.Result;

import com.subdigit.broker.RequestResponseBroker;
import com.subdigit.broker.play.PlayRequestResponseBroker;
import com.subdigit.data.BaseInterceptor;
import com.subdigit.data.BaseManager;
import com.subdigit.data.BaseModel;
import com.subdigit.data.BaseService;
import com.subdigit.data.BasicAccessor;
import com.subdigit.request.WebRequest;
import com.subdigit.request.WebRequest.Action;
import com.subdigit.request.play.PlayRequest;
import com.subdigit.result.WebResult;
import com.subdigit.result.play.PlayResult;


public abstract class PlayInterceptor<UserType, T extends BaseModel<K>, K, X extends BasicAccessor<T, K>, Y extends BaseManager<UserType, T, K, X>, Z extends BaseService<UserType, T, K, X, Y>> extends BaseInterceptor<UserType, T, K, X, Y, Z, Result, Html, Call>
{
	@Override
	protected RequestResponseBroker<?, ?, ?> getNewBroker(){ return getNewPlayBroker(); }
	protected abstract PlayRequestResponseBroker getNewPlayBroker();

	@Override
	protected WebResult<T, Result, Html, Call> getNewResult(WebRequest<UserType> request){ return getNewPlayResult((PlayRequest<UserType>) request); }
	protected abstract PlayResult<T> getNewPlayResult(PlayRequest<UserType> request);

	@Override
	protected WebRequest<UserType> getNewRequest(RequestResponseBroker<?, ?, ?> broker, Action action){ return getNewPlayRequest((PlayRequestResponseBroker) broker, action); }
	protected abstract PlayRequest<UserType> getNewPlayRequest(PlayRequestResponseBroker broker, Action action);
}
