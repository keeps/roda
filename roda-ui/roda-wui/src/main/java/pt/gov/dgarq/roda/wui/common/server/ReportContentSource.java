package pt.gov.dgarq.roda.wui.common.server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

public interface ReportContentSource<T> {

	public String getReportTitle();

	public int getCount(HttpSession session, Filter filter) throws Exception;

	public T[] getElements(HttpSession session, ContentAdapter adapter)
			throws Exception;

	public String getElementId(T element);

	public Map<String, String> getElementFields(HttpServletRequest req, T element);

	public String getFieldNameTranslation(String name);

	public String getFieldValueTranslation(String value);

}
