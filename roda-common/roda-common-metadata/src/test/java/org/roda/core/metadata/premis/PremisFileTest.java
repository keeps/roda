/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata.premis;

import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;

/**
 * Test class for {@link PremisRepresentationObjectHelper}.
 * 
 * @author Rui Castro
 */
public class PremisFileTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {

      PremisFileObjectHelper rObjectHelper = new PremisFileObjectHelper();

      System.out.println("Getting File from Object");
      System.out.println(rObjectHelper.getRepresentationFilePreservationObject());

    } catch (PremisMetadataException e) {
      e.printStackTrace();
    }
  }

}
