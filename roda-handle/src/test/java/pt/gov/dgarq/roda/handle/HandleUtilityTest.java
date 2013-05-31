package pt.gov.dgarq.roda.handle;

/**
 * Test class for {@link HandleUtility}.
 * 
 * @author Rui Castro
 * 
 */
public class HandleUtilityTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Handle URL for " + args[0] + " is "
				+ HandleUtility.getHandleURLForPID(args[0]));
	}

}
