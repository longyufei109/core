package com.subdigit.request.play;

import play.cache.Cache;
import play.mvc.Http.Session;

import com.subdigit.acronymity.data.managers.UserManager;
import com.subdigit.acronymity.data.models.User;
import com.subdigit.broker.play.PlayRequestResponseBroker;
import com.subdigit.request.WebRequest;


public class PlayRequest extends WebRequest<User>
{
	public PlayRequest(PlayRequestResponseBroker broker){ super(broker); }
	public PlayRequest(PlayRequestResponseBroker broker, WebRequest.Action value){ super(broker, value); }

	public PlayRequestResponseBroker getBroker(){ return (PlayRequestResponseBroker) super.getBroker(); }	
	
	@Override
	public void setAuthenticatedUser(User user)
	{
		Session session = getBroker().getSession();

		if(session != null && user != null && user.getId() != null){
			session.put("userid", user.getId().toString());
			Cache.set(user.getId().toString(), user);
		}
	}


	@Override
	public User getAuthenticatedUser()
	{
		User user = null;
		String id = null;
		UserManager um = new UserManager();
		Session session = getBroker().getSession();

		if(session != null) id = session.get("userid");

		if(id != null){
			user = (User) Cache.get(id);
			if(user == null) user = um.getById(id);
		}

		return user;
	}
}