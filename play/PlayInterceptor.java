package com.subdigit.data.play;

import org.apache.http.HttpStatus;

import play.api.templates.Html;
import play.mvc.Call;
import play.mvc.Result;

import com.subdigit.acronymity.data.models.User;
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
import com.subdigit.result.BasicResult.StatusLevel;
import com.subdigit.result.play.PlayResult;
import com.subdigit.result.ServiceResult;
import com.subdigit.result.WebResult;


public abstract class PlayInterceptor<T extends BaseModel<K>, K, X extends BasicAccessor<T, K>, Y extends BaseManager<User, T, K, X>, Z extends BaseService<User, T, K, X, Y>> extends BaseInterceptor<User, T, K, X, Y, Z, Result, Html, Call>
{
	@Override
	protected RequestResponseBroker<?, ?, ?> getNewBroker(){ return getNewPlayBroker(); }
	protected abstract PlayRequestResponseBroker getNewPlayBroker();

	@Override
	protected WebResult<T, Result, Html, Call> getNewResult(WebRequest<User> request){ return getNewPlayResult((PlayRequest) request); }
	protected abstract PlayResult<T> getNewPlayResult(PlayRequest request);

	@Override
	protected WebRequest<User> getNewRequest(RequestResponseBroker<?, ?, ?> broker, Action action){ return (WebRequest<User>) getNewPlayRequest((PlayRequestResponseBroker) broker, action); }
	protected abstract PlayRequest getNewPlayRequest(PlayRequestResponseBroker broker, Action action);


	@Override protected boolean isAllowedToRetrieve(ServiceResult<T> result, User user){ return true; }
	@Override protected boolean isAllowedToCreate(ServiceResult<T> result, User user)
	{
		if(user == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
			result.addStatus(HttpStatus.SC_UNAUTHORIZED, "You must login before being allowed to create.", StatusLevel.FATAL);
			
			return false;
		}

		return true;
	}

	@Override protected boolean isAllowedToUpdate(ServiceResult<T> result, User user, T model)
	{
		if(user == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
			result.addStatus(HttpStatus.SC_UNAUTHORIZED, "You must login before being allowed to update.", StatusLevel.FATAL);
			
			return false;

		} else if(user.isAdmin()){
			return true;

		} else if(!isCreator(user, model)){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
			result.addStatus(HttpStatus.SC_UNAUTHORIZED, "You are not authorized to update!", StatusLevel.FATAL);
			
			return false;
		}

		return true;
	}

	@Override protected boolean isAllowedToDelete(ServiceResult<T> result, User user, T model)
	{
		if(user == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
			result.addStatus(HttpStatus.SC_UNAUTHORIZED, "You must login before being allowed to delete.", StatusLevel.FATAL);
			
			return false;

		} else if(user.isAdmin()){
			return true;

		} else if(!isCreator(user, model)){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
			result.addStatus(HttpStatus.SC_UNAUTHORIZED, "You are not authorized to delete!", StatusLevel.FATAL);

			return false;
		}

		return true;
	}

	protected abstract boolean isCreator(User user, T model);
	protected abstract T setCreateOverrides(User user, T model);
}
