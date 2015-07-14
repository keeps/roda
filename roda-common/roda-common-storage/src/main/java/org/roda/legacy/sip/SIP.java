package org.roda.legacy.sip;

import org.roda.index.filter.Filter;
import org.roda.legacy.old.adapter.ContentAdapter;
import org.roda.legacy.sip.data.SIPState;

public interface SIP {

	void setIndexClient(Object indexClient)
			throws UnsupportedOperationException;

	SIPState acceptSIP(String sipID, String reason);

	SIPState rejectSIP(String sipID, String reason);

	int getSIPsCount(Filter filter);

	SIPState[] getSIPs(ContentAdapter contentAdapter);

	String[] getPossibleStates();
}
