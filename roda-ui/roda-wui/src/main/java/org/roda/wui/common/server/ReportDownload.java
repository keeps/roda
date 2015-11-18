/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.servlet.ServletContextURIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.common.LoginException;
import org.roda.core.data.common.NoSuchReportException;
import org.roda.core.data.common.RODAClientException;
import org.roda.core.data.common.ReportException;
import org.roda.wui.common.client.PrintReportException;

/**
 * Servlet implementation class for Servlet: ReportDownload
 * 
 */
public class ReportDownload extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
  static final long serialVersionUID = 1L;
  private static final Logger logger = LoggerFactory.getLogger(ReportDownload.class);
  private static ReportDownload instance = null;

  /**
   * Get current instance
   * 
   * @return
   */
  public static ReportDownload getInstance() {
    while (instance == null) {
      try {
        // Wait for ReportDownload to initialize
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // nothing to do
      }
    }
    return instance;
  }

  /**
   * Type parameter key
   */
  public static final String PARAMETER_TYPE = "type";

  /**
   * Locale parameter key
   */
  public static final String PARAMETER_LOCALE = "locale";

  /**
   * Output parameter key
   */
  public static final String PARAMETER_OUTPUT = "output";

  /**
   * Content adapter report type
   */
  public static final String REPORT_TYPE_CONTENT_ADAPTER = "CONTENT_ADAPTER";

  /**
   * Report report type
   */
  public static final String REPORT_TYPE_REPORT = "REPORT";

  /**
   * Result output a PDF
   */
  public static final String REPORT_OUTPUT_PDF = "PDF";

  /**
   * Result output a text file with Comma Separated Values
   */
  public static final String REPORT_OUTPUT_CSV = "CSV";

  private static final String CONTENT_ADAPTER_BUNDLE_NAME = "config.reports.xml-fo.content-adapter";
  private static final String REPORT_BUNDLE_NAME = "config.reports.xml-fo.report";

  private static final String REPORT_INFO_ATTRIB = "REPORT_INFO";

  /**
   * Report info holder class
   */
  public static class ReportInfo {
    private final ContentAdapter adapter;
    private final ReportContentSource<Object> source;
    private final Object[] elements;

    /**
     * Create a new Report info
     * 
     * @param adapter
     *          content adapter
     * @param source
     *          content source
     * @param elements
     */
    public ReportInfo(ContentAdapter adapter, ReportContentSource<Object> source, Object[] elements) {
      this.adapter = adapter;
      this.source = source;
      this.elements = elements;
    }

    /**
     * Get content adapter
     * 
     * @return
     */
    public ContentAdapter getAdapter() {
      return adapter;
    }

    /**
     * Get content source
     * 
     * @return
     */
    public ReportContentSource<Object> getSource() {
      return source;
    }

    /**
     * Get elements
     * 
     * @return
     */
    public Object[] getElements() {
      return elements;
    }
  }

  private final FopFactory fopFactory;

  private URIResolver uriResolver;

  /**
   * Create a new report download
   * 
   * @throws ServletException
   */
  public ReportDownload() throws ServletException {
    fopFactory = FopFactory.newInstance();
    DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
    // try {
    // Configuration cfg = cfgBuilder.build(RodaClientFactory
    // .getConfigurationFile("reports/xml-fo/fop.xconf"));
    // fopFactory.setUserConfig(cfg);
    // } catch (IOException e) {
    // logger.fatal("Error loading templates", e);
    // throw new ServletException("Error loading templates", e);
    // } catch (ConfigurationException e) {
    // logger.fatal("Error loading FOP configuration", e);
    // throw new ServletException("Error loading FOP configuration", e);
    // } catch (SAXException e) {
    // logger.fatal("Error parsing FOP configuration", e);
    // throw new ServletException("Error parsing FOP configuration", e);
    // }

    instance = this;

  }

  protected synchronized ReportInfo getReportInfo(HttpSession session) {
    ReportInfo reportInfo;
    do {
      reportInfo = (ReportInfo) session.getAttribute(REPORT_INFO_ATTRIB);
      if (reportInfo == null) {
        try {
          wait();
        } catch (InterruptedException e) {
          // do nothing
        }
      }
    } while (reportInfo == null);

    return reportInfo;
  }

  protected void removeReportInfo(HttpSession session) {
    session.removeAttribute(REPORT_INFO_ATTRIB);
  }

  protected ResourceBundle getResourceBundle(String bundleName, Locale locale) {
    ResourceBundle bundle;
    try {
      bundle = ResourceBundle.getBundle(bundleName, locale);
    } catch (MissingResourceException e) {
      bundle = ResourceBundle.getBundle(bundleName);
    }
    return bundle;
  }

  protected String getTemplate(String bundleName, String templateName, Locale locale) throws IOException {
    String xmlFoTemplate = getResourceBundle(bundleName, locale).getString(templateName);
    // InputStream configurationFile = RodaClientFactory
    // .getConfigurationFile("reports/xml-fo/" + xmlFoTemplate);
    // if (configurationFile == null) {
    // throw new FileNotFoundException(xmlFoTemplate);
    // }
    // return IOUtils.toString(configurationFile);
    return null;
  }

  public void init() throws ServletException {
    this.uriResolver = new ServletContextURIResolver(getServletContext());
    fopFactory.setURIResolver(uriResolver);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String type = request.getParameter(PARAMETER_TYPE);
    String localeString = request.getParameter(PARAMETER_LOCALE);
    String output = request.getParameter(PARAMETER_OUTPUT);

    // Set PDF as default output for retro-compatability
    if (output == null) {
      output = REPORT_OUTPUT_PDF;
    }

    Locale locale = ServerTools.parseLocale(localeString);
    String filename = getFilename(request, type, output);

    if (output.equals(REPORT_OUTPUT_PDF)) {
      getPDF(type, locale, filename, request, response);
    } else if (output.equals(REPORT_OUTPUT_CSV)) {
      getCSV(type, locale, filename, request, response);
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown requested output '" + output
        + "'. Available outputs [" + REPORT_OUTPUT_PDF + ", " + REPORT_OUTPUT_CSV + "]");
    }

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  private void getPDF(String type, Locale locale, String filename, HttpServletRequest request,
    HttpServletResponse response) throws IOException {
    response.setContentType("application/pdf");
    String xmlFO = null;

    if (type.equals(REPORT_TYPE_CONTENT_ADAPTER)) {
      try {
        xmlFO = getContentAdapterFO(request, locale);
      } catch (Exception e) {
        logger.error("Error creating XSL FO", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } else if (type.equals(REPORT_TYPE_REPORT)) {
      xmlFO = getReportFO(request, locale);
    } else {
      logger.error("Unsupported report type " + type);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported report type " + type);
    }

    if (xmlFO != null) {
      logger.debug("Creating PDF with FOP");
      try {
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, response.getOutputStream());
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        Source src = new StreamSource(new ByteArrayInputStream(xmlFO.getBytes("UTF-8")));

        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
      } catch (FOPException e) {
        logger.error("Error creating PDF report", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (TransformerConfigurationException e) {
        logger.error("Error creating PDF report", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (TransformerException e) {
        logger.error("Error creating PDF report", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (SocketException e) {
        logger.info("Client aborted", e);
      } finally {
        response.getOutputStream().close();
      }
    } else {
      logger.error("XML-FO came empty");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "XML-FO came empty");
    }

  }

  private void getCSV(String type, Locale locale, String filename, HttpServletRequest request,
    HttpServletResponse response) throws IOException {

    response.setContentType("text/csv");
    response.setCharacterEncoding("UTF-8");
    String csv = null;

    if (type.equals(REPORT_TYPE_CONTENT_ADAPTER)) {
      csv = getContentAdapterCSV(request, locale);
    } else if (type.equals(REPORT_TYPE_REPORT)) {
      try {
        csv = getReportCSV(request, locale);
      } catch (ReportException e) {
        logger.error("Error getting report CSV", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error getting report. See server log for more info");
      } catch (LoginException e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
      } catch (NoSuchReportException e) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
      } catch (RODAClientException e) {
        logger.error("Error getting report CSV", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error getting report. See server log for more info");
      }
    } else {
      logger.error("Unsupported report type " + type);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported report type " + type);
    }

    if (csv != null) {
      response.setHeader("Content-Disposition", "attachment; filename=" + filename);
      response.getWriter().write(csv);
      response.getWriter().flush();
      response.getWriter().close();
    } else {
      logger.error("CSV came empty");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV came empty");
    }

  }

  protected String getContentAdapterFO(HttpServletRequest request, Locale locale) throws Exception {
    logger.debug("Creating report info");
    final HttpSession session = request.getSession();
    StringBuilder buffer = new StringBuilder();
    ReportInfo info = getReportInfo(request.getSession());
    ContentAdapter adapter = info.getAdapter();
    ReportContentSource<Object> content = info.getSource();
    Object[] elements = info.getElements();
    removeReportInfo(session);

    Object[] reportInfo = new Object[10];
    reportInfo[0] = content.getReportTitle();
    // reportInfo[1] = RodaClientFactory.getServletUrl(request);
    // reportInfo[2] = new Date();
    // User user = RodaClientFactory.getRodaClient(request.getSession())
    // .getAuthenticatedUser();
    // reportInfo[3] = user.getFullName();
    // reportInfo[4] = user.getName();
    // reportInfo[5] = user.getEmail();
    reportInfo[6] = ContentAdapterHelper.translateFilter(adapter.getFilter(), content, locale);
    int total = content.getCount(request.getSession(), adapter.getFilter());
    reportInfo[7] = ContentAdapterHelper.translateSubList(adapter.getSublist(), total, locale);
    reportInfo[8] = ContentAdapterHelper.translateSorter(adapter.getSorter(), content, locale);

    logger.debug("Creating XML-FO");
    buffer.append(String.format(getTemplate(CONTENT_ADAPTER_BUNDLE_NAME, "xml.fo.header", locale), reportInfo));
    String dataFieldTemplate = getTemplate(CONTENT_ADAPTER_BUNDLE_NAME, "xml.fo.data.field", locale);
    String dataTemplate = getTemplate(CONTENT_ADAPTER_BUNDLE_NAME, "xml.fo.data", locale);
    for (Object element : elements) {
      String elementId = ServerTools.encodeXML(content.getElementId(element));
      Map<String, String> elementsFields = content.getElementFields(request, element);
      StringBuilder fieldsFO = new StringBuilder();
      for (Entry<String, String> entry : elementsFields.entrySet()) {
        if (entry.getValue() != null) {
          String name = ServerTools.encodeXML(entry.getKey());
          String value;
          if (ServerTools.isURL(entry.getValue())) {
            value = String.format("<fo:basic-link " + "external-destination=\"%1$s\">%1$s" + "</fo:basic-link>",
              entry.getValue());
          } else {
            value = ServerTools.encodeXML(entry.getValue());
          }
          fieldsFO.append(String.format(dataFieldTemplate, new Object[] {name, value}));
        }
      }
      buffer.append(String.format(dataTemplate, new Object[] {elementId, fieldsFO.toString()}));
    }

    buffer.append(getTemplate(CONTENT_ADAPTER_BUNDLE_NAME, "xml.fo.footer", locale));

    return buffer.toString();
  }

  protected String getReportFO(HttpServletRequest request, Locale locale) throws IOException {
    String ret = null;
    StringBuilder buffer = new StringBuilder();
    String header = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.header", locale);
    String attributes = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.attributes", locale);
    String attributesAttribute = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.attributes.attribute", locale);
    String items = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.items", locale);
    String itemsAttribute = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.items.attribute", locale);
    String footerTemplate = getTemplate(REPORT_BUNDLE_NAME, "xml.fo.footer", locale);

    String reportId = request.getParameter("id");
    if (reportId != null) {
      // try {
      // Report report = RodaClientFactory
      // .getRodaClient(request.getSession())
      // .getReportsService().getReport(reportId);
      // Object[] reportInfo = new Object[6];
      // reportInfo[0] = report.getTitle();
      // reportInfo[1] = RodaClientFactory.getServletUrl(request);
      // reportInfo[2] = new Date();
      // User user = RodaClientFactory.getRodaClient(
      // request.getSession()).getAuthenticatedUser();
      // reportInfo[3] = user.getFullName();
      // reportInfo[4] = user.getName();
      // reportInfo[5] = user.getEmail();
      // buffer.append(String.format(header, reportInfo));
      //
      // StringBuilder attributesFO = new StringBuilder();
      // for (Attribute attribute : report.getAttributes()) {
      // String name = ServerTools.encodeXML(attribute.getName());
      // String value;
      // if (ServerTools.isURL(attribute.getValue())) {
      // value = String.format("<fo:basic-link "
      // + "external-destination=\"%1$s\">%1$s"
      // + "</fo:basic-link>", attribute.getValue());
      // } else {
      // value = ServerTools.encodeXML(attribute.getValue());
      // }
      // attributesFO.append(String.format(attributesAttribute,
      // name, value));
      // }
      // buffer.append(String.format(attributes,
      // attributesFO.toString()));
      //
      // for (ReportItem item : report.getItems()) {
      // String itemId = item.getTitle();
      // StringBuilder itemAttributesFO = new StringBuilder();
      // for (Attribute attribute : item.getAttributes()) {
      // if (attribute.getValue() != null) {
      // String name = ServerTools.encodeXML(attribute
      // .getName());
      // String value;
      // if (ServerTools.isURL(attribute.getValue())) {
      // value = String.format("<fo:basic-link "
      // + "external-destination=\"%1$s\">%1$s"
      // + "</fo:basic-link>",
      // attribute.getValue());
      // } else {
      // value = ServerTools.encodeXML(attribute
      // .getValue());
      // }
      // itemAttributesFO.append(String.format(
      // itemsAttribute,
      // new Object[] { name, value }));
      // }
      // }
      //
      // buffer.append(String.format(items, new Object[] { itemId,
      // itemAttributesFO.toString() }));
      // }
      //
      // buffer.append(footerTemplate);
      // ret = buffer.toString();
      // } catch (Exception e) {
      // logger.error("Error creating report XML FO", e);
      // }
    }

    return ret;
  }

  private String getContentAdapterCSV(HttpServletRequest request, Locale locale) {
    StringBuffer sBuffer = new StringBuffer();
    ReportInfo info = getReportInfo(request.getSession());
    ReportContentSource<Object> content = info.getSource();
    Object[] elements = info.getElements();

    LinkedHashSet<String> headers = new LinkedHashSet<String>();
    headers.add("id");

    // Collect all headers
    for (Object element : elements) {
      Map<String, String> elementsFields = content.getElementFields(request, element);

      for (String name : elementsFields.keySet()) {
        headers.add(name);
      }
    }

    // Create header row
    int i = 0;
    for (String header : headers) {

      if (header.equals("id")) {
        sBuffer.append(ServerTools.encodeCSV("id"));
      } else {
        sBuffer.append(ServerTools.encodeCSV(content.getFieldNameTranslation(header)));
      }

      if (i < headers.size() - 1) {
        sBuffer.append(',');
      } else {
        sBuffer.append('\n');
      }
      i++;
    }

    // create values
    for (Object element : elements) {
      Map<String, String> elementsFields = content.getElementFields(request, element);
      int j = 0;
      for (String header : headers) {
        String value;

        if (header.equals("id")) {
          value = content.getElementId(element);
        } else {
          value = elementsFields.get(header);
        }

        if (value != null) {
          sBuffer.append(ServerTools.encodeCSV(content.getFieldValueTranslation(value)));
        }

        if (j < headers.size() - 1) {
          sBuffer.append(',');
        } else {
          sBuffer.append('\n');
        }
        j++;
      }
    }

    return sBuffer.toString();
  }

  private String getReportCSV(HttpServletRequest request, Locale locale) throws ReportException, LoginException,
    RemoteException, NoSuchReportException, RODAClientException {
    StringBuffer sBuffer = new StringBuffer();
    String reportId = request.getParameter("id");
    // Report report = RodaClientFactory.getRodaClient(request.getSession())
    // .getReportsService().getReport(reportId);
    //
    // // Add headers
    // sBuffer.append("\"Report Title\", \"Attribute Name\", \"Attribute
    // Value\"\n");
    //
    // // Add top level attributes
    // for (Attribute attrib : report.getAttributes()) {
    // sBuffer.append(ServerTools.encodeCSV(report.getTitle()) + ", "
    // + ServerTools.encodeCSV(attrib.getName()) + ", "
    // + ServerTools.encodeCSV(attrib.getValue()) + "\n");
    // }
    //
    // // Add items
    // for (ReportItem item : report.getItems()) {
    // for (Attribute attrib : item.getAttributes()) {
    // sBuffer.append(ServerTools.encodeCSV(item.getTitle()) + ", "
    // + ServerTools.encodeCSV(attrib.getName()) + ", "
    // + ServerTools.encodeCSV(attrib.getValue()) + "\n");
    // }
    // }

    return sBuffer.toString();
  }

  protected String getFilename(HttpServletRequest request, String type, String output) {
    String filename = "RODA_Report";
    if (type.equals(REPORT_TYPE_CONTENT_ADAPTER)) {
      ReportInfo info = getReportInfo(request.getSession());
      filename += "_" + info.getSource().getReportTitle().replaceAll("\\s", "_");
    }
    filename += output.equals(REPORT_OUTPUT_PDF) ? ".pdf" : ".csv";
    return filename;
  }

  /**
   * Create a new PDF Report
   * 
   * @param session
   * @param contentSource
   * @param adapter
   * @param locale
   * @throws PrintReportException
   */
  @SuppressWarnings("unchecked")
  public synchronized void createPDFReport(HttpSession session, ReportContentSource contentSource,
    ContentAdapter adapter) throws PrintReportException {
    Object[] elements;
    try {
      elements = contentSource.getElements(session, adapter);
      session.setAttribute(REPORT_INFO_ATTRIB, new ReportInfo(adapter, contentSource, elements));
      notifyAll();
    } catch (Exception e) {
      logger.error("Error getting elements", e);
      throw new PrintReportException(e.getMessage());
    }

  }

  @Override
  public void destroy() {
    instance = null;
    super.destroy();

  }

}