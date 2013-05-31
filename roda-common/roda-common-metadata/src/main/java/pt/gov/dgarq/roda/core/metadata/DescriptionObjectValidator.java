package pt.gov.dgarq.roda.core.metadata;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

/**
 * @author Rui Castro
 */
public class DescriptionObjectValidator {

	/**
	 * Validates the given {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to validate.
	 * 
	 * @throws InvalidDescriptionObjectException
	 */
	public static void validateDescriptionObject(DescriptionObject dObject)
			throws InvalidDescriptionObjectException {

		// Validate level
		if (dObject.getLevel() == null) {
			throw new InvalidDescriptionObjectException(
					Messages
							.getString("DescriptionObjectValidator.DESCRIPTION_LEVEL_IS_EMPTY")); //$NON-NLS-1$
		}
		if (!Arrays.asList(DescriptionLevel.LEVELS).contains(
				dObject.getLevel().getLevel())) {
			throw new InvalidDescriptionObjectException(
					String
							.format(
									Messages
											.getString("DescriptionObjectValidator.DESCRIPTION_LEVEL_X_IS_NOT_VALID"), dObject.getLevel() //$NON-NLS-1$
											.getLevel()));
		}

		// Validate ID
		if (StringUtils.isBlank(dObject.getId())) {
			throw new InvalidDescriptionObjectException(Messages
					.getString("DescriptionObjectValidator.ID_IS_EMPTY")); //$NON-NLS-1$
		}

		// Validate COUNTRY_CODE
		if (StringUtils.isBlank(dObject.getCountryCode())) {
			throw new InvalidDescriptionObjectException(
					Messages
							.getString("DescriptionObjectValidator.COUNTRY_CODE_IS_EMPTY")); //$NON-NLS-1$
		}

		// Validate REPOSITORY_CODE
		if (StringUtils.isBlank(dObject.getRepositoryCode())) {
			throw new InvalidDescriptionObjectException(
					Messages
							.getString("DescriptionObjectValidator.REPOSITORY_CORE_IS_EMPTY")); //$NON-NLS-1$
		}

		// Validate title
		if (StringUtils.isBlank(dObject.getTitle())) {
			throw new InvalidDescriptionObjectException(Messages
					.getString("DescriptionObjectValidator.TITLE_IS_EMPTY")); //$NON-NLS-1$
		}

		// Validate title
		if (StringUtils.isBlank(dObject.getOrigination())) {
			throw new InvalidDescriptionObjectException(
					Messages
							.getString("DescriptionObjectValidator.ORIGINATION_IS_EMPTY")); //$NON-NLS-1$
		}

		// Validate scope & content
		if (StringUtils.isBlank(dObject.getScopecontent())) {
			throw new InvalidDescriptionObjectException(
					Messages
							.getString("DescriptionObjectValidator.SCOPE_CONTENT_IS_EMPTY")); //$NON-NLS-1$
		}

		Date dateInitial = null;
		Date dateFinal = null;

		if (!StringUtils.isBlank(dObject.getDateInitial())) {
			try {
				dateInitial = DateParser.parse(dObject.getDateInitial());
			} catch (InvalidDateException e) {
				throw new InvalidDescriptionObjectException(
						Messages
								.getString("DescriptionObjectValidator.INITIAL_DATE_IS_INVALID"),//$NON-NLS-1$
						e);
			}
		}

		if (!StringUtils.isBlank(dObject.getDateFinal())) {
			try {
				dateFinal = DateParser.parse(dObject.getDateFinal());
			} catch (InvalidDateException e) {
				throw new InvalidDescriptionObjectException(
						Messages
								.getString("DescriptionObjectValidator.FINAL_DATE_IS_INVALID"), //$NON-NLS-1$
						e);
			}
		}

		if (dateInitial != null && dateFinal != null) {

			if (dateInitial.compareTo(dateFinal) > 0) {
				throw new InvalidDescriptionObjectException(
						Messages
								.getString("DescriptionObjectValidator.INITIAL_DATE_IS_AFTER_FINAL_DATE")); //$NON-NLS-1$
			}

		}

		// Validate unit dates
		if (dObject.getLevel().equals(DescriptionLevel.FILE)
				|| dObject.getLevel().equals(DescriptionLevel.ITEM)) {

			if (dateInitial == null && dateFinal == null) {
				throw new InvalidDescriptionObjectException(
						Messages
								.getString("DescriptionObjectValidator.INITIAL_FINAL_DATE_MUST_NOT_BE_EMPTY")); //$NON-NLS-1$
			} else {
				// OK
			}
		}

	}

}
