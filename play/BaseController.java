package com.subdigit.data.play;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpStatus;

import play.api.templates.Html;
import play.data.Form;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;

import com.subdigit.acronymity.data.models.User;
import com.subdigit.broker.PlayRequestResponseBroker;
import com.subdigit.data.BaseManager;
import com.subdigit.data.BaseModel;
import com.subdigit.data.BaseService;
import com.subdigit.data.BasicAccessor;
import com.subdigit.data.result.ModelTransactionResult;
import com.subdigit.result.BasicResult.StatusLevel;
import com.subdigit.result.ServiceResult;


//public abstract class AbstractModelControllerHelper<T extends BaseService<BaseModel, ObjectId, BaseManager<BaseModel,ObjectId>>,K extends BaseModel> extends Controller
//public abstract class AbstractModelControllerHelper<T extends BaseService<BaseModel,L,BaseManager<K,L>>, K extends BaseModel, L> extends Controller
//@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseController<T extends BaseModel<K>, K, X extends BasicAccessor<T, K>, Y extends BaseManager<T, K, X>, Z extends BaseService<T, K, X, Y>> extends Controller
//public class AccountController extends AbstractModelControllerHelper<AccountService, AccountManager, Account, ObjectId>
//public abstract class AbstractModelControllerHelper<T extends BaseService<K, Z, X>, X extends BaseManager<K, Z>, K extends BaseModel, Z> extends Controller
{
	protected Z _service;
	protected Form<T> _dataForm;


	public BaseController()
	{
		_service = initializeService();
		_dataForm = initializeDataForm();
	}


	protected abstract Z initializeService();
	protected abstract Form<T> initializeDataForm();
	protected abstract Html getSingleItemHtmlRenderTarget(T model);	// views.html.acronyms.item.render(acronym);
	protected abstract Call getSingleItemDisplayRedirectRoute(String id);	// routes.UserController.getUser(model.getId().toString())

	protected abstract boolean isAllowedToRetrieve(@NotNull ServiceResult<T> result);
	protected abstract boolean isAllowedToCreate(@NotNull ServiceResult<T> result);
	protected abstract boolean isAllowedToUpdate(@NotNull ServiceResult<T> result, @NotNull T model);
	protected abstract boolean isAllowedToDelete(@NotNull ServiceResult<T> result, @NotNull T model);

	protected abstract T setCreateOverrides(T model);


	public Result getList()
	{
		ServiceResult<T> result = new ServiceResult<T>(new PlayRequestResponseBroker(request(), response(), session()), ServiceResult.Action.GET);
		List<T> resultList = _service.retrieve(result.getParameters());

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

		return result.process();
	}


	public Result get(String value)
	{
		ServiceResult<T> result = new ServiceResult<T>(new PlayRequestResponseBroker(request(), response(), session()), ServiceResult.Action.GET);
		T model = null;

		// Stop if we aren't provided with anything.
		if(value == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result.process();
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

			return result.process();
		}

		// Found something!  Register and process the results.
		result.setSuccess(true);
		result.setStatusCode(HttpStatus.SC_OK);
		result.addResult(model);

		// If the request type turns out to be HTML, this is what we will render.
		result.setRenderTarget(getSingleItemHtmlRenderTarget(model));

		return result.process();
	}


	public Result create()
	{
		PlayRequestResponseBroker broker = new PlayRequestResponseBroker(request(), response(), session());
		ModelTransactionResult<T> createResult = null;
		ServiceResult<T> result = new ServiceResult<T>(broker, ServiceResult.Action.CREATE);
		T model = null;

		// Is this request authorized to create?
		if(!isAllowedToCreate(result)) return result.process();

		// Got something, let's see if we can create an object out of it.
		createResult = _service.instantiateModel(broker.extractRequestData());
		result.setTransactions(createResult.getTransactions());

		// Let see what the result was...
		if(createResult.success()){
			model = createResult.getModel();
			model = setCreateOverrides(model);

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
				result.setRedirectTarget(getSingleItemDisplayRedirectRoute(model.getId().toString()));
				result.setRedirect(true);
			}
		} else {
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not create with the information provided.", StatusLevel.FATAL);
		}

		return result.process();
	}

