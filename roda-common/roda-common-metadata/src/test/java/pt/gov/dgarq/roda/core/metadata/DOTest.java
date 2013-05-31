package pt.gov.dgarq.roda.core.metadata;

import java.io.File;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCHelper;

/**
 * @author Rui Castro
 */
public class DOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("ERROR - wrong number of arguments");
			System.err.println("Usage: " + DOTest.class.getSimpleName()
					+ " EADCFile");
			System.exit(1);
		}

		String eadcFile = args[0];

		try {

			EadCHelper eadCHelper = EadCHelper.newInstance(new File(eadcFile));
			DescriptionObject descriptionObject = eadCHelper
					.getDescriptionObject();

			DescriptionObjectValidator
					.validateDescriptionObject(descriptionObject);
			System.out.println("Description object is valid");

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with id '   '");
				System.out.println("**************************************");

				descriptionObject.setId("  ");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with countryCode ''");
				System.out.println("**************************************");

				descriptionObject.setCountryCode("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with repositoryCode ''");
				System.out.println("**************************************");

				descriptionObject.setRepositoryCode("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with title ''");
				System.out.println("**************************************");

				descriptionObject.setTitle("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with origination ''");
				System.out.println("**************************************");

				descriptionObject.setOrigination("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with scopecontent ''");
				System.out.println("**************************************");

				descriptionObject.setScopecontent("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with dateinitial ''");
				System.out.println("**************************************");

				descriptionObject.setDateInitial("abc");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with datefinal ''");
				System.out.println("**************************************");

				descriptionObject.setDateFinal("abc");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			descriptionObject = eadCHelper.getDescriptionObject();

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with dateinitial and datefinal ''");
				System.out.println("**************************************");

				descriptionObject.setDateInitial("");
				descriptionObject.setDateFinal("");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

			try {

				System.out.println("**************************************");
				System.out.println("Validate DO with dateinitial > datefinal");
				System.out.println("**************************************");

				descriptionObject.setLevel(DescriptionLevel.FONDS);
				descriptionObject.setDateInitial("2009");
				descriptionObject.setDateFinal("2008");
				DescriptionObjectValidator
						.validateDescriptionObject(descriptionObject);

			} catch (InvalidDescriptionObjectException e) {
				e.printStackTrace(System.out);
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
