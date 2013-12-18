package com.subdigit.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateResults;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.subdigit.data.result.ModelTransactionResult;
import com.subdigit.data.result.ModelTransactionResult.KeyTransactionResult;
import com.subdigit.data.utilities.DataConverter;


/**
 * All the business logic for managing the data goes here.  Basically,
 * these are the methods that will be available to the Java classes for manipulating
 * this object.  All the convenient wrapper classes should be placed here.  Any connection
 * to the underlying datastore is made through the appropriate DAO object.
 *
 * @author subdigit
 *
 */
public abstract class BaseManager<UserType, ModelType extends BaseModel<IdType>, IdType, AccessorType extends BasicAccessor<ModelType, IdType>>
{
	protected AccessorType _accessor;


	protected BaseManager()
	{
		initialize();
	}


	protected void initialize()
	{
		_accessor = initializeAccessor();
	}


	protected abstract AccessorType initializeAccessor();
	protected abstract IdType newKeyInstance(String value);
	public abstract ModelType newModelInstance();
	public abstract boolean validate(ModelType value);

	public List<ModelType> get()
	{
		return _accessor.get();
	}


	public Query<ModelType> createQuery()
	{
		return _accessor.createQuery();
	}


	public ModelType getById(IdType value){ return _accessor.get(value); }
	public ModelType getById(String value){ return _accessor.get(newKeyInstance(value)); }


	public List<ModelType> getByFieldName(String key, Object value)
	{
		Query<ModelType> query = _accessor.createQuery().field(key).equal(value);

		return get(query);
	}


	public ModelType getFirstByFieldName(String key, Object value)
	{
		List<ModelType> list = getByFieldName(key, value);
		if(list != null && list.size() > 0) return list.get(0);
		else return null;

	}


	public AggregationOutput getAggregationByField(String field)
	{
		DBObject fields = null;
		DBObject project = null;
		DBObject match = null;
		DBObject group = null;
		DBCollection collection = null;
		AggregationOutput output = null;

		collection = _accessor.getCollection();

		match = new BasicDBObject(field, new BasicDBObject("$exists", true));
		match = new BasicDBObject("$match", match);

		fields = new BasicDBObject(field, 1);
		fields.put("_id", 0);
		project = new BasicDBObject("$project", fields);

		fields = new BasicDBObject( "_id", "$" + field);
		fields.put("count", new BasicDBObject( "$sum", 1));
		group = new BasicDBObject("$group", fields);

		// run aggregation
		output = collection.aggregate(match, project, group);

		return output;
	}


	public List<ModelType> get(Query<ModelType> value)
	{
		QueryResults<ModelType> qr = _accessor.find(value);
		return qr.asList();
	}


	public ModelType save(ModelType value)
	{
		if(value != null){
			Date now = new Date();
			if(value.getDateCreated() == null) value.setDateCreated(now);
			value.setDateUpdated(now);

			if(!validate(value)){
System.err.println("Validation failed during manager.save");
				return null;
			}

			_accessor.save(value);

			// If this was something new, make it not new.
			value.setIsNew(false);
		}

		return value;
	}

	
	public ModelType delete(ModelType value)
	{
		if(value != null){
			Date now = new Date();
			value.setDateDeleted(now);
			value.setIsMarkedForDeletion(true);
		} else return null;

		WriteResult wr = _accessor.delete(value);
		UpdateResults<ModelType> ur = new UpdateResults<ModelType>(wr);

		if(ur.getHadError()) return null;
		else return value;
	}
	public ModelType deleteById(IdType value){ return delete(getById(value)); }
	public ModelType deleteById(String value){ return delete(getById(value)); }


	public long count(){ return _accessor.count(); }
	public long count(Query<ModelType> q){ return _accessor.count(q); }
	public long count(String key, Object value){ return _accessor.count(key, value); }


	public AccessorType getDAO(){ return _accessor; }


