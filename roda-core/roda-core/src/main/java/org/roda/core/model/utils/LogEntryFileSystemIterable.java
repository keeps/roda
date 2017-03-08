package org.roda.core.model.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.log.LogEntry;

public class LogEntryFileSystemIterable implements CloseableIterable<OptionalWithCause<LogEntry>> {

  private final DirectoryStream<Path> directoryStream;

  public LogEntryFileSystemIterable(Path logPath) throws IOException {
    this.directoryStream = Files.newDirectoryStream(logPath);
  }

  @Override
  public void close() throws IOException {
    directoryStream.close();
  }

  @Override
  public Iterator<OptionalWithCause<LogEntry>> iterator() {
    Iterator<Path> paths = directoryStream.iterator();

    return new Iterator<OptionalWithCause<LogEntry>>() {
      LogEntry nextLogEntry = null;
      BufferedReader br = null;

      @Override
      public boolean hasNext() {
        if (nextLogEntry == null) {
          while (paths.hasNext()) {
            try {
              Path logFile = paths.next();
              br = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile)));
              String nextLine = null;
              if ((nextLine = br.readLine()) != null) {
                nextLogEntry = JsonUtils.getObjectFromJson(nextLine, LogEntry.class);
                break;
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
          String nextLine = null;

          if ((nextLine = br.readLine()) == null) {
            IOUtils.closeQuietly(br);
            while (paths.hasNext()) {
              try {
                Path logFile = paths.next();
                br = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile)));
                if ((nextLine = br.readLine()) != null) {
                  nextLogEntry = JsonUtils.getObjectFromJson(nextLine, LogEntry.class);
                  break;
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
    };
  }
}
