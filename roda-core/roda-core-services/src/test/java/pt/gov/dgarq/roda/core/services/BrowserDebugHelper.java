package pt.gov.dgarq.roda.core.services;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;

/**
 * @author Rui Castro
 * 
 */
public class BrowserDebugHelper {

	static final private Logger logger = Logger
			.getLogger(BrowserDebugHelper.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			Browser browserService = new Browser();

			System.out.println("\n**************************************");
			System.out.println("getDescriptionObject() for roda:4747");
			System.out.println("**************************************");

			DescriptionObject descriptionObject = browserService
					.getDescriptionObject("roda:4747");
			System.out.println(descriptionObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
