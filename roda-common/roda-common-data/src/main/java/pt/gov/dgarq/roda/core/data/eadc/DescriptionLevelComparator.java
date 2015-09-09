package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A {@link Comparator} of {@link DescriptionLevel}. Level
 * {@link DescriptionLevel#FONDS} is the lowest and
 * {@link DescriptionLevel#ITEM} is the highest. <br/>
 * The complete list of levels ordered from the lowest to the highest is:
 * <ol>
 * <li>{@link DescriptionLevel#FONDS},</li>
 * <li>{@link DescriptionLevel#SUBFONDS},</li>
 * <li>{@link DescriptionLevel#CLASS},</li>
 * <li>{@link DescriptionLevel#SUBCLASS},</li>
 * <li>{@link DescriptionLevel#SERIES},</li>
 * <li>{@link DescriptionLevel#SUBSERIES},</li>
 * <li>{@link DescriptionLevel#FILE},</li>
 * <li>{@link DescriptionLevel#ITEM}.</li>
 * </ol>
 * 
 * @author Rui Castro
 */
public class DescriptionLevelComparator implements Comparator<DescriptionLevel>, Serializable {
  private static final long serialVersionUID = -8726486397243945069L;

  /**
   * @param level1
   * @param level2
   * @return greater than 0 if other level1 is greater than level2. returns 0 if
   *         they are equal.
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(DescriptionLevel level1, DescriptionLevel level2) {
    return level1.compareTo(level2);
  }
}
