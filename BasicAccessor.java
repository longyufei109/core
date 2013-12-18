package com.subdigit.data;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

import com.subdigit.data.connector.morphia.MorphiaDataConnector;


public class BasicAccessor<ModelType, IdType> extends BasicDAO<ModelType, IdType>
{
//	private BaseAccessor(Mongo mongo, Morphia morphia, String datastore){ super(mongo, morphia, datastore); }
	private BasicAccessor(Datastore datastore){ super(datastore); }
	public BasicAccessor(){ this(MorphiaDataConnector.getInstance().getDatastore()); }

	public List<ModelType> get()
	{
		return find().asList();
	}
}
