package pt.gov.dgarq.roda.wui.main.client;

public class BreadcrumbItem {
	private String label;
	private String path;

	public BreadcrumbItem(String label, String path) {
		super();
		this.label = label;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
