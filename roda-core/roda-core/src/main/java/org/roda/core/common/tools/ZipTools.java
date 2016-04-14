/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Tools to handle Zips
 * 
 * @author Luis Faria
 * 
 */
public class ZipTools {

  /**
   * Zip a list of files into an output stream
   * 
   * @param files
   * @param out
   * @throws IOException
   */
  public static void zip(List<ZipEntryInfo> files, OutputStream out) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(out);

    for (ZipEntryInfo file : files) {
      ZipEntry entry = new ZipEntry(file.getName());
      zos.putNextEntry(entry);
      InputStream inputStream = file.getPayload().createInputStream();
      sendToZip(inputStream, zos);
      IOUtils.closeQuietly(inputStream);
      zos.closeEntry();
    }

    IOUtils.closeQuietly(zos);
    IOUtils.closeQuietly(out);
  }

  private static String createFileName(String original, int append) {
    String ret;
    int dotIndex = original.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < original.length() - 1) {
      String name = original.substring(0, dotIndex);
      String extention = original.substring(dotIndex);
      ret = name + "_" + append + extention;
    } else {
      ret = original + "_" + append;
    }
    return ret;
  }

  private static void sendToZip(InputStream in, ZipOutputStream zos) throws IOException {
    byte[] buffer = new byte[4096];
    int retval;

    do {
      retval = in.read(buffer, 0, 4096);
      if (retval != -1) {
        zos.write(buffer, 0, retval);
      }
    } while (retval != -1);

    in.close();

  }
}
