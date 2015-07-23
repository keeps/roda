package org.roda.index;

import java.util.Date;

import org.roda.model.preservation.EventPreservationObject;


public class SimpleEventPreservationMetadata extends SimplePreservationMetadata{
	private String fileId;
	private Date date;
	private String name;
	private String description;
	private String outcomeResult;
	private String outcomeDetails;
	private String agentID;
	private String targetID;
	
	public SimpleEventPreservationMetadata(){
		
	}
	
	public SimpleEventPreservationMetadata(EventPreservationObject epo){
		setAgentID(epo.getAgentID());
		setCreatedDate(epo.getCreatedDate());
		setDate(epo.getDate());
		setDescription(epo.getDescription());
		setFileId(epo.getFileId());
		setLabel(epo.getLabel());
		setLastModifiedDate(epo.getLastModifiedDate());
		setName(epo.getName());
		setOutcomeDetails(epo.getOutcomeDetails());
		setOutcomeResult(epo.getOutcomeResult());
		setState(epo.getState());
		setTargetID(epo.getTargetID());
		setType(epo.getType());
		setAipId(epo.getAipId());
		setRepresentationId(epo.getRepresentationId());
		setFileId(epo.getFileId());
	}
	public SimpleEventPreservationMetadata(SimpleEventPreservationMetadata simple) {
		this.setAgentID(simple.getAgentID());
		this.setAipId(simple.getAipId());
		this.setCreatedDate(simple.getCreatedDate());
		this.setDate(simple.getDate());
		this.setDescription(simple.getDescription());
		this.setFileId(simple.getFileId());
		this.setId(simple.getId());
		this.setID(simple.getID());
		this.setLabel(simple.getLabel());
		this.setLastModifiedDate(simple.getLastModifiedDate());
		this.setModel(simple.getModel());
		this.setName(simple.getName());
		this.setOutcomeDetails(simple.getOutcomeDetails());
		this.setOutcomeResult(simple.getOutcomeResult());
		this.setRepresentationId(simple.getRepresentationId());
		this.setState(simple.getState());
		this.setTargetID(simple.getTargetID());
		this.setType(simple.getType());
	}

	public String getTargetID() {
		return targetID;
	}
	public void setTargetID(String targetID) {
		this.targetID = targetID;
	}
	public String getAgentID() {
		return agentID;
	}
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOutcomeResult() {
		return outcomeResult;
	}
	public void setOutcomeResult(String outcomeResult) {
		this.outcomeResult = outcomeResult;
	}
	public String getOutcomeDetails() {
		return outcomeDetails;
	}
	public void setOutcomeDetails(String outcomeDetails) {
		this.outcomeDetails = outcomeDetails;
	}
	
	
}
