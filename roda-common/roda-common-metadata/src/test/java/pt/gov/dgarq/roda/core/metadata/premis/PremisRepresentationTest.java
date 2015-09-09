package pt.gov.dgarq.roda.core.metadata.premis;

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
