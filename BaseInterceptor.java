package com.subdigit.data;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpStatus;

import com.subdigit.broker.RequestResponseBroker;
import com.subdigit.data.result.ModelTransactionResult;
import com.subdigit.request.WebRequest;
import com.subdigit.result.BasicResult.StatusLevel;
import com.subdigit.result.ServiceResult;
import com.subdigit.result.WebResult;


public abstract class BaseInterceptor<UserType, T extends BaseModel<K>, K, X extends BasicAccessor<T, K>, Y extends BaseManager<UserType, T, K, X>, Z extends BaseService<UserType, T, K, X, Y>, ResultType, RenderTargetType, RedirectTargetType>
{
	protected Z _service;


	public BaseInterceptor()
	{
		_service = initializeService();
	}


	protected abstract Z initializeService();
	protected abstract RequestResponseBroker<?,?,?> getNewBroker();
	protected abstract WebResult<T,ResultType,RenderTargetType,RedirectTargetType> getNewResult(WebRequest<UserType> request);
	protected abstract WebRequest<UserType> getNewRequest(RequestResponseBroker<?,?,?> broker, WebRequest.Action action);

	protected abstract RenderTargetType getRenderTarget(T model);
	protected abstract RedirectTargetType getRedirectTarget(String id);
	
	protected abstract boolean isAllowedToRetrieve(@NotNull ServiceResult<T> result, UserType userType);
	protected abstract boolean isAllowedToCreate(@NotNull ServiceResult<T> result, UserType userType);
	protected abstract boolean isAllowedToUpdate(@NotNull ServiceResult<T> result, UserType userType, @NotNull T model);
	protected abstract boolean isAllowedToDelete(@NotNull ServiceResult<T> result, UserType userType, @NotNull T model);

	protected abstract T setCreateOverrides(UserType userType, T model);


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> getList()
	{
		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.GET);
//		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.GET);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);
//		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = new WebResult<T,ResultType,RenderTargetType,RedirectTargetType>(request);

		// Is this request authorized to retrieve?
		if(!isAllowedToRetrieve(result, request.getAuthenticatedUser())){
			return result;
		}
		
		List<T> resultList = _service.retrieve(request.getParameters());

		if(resultList == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Error getting results.", StatusLevel.WARNING);
//			result.addResults(new ArrayList<K>());
		} else {
			result.setSuccess(true);
			result.setStatusCode(HttpStatus.SC_OK);
			result.addResults(resultList);
		}

		return result;
	}


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> get(String value)
	{
		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.GET);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);
		T model = null;

		// Stop if we aren't provided with anything.
		if(value == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result;
		}

		// Is this request authorized to retrieve?
		if(!isAllowedToRetrieve(result, request.getAuthenticatedUser())){
			return result;
		}
		
		// Check to see if this is a valid Id.
		try {
			model = _service.retrieveById(value);
		} catch(IllegalArgumentException e){
			model = null;
		}

		// Since we found no one, return an error.
		if(model == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided could not be found: " + value + ".", StatusLevel.FATAL);

			return result;
		}

		// Found something!  Register and process the results.
		result.setSuccess(true);
		result.setStatusCode(HttpStatus.SC_OK);
		result.addResult(model);

		// If the request type turns out to be HTML, this is what we will render.
		result.setRenderTarget(getRenderTarget(model));

		return result;
	}


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> create()
	{
		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.CREATE);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);
		ModelTransactionResult<T> createResult = null;
		T model = null;

		// Is this request authorized to create?
		if(!isAllowedToCreate(result, request.getAuthenticatedUser())){
			return result;
		}

		// Got something, let's see if we can create an object out of it.
		createResult = _service.instantiateModel(request.getParameters());
		result.setTransactions(createResult.getTransactions());

		// Let see what the result was...
		if(createResult.success()){
			model = createResult.getModel();
			model = setCreateOverrides(request.getAuthenticatedUser(), model);

			// Only try to create if we have a green light everything else.
			model = _service.create(model);

			// Something went wrong.
			if(model == null){
				result.setSuccess(false);
				result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not create with the information provided.", StatusLevel.FATAL);

			// Creation successful.
			} else {
				result.setSuccess(true);
				result.setStatusCode(HttpStatus.SC_CREATED);
				result.addResult(model);
				result.setRedirectTarget(getRedirectTarget(model.getId().toString()));
				result.setRedirect(true);
			}
		} else {
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not create with the information provided.", StatusLevel.FATAL);
		}

		return result;
	}

