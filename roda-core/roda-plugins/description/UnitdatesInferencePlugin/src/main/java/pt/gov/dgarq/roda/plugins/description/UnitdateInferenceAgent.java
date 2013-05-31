package pt.gov.dgarq.roda.plugins.description;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.plugins.description.DescriptionTraverserAgent;

/**
 * @author Rui Castro
 */
public class UnitdateInferenceAgent implements
		DescriptionTraverserAgent<UnitdateInferenceResult> {

	private Browser browserService = null;
	private Editor editorService = null;

	/**
	 * Constructs a new {@link UnitdateInferenceAgent}.
	 * 
	 * @param browserService
	 * @param editorService
	 */
	public UnitdateInferenceAgent(Browser browserService, Editor editorService) {
		this.browserService = browserService;
		this.editorService = editorService;
	}

	/**
	 * @throws Exception
	 * 
	 * @see DescriptionTraverserAgent#apply(SimpleDescriptionObject, List)
	 */
	public UnitdateInferenceResult apply(SimpleDescriptionObject sdo,
			List<UnitdateInferenceResult> childResults) throws Exception {

		UnitdateInferenceResult result = new UnitdateInferenceResult(null, null);

		if (childResults.size() == 0) {
			// Copy the dates from this SDO
			result.setInitialDate(sdo.getDateInitial());
			result.setFinalDate(sdo.getDateFinal());

		} else {

			for (UnitdateInferenceResult childResult : childResults) {

				// For initial dates

				if (StringUtils.isBlank(result.getInitialDate())) {
					// Result date is null, copy date from child
					result.setInitialDate(childResult.getInitialDate());

				} else {

					if (!StringUtils.isBlank(childResult.getInitialDate())) {

						if (childResult.getInitialDate().compareTo(
								result.getInitialDate()) < 0) {
							// childDate < currentDate
							result.setInitialDate(childResult.getInitialDate());
						}

					} else {
						// Child date is null, ignore it!
					}
				}

				// For final dates

				if (StringUtils.isBlank(result.getFinalDate())) {

					// Result date is null, copy date from child
					result.setFinalDate(childResult.getFinalDate());

				} else {

					if (!StringUtils.isBlank(childResult.getFinalDate())) {

						if (childResult.getFinalDate().compareTo(
								result.getFinalDate()) > 0) {
							// childDate > currentDate
							result.setFinalDate(childResult.getFinalDate());
						}

					} else {
						// Child date is null, ignore it!
					}
				}

			}

			if (!StringUtils.isBlank(result.getInitialDate())
					&& StringUtils.isBlank(result.getFinalDate())) {
				result.setFinalDate(result.getInitialDate());
			}

			if (StringUtils.isBlank(result.getInitialDate())
					&& !StringUtils.isBlank(result.getFinalDate())) {
				result.setInitialDate(result.getFinalDate());
			}

			boolean equalInitialDate;
			if (sdo.getDateInitial() == null) {
				equalInitialDate = result.getInitialDate() == null;
			} else {
				equalInitialDate = sdo.getDateInitial().equals(
						result.getInitialDate());
			}

			boolean equalFinalDate;
			if (sdo.getDateFinal() == null) {
				equalFinalDate = result.getFinalDate() == null;
			} else {
				equalFinalDate = sdo.getDateFinal().equals(
						result.getFinalDate());
			}

			if (!equalInitialDate || !equalFinalDate) {
				modifySDO(sdo, result);
			}

		}

		return result;
	}

	private void modifySDO(SimpleDescriptionObject sdo,
			UnitdateInferenceResult result) throws Exception {

		DescriptionObject descriptionObject = this.browserService
				.getDescriptionObject(sdo.getPid());

		descriptionObject.setDateInitial(result.getInitialDate());
		descriptionObject.setDateFinal(result.getFinalDate());

		this.editorService.modifyDescriptionObject(descriptionObject);
	}
}
