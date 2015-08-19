package org.roda.model;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.data.v2.User;

public abstract class ModelObservable {
	private final List<ModelObserver> observers;

	public ModelObservable() {
		super();
		this.observers = new ArrayList<ModelObserver>();
	}

	public void addModelObserver(ModelObserver observer) {
		observers.add(observer);
	}

	public void removeModelObserver(ModelObserver observer) {
		observers.remove(observer);
	}

	protected void notifyAipCreated(AIP aip) {
		for (ModelObserver observer : observers) {
			observer.aipCreated(aip);
		}
	}

	protected void notifyAipUpdated(AIP aip) {
		for (ModelObserver observer : observers) {
			observer.aipUpdated(aip);
		}
	}

	protected void notifyAipDeleted(String aipId) {
		for (ModelObserver observer : observers) {
			observer.aipDeleted(aipId);
		}
	}

	protected void notifyDescriptiveMetadataCreated(
			DescriptiveMetadata descriptiveMetadata) {
		for (ModelObserver observer : observers) {
			observer.descriptiveMetadataCreated(descriptiveMetadata);
		}
	}

	protected void notifyDescriptiveMetadataUpdated(
			DescriptiveMetadata descriptiveMetadata) {
		for (ModelObserver observer : observers) {
			observer.descriptiveMetadataUpdated(descriptiveMetadata);
		}
	}

	protected void notifyDescriptiveMetadataDeleted(String aipId,
			String descriptiveMetadataBinaryId) {
		for (ModelObserver observer : observers) {
			observer.descriptiveMetadataDeleted(aipId,
					descriptiveMetadataBinaryId);
		}
	}

	protected void notifyRepresentationCreated(Representation representation) {
		for (ModelObserver observer : observers) {
			observer.representationCreated(representation);
		}
	}

	protected void notifyRepresentationUpdated(Representation representation) {
		for (ModelObserver observer : observers) {
			observer.representationUpdated(representation);
		}
	}

	protected void notifyRepresentationDeleted(String aipId,
			String representationId) {
		for (ModelObserver observer : observers) {
			observer.representationDeleted(aipId, representationId);
		}
	}

	protected void notifyFileCreated(File file) {
		for (ModelObserver observer : observers) {
			observer.fileCreated(file);
		}
	}

	protected void notifyFileUpdated(File file) {
		for (ModelObserver observer : observers) {
			observer.fileUpdated(file);
		}
	}

	protected void notifyFileDeleted(String aipId, String representationId,
			String fileId) {
		for (ModelObserver observer : observers) {
			observer.fileDeleted(aipId, representationId, fileId);
		}
	}
	
	protected void notifyLogEntryCreated(LogEntry entry){
		for (ModelObserver observer : observers) {
			observer.logEntryCreated(entry);
		}
	}
	
	protected void notifySipStateCreated(SIPReport state){
		for (ModelObserver observer : observers) {
			observer.sipReportCreated(state);
		}
	}

	protected void notifyUserCreated(User user){
		for (ModelObserver observer : observers) {
			observer.userCreated(user);
		}
	}
	
	protected void notifyUserUpdated(User user){
		for (ModelObserver observer : observers) {
			observer.userUpdated(user);
		}
	}
	
	protected void notifyUserDeleted(String userID){
		for (ModelObserver observer : observers) {
			observer.userDeleted(userID);
		}
	}
	protected void notifyGroupCreated(Group group){
		for (ModelObserver observer : observers) {
			observer.groupCreated(group);
		}
	}
	
	protected void notifyGroupUpdated(Group group){
		for (ModelObserver observer : observers) {
			observer.groupUpdated(group);
		}
	}
	
	protected void notifyGroupDeleted(String groupID){
		for (ModelObserver observer : observers) {
			observer.groupDeleted(groupID);
		}
	}
	
	
	protected void notifyRODAMemberCreated(RODAMember member){
		if(member.isUser()){
			notifyUserCreated(new User(member));
		}else{
			notifyGroupCreated(new Group(member));
		}
	}
	
	protected void notifyRODAMemberUpdated(RODAMember member){
		if(member.isUser()){
			notifyUserUpdated(new User(member));
		}else{
			notifyGroupUpdated(new Group(member));
		}
	}
	
	
	//TODO: improve this method...
	protected void notifyRODAMemberDeleted(String memberID){
		notifyUserDeleted(memberID);
		notifyGroupDeleted(memberID);
	}
}
