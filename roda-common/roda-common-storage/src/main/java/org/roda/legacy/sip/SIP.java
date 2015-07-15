package org.roda.legacy.sip;

import org.roda.legacy.sip.data.SIPState;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

public interface SIP {

	void setIndexClient(Object indexClient) throws UnsupportedOperationException;

	SIPState acceptSIP(String sipID, String reason);

	SIPState rejectSIP(String sipID, String reason);

	int getSIPsCount(Filter filter);

	SIPState[] getSIPs(ContentAdapter contentAdapter);

	String[] getPossibleStates();
}
