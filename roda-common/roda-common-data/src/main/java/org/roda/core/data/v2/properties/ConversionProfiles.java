package org.roda.core.data.v2.properties;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConversionProfiles implements Serializable {
  @Serial
  private static final long serialVersionUID = -4673854066618821461L;

  private Set<ConversionProfile> conversionProfileSet;

  public ConversionProfiles() {
    conversionProfileSet = new HashSet<>();
  }

  public Set<ConversionProfile> getConversionProfileSet() {
    return conversionProfileSet;
  }

  public void setConversionProfileSet(Set<ConversionProfile> conversionProfileSet) {
    this.conversionProfileSet = conversionProfileSet;
  }

  public boolean addObject(ConversionProfile item) {
    return this.conversionProfileSet.add(item);
  }

  public boolean addObjects(Set<ConversionProfile> list) {
    return this.conversionProfileSet.addAll(list);
  }
}
