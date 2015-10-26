/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Castro
 */
public class TooManyOpenFilesTest {

  /**
   * @param args
   */
  public static void main(String[] args) {

    File file = null;
    try {
      file = File.createTempFile("tmof", "xxx"); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<FileInputStream> fis = new ArrayList<FileInputStream>();

    try {

      for (int i = 0; true; i++) {
        fis.add(new FileInputStream(file));
        System.out.println("Open files " + i);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
