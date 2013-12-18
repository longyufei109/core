package com.subdigit.data.utilities;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;

import com.subdigit.data.BasicAccessor;
import com.subdigit.data.BaseManager;
import com.subdigit.data.BaseModel;
import com.subdigit.data.result.ModelTransactionResult.KeyTransactionResult;
import com.subdigit.utilities.Converter;


public class DataConverter
{
//	public static final String DEFAULT_MANAGERPACKAGE	= "com.subdigit.acronymity.data.managers";
//	public static final String DEFAULT_MANAGERSUFFIX	= "Manager";

	public static Object convert(@NotNull KeyTransactionResult result, String value, Class<?> type)
	{
		// Cant do much...
		if(result == null) return null;

		// No clue what to convert to.
		if(type == null) return null;

		// Nothing really to convert.
		if(value == null){
			result.setNewValue(type.cast(null));
			result.setSuccess(true);
			result.setState(KeyTransactionResult.State.NULLVALUE);
			return type.cast(null);
		}

		// If we want a String, just return it as is.
		if(matchesClass(type, String.class)){
			result.setNewValue(value);
			result.setSuccess(true);
			result.setState(KeyTransactionResult.State.ACCEPTED);
			return value;

		} else if(matchesClass(type, Date.class)){
			Date date = null;

			try {
				date = new Date(Converter.tolong(value));
			} catch(NumberFormatException e){
				result.setState(KeyTransactionResult.State.CONVERSIONFAILED);
				return null;
			}

			result.setNewValue(date);
			result.setSuccess(true);
			result.setState(KeyTransactionResult.State.ACCEPTED);
			return date;

		} else if(matchesClass(type, ObjectId.class)){
			ObjectId objectId = null;

			try {
				objectId = new ObjectId(value);
			} catch(IllegalArgumentException e){
				result.setState(KeyTransactionResult.State.CONVERSIONFAILED);
				return null;
			}

			result.setNewValue(objectId);
			result.setSuccess(true);
			result.setState(KeyTransactionResult.State.ACCEPTED);
			return objectId;

		} else if(isSuperClassOf(type, BaseModel.class)){
			BaseModel<?> model = null;

			@SuppressWarnings("unchecked")
			BaseManager<?, ? extends BaseModel<?>, ?, ? extends BasicAccessor<? extends BaseModel<?>, ?>> manager = getManager((Class<BaseModel<?>>) type);

			if(manager != null){
				model = manager.getById(value);
				if(model == null){
					result.setState(KeyTransactionResult.State.CONVERSIONFAILED);
					return null;
				}
			} else {
				result.setState(KeyTransactionResult.State.CONVERTERNOTFOUND);
				return null;
			}

			result.setNewValue(model);
			result.setSuccess(true);
			result.setState(KeyTransactionResult.State.ACCEPTED);
			return model;
		}

		// Oh well...
		result.setState(KeyTransactionResult.State.CONVERTERNOTFOUND);
		return value;
	}


	public static boolean matchesClass(@NotNull Class<?> type, @NotNull Class<?> clazz){ return type.getCanonicalName().equals(clazz.getCanonicalName()); }
	public static boolean isSuperClassOf(@NotNull Class<?> type, @NotNull Class<?> clazz){ return clazz.isAssignableFrom(type); }


//	public static String getManagerPackage(){ return DEFAULT_MANAGERPACKAGE; }
//	public static String getManagerSuffix(){ return DEFAULT_MANAGERSUFFIX; }
//	public static String getManagerFullClassName(Class<?> model)
//	{
//		return getManagerPackage() + "." + model.getSimpleName() + getManagerSuffix();
//	}

//	@SuppressWarnings("unchecked")
	public static BaseManager<?, ? extends BaseModel<?>, ?, ? extends BasicAccessor<? extends BaseModel<?>, ?>> getManager(Class<BaseModel<?>> type)
	{
		try {
			BaseModel<?> model = (BaseModel<?>) Class.forName(type.getCanonicalName()).newInstance();
			if(model != null) return model.getManager();
//			return (BaseManager<? extends BaseMorphiaModel<?>, ?, ? extends BaseAccessor<? extends BaseMorphiaModel<?>, ?>>) Class.forName(getManagerFullClassName(type)).newInstance();
		} catch(InstantiationException | IllegalAccessException | ClassNotFoundException e){}

		return null;
	}
}
