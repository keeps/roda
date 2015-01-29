package pt.gov.dgarq.roda.core.services;

import org.apache.xmlbeans.XmlOptions;

import pt.gov.dgarq.roda.x2014.eadcSchema.C;
import pt.gov.dgarq.roda.x2014.eadcSchema.Did;
import pt.gov.dgarq.roda.x2014.eadcSchema.Dimensions;
import pt.gov.dgarq.roda.x2014.eadcSchema.EadCDocument;
import pt.gov.dgarq.roda.x2014.eadcSchema.Physdesc;

/**
 * @author Rui Castro
 */
public class EadCTest {

	public static void main(String[] args) {
		try {
			EadCDocument eadpartDocument = EadCDocument.Factory.newInstance();

			C c = eadpartDocument.addNewEadC();

			Did newDid = c.addNewDid();

			Physdesc newPhysdesc = newDid.addNewPhysdesc();

			Dimensions newDimensions = newPhysdesc.addNewDimensions();
			newDimensions.setStringValue("5");
			newDimensions.setUnit("tons of flax");

			eadpartDocument.save(System.out, new XmlOptions()
					.setSavePrettyPrint());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
