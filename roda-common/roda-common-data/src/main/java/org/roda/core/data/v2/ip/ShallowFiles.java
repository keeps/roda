package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_SHALLOWS_FILE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShallowFiles implements RODAObjectList<ShallowFile> {
  private static final long serialVersionUID = 3404494208482338468L;
  private List<ShallowFile> shallowFiles;

  public ShallowFiles() {
    super();
    shallowFiles = new ArrayList<>();
  }

  @Override
  public List<ShallowFile> getObjects() {
    return shallowFiles;
  }

  @Override
  public void setObjects(List<ShallowFile> objects) {
    this.shallowFiles = objects;
  }

  @Override
  public void addObject(ShallowFile object) {
    this.shallowFiles.add(object);
  }

  public ShallowFile getObject(String UUID) {
    for (ShallowFile file : shallowFiles) {
      if (file.getUUID().equals(UUID)) {
        return file;
      }
    }
    return null;
  }

  public void removeObject(String UUID) {
    this.shallowFiles.removeIf(shallowFile -> shallowFile.getUUID().equals(UUID));
  }
}
