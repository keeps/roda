package pt.gov.dgarq.roda.core.metadata.premis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyHelper;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyMetadataException;

/**
 * Test class for {@link PolicyHelper}
 * 
 * @author Rui Castro
 */
public class PolicyHelperTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			PolicyHelper policyHelper = PolicyHelper.newInstance(new File(
					args[0]));

			RODAObjectPermissions permissions = policyHelper
					.getRODAObjectPermissions();

			System.out.println("getRODAObjectPermissions() => " + permissions);

			policyHelper.setRODAObjectPermissions(permissions);

			System.out.println("setRODAObjectPermissions() => "
					+ policyHelper.getPolicyDocument());

		} catch (PolicyMetadataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
