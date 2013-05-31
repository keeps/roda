package pt.gov.dgarq.roda.sipcreator.description;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * @author Rui Castro
 */
public class LocalizedDescriptionLevel extends DescriptionLevel {
	private static final long serialVersionUID = -1534251465941338469L;

	/**
	 * Constructs a new {@link LocalizedDescriptionLevel}.
	 * 
	 * @param level
	 */
	public LocalizedDescriptionLevel(String level) {
		super(level);
	}

	/**
	 * Constructs a new {@link LocalizedDescriptionLevel}.
	 * 
	 * @param dLevel
	 */
	public LocalizedDescriptionLevel(DescriptionLevel dLevel) {
		super(dLevel);
	}

	/**
	 * @see DescriptionLevel#toString()
	 */
	@Override
	public String toString() {
		return Messages.getString("DescriptionLevel." + super.getLevel()); //$NON-NLS-1$
	}

}
