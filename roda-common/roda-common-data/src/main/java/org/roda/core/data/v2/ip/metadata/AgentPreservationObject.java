/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

/**
 * This is a agent preservation object.
 * 
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
public class AgentPreservationObject extends PreservationMetadata {

  private static final long serialVersionUID = -2413075074669272807L;

  public static final String PRESERVATION_AGENT_TYPE_INGEST_TASK = "software:ingest_task";
  public static final String PRESERVATION_AGENT_TYPE_MIGRATOR = "software:migrator";
  public static final String PRESERVATION_AGENT_TYPE_DIGITALIZATION = "software:digitalization";
  public static final String PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN = "software:plugin:fixity_check";
  public static final String PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN = "software:plugin:characterization";
  public static final String PRESERVATION_AGENT_TYPE_VIRUS_CHECK_PLUGIN = "software:plugin:virus_check";
  public static final String PRESERVATION_AGENT_TYPE_VERAPDF_CHECK_PLUGIN = "software:plugin:verapdf_check";
  public static final String PRESERVATION_AGENT_TYPE_PDFTOPDFA_CONVERSION_PLUGIN = "software:plugin:pdftopdfa_conversion";
  public static final String PRESERVATION_AGENT_TYPE_UNKNOWN_PREFIX = "unknown:";

  public static final String[] PRESERVATION_AGENT_TYPES = new String[] {PRESERVATION_AGENT_TYPE_INGEST_TASK,
    PRESERVATION_AGENT_TYPE_MIGRATOR, PRESERVATION_AGENT_TYPE_DIGITALIZATION,
    PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN, PRESERVATION_AGENT_TYPE_VERAPDF_CHECK_PLUGIN,
    PRESERVATION_AGENT_TYPE_PDFTOPDFA_CONVERSION_PLUGIN, PRESERVATION_AGENT_TYPE_VIRUS_CHECK_PLUGIN};

  private String agentName = null;
  private String agentType = null;

  // TODO add agentVersion
  // TODO add agentNote
  // TODO add agentExtension

  // TODO add linking events
  // TODO add linking rights
  // TODO add linking environments

  /**
   * Constructs an empty {@link AgentPreservationObject}.
   */
  public AgentPreservationObject() {
    setType(PreservationMetadataType.AGENT);
  }

  /**
   * @param agent
   */
  public AgentPreservationObject(AgentPreservationObject agent) {
    this(agent.getId(), agent.getAipId(), agent.getRepresentationId());

    setAgentName(agent.getAgentName());
    setAgentType(agent.getAgentType());
  }

  /**
   * @param id
   * @param label
   * @param model
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   */
  public AgentPreservationObject(String id, String aipId, String representationId) {
    super(id, aipId, representationId, PreservationMetadataType.AGENT);
  }

  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public String getAgentType() {
    return agentType;
  }

  public void setAgentType(String agentType) {
    this.agentType = agentType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((agentName == null) ? 0 : agentName.hashCode());
    result = prime * result + ((agentType == null) ? 0 : agentType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AgentPreservationObject other = (AgentPreservationObject) obj;
    if (agentName == null) {
      if (other.agentName != null)
        return false;
    } else if (!agentName.equals(other.agentName))
      return false;
    if (agentType == null) {
      if (other.agentType != null)
        return false;
    } else if (!agentType.equals(other.agentType))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AgentPreservationObject [agentName=" + agentName + ", agentType=" + agentType + ", super.toString()="
      + super.toString() + "]";
  }

}
