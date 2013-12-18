package com.subdigit.data.json.helpers;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.subdigit.data.BaseModel;

public class BaseModelReferenceSerializer extends JsonSerializer<BaseModel<ObjectId>>
{
	@Override
	public void serialize(BaseModel<ObjectId> value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException
	{
		if(value != null){
			generator.writeString(value.getId().toString());
		}
	}
}
