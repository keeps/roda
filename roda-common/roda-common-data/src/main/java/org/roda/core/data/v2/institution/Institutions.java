package org.roda.core.data.v2.institution;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_INSTITUTIONS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Institutions implements RODAObjectList<Institution> {
  private static final long serialVersionUID = -2558439591345762182L;
  private List<Institution> institutions;

  public Institutions() {
    super();
    institutions = new ArrayList<>();
  }

  public Institutions(List<Institution> institutions) {
    super();
    this.institutions = institutions;
  }

  @Override
  public List<Institution> getObjects() {
    return institutions;
  }

  @Override
  public void setObjects(List<Institution> institutions) {
    this.institutions = institutions;
  }

  @Override
  public void addObject(Institution institution) {
    this.institutions.add(institution);
  }
}
