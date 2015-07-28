package pt.gov.dgarq.roda.core.data.v2;


public class SimpleRepresentationFilePreservationMetadata extends SimplePreservationMetadata {
	private String fileId;
	private String representationObjectId;
	private String pronomId;
	private String mimetype;
	private long size;
	private String hash;
	
	
	public SimpleRepresentationFilePreservationMetadata(SimpleRepresentationFilePreservationMetadata simple) {
		this.setAipId(simple.getAipId());
		this.setCreatedDate(simple.getCreatedDate());
		this.setFileId(simple.getFileId());
		this.setHash(simple.getHash());
		this.setID(simple.getID());
		this.setId(simple.getId());
		this.setLabel(simple.getLabel());
		this.setLastModifiedDate(simple.getLastModifiedDate());
		this.setMimetype(simple.getMimetype());
		this.setModel(simple.getModel());
		this.setPronomId(simple.getPronomId());
		this.setRepresentationId(simple.getRepresentationId());
		this.setRepresentationObjectId(simple.getRepresentationObjectId());
		this.setSize(simple.getSize());
		this.setState(simple.getState());
		this.setType(simple.getType());
	}
	public SimpleRepresentationFilePreservationMetadata() {
		this.size=-1;
	}
	public String getRepresentationObjectId() {
		return representationObjectId;
	}
	public void setRepresentationObjectId(String representationObjectId) {
		this.representationObjectId = representationObjectId;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getPronomId() {
		return pronomId;
	}
	public void setPronomId(String pronomId) {
		this.pronomId = pronomId;
	}
	public String getMimetype() {
		return mimetype;
	}
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
}
