package org.roda.model;

import java.util.ArrayList;
import java.util.List;

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

}
