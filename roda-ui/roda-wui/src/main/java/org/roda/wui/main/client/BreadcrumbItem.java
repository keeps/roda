/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.main.client;

import java.util.List;

import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

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
