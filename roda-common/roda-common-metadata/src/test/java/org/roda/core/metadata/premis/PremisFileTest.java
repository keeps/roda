package org.roda.core.metadata.premis;

import org.roda.core.metadata.premis.PremisFileObjectHelper;
import org.roda.core.metadata.premis.PremisMetadataException;
import org.roda.core.metadata.premis.PremisRepresentationObjectHelper;

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
