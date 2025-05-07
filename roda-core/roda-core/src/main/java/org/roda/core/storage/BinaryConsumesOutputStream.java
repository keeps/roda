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
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.v2.ConsumesSkipableOutputStream;

public class BinaryConsumesOutputStream implements ConsumesSkipableOutputStream {

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private final Binary binary;
  private final Path directAccessPath;
  private final String mediaType;

  public BinaryConsumesOutputStream(Binary binary, Path directAccessPath, String mediaType) {
    this.binary = binary;
    this.directAccessPath = directAccessPath;
    this.mediaType = mediaType;
  }

  public BinaryConsumesOutputStream(Binary binary, Path directAccessPath) {
    this(binary, directAccessPath, DEFAULT_MIME_TYPE);
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    try (InputStream in = binary.getContent().createInputStream()) {
      IOUtils.copyLarge(in, out);
    }
  }

  @Override
  public void consumeOutputStream(OutputStream out, int from, int len) throws IOException {
    try (InputStream in = binary.getContent().createInputStream()) {
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
    return null;
  }

  @Override
  public long getSize() {
    return binary.getSizeInBytes();
  }

  @Override
  public String getFileName() {
    return binary.getStoragePath().getName();
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }
}
