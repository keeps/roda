/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.common.tools;

import java.io.File;

/**
 * 
 * @author Luis Faria
 * 
 */
public class FileTools {

  /**
   * Deletes all files and subdirectories under dir. If a deletion fails, the
   * method stops attempting to delete and returns false.
   * 
   * @param dir
   *          the directory to delete
   * @return true if all deletions were successful.
   */
  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }

}
