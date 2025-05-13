package org.roda.core.data.v2.ip.metadata;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({RodaConstants.DESCRIPTIVE_METADATA_AIP_ID, RodaConstants.DESCRIPTIVE_METADATA_REPRESENTATION_ID})
public class TechnicalMetadataMixIn {
}
