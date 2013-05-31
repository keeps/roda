package pt.gov.dgarq.roda.plugins.description;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.plugins.description.DescriptionTraverserAgent;

/**
 * @author Miguel Ferreira
 * @author Rui Castro
 */
public class AccessRestrictNotifierAgent implements
		DescriptionTraverserAgent<AccessRestrictNotifierResult> {
	private static Logger logger = Logger
			.getLogger(AccessRestrictNotifierAgent.class);

	private Browser browserService = null;

	/**
	 * Constructs a new {@link AccessRestrictNotifierAgent}.
	 * 
	 * @param browserService
	 */
	public AccessRestrictNotifierAgent(Browser browserService) {
		this.browserService = browserService;
	}

	/**
	 * @throws Exception
	 * 
	 * @see DescriptionTraverserAgent#apply(SimpleDescriptionObject, List)
	 */
	public AccessRestrictNotifierResult apply(SimpleDescriptionObject sdo,
			List<AccessRestrictNotifierResult> childResults) throws Exception {

		AccessRestrictNotifierResult result = new AccessRestrictNotifierResult();

		if (childResults != null) {
			// Add the results from the children
			for (AccessRestrictNotifierResult childResult : childResults) {
				if (childResult.getDescriptionObjects().size() > 0) {
					result.addDescriptionObjects(childResult
							.getDescriptionObjects());
				}
			}
		}

		if (sdo.getSubElementsCount() == 0
				&& (sdo.getLevel().equals(DescriptionLevel.FILE) || sdo
						.getLevel().equals(DescriptionLevel.ITEM))) {

			DescriptionObject descriptionObject = browserService
					.getDescriptionObject(sdo.getPid());

			String accessrestrict = descriptionObject.getAccessrestrict();

			if (!StringUtils.isBlank(accessrestrict)) {

				Date currentDate = new Date();

				Date representationDate = DateParser.parse(descriptionObject
						.getDateFinal());

				Calendar representationCalendar = Calendar.getInstance();
				representationCalendar.setTime(representationDate);

				if (accessrestrict.toLowerCase()
						.startsWith("segredo de estado")) {

					// segredo de estado => 25 years
					representationCalendar.add(Calendar.YEAR, 25);

				} else if (accessrestrict.equalsIgnoreCase("Dados pessoais")) {

					// Dados pessoais => 75 years
					representationCalendar.add(Calendar.YEAR, 75);

				} else if (accessrestrict
						.equalsIgnoreCase("Dados sensíveis de pessoas colectivas")) {

					// Dados sensíveis de pessoas colectivas => 50 years
					representationCalendar.add(Calendar.YEAR, 50);

				} else {
					logger.warn("DO " + sdo.getPid() + " accessrestrict '"
							+ accessrestrict + "' is not a recognized value.");
					representationCalendar = null;
				}

				if (representationCalendar != null
						&& currentDate.compareTo(representationCalendar
								.getTime()) > 0) {
					result.addDescriptionObject(descriptionObject);
				}

			} else {
				// DO doesn't have accessrestrict value
			}

		}

		return result;
	}

}
