/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata.premis;

import org.roda.core.metadata.premis.PremisMetadataException;
import org.roda.core.metadata.premis.PremisRepresentationObjectHelper;

/**
 * Test class for {@link PremisRepresentationObjectHelper}.
 * 
 * @author Rui Castro
 */
public class PremisRepresentationTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {

      PremisRepresentationObjectHelper rObjectHelper = new PremisRepresentationObjectHelper();

      System.out.println("Getting Representation from Object");
      System.out.println(rObjectHelper.getRepresentationPreservationObject());

    } catch (PremisMetadataException e) {
      e.printStackTrace();
    }
  }

}
