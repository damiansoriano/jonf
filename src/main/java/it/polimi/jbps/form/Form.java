package it.polimi.jbps.form;

import static com.google.common.collect.Lists.newLinkedList;
import static it.polimi.jbps.utils.ObjectUtils.isNotNull;
import it.polimi.jbps.actions.Action;

import java.util.List;

public class Form {
	
	protected final FormsConfiguration configuration;
	
	public Form(FormsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public List<Action> getActions(String actionURI) {
		if(isNotNull(configuration.getConfiguration()) && configuration.getConfiguration().containsKey(actionURI)){
			return configuration.getConfiguration().get(actionURI);
		}
		return newLinkedList();
	}
	
}
