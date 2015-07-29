package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;

public class BreadcrumbItem {
	private SafeHtml label;
	private Command command;

	public BreadcrumbItem(SafeHtml label, Command command) {
		super();
		this.label = label;
		this.setCommand(command);
	}

	public BreadcrumbItem(SafeHtml label, final String path) {
		this(label, new Command() {

			@Override
			public void execute() {
				History.newItem(path);
			}
		});
	}

	public BreadcrumbItem(String label, Command command) {
		this(SafeHtmlUtils.fromSafeConstant(label), command);
	}

	public BreadcrumbItem(String label, String path) {
		this(SafeHtmlUtils.fromSafeConstant(label), path);
	}

	public SafeHtml getLabel() {
		return label;
	}

	public void setLabel(SafeHtml label) {
		this.label = label;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

}
