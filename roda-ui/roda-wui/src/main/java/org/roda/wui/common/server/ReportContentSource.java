/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.filter.Filter;

public interface ReportContentSource<T> {

  public String getReportTitle();

  public int getCount(HttpSession session, Filter filter) throws Exception;

  public T[] getElements(HttpSession session, ContentAdapter adapter) throws Exception;

  public String getElementId(T element);

  public Map<String, String> getElementFields(HttpServletRequest req, T element);

  public String getFieldNameTranslation(String name);

  public String getFieldValueTranslation(String value);

}
