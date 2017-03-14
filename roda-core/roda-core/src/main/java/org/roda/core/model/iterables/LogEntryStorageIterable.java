/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.iterables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEntryStorageIterable implements CloseableIterable<OptionalWithCause<LogEntry>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryStorageIterable.class);

  private final class LogEntryIterator implements Iterator<OptionalWithCause<LogEntry>> {

    private final Iterator<Resource> resources;
    private OptionalWithCause<LogEntry> next = null;
    private BufferedReader br = null;

    public LogEntryIterator(Iterator<Resource> resources) {
      this.resources = resources;
    }

    private boolean forwardNextFile() {
      boolean foundIt = false;
      while (resources.hasNext()) {
        Resource resource = resources.next();
        if (resource instanceof Binary) {
          Binary b = (Binary) resource;
          LOGGER.debug("Processing log file: {}", b.getStoragePath());

          try {
            IOUtils.closeQuietly(br);
            // input stream is closed by the buffer
            br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
            if (forwardInFile()) {
              foundIt = true;
              break;
            }

          } catch (IOException e) {
            LOGGER.debug("Error loading log entry", e);
            foundIt = true;
            next = OptionalWithCause.empty(new GenericException(e));
          }
        }
      }

      if (!foundIt) {
        next = null;
      }

      return foundIt;

    }

    private boolean forwardInFile() {
      boolean foundIt = false;
      try {
        String nextLine = br.readLine();

        if (nextLine != null) {
          next = OptionalWithCause.of(JsonUtils.getObjectFromJson(nextLine, LogEntry.class));
          foundIt = true;
        }
      } catch (GenericException e) {
        LOGGER.debug("Error loading log entry", e);
        next = OptionalWithCause.empty(e);
        foundIt = true;
      } catch (IOException e) {
        LOGGER.debug("Error loading log entry", e);
        next = OptionalWithCause.empty(new GenericException(e));
        foundIt = true;
      }

      return foundIt;
    }

    @Override
    public boolean hasNext() {
      if (next == null) {
        forwardNextFile();
      }

      return next != null;
    }

    @Override
    public OptionalWithCause<LogEntry> next() {
      OptionalWithCause<LogEntry> ret;
      if (next != null) {
        ret = next;
        if (!forwardInFile()) {
          forwardNextFile();
        }
      } else {
        throw new NoSuchElementException();
      }

      return ret;
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
