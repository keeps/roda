/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.characterization.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@XmlRootElement(name = "featureExtractor")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.loc.gov/premis/v3")
public class TechnicalMetadata {

  @XmlAttribute
  private String type;
  @XmlAttribute
  private String featureExtractorType;
  @XmlAttribute
  private String featureExtractorVersion;
  @XmlAttribute
  private String digest;
  @XmlAttribute
  private String algorithm;

  @XmlElement(name = "metadata", namespace = "http://www.loc.gov/premis/v3")
  private TechnicalMetadataElement technicalMetadataElement;

  public TechnicalMetadata() {
    // do nothing
  }

  public TechnicalMetadata(String type, String featureExtractorType, String featureExtractorVersion, String digest,
    String algorithm) {
    this.type = type;
    this.featureExtractorType = featureExtractorType;
    this.featureExtractorVersion = featureExtractorVersion;
    this.digest = digest;
    this.algorithm = algorithm;
  }

  public TechnicalMetadata(String type, String featureExtractorType, String featureExtractorVersion, String digest,
    String algorithm, TechnicalMetadataElement technicalMetadataElement) {
    this.type = type;
    this.featureExtractorType = featureExtractorType;
    this.featureExtractorVersion = featureExtractorVersion;
    this.digest = digest;
    this.algorithm = algorithm;
    this.technicalMetadataElement = technicalMetadataElement;
  }

  @Override
  public String toString() {
    return "EventData{" + "type='" + type + '\'' + ", featureExtractorType='" + featureExtractorType + '\''
      + ", featureExtractorVersion='" + featureExtractorVersion + '\'' + ", digest='" + digest + '\'' + ", algorithm='"
      + algorithm + '\'' + '}';
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFeatureExtractorType() {
    return featureExtractorType;
  }

  public void setFeatureExtractorType(String featureExtractorType) {
    this.featureExtractorType = featureExtractorType;
  }

  public String getFeatureExtractorVersion() {
    return featureExtractorVersion;
  }

  public void setFeatureExtractorVersion(String featureExtractorVersion) {
    this.featureExtractorVersion = featureExtractorVersion;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public TechnicalMetadataElement getTechnicalMetadataElement() {
    return technicalMetadataElement;
  }

  public void setTechnicalMetadataElement(TechnicalMetadataElement technicalMetadataElement) {
    this.technicalMetadataElement = technicalMetadataElement;
  }
}
