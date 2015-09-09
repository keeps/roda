package pt.gov.dgarq.roda.core.metadata.premis;

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
