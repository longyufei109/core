package com.subdigit.data;

import java.util.List;
import java.util.Map;

import org.mongodb.morphia.query.Query;

import com.subdigit.data.result.ModelTransactionResult;
import com.subdigit.data.result.ModelTransactionResult.KeyTransactionResult;
import com.subdigit.utilities.Converter;

/**
 * All the business logic for handling the User class goes here.  Basically,
 * these are the methods that will be available to the Java classes for manipulating
 * this object.  All the convenient wrapper classes should be placed here.
 * 
 * @author subdigit
 *
 */
public abstract class BaseService<UserType, ModelType extends BaseModel<IdType>, IdType, AccessorType extends BasicAccessor<ModelType, IdType>, ManagerType extends BaseManager<UserType, ModelType, IdType, AccessorType>>
{
	public static final int DEFAULT_LIMIT = 20;
	public static final int MAX_LIMIT = 100;


	protected ManagerType _manager;


	public BaseService()
	{
		initialize();
	}

	public void initialize()
	{
		_manager = initializeManager();
	}

	protected abstract ManagerType initializeManager();

	public ModelType create(ModelType value){ return _manager.save(value); }
	public ModelTransactionResult<ModelType> instantiateModel(Map<String,String> parameterMap)
	{
		boolean success = true;
		KeyTransactionResult keyCreateResult = null;
		ModelTransactionResult<ModelType> createResult = null;
		ModelType model = null;
		
		createResult = new ModelTransactionResult<ModelType>();
		createResult.setSuccess(false);

		model = _manager.newModelInstance();

System.err.println("Model before: " + model);

		// Hmm, should this throw an NPE instead?
		if(model == null){
			return createResult;
		}

		// Do we have all the parameters needed to create?
		// Do the constraints hold?
		if(!_manager.parameterRequirementCheck(createResult, parameterMap)){
			return createResult;
		}

		if(parameterMap != null){
			// We should probably drop some hints here about the parameters that are incoming.
			// Ugh, this is rebuilding the play.form.Form isn't it...
			for(String key : parameterMap.keySet()){
System.err.println("Checking key: " + key);
				if(_manager.canHandleKey(model, key)){
					keyCreateResult = new KeyTransactionResult();
					keyCreateResult.setKey(key);
					keyCreateResult.setStringValue(parameterMap.get(key));
					keyCreateResult.setSuccess(false);
					
					_manager.setToModel(keyCreateResult, model, keyCreateResult.getKey(), keyCreateResult.getStringValue());
					success = keyCreateResult.success() && success;
				
					createResult.addKeyTransaction(keyCreateResult);
				}
			}
		}

		// Remember the current state.
		createResult.setSuccess(success);
		if(createResult.success()) createResult.setModel(model);
		
		return createResult;
	}

	public List<ModelType> retrieve(){ return _manager.get(); }
	public ModelType retrieveById(IdType value){ return _manager.getById(value); }
	public ModelType retrieveById(String value){ return _manager.getById(value); }
	public List<ModelType> retrieve(Query<ModelType> value){ return _manager.get(value); }
	public List<ModelType> retrieve(Map<String,String> parameters){ return _manager.get(parameterizedQuery(createBaseQuery(parameters), parameters)); }

	public Query<ModelType> createBaseQuery(Map<String,String> parameters)
	{
		Query<ModelType> query = _manager.createQuery();
		int limit = DEFAULT_LIMIT;
		int offset = 0;
		int page = 1;

		if(query == null) return query;
		
		if(parameters != null){
			if(parameters.containsKey("limit")){
				limit = Converter.toint(parameters.get("limit"), limit);
				if(limit > MAX_LIMIT) limit = MAX_LIMIT;
			}
			
			if(parameters.containsKey("page")){
				offset = (Converter.toint(parameters.get("page"), page)-1) * limit;
			}
		}

		query.limit(limit).offset(offset);

		return query;
	}
	protected abstract Query<ModelType> parameterizedQuery(Query<ModelType> query, Map<String,String> parameters);

	public ModelType update(ModelType value){ return _manager.save(value); }
	
	public ModelTransactionResult<ModelType> update(UserType user, String id, Map<String,String> parameterMap){ return update(user, retrieveById(id), parameterMap); }
	public ModelTransactionResult<ModelType> update(UserType user, ModelType model, Map<String,String> parameterMap)
	{
		boolean success = true;
		KeyTransactionResult keyUpdateResult = null;
		ModelTransactionResult<ModelType> updateResult = new ModelTransactionResult<ModelType>();

System.err.println("Model before: " + model);

		// Hmm, should this throw an NPE instead?
		if(model == null || parameterMap == null || parameterMap.size() == 0){
			return updateResult;
		}

		// We should probably drop some hints here about the parameters that are incoming.
		// Ugh, this is rebuilding the play.form.Form isn't it...
		for(String key : parameterMap.keySet()){
System.err.println("Checking if can handle: " + key);
			if(_manager.canHandleKey(model, key)){
System.err.println("  Maybe");
				keyUpdateResult = new KeyTransactionResult();
				keyUpdateResult.setKey(key);
				keyUpdateResult.setStringValue(parameterMap.get(key));
				keyUpdateResult.setSuccess(false);

				if(_manager.allowedToUpdateKey(user, model, key)){
System.err.println("  Yes");
					_manager.setToModel(keyUpdateResult, model, keyUpdateResult.getKey(), keyUpdateResult.getStringValue());
					success = keyUpdateResult.success() && success;
				
					updateResult.addKeyTransaction(keyUpdateResult);
				} else {
System.err.println("  No");
					keyUpdateResult.setState(KeyTransactionResult.State.IMMUTABLEKEY);
					updateResult.addKeyTransaction(keyUpdateResult);
					success = false;
				}
System.err.println("  NO");
			}
		}

		updateResult.setSuccess(success);
System.err.println("Model after: " + updateResult.getModel());
		
		// Only update if we have a green light.
		if(updateResult.success()){
			model = update(model);
			updateResult.setModel(model);
		}
		
		return updateResult;
	}


	public ModelType delete(ModelType value){ return _manager.delete(value); }
	public ModelType deleteById(IdType value){ return _manager.deleteById(value); }
	public ModelType deleteById(String value){ return _manager.deleteById(value); }

	public long count(){ return _manager.count(); }
	public long count(Query<ModelType> q){ return _manager.count(q); }
	public long count(String key, Object value){ return _manager.count(key, value); }

	public ManagerType getManager(){ return _manager; }
	public AccessorType getDAO(){ return _manager.getDAO(); }
}
