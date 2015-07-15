package org.roda.legacy.log.user;

import org.roda.legacy.log.LogEntry;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

public interface UserLog {
	LogEntry[] getLogEntries(ContentAdapter contentAdapter);

	int getLogEntriesCount(Filter filter);

	void addLogEntry(LogEntry logEntry);
}
