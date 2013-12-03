package com.subdigit.data;

import java.util.Date;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.PreLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.PreSave;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subdigit.data.json.helpers.BaseModelModule;


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public abstract class BaseModel<IdType>
{
	@Id
	public IdType id;
 
	@Version
	@JsonIgnore
	public Long version;

	@Transient
	public transient boolean isNew = true;

	@Transient
	public transient boolean isMarkedForDeletion;
	
	@Property("datecreated")
	@JsonProperty("datecreated")
	public Date dateCreated;	// Date created

	@Property("dateupdated")
	@JsonProperty("dateupdated")
	public Date dateUpdated;	// Date updated

	@Property("datetouched")
	@JsonProperty("datetouched")
	public Date dateTouched;	// Date last accessed

	@Property("datedeleted")
	@JsonProperty("datedeleted")
	public Date dateDeleted;	// Date deleted


	/*
	 * I wanted to use these, but I think they're just in the wrong place.
	 * These shouldn't be part of the model if the point of the model is to
	 * stay simply and not really include logic.  A real POJO.  So I'm not
	 * going to use them.  Well, that and they seem to be called way too often
	 * at times I cant figure out.
	 * 
	 * So for now, go look in the BaseManager class, as that is supposed to have
	 * all the business logic about an object, which includes something like this.
	 */
	@PrePersist void prePersist(){}
	@PostPersist void postPersist(){}
	@PreLoad void preLoad(){}
	@PostLoad void postLoad(){}
	@PreSave void preSave(){}


	public IdType getId(){ return id; }
	protected void setId(IdType value){ id = value; }

//	private String getVersion(){ return version; }
//	private void setVersion(String value){ version = value; }

	public boolean getIsNew(){ return isNew; }
	public void setIsNew(boolean isNew){ this.isNew = isNew; }
	public boolean isNew(){ return getIsNew(); }
	
	public boolean getIsMarkedForDeletion(){ return isMarkedForDeletion; }
	public void setIsMarkedForDeletion(boolean isMarkedForDeletion){ this.isMarkedForDeletion = isMarkedForDeletion; }
	public boolean isMarkedForDeletion(){ return getIsMarkedForDeletion(); }

	public Date getDateCreated(){ return dateCreated; }
	public void setDateCreated(Date dateCreated){ this.dateCreated = dateCreated; }

	public Date getDateUpdated(){ return dateUpdated; }
	public void setDateUpdated(Date dateUpdated){ this.dateUpdated = dateUpdated; }

	public Date getDateTouched(){ return dateTouched; }
	public void setDateTouched(Date dateTouched){ this.dateTouched = dateTouched; }

	public Date getDateDeleted(){ return dateDeleted; }
	public void setDateDeleted(Date dateDeleted){ this.dateDeleted = dateDeleted; }

	
	public abstract BaseManager<? extends BaseModel<?>, ?, ? extends BasicAccessor<? extends BaseModel<?>, ?>> getManager();
	

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;

		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());

		return result;
	}


	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;

		BaseModel<IdType> other = (BaseModel<IdType>) obj;
		if(id == null){
			if(other.id != null) return false;
		} else if(!id.equals(other.id)) return false;

		if(version == null){
			if(other.version != null) return false;
		} else if (!version.equals(other.version)) return false;

		return true;
	}


	@Override
	public String toString()
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.registerModule(new BaseModelModule());
		mapper.setSerializationInclusion(Include.NON_NULL);

		try {
			return mapper.writeValueAsString(this);
		} catch(JsonProcessingException e){}
		
		return super.toString();
	}
}