/*	
	public Result create_ORIG()
	{
		ServiceResult result = new ServiceResult(new PlayRequestResponseBroker(request(), response(), session()));
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

	public Result update(String value)
	{
		PlayRequestResponseBroker broker = new PlayRequestResponseBroker(request(), response(), session());
		ModelTransactionResult<T> updateResult = null;
		ServiceResult<T> result = new ServiceResult<T>(broker, ServiceResult.Action.UPDATE);
//		Form<K> filledForm = _dataForm.bindFromRequest();
		T model = null;

		// Stop if we aren't provided with anything.
		if(value == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result.process();
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

			return result.process();
		}

		// Is this request authorized to update?
		if(!isAllowedToUpdate(result, model)) return result.process();

		// Found something!  Update it and process the results.
		updateResult = _service.update(broker.getSessionUser(), model, broker.extractRequestData());
		result.setTransactions(updateResult.getTransactions());

		// Let see what the result was...
		if(updateResult.success()){
			model = updateResult.getModel();

			result.setSuccess(true);
			result.setStatusCode(HttpStatus.SC_OK);
			result.addResult(model);
			if(model != null && model.getId() != null){
				result.setRedirectTarget(getSingleItemDisplayRedirectRoute(model.getId().toString()));
				result.setRedirect(true);
			}
		} else {
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			result.addStatus(HttpStatus.SC_BAD_REQUEST, "Could not save with the information provided.", StatusLevel.FATAL);
		}

		return result.process();
	}


	public Result delete(String id)
	{
		ServiceResult<T> result = new ServiceResult<T>(new PlayRequestResponseBroker(request(), response(), session()), ServiceResult.Action.DELETE);
		T model = null;
		
		// Stop if we aren't provided with anything.
		if(id == null){
			result.setSuccess(false);
			result.setStatusCode(HttpStatus.SC_NOT_FOUND);
			result.addStatus(HttpStatus.SC_NOT_FOUND, "Id provided was null.", StatusLevel.FATAL);

			return result.process();
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

			return result.process();
		}

		// Is this request authorized to delete?
		if(!isAllowedToDelete(result, model)) return result.process();
		
		// Found something!  Delete it and process the results.
		_service.delete(model);

		// If we got back a valid model, that means it updated properly.
		result.setSuccess(true);
		result.setStatusCode(HttpStatus.SC_OK);
		result.addResult(model);
//		result.setRedirectTarget(getSingleItemDisplayRedirectRoute(model.getId().toString()));
//		result.setRedirect(true);
		
		return result.process();
	}


	public Result restricted(ServiceResult.Action action, String message)
	{
		ServiceResult<T> result = new ServiceResult<T>(new PlayRequestResponseBroker(request(), response(), session()), action);

		result.setSuccess(false);
		result.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
		result.addStatus(HttpStatus.SC_METHOD_NOT_ALLOWED, message, StatusLevel.FATAL);

		return result.process();
	}


	public Result unknown(String action)
	{
		ServiceResult<T> result = new ServiceResult<T>(new PlayRequestResponseBroker(request(), response(), session()), ServiceResult.Action.UNKNOWN);

		result.setSuccess(false);
		result.setStatusCode(HttpStatus.SC_PAYMENT_REQUIRED);
		result.addStatus(HttpStatus.SC_PAYMENT_REQUIRED, "Ah, we haven't implemented the '" + action + "' action to do whatever you wanted to do.  Perhaps if the status code requirement was met...", StatusLevel.FATAL);

		return result.process();
	}


	public void setSessionUser(User user)
	{
		PlayRequestResponseBroker broker = new PlayRequestResponseBroker(request(), response(), session());

		broker.setSessionUser(user);
	}


	public User getSessionUser()
	{
		PlayRequestResponseBroker broker = new PlayRequestResponseBroker(request(), response(), session());

		return broker.getSessionUser();
	}
}
