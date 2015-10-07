package org.roda.action.antivirus.utils;

import org.roda.action.orchestrate.PluginException;

/**
 * Exception thrown if something went wrong in a particular implementation of
 * {@link AntiVirus} (as defined by the interface)
 * */
public class AntiVirusException extends PluginException {
	private static final long serialVersionUID = 3567732420458265208L;

	public AntiVirusException(String message, Throwable cause) {
		super(message, cause);
	}
}
