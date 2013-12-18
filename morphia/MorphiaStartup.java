package com.subdigit.startup.morphia;

import org.mongodb.morphia.Datastore;

import com.subdigit.data.connector.DataConnectorConfiguration;
import com.subdigit.data.connector.morphia.MorphiaDataConnector;


public abstract class MorphiaStartup
{
	public MorphiaStartup()
	{
		initialize();
	}


	protected void initialize()
	{
		connectDatastore();
		setup();
	}


	public Datastore getDatastore(){ return MorphiaDataConnector.getInstance().getDatastore(); }
	protected void connectDatastore()
	{
System.err.println("+++ Attempting to connect to datastore: " + DataConnectorConfiguration.getInstance().getServer() + ":" + DataConnectorConfiguration.getInstance().getPort() + "/" + DataConnectorConfiguration.getInstance().getDatastore());

		// This isn't _really_ necessary but it simply gets it out of the way early.
		MorphiaDataConnector connector = MorphiaDataConnector.getInstance();

		if(connector.getDatastore() != null){
System.err.println("+++ Connected to Morphia datastore: " + connector.getDatastore().getDB());
		} else {
System.err.println("--- Could not connect to datastore: " + DataConnectorConfiguration.getInstance().getDatastore());
		}
	}


	public abstract void setup();
}
