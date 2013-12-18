package com.subdigit.data.connector.morphia;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.subdigit.data.connector.DataConnectorConfiguration;


public class MorphiaDataConnector
{
	private Mongo _mongo;
	private Morphia _morphia;
	private Datastore _datastore;
	private DataConnectorConfiguration _dcc;

	// http://stackoverflow.com/a/71683
	public static MorphiaDataConnector getInstance(){ return _Holder.instance; }
	private static class _Holder { public static MorphiaDataConnector instance = new MorphiaDataConnector(); }


	private MorphiaDataConnector()
	{
		_dcc = DataConnectorConfiguration.getInstance();

		initialize(_dcc.getServer(), _dcc.getPort(), _dcc.getDatastore());
	}


	public MorphiaDataConnector(String server, int port, String datastoreName)
	{
		initialize(server, port, datastoreName);
	}
	

	public void initialize(String server, int port, String datastoreName)
	{
		Mongo mongo = null;
		
		try {
			mongo = new MongoClient(server, port);
		} catch (UnknownHostException e){
System.err.println("--- Could not connect to datastore " + datastoreName + " because of unknown host: " + server + ":" + port + ":" + e);
			return;
		}

		initialize(mongo, datastoreName);
	}


	public void initialize(Mongo mongo, String datastoreName)
	{
		Morphia morphia = new Morphia();

		// Since we are extending the BasicDAO, I dont think we need to map classes that are managed through the Accessors.
//		_morphia
//			.map(User.class)
//			.map(Acronym.class)
//			.map(Association.class)
//			.map(Company.class)
//			.map(Account.class);
//		morphia.mapPackage("com.subdigit.acronymity.data.models");

		Datastore datastore = morphia.createDatastore(mongo, datastoreName);
		datastore.ensureIndexes();
		datastore.ensureCaps();

		initialize(mongo, morphia, datastore);
	}


	public void initialize(Mongo mongo, Morphia morphia, Datastore datastore)
	{
		_mongo = mongo;
		_morphia = morphia;
		_datastore = datastore;
	}
	
	
	public Morphia getMorphia(){ return _morphia; }
	public Mongo getMongo(){ return _mongo; }
	public Datastore getDatastore(){ return _datastore; }
}


/*
public class DataConnectivityX
{
	public static Mongo mongo;
	public static Morphia morphia;
	public static Datastore datastore;


	public static void initialize(String server, int port, String datastoreName) throws UnknownHostException
	{
		Mongo _mongo = new MongoClient(server, port);
		initialize(_mongo, datastoreName);
	}


	public static void initialize(Mongo _mongo, String datastoreName)
	{
		Morphia _morphia = new Morphia();

		// Since we are extending the BasicDAO, I dont think we need to map classes that are managed through the Accessors.
//		_morphia
//			.map(User.class)
//			.map(Acronym.class)
//			.map(Association.class)
//			.map(Company.class)
//			.map(Account.class);
//		morphia.mapPackage("com.subdigit.acronymity.data.models");

		Datastore _datastore = _morphia.createDatastore(_mongo, datastoreName);
		_datastore.ensureIndexes();
		_datastore.ensureCaps();

		initialize(_mongo, _morphia, _datastore);
	}


	public static void initialize(Mongo _mongo, Morphia _morphia, Datastore _datastore)
	{
		mongo = _mongo;
		morphia = _morphia;
		datastore = _datastore;
	}
}
*/