package pt.gov.dgarq.roda.common.format;

import java.io.File;
import java.io.FileNotFoundException;

import pt.gov.dgarq.roda.common.JhoveUtility;
import pt.gov.dgarq.roda.common.JhoveUtilityException;

/**
 * @author Rui Castro
 * 
 */
public class JhoveUtilityTest {

	public static void main(String... args) {

		if (args.length != 1) {
			System.err.println("Usage: java " + JhoveUtility.class.getName()
					+ " file");
			System.exit(1);
		}

		try {

			String jhoveOutput = JhoveUtility.inspect(new File(args[0]));

			System.out.println("JHOVE output:\n" + jhoveOutput);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (JhoveUtilityException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
