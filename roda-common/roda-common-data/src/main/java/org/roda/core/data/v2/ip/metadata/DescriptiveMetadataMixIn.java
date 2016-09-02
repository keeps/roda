/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({RodaConstants.DESCRIPTIVE_METADATA_AIP_ID, RodaConstants.DESCRIPTIVE_METADATA_REPRESENTATION_ID})
public class DescriptiveMetadataMixIn {
}
