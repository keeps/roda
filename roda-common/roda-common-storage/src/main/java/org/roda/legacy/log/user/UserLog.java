package org.roda.legacy.log.user;

import org.roda.index.filter.Filter;
import org.roda.legacy.log.LogEntry;
import org.roda.legacy.old.adapter.ContentAdapter;

public interface UserLog {
	LogEntry[] getLogEntries(ContentAdapter contentAdapter);

	int getLogEntriesCount(Filter filter);

	void addLogEntry(LogEntry logEntry);
}
