/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.roda.core.storage.SeekableContentPayload;

/**
 * Class that implements {@code ContentPayload} for File System
 *
 * @author Luis Faria <lfaria@keep.pt>
 */
public class FSPathContentPayload implements SeekableContentPayload {

  private final Path path;

  public FSPathContentPayload(Path path) {
    this.path = path;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return Files.newInputStream(path);
  }

  @Override
  public void writeToPath(Path outPath) throws IOException {
    Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    return path.toUri();
  }

  @Override
  public void writeTo(OutputStream out, long offset, long length) throws IOException {
    // 1. Use NIO InputStream (Efficient: uses native pread/lseek)
    try (InputStream is = Files.newInputStream(path)) {
      
      // 2. Seek to the start position (Instant operation on files)
      long skipped = is.skip(offset);
      if (skipped < offset) {
        // File is smaller than the offset requested
        return; 
      }

      // 3. Transfer only the requested amount
      byte[] buffer = new byte[8192]; // Standard 8KB buffer
      long remaining = length;
      int bytesRead;

      // Loop while we still need data AND we haven't hit EOF
      while (remaining > 0) {
        // Determine how much to read: either the full buffer or the remaining bytes
        int bytesToRead = (int) Math.min(buffer.length, remaining);
        
        bytesRead = is.read(buffer, 0, bytesToRead);
        
        if (bytesRead == -1) {
          break; // End of file reached prematurely
        }

        // Critical: Only write the bytes we actually read
        out.write(buffer, 0, bytesRead);
        
        remaining -= bytesRead;
      }
      
    }
  }

}
