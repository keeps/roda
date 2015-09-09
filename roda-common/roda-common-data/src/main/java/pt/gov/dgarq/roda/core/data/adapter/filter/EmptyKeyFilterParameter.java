package pt.gov.dgarq.roda.core.data.adapter.filter;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public class EmptyKeyFilterParameter extends FilterParameter {
  private static final long serialVersionUID = 5888125949326684987L;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public EmptyKeyFilterParameter() {
  }

  public EmptyKeyFilterParameter(EmptyKeyFilterParameter emptyKeyFilterParameter) {
    this(emptyKeyFilterParameter.getName());
  }

  public EmptyKeyFilterParameter(String name) {
    setName(name);
  }

  @Override
  public String toString() {
    return "EmptyKeyFilterParameter [getName()=" + getName() + ", getClass()=" + getClass() + ", hashCode()="
      + hashCode() + ", toString()=" + super.toString() + "]";
  }

}
