package org.roda.core.model.utils;

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
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.log.LogEntry;

public class LogEntryFileSystemIterable implements CloseableIterable<OptionalWithCause<LogEntry>> {

  private final class LogEntryIterator implements Iterator<OptionalWithCause<LogEntry>> {

    private final Iterator<Path> paths;
    private BufferedReader br = null;
    private OptionalWithCause<LogEntry> next = null;

    public LogEntryIterator(Iterator<Path> paths) {
      this.paths = paths;
    }

    private boolean forwardNextFile() {
      boolean foundIt = false;
      while (paths.hasNext()) {
        Path logFile = paths.next();
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
          next = OptionalWithCause.empty(new GenericException(e));
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

        if (nextLine != null) {
          next = OptionalWithCause.of(JsonUtils.getObjectFromJson(nextLine, LogEntry.class));
          foundIt = true;
        }
      } catch (GenericException e) {
        next = OptionalWithCause.empty(e);
        foundIt = true;
      } catch (IOException e) {
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
