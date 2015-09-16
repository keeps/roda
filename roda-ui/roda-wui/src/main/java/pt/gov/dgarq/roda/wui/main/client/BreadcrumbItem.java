package pt.gov.dgarq.roda.wui.main.client;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

public class BreadcrumbItem {
  private SafeHtml label;
  private Command command;

  public BreadcrumbItem(SafeHtml label, Command command) {
    super();
    this.label = label;
    this.setCommand(command);
  }

  public BreadcrumbItem(SafeHtml label, final List<String> path) {
    this(label, new Command() {

      @Override
      public void execute() {
        Tools.newHistory(path);
      }
    });
  }

  public BreadcrumbItem(String label, Command command) {
    this(SafeHtmlUtils.fromSafeConstant(label), command);
  }

  public BreadcrumbItem(String label, List<String> path) {
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