	public Method getterMethod(@NotNull ModelType model, String key)
	{
		Method getMethod = null;

		for(Method method : model.getClass().getMethods()){
			if(method.getName().equalsIgnoreCase("get" + key)){
				getMethod = method;
				break;
			}
		}

		return getMethod;
	}


	public Method setterMethod(@NotNull ModelType model, String key)
	{
		Method setMethod = null;

		for(Method method : model.getClass().getMethods()){
			if(method.getName().equalsIgnoreCase("set" + key)){
				setMethod = method;
				break;
			}
		}

		return setMethod;
	}


	public boolean canHandleKey(@NotNull ModelType model, String key)
	{
		Method setMethod = null;
		Class<?>[] parameterTypes = null;

		setMethod = setterMethod(model, key);

		// If there wasn't a corresponding set method, c'est la vie, just move on.
		if(setMethod == null) return false;

		// If we arent dealing with simple parameters, we cant handle this.
		parameterTypes = setMethod.getParameterTypes();
		if(parameterTypes == null || parameterTypes.length != 1) return false;

		// If we got this far, we can handle this key.
		return true;
	}


	public abstract boolean allowedToUpdateKey(@NotNull UserType user, @NotNull ModelType model, String key);
	public abstract boolean parameterRequirementCheck(@NotNull ModelTransactionResult<ModelType> result, Map<String,String> parameterMap);

	protected boolean mustBePresent(@NotNull ModelTransactionResult<ModelType> result, String key, Map<String,String> parameterMap)
	{
		boolean success = true;
		KeyTransactionResult keyResult;

		// If this is null, we have bigger problems.
		if(result == null) return false;
		
		if(key != null && (parameterMap == null || !parameterMap.containsKey(key))){
			success = false;

			keyResult = new KeyTransactionResult();
			keyResult.setKey(key);
			keyResult.setState(KeyTransactionResult.State.REQUIRED);
			keyResult.setSuccess(success);

			result.addKeyTransaction(keyResult);
		}
		
		return success;
	}
	

	public void setToModel(@NotNull KeyTransactionResult keyUpdateResult, @NotNull ModelType model, String key, String stringValue)
	{
		Method setMethod = null;
		Method getMethod = null;
		Object currentValue = null;
		Class<?>[] parameterTypes = null;
		Object value = null;

		setMethod = setterMethod(model, key);
		getMethod = getterMethod(model, key);
		
		// If there wasn't a corresponding set method, c'est la vie, just move on.
		if(setMethod == null){
			keyUpdateResult.setState(KeyTransactionResult.State.KEYNOTAPPLICABLE);
			keyUpdateResult.setSuccess(true);
		}

		parameterTypes = setMethod.getParameterTypes();
		if(parameterTypes == null || parameterTypes.length != 1){
			keyUpdateResult.setState(KeyTransactionResult.State.KEYINCOMPATIBLE);
			keyUpdateResult.setSuccess(false);
		}

		// Save the target type
		keyUpdateResult.setValueClass(parameterTypes[0]);

		
		// Store the current value.
		if(getMethod != null){ try { currentValue = getMethod.invoke(model); } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){}}
		keyUpdateResult.setOriginalValue(currentValue);
		
		// Sooooo, now we have the method we want to call, and the type of parameter it wants.
		// We need to convert the String to that type and then execute the method.
		value = DataConverter.convert(keyUpdateResult, keyUpdateResult.getStringValue(), keyUpdateResult.getValueClass());

		// Did we manage to achieve a success state?
		if(!keyUpdateResult.success()) return;

		// If so, set the results.  And then see if we are _still_ in a success state.
		try {
			setMethod.invoke(model, value);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			keyUpdateResult.setState(KeyTransactionResult.State.REJECTED);
			e.printStackTrace();
			keyUpdateResult.setSuccess(false);
			return;
		}

		// Get the new value.  We need to do this since we're allowing the internal methods to do whatever they need to do to the raw value.
		if(getMethod != null){ try { currentValue = getMethod.invoke(model); } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){}}
		keyUpdateResult.setNewValue(currentValue);
		keyUpdateResult.setSuccess(true);
	}
}
