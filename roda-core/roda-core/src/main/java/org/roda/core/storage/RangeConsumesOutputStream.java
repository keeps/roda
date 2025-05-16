/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.v2.ConsumesSkipableOutputStream;

public class RangeConsumesOutputStream implements ConsumesSkipableOutputStream {

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private final Path directAccessPath;
  private final String mediaType;

  public RangeConsumesOutputStream(Path directAccessPath, String mediaType) {
    this.directAccessPath = directAccessPath;
    this.mediaType = mediaType;
  }

  public RangeConsumesOutputStream(Path directAccessPath) {
    this(directAccessPath, DEFAULT_MIME_TYPE);
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    try (InputStream in = Files.newInputStream(directAccessPath)) {
      IOUtils.copyLarge(in, out);
    }
  }

  @Override
  public void consumeOutputStream(OutputStream out, int from, int len) throws IOException {
    try (InputStream in = Files.newInputStream(directAccessPath)) {
      IOUtils.copyLarge(in, out, from, len);
    }
  }

  @Override
  public void consumeOutputStream(OutputStream out, long from, long end) {
    try {
      File file = directAccessPath.toFile();
      byte[] buffer = new byte[1024];
      try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
        long pos = from;
        randomAccessFile.seek(pos);
        while (pos < end) {
          randomAccessFile.read(buffer);
          out.write(buffer);
          pos += buffer.length;
        }
        out.flush();
      }
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public Date getLastModified() {
    try {
      return new Date(Files.getLastModifiedTime(directAccessPath).toMillis());
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public long getSize() {
    try {
      return Files.size(directAccessPath);
    } catch (IOException e) {
      return -1;
    }
  }

  @Override
  public String getFileName() {
    return directAccessPath.getFileName().toString();
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }
}
