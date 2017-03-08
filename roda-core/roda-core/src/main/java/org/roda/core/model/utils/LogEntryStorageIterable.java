package org.roda.core.model.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;

public class LogEntryStorageIterable implements CloseableIterable<OptionalWithCause<LogEntry>> {

  private final class LogEntryIterator implements Iterator<OptionalWithCause<LogEntry>> {
    LogEntry nextLogEntry = null;
    BufferedReader br = null;

    final Iterator<Resource> resources;

    public LogEntryIterator(Iterator<Resource> resources) {
      this.resources = resources;
    }

    @Override
    public boolean hasNext() {
      if (nextLogEntry == null) {
        while (resources.hasNext()) {
          try {
            Resource resource = resources.next();
            if (resource instanceof Binary) {
              Binary b = (Binary) resource;
              br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
              String nextLine;
              if ((nextLine = br.readLine()) != null) {
                nextLogEntry = JsonUtils.getObjectFromJson(nextLine, LogEntry.class);
                break;
              }
            }
          } catch (GenericException | IOException e) {
            // do nothing
          }
        }
      }

      return nextLogEntry != null;
    }

    @Override
    public OptionalWithCause<LogEntry> next() {
      OptionalWithCause<LogEntry> entry = OptionalWithCause.of(nextLogEntry);

      try {
        String nextLine;

        if ((nextLine = br.readLine()) == null) {
          IOUtils.closeQuietly(br);
          while (resources.hasNext()) {
            try {
              Resource resource = resources.next();
              if (resource instanceof Binary) {
                Binary b = (Binary) resource;
                br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
                if ((nextLine = br.readLine()) != null) {
                  nextLogEntry = JsonUtils.getObjectFromJson(nextLine, LogEntry.class);
                  break;
                }
              }
            } catch (GenericException | IOException e) {
              // do nothing
            }
          }
        } else {
          nextLogEntry = JsonUtils.getObjectFromJson(nextLine, LogEntry.class);
        }
      } catch (GenericException | IOException e) {
        nextLogEntry = null;
      }

      return entry;
    }
  }

  private final CloseableIterable<Resource> actionLogs;

  public LogEntryStorageIterable(CloseableIterable<Resource> actionLogs) {
    this.actionLogs = actionLogs;
  }

  @Override
  public void close() throws IOException {
    actionLogs.close();
  }

  @Override
  public Iterator<OptionalWithCause<LogEntry>> iterator() {
    return new LogEntryIterator(actionLogs.iterator());
  }
}
