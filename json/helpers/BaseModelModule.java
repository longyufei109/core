package com.subdigit.data.json.helpers;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.subdigit.result.BasicResult;


public final class BaseModelModule extends SimpleModule
{
	private static final long serialVersionUID = 1L;

	public BaseModelModule()
	{
//		addDeserializer(ObjectId.class, new ObjectIdDeserializer());
		addSerializer(ObjectId.class, new ObjectIdSerializer());
		addSerializer(Enum.class, new EnumSerializer());

		// https://github.com/FasterXML/jackson-databind/issues/274
		setMixInAnnotation(BasicResult.class, BasicResultMixIn.class);
//		setMixInAnnotation(ServiceResult.class, BasicResultMixIn.class);
	}


	public class ObjectIdSerializer extends JsonSerializer<ObjectId>
	{
		@Override
		public void serialize(ObjectId value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException
		{
			if(value != null){
				generator.writeString(value.toString());
			}
		}
	}


	@SuppressWarnings("rawtypes")
	public class EnumSerializer extends JsonSerializer<Enum>
	{
		@Override
		public void serialize(Enum value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException
		{
			if(value != null){
				generator.writeString(value.toString());
			}
		}
	}


	public class BasicResultMixIn
	{
		@JsonProperty("success")
		String success;

		@JsonProperty("returndata")
		String returnData;

		@JsonProperty("status")
		String statusArchive;

		@JsonProperty("parameters")
		String dataStore;
	}
}
