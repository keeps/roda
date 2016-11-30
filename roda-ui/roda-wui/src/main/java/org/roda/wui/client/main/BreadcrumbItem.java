/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import java.util.List;

import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;

public class BreadcrumbItem {
  private SafeHtml label;
  private String title;
  private Command command;

  public BreadcrumbItem(SafeHtml label, String title, Command command) {
    super();
    this.label = label;
    this.title = title;
    this.setCommand(command);
  }

  public BreadcrumbItem(SafeHtml label, String title, final List<String> path) {
    this(label, title, new Command() {

      @Override
      public void execute() {
        HistoryUtils.newHistory(path);
      }
    });
  }

  public BreadcrumbItem(String label, Command command) {
    this(SafeHtmlUtils.fromSafeConstant(label), label, command);
  }

  public BreadcrumbItem(String label, List<String> path) {
    this(SafeHtmlUtils.fromSafeConstant(label), label, path);
  }

  public SafeHtml getLabel() {
    return label;
  }

  public void setLabel(SafeHtml label) {
    this.label = label;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

}
