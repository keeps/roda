package org.roda.core.data.v2.ip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.net.URL;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_SHALLOW_FILE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShallowFile implements IsModelObject {
  private static final long serialVersionUID = 6755039131089472458L;
  private String UUID;
  private String name;
  private URI location;
  private Long size;
  private XMLGregorianCalendar created;
  private String mimeType;
  private String checksum;
  private String checksumType;


  public ShallowFile() {
  }

  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public URI getLocation() {
    return location;
  }

  public void setLocation(URI location) {
    this.location = location;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 0;
  }

  @JsonIgnore
  @Override
  public String getId() {
    return null;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public XMLGregorianCalendar getCreated() {
    return created;
  }

  public void setCreated(XMLGregorianCalendar created) {
    this.created = created;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public String getChecksumType() {
    return checksumType;
  }

  public void setChecksumType(String checksumType) {
    this.checksumType = checksumType;
  }
}
