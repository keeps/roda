package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;

/**
 * This is the description level of a {@link SimpleDescriptionObject}.
 * 
 * Valid values for level are:
 * <ul>
 * <li>{@link DescriptionLevel#FONDS},</li>
 * <li>{@link DescriptionLevel#SUBFONDS},</li>
 * <li>{@link DescriptionLevel#CLASS},</li>
 * <li>{@link DescriptionLevel#SUBCLASS},</li>
 * <li>{@link DescriptionLevel#SERIES},</li>
 * <li>{@link DescriptionLevel#SUBSERIES},</li>
 * <li>{@link DescriptionLevel#FILE},</li>
 * <li>{@link DescriptionLevel#ITEM}.</li>
 * </ul>
 * 
 * @author Rui Castro
 */
public class DescriptionLevel implements EadCValue,
		Comparable<DescriptionLevel>, Serializable {
	private static final long serialVersionUID = 9038357012292858570L;

	/**
	 * All the possible values of {@link DescriptionLevel}'s level, in order,
	 * from fonds to item.
	 */
	public static final String[] LEVELS = new String[] { "fonds", "subfonds",
			"class", "subclass", "series", "subseries", "file", "item" };

	/**
	 * Description Object Level Fonds (fonds)
	 */
	public final static DescriptionLevel FONDS = new DescriptionLevel("fonds");

	/**
	 * Description Object Level Subfonds (subfonds)
	 */
	public final static DescriptionLevel SUBFONDS = new DescriptionLevel(
			"subfonds");

	/**
	 * Description Object Level Class (class)
	 */
	public final static DescriptionLevel CLASS = new DescriptionLevel("class");

	/**
	 * Description Object Level Subclass (subclass)
	 */
	public final static DescriptionLevel SUBCLASS = new DescriptionLevel(
			"subclass");

	/**
	 * Description Object Level Series (series)
	 */
	public final static DescriptionLevel SERIES = new DescriptionLevel("series");

	/**
	 * Description Object Level Subseries (subseries)
	 */
	public final static DescriptionLevel SUBSERIES = new DescriptionLevel(
			"subseries");

	/**
	 * Description Object Level File (file)
	 */
	public final static DescriptionLevel FILE = new DescriptionLevel("file");

	/**
	 * Description Object Level Item (item)
	 */
	public final static DescriptionLevel ITEM = new DescriptionLevel("item");

	/**
	 * All the possible values of {@link DescriptionLevel}, in order, from fonds
	 * to item.
	 */
	public static final DescriptionLevel[] DESCRIPTION_LEVELS = new DescriptionLevel[] {
			FONDS, SUBFONDS, CLASS, SUBCLASS, SERIES, SUBSERIES, FILE, ITEM };

	private String level = null;

	/**
	 * Constructs an empty (<strong>invalid</strong>) {@link DescriptionLevel}.
	 * <p>
	 * <strong>This method should not be used. All the possible values for a
	 * {@link DescriptionLevel} are already defined as constant values.</strong>
	 * </p>
	 */
	public DescriptionLevel() {
	}

	/**
	 * Constructs a {@link DescriptionLevel} clonning an existing
	 * {@link DescriptionLevel}.
	 * 
	 * @param dLevel
	 *            the {@link DescriptionLevel} to clone.
	 * 
	 * @throws InvalidDescriptionLevel
	 *             if the specified level is not one of the allowed levels.
	 */
	public DescriptionLevel(DescriptionLevel dLevel)
			throws InvalidDescriptionLevel {
		this(dLevel.getLevel());
	}

	/**
	 * Constructs a new {@link DescriptionLevel} of the specified level.
	 * 
	 * @param level
	 *            the level of this {@link DescriptionLevel}. Valid values for
	 *            description level are:
	 *            <ul>
	 *            <li>fonds,</li>
	 *            <li>subfonds,</li>
	 *            <li>class,</li>
	 *            <li>subclass,</li>
	 *            <li>series,</li>
	 *            <li>subseries,</li>
	 *            <li>file,</li>
	 *            <li>item.</li>
	 *            </ul>
	 * 
	 * @throws InvalidDescriptionLevel
	 *             if the specified level is not one of the allowed levels.
	 */
	public DescriptionLevel(String level) throws InvalidDescriptionLevel {
		setLevel(level);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean equal = false;

		if (obj != null && obj instanceof DescriptionLevel) {
			DescriptionLevel other = (DescriptionLevel) obj;
			equal = getLevel() == other.getLevel()
					|| getLevel().equals(other.getLevel());
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * Compare to other description level following the order in
	 * {@link DescriptionLevel#DESCRIPTION_LEVELS}
	 * 
	 * @param other
	 * @return greater than 0 if other level is lesser than this. returns 0 if equal.
	 */
	public int compareTo(DescriptionLevel other) {

		DescriptionLevel otherDescriptionLevel = (DescriptionLevel) other;

		List<DescriptionLevel> levels = Arrays.asList(DESCRIPTION_LEVELS);

		return levels.indexOf(this) - levels.indexOf(otherDescriptionLevel);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return getLevel();
	}

	/**
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set. Valid values for description level are:
	 *            <ul>
	 *            <li>fonds,</li>
	 *            <li>subfonds,</li>
	 *            <li>class,</li>
	 *            <li>subclass,</li>
	 *            <li>series,</li>
	 *            <li>subseries,</li>
	 *            <li>file,</li>
	 *            <li>item.</li>
	 *            </ul>
	 * @throws InvalidDescriptionLevel
	 *             if the specified level is not one of the allowed levels.
	 */
	public void setLevel(String level) throws InvalidDescriptionLevel {
		if (Arrays.asList(LEVELS).contains(level.toLowerCase())) {
			this.level = level.toLowerCase();
		} else {
			throw new InvalidDescriptionLevel("Invalid level: " + level);
		}
	}
}