/*	
	public Result create_ORIG()
	{
		ServiceResult result = new ServiceResult(getNewBroker());
		Form<T> filledForm = _dataForm.bindFromRequest();
		T model = null;

		// Is this request authorized to create?
		if(!isAllowedToCreate(result)) return result.process();

		if(!filledForm.hasErrors()){
			model = filledForm.get();
			model = _service.create(model);

			result.setSuccess(true);
			result.setStatusCode(HttpStatus.SC_CREATED);
			result.addResult(model);
			result.setRedirectTarget(getSingleItemDisplayRedirectRoute(model.getId().toString()));
			result.setRedirect(true);
		} else {
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
System.err.println("Adding bad request status on create");
			result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not create with the information provided.", StatusLevel.FATAL, filledForm.errorsAsJson());
		}

		return result.process();
	}
*/

	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> update(String value)
	{
		RequestResponseBroker<?,?,?> broker = getNewBroker();
		WebRequest<UserType> request = getNewRequest(broker, WebRequest.Action.UPDATE);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);
		ModelTransactionResult<T> updateResult = null;
//		Form<K> filledForm = _dataForm.bindFromRequest();
		T model = null;

		// Stop if we aren't provided with anything.
		if(value == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result;
		}

		// Check to see if this is a valid Id.
		try {
			model = _service.retrieveById(value);
		} catch(IllegalArgumentException e){
			model = null;
		}

		// Since we found nothing, return an error.
		if(model == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided could not be found: " + value + ".", StatusLevel.FATAL);

			return result;
		}

		// Is this request authorized to update?
		if(!isAllowedToUpdate(result, request.getAuthenticatedUser(), model)){
			return result;
		}

		// Found something!  Update it and process the results.
		updateResult = _service.update(request.getAuthenticatedUser(), model, request.getParameters());
		result.setTransactions(updateResult.getTransactions());

		// Let see what the result was...
		if(updateResult.success()){
			model = updateResult.getModel();

			result.setSuccess(true);
			result.setStatusCode(HttpStatus.SC_OK);
			result.addResult(model);
			if(model != null && model.getId() != null){
				result.setRedirectTarget(getRedirectTarget(model.getId().toString()));
				result.setRedirect(true);
			}
		} else {
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not save with the information provided.", StatusLevel.FATAL);
		}

		return result;
	}


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> delete(String id)
	{
		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.DELETE);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);
		T model = null;
		
		// Stop if we aren't provided with anything.
		if(id == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result;
		}

		// Check to see if this is a valid Id.
		try {
			model = _service.retrieveById(id);
		} catch(IllegalArgumentException e){
			model = null;
		}

		// Since we found nothing, return an error.
		if(model == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided could not be found: " + id + ".", StatusLevel.FATAL);

			return result;
		}

		// Is this request authorized to delete?
		if(!isAllowedToDelete(result, request.getAuthenticatedUser(), model)){
			return result;
		}
		
		// Found something!  Delete it and process the results.
		_service.delete(model);

		// If we got back a valid model, that means it updated properly.
		result.setSuccess(true);
		result.setStatusCode(HttpStatus.SC_OK);
		result.addResult(model);
//		result.setRedirectTarget(getSingleItemDisplayRedirectRoute(model.getId().toString()));
//		result.setRedirect(true);
		
		return result;
	}


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> restricted(WebRequest.Action action, String message)
	{
//		WebRequest<UserType> request = getNewRequest(action);
		WebRequest<UserType> request = getNewRequest(getNewBroker(), action);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);

		result.setSuccess(false);
		result.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
		result.addStatus(HttpStatus.SC_METHOD_NOT_ALLOWED, message, StatusLevel.FATAL);

		return result;
	}


	public WebResult<T,ResultType,RenderTargetType,RedirectTargetType> unknown(String action)
	{
		WebRequest<UserType> request = getNewRequest(getNewBroker(), WebRequest.Action.UNKNOWN);
		WebResult<T,ResultType,RenderTargetType,RedirectTargetType> result = getNewResult(request);

		result.setSuccess(false);
		result.setStatusCode(HttpStatus.SC_PAYMENT_REQUIRED);
		result.addStatus(HttpStatus.SC_PAYMENT_REQUIRED, "Ah, we haven't implemented the '" + action + "' action to do whatever you wanted to do.  Perhaps if the status code requirement was met...", StatusLevel.FATAL);

		return result;
	}
}
