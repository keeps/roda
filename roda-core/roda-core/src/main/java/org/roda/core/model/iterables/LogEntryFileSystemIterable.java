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
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LogEntryJsonParseException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.log.LogEntry;

public class LogEntryFileSystemIterable implements CloseableIterable<OptionalWithCause<LogEntry>> {

  private final class LogEntryIterator implements Iterator<OptionalWithCause<LogEntry>> {

    private final Iterator<Path> paths;
    private BufferedReader br = null;
    private OptionalWithCause<LogEntry> next = null;
    Path logFile = null;
    int line = 0;

    public LogEntryIterator(Iterator<Path> paths) {
      this.paths = paths;
    }

    private boolean forwardNextFile() {
      boolean foundIt = false;
      while (paths.hasNext()) {
        logFile = paths.next();
        line = 0;
        try {
          IOUtils.closeQuietly(br);
          // input stream is closed by the buffer
          br = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile)));
          if (forwardInFile()) {
            foundIt = true;
            break;
          }

        } catch (IOException e) {
          foundIt = true;
          next = OptionalWithCause
            .empty(new LogEntryJsonParseException(e).setFilename(logFile.toString()).setLine(line));
        }
      }

      if (!foundIt) {
        next = null;
        IOUtils.closeQuietly(br);
      }

      return foundIt;
    }

    private boolean forwardInFile() {
      boolean foundIt = false;
      try {
        String nextLine = br.readLine();
        line++;

        if (nextLine != null) {
          next = OptionalWithCause.of(JsonUtils.getObjectFromJson(nextLine, LogEntry.class));
          foundIt = true;
        }
      } catch (GenericException e) {
        next = OptionalWithCause.empty(new LogEntryJsonParseException(e).setFilename(logFile.toString()).setLine(line));
        foundIt = true;
      } catch (IOException e) {
        next = OptionalWithCause.empty(new LogEntryJsonParseException(e).setFilename(logFile.toString()).setLine(line));
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

  private final DirectoryStream<Path> directoryStream;

  public LogEntryFileSystemIterable(Path logPath) throws IOException {
    this.directoryStream = Files.newDirectoryStream(logPath);
  }

  public LogEntryFileSystemIterable(Path logPath, Filter<? super Path> filter) throws IOException {
    this.directoryStream = Files.newDirectoryStream(logPath, filter);
  }

  @Override
  public void close() throws IOException {
    directoryStream.close();
  }

  @Override
  public Iterator<OptionalWithCause<LogEntry>> iterator() {
    Iterator<Path> paths = directoryStream.iterator();
    return new LogEntryIterator(paths);
  }
}
