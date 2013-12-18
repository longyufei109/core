package com.subdigit.request.play;

import com.subdigit.broker.play.PlayRequestResponseBroker;
import com.subdigit.request.WebRequest;


public abstract class PlayRequest<UserType> extends WebRequest<UserType>
{
	public PlayRequest(PlayRequestResponseBroker broker){ super(broker); }
	public PlayRequest(PlayRequestResponseBroker broker, WebRequest.Action value){ super(broker, value); }

	@Override public PlayRequestResponseBroker getBroker(){ return (PlayRequestResponseBroker) super.getBroker(); }	
}