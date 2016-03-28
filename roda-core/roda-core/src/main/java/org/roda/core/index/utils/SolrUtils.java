/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.handler.loader.XMLLoader;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.adapter.facet.FacetParameter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.RangeFacetParameter;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.LongRangeFilterParameter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DateGranularity;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.FacetFieldResult;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaGroup;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Utilities class related to Apache Solr
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luís Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public class SolrUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);

  private static final Set<String> NON_REPEATABLE_FIELDS = new HashSet<>(Arrays.asList(RodaConstants.AIP_TITLE,
    RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));

  /** Private empty constructor */
  private SolrUtils() {

  }

  public static <T extends Serializable> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    Class<T> responseClass, Facets facets) throws GenericException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<T>();

    for (SolrDocument doc : docList) {
      T result = solrDocumentTo(responseClass, doc);
      docs.add(result);
    }

    return new IndexResult<T>(offset, limit, totalCount, docs, facetResults);
  }

  private static List<FacetFieldResult> processFacetFields(Facets facets, List<FacetField> facetFields) {
    List<FacetFieldResult> ret = new ArrayList<FacetFieldResult>();
    FacetFieldResult facetResult;
    if (facetFields != null) {
      for (FacetField facet : facetFields) {
        LOGGER.trace("facet:{} count:{}", facet.getName(), facet.getValueCount());
        facetResult = new FacetFieldResult(facet.getName(), facet.getValueCount(),
          facets.getParameters().get(facet.getName()).getValues());
        for (Count count : facet.getValues()) {
          LOGGER.trace("   value:{} value:{}", count.getName(), count.getCount());
          facetResult.addFacetValue(count.getName(), count.getName(), count.getCount());
        }
        ret.add(facetResult);
      }
    }
    return ret;

  }

  public static SolrInputDocument getDescriptiveMetataFields(Binary binary, String metadataType, String metadataVersion)
    throws GenericException {
    SolrInputDocument doc;
    InputStream inputStream;
    String xsltFilename = null;
    InputStream transformerStream = null;

    try {

      // get xslt from metadata type and version if defined
      if (metadataType != null) {
        String lowerCaseMetadataType = metadataType.toLowerCase();
        if (metadataVersion != null) {
          String lowerCaseMetadataTypeWithVersion = lowerCaseMetadataType + RodaConstants.METADATA_VERSION_SEPARATOR
            + metadataVersion;
          // FIXME 20160314 hsilva: replace hardcoded path by constant or method
          // (to support both filesystem in win/linux and classpath)
          transformerStream = RodaCoreFactory
            .getConfigurationFileAsStream("crosswalks/ingest/" + lowerCaseMetadataTypeWithVersion + ".xslt");
        }
        if (transformerStream == null) {
          transformerStream = RodaCoreFactory
            .getConfigurationFileAsStream("crosswalks/ingest/" + lowerCaseMetadataType + ".xslt");
        }
      }

      // get xslt from filename
      if (transformerStream == null) {
        String filename = FilenameUtils.removeExtension(binary.getStoragePath().getName());
        if (filename != null) {
          filename = filename.toLowerCase();
          transformerStream = RodaCoreFactory.getConfigurationFileAsStream("crosswalks/ingest/" + filename + ".xslt");
        }
      }
      // fallback
      if (transformerStream == null) {
        transformerStream = RodaCoreFactory.getConfigurationFileAsStream("crosswalks/ingest/" + "plain.xslt");
      }

      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);

      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
      RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
      descMetadataReader.close();

      XMLLoader loader = new XMLLoader();
      LOGGER.trace("Transformed desc. metadata:\n{}", transformerResult);
      CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

      boolean parsing = true;
      doc = null;
      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT) {
          String currTag = parser.getLocalName();
          if ("doc".equals(currTag)) {
            doc = loader.readDoc(parser);
          }
        }

      }
      transformationResult.close();

    } catch (IOException | TransformerException | XMLStreamException |

    FactoryConfigurationError e)

    {
      throw new GenericException(
        "Could not process descriptive metadata binary " + binary.getStoragePath() + " using xslt " + xsltFilename, e);
    }
    return validateDescriptiveMetadataFields(doc);
  }

  private static SolrInputDocument validateDescriptiveMetadataFields(SolrInputDocument doc) {
    SimpleDateFormat df = new SimpleDateFormat(RodaConstants.SOLRDATEFORMAT);
    if (doc.get(RodaConstants.AIP_DATE_INITIAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_INITIAL).getValue();
      if (value instanceof String) {
        try {
          Date d = df.parse((String) value);
          doc.setField(RodaConstants.AIP_DATE_INITIAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_INITIAL);
          doc.setField(RodaConstants.AIP_DATE_INITIAL + "_txt", value);
        }
      }
    }
    if (doc.get(RodaConstants.AIP_DATE_FINAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_FINAL).getValue();
      if (value instanceof String) {
        try {
          Date d = df.parse((String) value);
          doc.setField(RodaConstants.AIP_DATE_FINAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_FINAL);
          doc.setField(RodaConstants.AIP_DATE_FINAL + "_txt", value);
        }
      }
    }
    return doc;
  }

  public static String parseFilter(Filter filter) throws RequestNotValidException {
    StringBuilder ret = new StringBuilder();

    if (filter == null || filter.getParameters().isEmpty()) {
      ret.append("*:*");
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        parseFilterParameter(ret, parameter);
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.trace("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter)
    throws RequestNotValidException {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
      appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, true);
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
      appendValuesUsingOROperator(ret, param.getName(), param.getValues());
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", true);
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
      appendANDOperator(ret, true);
      ret.append("(*:* NOT " + param.getName() + ":*)");
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
        processToDate(param.getToValue(), param.getGranularity(), false));
    } else if (parameter instanceof DateIntervalFilterParameter) {
      DateIntervalFilterParameter param = (DateIntervalFilterParameter) parameter;
      appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
        param.getGranularity());
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter param = (LongRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue());
    } else {
      LOGGER.error("Unsupported filter parameter class: {}", parameter.getClass().getName());
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
    }
  }

  private static String processFromDate(Date fromValue) {
    final String ret;

    if (fromValue != null) {
      return DateUtil.getThreadLocalDateFormat().format(fromValue);
    } else {
      ret = "*";
    }

    return ret;
  }

  private static String processToDate(Date toValue, DateGranularity granularity) {
    return processToDate(toValue, granularity, true);
  }

  private static String processToDate(Date toValue, DateGranularity granularity, boolean returnStartOnNull) {
    final String ret;
    StringBuilder sb = new StringBuilder();
    if (toValue != null) {
      sb.append(DateUtil.getThreadLocalDateFormat().format(toValue));
      switch (granularity) {
        case YEAR:
          sb.append("+1YEAR-1MILLISECOND");
          break;
        case MONTH:
          sb.append("+1MONTH-1MILLISECOND");
          break;
        case DAY:
          sb.append("+1DAY-1MILLISECOND");
          break;
        case HOUR:
          sb.append("+1HOUR-1MILLISECOND");
          break;
        case MINUTE:
          sb.append("+1MINUTE-1MILLISECOND");
          break;
        case SECOND:
          sb.append("+1SECOND-1MILLISECOND");
          break;
        default:
          // do nothing
          break;
      }
      ret = sb.toString();
    } else {
      ret = returnStartOnNull ? "*" : null;
    }
    return ret;
  }

  private static void appendRangeInterval(StringBuilder ret, String fromKey, String toKey, Date fromValue, Date toValue,
    DateGranularity granularity) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);
      ret.append("(");

      ret.append(fromKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]").append(" OR ");

      ret.append(toKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]");

      if (fromValue != null && toValue != null) {
        ret.append(" OR ").append("(").append(fromKey).append(":[* TO ").append(processToDate(fromValue, granularity))
          .append("]");
        ret.append(" AND ").append(toKey).append(":[").append(processFromDate(toValue)).append(" TO *]").append(")");
      }

      ret.append(")");
    }
  }

  private static <T extends Serializable, T1 extends Serializable> void appendRange(StringBuilder ret, String key,
    Class<T> fromClass, T fromValue, Class<T1> toClass, T1 toValue) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);

      ret.append("(").append(key).append(":[");
      generateRangeValue(ret, fromClass, fromValue);
      ret.append(" TO ");
      generateRangeValue(ret, toClass, toValue);
      ret.append("])");
    }
  }

  private static <T extends Serializable> void generateRangeValue(StringBuilder ret, Class<T> valueClass, T value) {
    if (value != null) {
      if (valueClass.equals(Date.class)) {
        String date = DateUtil.getThreadLocalDateFormat().format(Date.class.cast(value));
        LOGGER.trace("Appending date value \"{}\" to range", date);
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append(Long.class.cast(value));
      } else if (valueClass.equals(String.class)) {
        ret.append(String.class.cast(value));
      } else {
        LOGGER.error("Cannot process range of the type {}", valueClass);
      }
    } else {
      ret.append("*");
    }
  }

  public static List<SortClause> parseSorter(Sorter sorter) {
    List<SortClause> ret = new ArrayList<SortClause>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SortClause(sortParameter.getName(), sortParameter.isDescending() ? ORDER.desc : ORDER.asc));
      }
    }
    return ret;
  }

  private static void parseAndConfigureFacets(Facets facets, SolrQuery query) {
    if (facets != null) {
      query.setFacetSort(FacetParams.FACET_SORT_INDEX);
      if (!"".equals(facets.getQuery())) {
        query.addFacetQuery(facets.getQuery());
      }
      StringBuilder filterQuery = new StringBuilder();
      for (Entry<String, FacetParameter> parameter : facets.getParameters().entrySet()) {
        FacetParameter facetParameter = parameter.getValue();

        if (facetParameter instanceof SimpleFacetParameter) {
          setQueryFacetParameter(query, (SimpleFacetParameter) facetParameter);
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(),
            ((SimpleFacetParameter) facetParameter).getValues());
        } else if (facetParameter instanceof RangeFacetParameter) {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        } else {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        }
      }
      if (filterQuery.length() > 0) {
        query.addFilterQuery(filterQuery.toString());
        LOGGER.trace("Query after defining facets: " + query.toString());
      }
    }
  }

  private static void setQueryFacetParameter(SolrQuery query, SimpleFacetParameter facetParameter) {
    query.addFacetField(facetParameter.getName());
    if (facetParameter.getMinCount() != FacetParameter.DEFAULT_MIN_COUNT) {
      query.add(String.format("f.%s.facet.mincount", facetParameter.getName()),
        String.valueOf(facetParameter.getMinCount()));
    }
    if (facetParameter.getLimit() != SimpleFacetParameter.DEFAULT_LIMIT) {
      query.add(String.format("f.%s.facet.limit", facetParameter.getName()), String.valueOf(facetParameter.getLimit()));
    }
  }

  private static void appendANDOperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" AND ");
    }
  }

  private static void appendOROperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" OR ");
    }
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values) {
    if (!values.isEmpty()) {
      appendANDOperator(ret, true);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values,
    boolean prependWithOrIfNeeded) {
    if (!values.isEmpty()) {
      if (prependWithOrIfNeeded) {
        appendOROperator(ret, true);
      } else {
        appendANDOperator(ret, true);
      }

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    ret.append("(").append(key).append(": ");
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(value.replaceAll("(\")", "\\\\$1"));
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(")");
  }

  private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (StringUtils.isBlank(value)) {
      appendExactMatch(ret, key, "*", false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (value.matches("^\".+\"$")) {
      appendExactMatch(ret, key, value.substring(1, value.length() - 1), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      appendWhiteSpaceTokenizedString(ret, key, value, operator);
    }
  }

  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator) {
    appendANDOperator(ret, true);

    String[] split = value.trim().split("\\s+");
    ret.append("(");
    for (int i = 0; i < split.length; i++) {
      if (i != 0 && operator != null) {
        ret.append(" " + operator + " ");
      }
      if (split[i].matches("(AND|OR|NOT)")) {
        ret.append(key).append(": \"").append(split[i]).append("\"");
      } else {
        ret.append(key).append(": (").append(escapeSolrSpecialChars(split[i])).append(")");
      }
    }
    ret.append(")");
  }

  /**
   * Method that knows how to escape characters for Solr
   * <p>
   * <code>+ - && || ! ( ) { } [ ] ^ " ~ * ? : \</code>
   * </p>
   * <p>
   * Note: chars <code>'-', '"' and '*'</code> are not being escaped on purpose
   * </p>
   * 
   * @return a string with special characters escaped
   */
  // FIXME perhaps && and || are not being properly escaped: see how to do it
  public static String escapeSolrSpecialChars(String string) {
    return string.replaceAll("([+&|!(){}\\[\\]\\^\\\\~?:\"])", "\\\\$1");
  }

  private static List<String> objectToListString(Object object) {
    List<String> ret;
    if (object == null) {
      ret = new ArrayList<String>();
    } else if (object instanceof String) {
      List<String> l = new ArrayList<String>();
      l.add((String) object);
      return l;
    } else if (object instanceof List<?>) {
      List<?> l = (List<?>) object;
      ret = new ArrayList<String>();
      for (Object o : l) {
        ret.add(o.toString());
      }
    } else {
      LOGGER.error("Could not convert Solr object to List<String> ({})", object.getClass().getName());
      ret = new ArrayList<String>();
    }
    return ret;
  }

  public static Integer objectToInteger(Object object, Integer defaultValue) {
    Integer ret;
    if (object instanceof Integer) {
      ret = (Integer) object;
    } else if (object instanceof String) {
      try {
        ret = Integer.parseInt((String) object);
      } catch (NumberFormatException e) {
        LOGGER.error("Could not convert Solr object to integer", e);
        ret = defaultValue;
      }
    } else {
      LOGGER.error("Could not convert Solr object to integer ({})", object.getClass().getName());
      ret = defaultValue;
    }
    return ret;
  }

  public static Long objectToLong(Object object, Long defaultValue) {
    Long ret = defaultValue;
    if (object != null) {
      if (object instanceof Long) {
        ret = (Long) object;
      } else if (object instanceof String) {
        try {
          ret = Long.parseLong((String) object);
        } catch (NumberFormatException e) {
          LOGGER.error("Could not convert Solr object to long", e);
          ret = defaultValue;
        }
      } else {
        LOGGER.error("Could not convert Solr object to long ({})", object.getClass().getName());
        ret = defaultValue;
      }
    }
    return ret;
  }

  private static Float objectToFloat(Object object) {
    Float ret;
    if (object instanceof Float) {
      ret = (Float) object;
    } else if (object instanceof String) {
      try {
        ret = Float.parseFloat((String) object);
      } catch (NumberFormatException e) {
        LOGGER.error("Could not convert Solr object to float", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to float ({})", object.getClass().getName());
      ret = null;
    }
    return ret;
  }

  private static Date objectToDate(Object object) {
    Date ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Date) {
      ret = (Date) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = RodaUtils.parseDate((String) object);
      } catch (ParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  private static Boolean objectToBoolean(Object object) {
    return objectToBoolean(object, null);
  }

  private static Boolean objectToBoolean(Object object, Boolean defaultValue) {
    Boolean ret;
    if (object == null) {
      ret = defaultValue;
    } else if (object instanceof Boolean) {
      ret = (Boolean) object;
    } else if (object instanceof String) {
      ret = Boolean.parseBoolean((String) object);
    } else {
      LOGGER.error("Could not convert Solr object to Boolean ({})", object.getClass().getName());
      ret = null;
    }
    return ret;
  }

  private static String objectToString(Object object) {
    String ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof String) {
      ret = (String) object;
    } else {
      LOGGER.warn("Could not convert Solr object to string, unsupported class: {}", object.getClass().getName());
      ret = object.toString();
    }
    return ret;
  }

  private static <T extends IsIndexed> String getIndexName(Class<T> resultClass) throws GenericException {
    String indexName;
    if (resultClass.equals(AIP.class)) {
      indexName = RodaConstants.INDEX_AIP;
    } else if (resultClass.equals(IndexedAIP.class)) {
      indexName = RodaConstants.INDEX_AIP;
    } else if (resultClass.equals(Representation.class)) {
      indexName = RodaConstants.INDEX_REPRESENTATION;
    } else if (resultClass.equals(IndexedRepresentation.class)) {
      indexName = RodaConstants.INDEX_REPRESENTATION;
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      indexName = RodaConstants.INDEX_PRESERVATION_EVENTS;
    } else if (resultClass.equals(IndexedPreservationAgent.class)) {
      indexName = RodaConstants.INDEX_PRESERVATION_AGENTS;
    } else if (resultClass.equals(LogEntry.class)) {
      indexName = RodaConstants.INDEX_ACTION_LOG;
    } else if (resultClass.equals(Report.class)) {
      indexName = RodaConstants.INDEX_JOB_REPORT;
    } else if (resultClass.equals(User.class)) {
      LOGGER.warn("Use {} instead of {}", RODAMember.class.getCanonicalName(), User.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(Group.class)) {
      LOGGER.warn("Use {} instead of {}", RODAMember.class.getCanonicalName(), User.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(RODAMember.class)) {
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(TransferredResource.class)) {
      indexName = RodaConstants.INDEX_TRANSFERRED_RESOURCE;
    } else if (resultClass.equals(Job.class)) {
      indexName = RodaConstants.INDEX_JOB;
    } else if (resultClass.equals(IndexedFile.class)) {
      indexName = RodaConstants.INDEX_FILE;
    } else if (resultClass.equals(Risk.class)) {
      indexName = RodaConstants.INDEX_RISK;
    } else if (resultClass.equals(Agent.class)) {
      indexName = RodaConstants.INDEX_AGENT;
    } else if (resultClass.equals(Format.class)) {
      indexName = RodaConstants.INDEX_FORMAT;
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return indexName;
  }

  private static void commit(SolrClient index, String... collections) {

    boolean waitFlush = false;
    boolean waitSearcher = true;
    boolean softCommit = true;

    for (String collection : collections) {
      try {
        index.commit(collection, waitFlush, waitSearcher, softCommit);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error commiting into collection: " + collection, e);
      }
    }
  }

  public static void commit(SolrClient index, List<Class<? extends IsIndexed>> resultClasses) throws GenericException {
    List<String> collections = new ArrayList<>();
    for (Class<? extends IsIndexed> resultClass : resultClasses) {
      collections.add(getIndexName(resultClass));
    }

    commit(index, collections.toArray(new String[] {}));
  }

  @SafeVarargs
  public static <T extends IsIndexed> void commit(SolrClient index, Class<? extends IsIndexed>... resultClasses)
    throws GenericException {
    commit(index, Arrays.asList(resultClasses));
  }

  private static <T> boolean hasPermissionFilters(Class<T> resultClass) throws GenericException {
    return resultClass.equals(AIP.class) || resultClass.equals(IndexedAIP.class)
      || resultClass.equals(Representation.class) || resultClass.equals(IndexedRepresentation.class)
      || resultClass.equals(IndexedFile.class) || resultClass.equals(IndexedPreservationEvent.class);
  }

  private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc) throws GenericException {
    T ret;
    if (resultClass.equals(IndexedAIP.class)) {
      ret = resultClass.cast(solrDocumentToIndexAIP(doc));
    } else if (resultClass.equals(IndexedRepresentation.class) || resultClass.equals(Representation.class)) {
      ret = resultClass.cast(solrDocumentToRepresentation(doc));
    } else if (resultClass.equals(LogEntry.class)) {
      ret = resultClass.cast(solrDocumentToLogEntry(doc));
    } else if (resultClass.equals(Report.class)) {
      ret = resultClass.cast(solrDocumentToJobReport(doc));
    } else if (resultClass.equals(RODAMember.class) || resultClass.equals(User.class)
      || resultClass.equals(Group.class)) {
      ret = resultClass.cast(solrDocumentToRodaMember(doc));
    } else if (resultClass.equals(TransferredResource.class)) {
      ret = resultClass.cast(solrDocumentToTransferredResource(doc));
    } else if (resultClass.equals(Job.class)) {
      ret = resultClass.cast(solrDocumentToJob(doc));
    } else if (resultClass.equals(Risk.class)) {
      ret = resultClass.cast(solrDocumentToRisk(doc));
    } else if (resultClass.equals(Agent.class)) {
      ret = resultClass.cast(solrDocumentToAgent(doc));
    } else if (resultClass.equals(Format.class)) {
      ret = resultClass.cast(solrDocumentToFormat(doc));
    } else if (resultClass.equals(IndexedFile.class)) {
      ret = resultClass.cast(solrDocumentToIndexedFile(doc));
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      ret = resultClass.cast(solrDocumentToIndexedPreservationEvent(doc));
    } else if (resultClass.equals(IndexedPreservationAgent.class)) {
      ret = resultClass.cast(solrDocumentToIndexedPreservationAgent(doc));
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return ret;
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, Class<T> classToRetrieve, String id)
    throws NotFoundException, GenericException {
    T ret;
    try {
      SolrDocument doc = index.getById(getIndexName(classToRetrieve), id);
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc);
      } else {
        throw new NotFoundException("Could not find document " + id);
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve AIP from index", e);
    }
    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, sorter, sublist, null);
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, RodaUser user, boolean showInactive)
      throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);
    if (hasPermissionFilters(classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, showInactive));
    }

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  private static String getFilterQueries(RodaUser user, boolean showInactive) {

    StringBuilder fq = new StringBuilder();

    // TODO find a better way to define admin super powers
    if (user != null && !user.getName().equals("admin")) {
      String usersKey = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + PermissionType.READ;
      appendExactMatch(fq, usersKey, user.getId(), true, false);

      String groupsKey = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + PermissionType.READ;
      appendValuesUsingOROperator(fq, groupsKey, new ArrayList<>(user.getAllGroups()), true);
    }

    if (!showInactive) {
      appendExactMatch(fq, RodaConstants.ACTIVE, Boolean.TRUE.toString(), true, true);
    }

    return fq.toString();
  }

  public static <T extends IsIndexed> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
    throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  public static <T extends IsIndexed> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter,
    RodaUser user, boolean showInactive) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0), null, user, showInactive).getTotalCount();
  }

  public static IndexedAIP solrDocumentToIndexAIP(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.AIP_ID));
    final Boolean active = objectToBoolean(doc.get(RodaConstants.ACTIVE));
    final AIPState state = active ? AIPState.ACTIVE : AIPState.INACTIVE;
    final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID));
    final List<String> levels = objectToListString(doc.get(RodaConstants.AIP_LEVEL));
    final List<String> titles = objectToListString(doc.get(RodaConstants.AIP_TITLE));
    final List<String> descriptions = objectToListString(doc.get(RodaConstants.AIP_DESCRIPTION));
    final Date dateInitial = objectToDate(doc.get(RodaConstants.AIP_DATE_INITIAL));
    final Date dateFinal = objectToDate(doc.get(RodaConstants.AIP_DATE_FINAL));
    Permissions permissions = getPermissions(doc);
    final String level = levels.isEmpty() ? null : levels.get(0);
    final String title = titles.isEmpty() ? null : titles.get(0);
    final String description = descriptions.isEmpty() ? null : descriptions.get(0);

    // FIXME 20160318 hsilva: 0??? for real???
    final int childrenCount = 0;

    return new IndexedAIP(id, state, level, title, dateInitial, dateFinal, description, parentId, childrenCount,
      permissions);
  }

  public static SolrInputDocument aipToSolrInputDocument(AIP aip, ModelService model, boolean safemode)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument ret = new SolrInputDocument();

    ret.addField(RodaConstants.AIP_ID, aip.getId());
    ret.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
    ret.addField(RodaConstants.ACTIVE, aip.isActive());

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());

    ret.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, descriptiveMetadataIds);

    List<String> representationIds = aip.getRepresentations().stream().map(r -> r.getId()).collect(Collectors.toList());
    ret.addField(RodaConstants.AIP_REPRESENTATION_ID, representationIds);
    ret.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, !representationIds.isEmpty());

    setPermissions(aip.getPermissions(), ret);

    if (!safemode) {
      // guarding against repeated fields
      Set<String> usedNonRepeatableFields = new HashSet<>();

      for (DescriptiveMetadata metadata : aip.getDescriptiveMetadata()) {
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aip.getId(), metadata.getId());
        Binary binary = model.getStorage().getBinary(storagePath);
        try {
          SolrInputDocument fields = getDescriptiveMetataFields(binary, metadata.getType(), metadata.getVersion());
          for (SolrInputField field : fields) {
            if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
              boolean added = usedNonRepeatableFields.add(field.getName());
              if (added) {
                ret.addField(field.getName(), field.getValue(), field.getBoost());
              }
            } else {
              ret.addField(field.getName(), field.getValue(), field.getBoost());
            }
          }
        } catch (GenericException ise) {
          // TODO index the index errors for later processing
          LOGGER.warn("Error processing descriptive metadata: {}", metadata);
        } catch (Throwable e) {
          LOGGER.error("Error processing descriptive metadata: " + metadata, e);
        }
      }
    }

    return ret;
  }

  public static SolrInputDocument aipActiveFlagUpdateToSolrDocument(AIP aip) {
    return activeFlagUpdateToSolrDocument(RodaConstants.AIP_ID, aip.getId(), aip.isActive());
  }

  public static SolrInputDocument representationActiveFlagUpdateToSolrDocument(Representation representation,
    boolean active) {
    return activeFlagUpdateToSolrDocument(RodaConstants.REPRESENTATION_UUID,
      IdUtils.getRepresentationId(representation.getAipId(), representation.getId()), active);
  }

  public static SolrInputDocument fileActiveFlagUpdateToSolrDocument(File file, boolean active) {
    return activeFlagUpdateToSolrDocument(RodaConstants.FILE_UUID, file.getId(), active);
  }

  public static SolrInputDocument preservationEventActiveFlagUpdateToSolrDocument(String preservationEventID,
    boolean active) {
    return activeFlagUpdateToSolrDocument(RodaConstants.PRESERVATION_EVENT_ID, preservationEventID, active);
  }

  private static SolrInputDocument activeFlagUpdateToSolrDocument(String idField, String idValue, boolean active) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(idField, idValue);
    Map<String, Object> fieldModifier = new HashMap<>(1);
    fieldModifier.put("set", active);
    doc.addField(RodaConstants.ACTIVE, fieldModifier);
    return doc;
  }

  public static SolrInputDocument aipPermissionsUpdateToSolrDocument(AIP aip) {
    return permissionsUpdateToSolrDocument(RodaConstants.AIP_ID, aip.getId(), aip.getPermissions());
  }

  public static SolrInputDocument representationPermissionsUpdateToSolrDocument(Representation representation,
    Permissions permissions) {
    return permissionsUpdateToSolrDocument(RodaConstants.REPRESENTATION_UUID,
      IdUtils.getRepresentationId(representation.getAipId(), representation.getId()), permissions);
  }

  public static SolrInputDocument filePermissionsUpdateToSolrDocument(File file, Permissions permissions) {
    return permissionsUpdateToSolrDocument(RodaConstants.FILE_UUID, file.getId(), permissions);
  }

  public static SolrInputDocument preservationEventPermissionsUpdateToSolrDocument(String preservationEventID,
    Permissions permissions) {
    return permissionsUpdateToSolrDocument(RodaConstants.PRESERVATION_EVENT_ID, preservationEventID, permissions);
  }

  private static SolrInputDocument permissionsUpdateToSolrDocument(String idField, String idValue,
    Permissions permissions) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(idField, idValue);

    for (Entry<PermissionType, Set<String>> entry : permissions.getUsers().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      Map<String, Object> fieldModifier = new HashMap<>(1);
      fieldModifier.put("set", value);
      doc.addField(key, fieldModifier);
    }

    for (Entry<PermissionType, Set<String>> entry : permissions.getGroups().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      Map<String, Object> fieldModifier = new HashMap<>(1);
      fieldModifier.put("set", value);
      doc.addField(key, fieldModifier);
    }

    return doc;
  }

  private static Permissions getPermissions(SolrDocument doc) {

    Permissions permissions = new Permissions();

    Map<PermissionType, Set<String>> userPermissions = new HashMap<>();

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + type;
      Set<String> users = new HashSet<>();
      users.addAll(objectToListString(doc.get(key)));
      userPermissions.put(type, users);
    }

    Map<PermissionType, Set<String>> groupPermissions = new HashMap<>();

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + type;
      Set<String> groups = new HashSet<>();
      groups.addAll(objectToListString(doc.get(key)));
      groupPermissions.put(type, groups);
    }
    
    permissions.setUsers(userPermissions);
    permissions.setGroups(userPermissions);
    
    return permissions;
  }

  private static void setPermissions(Permissions permissions, final SolrInputDocument ret) {

    for (Entry<PermissionType, Set<String>> entry : permissions.getUsers().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());

      ret.addField(key, value);
    }
    for (Entry<PermissionType, Set<String>> entry : permissions.getGroups().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());

      ret.addField(key, value);
    }
  }

  public static IndexedRepresentation solrDocumentToRepresentation(SolrDocument doc) {
    final String uuid = objectToString(doc.get(RodaConstants.REPRESENTATION_UUID));
    final String id = objectToString(doc.get(RodaConstants.REPRESENTATION_ID));
    final String aipId = objectToString(doc.get(RodaConstants.REPRESENTATION_AIP_ID));
    final Boolean original = objectToBoolean(doc.get(RodaConstants.REPRESENTATION_ORIGINAL), Boolean.FALSE);

    final Long sizeInBytes = objectToLong(doc.get(RodaConstants.REPRESENTATION_SIZE_IN_BYTES), 0L);
    final Long totalNumberOfFiles = objectToLong(doc.get(RodaConstants.REPRESENTATION_TOTAL_NUMBER_OF_FILES), 0L);

    return new IndexedRepresentation(uuid, id, aipId, Boolean.TRUE.equals(original), sizeInBytes, totalNumberOfFiles);
  }

  public static SolrInputDocument representationToSolrDocument(AIP aip, Representation rep) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.REPRESENTATION_UUID, IdUtils.getRepresentationId(rep.getAipId(), rep.getId()));
    doc.addField(RodaConstants.REPRESENTATION_ID, rep.getId());
    doc.addField(RodaConstants.REPRESENTATION_AIP_ID, rep.getAipId());
    doc.addField(RodaConstants.REPRESENTATION_ORIGINAL, rep.isOriginal());

    // FIXME calculate storage size and number of files or get it from arguments
    doc.addField(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, 0L);
    doc.addField(RodaConstants.REPRESENTATION_TOTAL_NUMBER_OF_FILES, 0L);

    // indexing active state and permissions
    doc.addField(RodaConstants.ACTIVE, aip.isActive());
    setPermissions(aip.getPermissions(), doc);

    return doc;
  }

  private static LogEntry solrDocumentToLogEntry(SolrDocument doc) {
    final String actionComponent = objectToString(doc.get(RodaConstants.LOG_ACTION_COMPONENT));
    final String actionMethod = objectToString(doc.get(RodaConstants.LOG_ACTION_METHOD));
    final String address = objectToString(doc.get(RodaConstants.LOG_ADDRESS));
    final Date datetime = objectToDate(doc.get(RodaConstants.LOG_DATETIME));
    final long duration = objectToLong(doc.get(RodaConstants.LOG_DURATION), 0L);
    final String id = objectToString(doc.get(RodaConstants.LOG_ID));
    final String parameters = objectToString(doc.get(RodaConstants.LOG_PARAMETERS));
    final String relatedObjectId = objectToString(doc.get(RodaConstants.LOG_RELATED_OBJECT_ID));
    final String username = objectToString(doc.get(RodaConstants.LOG_USERNAME));
    LogEntry entry = new LogEntry();
    entry.setActionComponent(actionComponent);
    entry.setActionMethod(actionMethod);
    entry.setAddress(address);
    entry.setDatetime(datetime);
    entry.setDuration(duration);
    entry.setId(id);
    try {
      entry.setParameters(JsonUtils.getListFromJson(parameters == null ? "" : parameters, LogEntryParameter.class));
    } catch (GenericException e) {
      LOGGER.error("Error parsing log entry parameters", e);
    }

    entry.setRelatedObjectID(relatedObjectId);
    entry.setUsername(username);

    return entry;
  }

  public static SolrInputDocument logEntryToSolrDocument(LogEntry logEntry) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.LOG_ACTION_COMPONENT, logEntry.getActionComponent());
    doc.addField(RodaConstants.LOG_ACTION_METHOD, logEntry.getActionMethod());
    doc.addField(RodaConstants.LOG_ADDRESS, logEntry.getAddress());
    doc.addField(RodaConstants.LOG_DATETIME, logEntry.getDatetime());
    doc.addField(RodaConstants.LOG_DURATION, logEntry.getDuration());
    doc.addField(RodaConstants.LOG_ID, logEntry.getId());
    doc.addField(RodaConstants.LOG_PARAMETERS, JsonUtils.getJsonFromObject(logEntry.getParameters()));
    doc.addField(RodaConstants.LOG_RELATED_OBJECT_ID, logEntry.getRelatedObjectID());
    doc.addField(RodaConstants.LOG_USERNAME, logEntry.getUsername());
    return doc;
  }

  public static SolrInputDocument rodaMemberToSolrDocument(RODAMember member) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, member.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, member.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, member.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, member.getName());
    if (member.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(member.getAllGroups()));
    }
    if (member.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(member.getAllRoles()));
    }

    return doc;
  }

  private static RODAMember solrDocumentToRodaMember(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.MEMBERS_ID));
    final boolean isActive = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE));
    final boolean isUser = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER));
    final String name = objectToString(doc.get(RodaConstants.MEMBERS_NAME));
    final Set<String> groups = new HashSet<String>();
    List<String> possibleGroups = objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS_ALL));
    groups.addAll(possibleGroups);
    final Set<String> roles = new HashSet<String>();
    List<String> possibleRoles = objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL));
    roles.addAll(possibleRoles);
    if (isUser) {
      RodaUser user = new RodaUser();
      user.setId(id);
      user.setActive(isActive);
      user.setAllGroups(groups);
      user.setAllRoles(roles);
      user.setActive(isActive);
      user.setName(name);
      return user;
    } else {
      RodaGroup group = new RodaGroup();
      group.setId(id);
      group.setActive(isActive);
      group.setAllGroups(groups);
      group.setAllRoles(roles);
      group.setActive(isActive);
      group.setName(name);
      return group;
    }
  }

  public static SolrInputDocument userToSolrDocument(User user) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, user.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, user.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, user.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, user.getName());
    if (user.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(user.getAllGroups()));
    }
    if (user.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(user.getAllRoles()));
    }

    return doc;
  }

  public static SolrInputDocument groupToSolrDocument(Group group) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, group.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, group.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, group.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, group.getName());
    if (group.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(group.getAllGroups()));
    }
    if (group.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(group.getAllRoles()));
    }

    return doc;
  }

  /**
   * WARNING: this should only be used to debug/tests only
   * 
   * @return
   * @throws IOException
   * @throws SolrServerException
   */
  public static QueryResponse executeSolrQuery(SolrClient index, String collection, String solrQueryString)
    throws SolrServerException, IOException {
    LOGGER.trace("query string: " + solrQueryString);
    SolrQuery query = new SolrQuery();
    for (String string : solrQueryString.split("&")) {
      String[] split = string.split("=");
      query.add(split[0], split[1]);
    }
    LOGGER.trace("executeSolrQuery: " + query);
    return index.query(collection, query);
  }

  private static IndexedPreservationEvent solrDocumentToIndexedPreservationEvent(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_ID));
    final String aipID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_AIP_ID));
    final String representationID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_ID));
    final String fileID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_FILE_ID));
    final Date eventDateTime = objectToDate(doc.get(RodaConstants.PRESERVATION_EVENT_DATETIME));
    final String eventDetail = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_DETAIL));
    final String eventType = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_TYPE));
    final String eventOutcome = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME));
    final String eventOutcomeDetailExtension = objectToString(
      doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME_DETAIL_EXTENSION));

    final String eventOutcomeDetailNote = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME_DETAIL_NOTE));
    final List<String> agents = objectToListString(doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER));
    final List<String> outcomes = objectToListString(
      doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER));
    final List<String> sources = objectToListString(
      doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER));
    IndexedPreservationEvent ipe = new IndexedPreservationEvent();
    ipe.setId(id);
    ipe.setAipId(aipID);
    ipe.setRepresentationId(representationID);
    ipe.setFileId(fileID);
    ipe.setEventDateTime(eventDateTime);
    ipe.setEventDetail(eventDetail);
    ipe.setEventType(eventType);
    ipe.setEventOutcome(eventOutcome);
    ipe.setEventOutcomeDetailExtension(eventOutcomeDetailExtension);
    ipe.setEventOutcomeDetailNote(eventOutcomeDetailNote);
    try {
      List<LinkingIdentifier> ids = new ArrayList<LinkingIdentifier>();
      for (String source : sources) {
        ids.add(JsonUtils.getObjectFromJson(source, LinkingIdentifier.class));
      }
      ipe.setSourcesObjectIds(ids);
    } catch (Throwable e) {
      LOGGER.error("Error setting event linking source: " + e.getMessage(), e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<LinkingIdentifier>();
      for (String outcome : outcomes) {
        ids.add(JsonUtils.getObjectFromJson(outcome, LinkingIdentifier.class));
      }
      ipe.setOutcomeObjectIds(ids);
    } catch (Throwable e) {
      LOGGER.error("Error setting event linking outcome: " + e.getMessage(), e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<LinkingIdentifier>();
      for (String agent : agents) {
        ids.add(JsonUtils.getObjectFromJson(agent, LinkingIdentifier.class));
      }
      ipe.setLinkingAgentIds(ids);
    } catch (Throwable e) {
      LOGGER.error("Error setting event linking agents: " + e.getMessage(), e);
    }
    return ipe;
  }

  private static IndexedPreservationAgent solrDocumentToIndexedPreservationAgent(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_ID));
    final String name = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NAME));
    final String type = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_TYPE));
    final String extension = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_EXTENSION));
    final String version = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_VERSION));
    final String note = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NOTE));
    final List<String> roles = objectToListString(doc.get(RodaConstants.PRESERVATION_AGENT_ROLES));
    IndexedPreservationAgent ipa = new IndexedPreservationAgent();
    ipa.setId(id);
    ipa.setName(name);
    ipa.setType(type);
    ipa.setExtension(extension);
    ipa.setVersion(version);
    ipa.setNote(note);
    ipa.setRoles(roles);
    return ipa;
  }

  public static SolrInputDocument premisToSolr(PreservationMetadataType preservationMetadataType, AIP aip,
    String representationID, String fileID, Binary binary) throws GenericException {
    SolrInputDocument doc;
    InputStream inputStream;
    try {
      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);

      // FIXME 20160314 hsilva: replace hardcoded path by constant or method (to
      // support both filesystem in win/linux and classpath)
      InputStream transformerStream = RodaCoreFactory
        .getConfigurationFileAsStream("crosswalks/ingest/other/premis.xslt");
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      if (aip != null) {
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId());
      }
      if (representationID != null) {
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_ID, representationID);
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID,
          IdUtils.getRepresentationId(aip.getId(), representationID));
      }
      if (fileID != null) {
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_FILE_ID, fileID);
      }
      RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
      descMetadataReader.close();

      XMLLoader loader = new XMLLoader();
      LOGGER.trace("Transformed premis metadata:\n{}", transformerResult);
      CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

      boolean parsing = true;
      doc = null;
      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT) {
          String currTag = parser.getLocalName();
          if ("doc".equals(currTag)) {
            doc = loader.readDoc(parser);
          }
        }

      }
      transformationResult.close();

    } catch (IOException | TransformerException | XMLStreamException | FactoryConfigurationError e) {
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath()
        + " using xslt " + "crosswalks/ingest/other/premis.xslt", e);
    }

    if (preservationMetadataType == PreservationMetadataType.EVENT) {
      try {
        List<LinkingIdentifier> agents = PremisV3Utils.extractAgentsFromEvent(binary);
        for (LinkingIdentifier id : agents) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER, JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking agent field: {}", e.getMessage());
      }
      try {
        List<LinkingIdentifier> sources = PremisV3Utils.extractObjectFromEvent(binary);
        for (LinkingIdentifier id : sources) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking source field: {}", e.getMessage());
      }
      try {
        List<LinkingIdentifier> outcomes = PremisV3Utils.extractObjectFromEvent(binary);
        for (LinkingIdentifier id : outcomes) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking outcome field: {}", e.getMessage());
      }

      // indexing active state and permissions
      if (aip != null) {
        doc.addField(RodaConstants.ACTIVE, aip.isActive());
        setPermissions(aip.getPermissions(), doc);
      }
    }
    return doc;
  }

  private static TransferredResource solrDocumentToTransferredResource(SolrDocument doc) {
    TransferredResource tr = new TransferredResource();
    String id = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ID));
    String uuid = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_UUID));
    String fullPath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH));
    String parentId = null;
    if (doc.containsKey(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)) {
      parentId = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));
    }
    String relativePath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH));

    Date d = objectToDate(doc.get(RodaConstants.TRANSFERRED_RESOURCE_DATE));
    if (d == null) {
      LOGGER.warn("Error parsing transferred resource date: {}. Setting date to current date.");
      d = new Date();
    }

    boolean isFile = objectToBoolean(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ISFILE));
    long size = objectToLong(doc.get(RodaConstants.TRANSFERRED_RESOURCE_SIZE), 0L);
    String name = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_NAME));

    List<String> ancestorsPath = objectToListString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS));

    tr.setId(id);
    tr.setUUID(uuid);
    tr.setCreationDate(d);
    tr.setFullPath(fullPath);
    tr.setName(name);
    tr.setRelativePath(relativePath);
    tr.setSize(size);
    tr.setParentId(parentId);
    tr.setFile(isFile);
    tr.setAncestorsPaths(ancestorsPath);
    return tr;
  }

  public static SolrInputDocument transferredResourceToSolrDocument(TransferredResource resource) throws IOException {
    SolrInputDocument transferredResource = new SolrInputDocument();

    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_UUID,
      UUID.nameUUIDFromBytes(resource.getId().getBytes()).toString());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ID, resource.getId());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH, resource.getFullPath());
    if (resource.getParentId() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, resource.getParentId());
    }
    if (resource.getRelativePath() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, resource.getRelativePath());
    }
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_DATE, resource.getCreationDate());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, resource.isFile());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_SIZE, resource.getSize());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_NAME, resource.getName());
    if (resource.getAncestorsPaths() != null && !resource.getAncestorsPaths().isEmpty()) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, resource.getAncestorsPaths());
    }
    return transferredResource;
  }

  public static long getSizePath(Path startPath) throws IOException {
    final AtomicLong size = new AtomicLong(0);
    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        size.addAndGet(attrs.size());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }
    });
    return size.get();
  }

  public static SolrInputDocument jobToSolrDocument(Job job) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.JOB_ID, job.getId());
    doc.addField(RodaConstants.JOB_NAME, job.getName());
    doc.addField(RodaConstants.JOB_USERNAME, job.getUsername());
    doc.addField(RodaConstants.JOB_START_DATE, job.getStartDate());
    doc.addField(RodaConstants.JOB_END_DATE, job.getEndDate());
    doc.addField(RodaConstants.JOB_STATE, job.getState());
    doc.addField(RodaConstants.JOB_COMPLETION_PERCENTAGE, job.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_PLUGIN_TYPE, job.getPluginType());
    doc.addField(RodaConstants.JOB_PLUGIN, job.getPlugin());
    doc.addField(RodaConstants.JOB_PLUGIN_PARAMETERS, JsonUtils.getJsonFromObject(job.getPluginParameters()));
    doc.addField(RodaConstants.JOB_ORCHESTRATOR_METHOD, job.getOrchestratorMethod());
    doc.addField(RodaConstants.JOB_OBJECT_IDS, job.getObjectIds());

    return doc;
  }

  public static Job solrDocumentToJob(SolrDocument doc) {
    Job job = new Job();

    job.setId(objectToString(doc.get(RodaConstants.JOB_ID)));
    job.setName(objectToString(doc.get(RodaConstants.JOB_NAME)));
    job.setUsername(objectToString(doc.get(RodaConstants.JOB_USERNAME)));
    job.setStartDate(objectToDate(doc.get(RodaConstants.JOB_START_DATE)));
    job.setEndDate(objectToDate(doc.get(RodaConstants.JOB_END_DATE)));
    job.setState(JOB_STATE.valueOf(objectToString(doc.get(RodaConstants.JOB_STATE))));
    job.setCompletionPercentage(objectToInteger(doc.get(RodaConstants.JOB_COMPLETION_PERCENTAGE), 0));
    job.setPluginType(PluginType.valueOf(objectToString(doc.get(RodaConstants.JOB_PLUGIN_TYPE))));
    job.setPlugin(objectToString(doc.get(RodaConstants.JOB_PLUGIN)));
    job.setPluginParameters(JsonUtils.getMapFromJson(objectToString(doc.get(RodaConstants.JOB_PLUGIN_PARAMETERS))));
    job.setOrchestratorMethod(
      ORCHESTRATOR_METHOD.valueOf(objectToString(doc.get(RodaConstants.JOB_ORCHESTRATOR_METHOD))));
    job.setObjectIds(objectToListString(doc.get(RodaConstants.JOB_OBJECT_IDS)));

    return job;
  }

  public static SolrInputDocument riskToSolrDocument(Risk risk) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.RISK_ID, risk.getId());
    doc.addField(RodaConstants.RISK_NAME, risk.getName());
    doc.addField(RodaConstants.RISK_DESCRIPTION, risk.getDescription());
    doc.addField(RodaConstants.RISK_IDENTIFIED_ON, risk.getIdentifiedOn());
    doc.addField(RodaConstants.RISK_IDENTIFIED_BY, risk.getIdentifiedBy());
    doc.addField(RodaConstants.RISK_CATEGORY, risk.getCategory());
    doc.addField(RodaConstants.RISK_NOTES, risk.getNotes());

    doc.addField(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, risk.getPreMitigationProbability());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_IMPACT, risk.getPreMitigationImpact());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_SEVERITY, risk.getPreMitigationSeverity());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_NOTES, risk.getPreMitigationNotes());

    doc.addField(RodaConstants.RISK_POS_MITIGATION_PROBABILITY, risk.getPosMitigationProbability());
    doc.addField(RodaConstants.RISK_POS_MITIGATION_IMPACT, risk.getPosMitigationImpact());
    doc.addField(RodaConstants.RISK_POS_MITIGATION_SEVERITY, risk.getPosMitigationSeverity());
    doc.addField(RodaConstants.RISK_POS_MITIGATION_NOTES, risk.getPosMitigationNotes());

    doc.addField(RodaConstants.RISK_MITIGATION_STRATEGY, risk.getMitigationStrategy());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER_TYPE, risk.getMitigationOwnerType());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER, risk.getMitigationOwner());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
      risk.getMitigationRelatedEventIdentifierType());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE,
      risk.getMitigationRelatedEventIdentifierValue());

    doc.addField(RodaConstants.RISK_AFFECTED_OBJECTS, JsonUtils.getJsonFromObject(risk.getAffectedObjects()));

    return doc;
  }

  public static Risk solrDocumentToRisk(SolrDocument doc) {
    Risk risk = new Risk();

    risk.setId(objectToString(doc.get(RodaConstants.RISK_ID)));
    risk.setName(objectToString(doc.get(RodaConstants.RISK_NAME)));
    risk.setDescription(objectToString(doc.get(RodaConstants.RISK_DESCRIPTION)));
    risk.setIdentifiedOn(objectToDate(doc.get(RodaConstants.RISK_IDENTIFIED_ON)));
    risk.setIdentifiedBy(objectToString(doc.get(RodaConstants.RISK_IDENTIFIED_BY)));
    risk.setCategory(objectToString(doc.get(RodaConstants.RISK_CATEGORY)));
    risk.setNotes(objectToString(doc.get(RodaConstants.RISK_NOTES)));

    risk.setPreMitigationProbability(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY), 0));
    risk.setPreMitigationImpact(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_IMPACT), 0));
    risk.setPreMitigationSeverity(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_SEVERITY), 0));
    risk.setPreMitigationNotes(objectToString(doc.get(RodaConstants.RISK_PRE_MITIGATION_NOTES)));

    risk.setPosMitigationProbability(objectToInteger(doc.get(RodaConstants.RISK_POS_MITIGATION_PROBABILITY), 0));
    risk.setPosMitigationImpact(objectToInteger(doc.get(RodaConstants.RISK_POS_MITIGATION_IMPACT), 0));
    risk.setPosMitigationSeverity(objectToInteger(doc.get(RodaConstants.RISK_POS_MITIGATION_SEVERITY), 0));
    risk.setPosMitigationNotes(objectToString(doc.get(RodaConstants.RISK_POS_MITIGATION_NOTES)));

    risk.setMitigationStrategy(objectToString(doc.get(RodaConstants.RISK_MITIGATION_STRATEGY)));
    risk.setMitigationOwnerType(objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER_TYPE)));
    risk.setMitigationOwner(objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER)));
    risk.setMitigationRelatedEventIdentifierType(
      objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE)));
    risk.setMitigationRelatedEventIdentifierValue(
      objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE)));

    risk.setAffectedObjects(JsonUtils.getMapFromJson(objectToString(doc.get(RodaConstants.RISK_AFFECTED_OBJECTS))));

    return risk;
  }

  public static SolrInputDocument agentToSolrDocument(Agent agent) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.AGENT_ID, agent.getId());
    doc.addField(RodaConstants.AGENT_NAME, agent.getName());
    doc.addField(RodaConstants.AGENT_TYPE, agent.getType());
    doc.addField(RodaConstants.AGENT_DESCRIPTION, agent.getDescription());
    doc.addField(RodaConstants.AGENT_CATEGORY, agent.getCategory());
    doc.addField(RodaConstants.AGENT_VERSION, agent.getVersion());
    doc.addField(RodaConstants.AGENT_LICENSE, agent.getLicense());
    doc.addField(RodaConstants.AGENT_POPULARITY, agent.getPopularity());
    doc.addField(RodaConstants.AGENT_DEVELOPER, agent.getDeveloper());
    doc.addField(RodaConstants.AGENT_INITIAL_RELEASE, agent.getInitialRelease());
    doc.addField(RodaConstants.AGENT_WEBSITE, agent.getWebsite());
    doc.addField(RodaConstants.AGENT_DOWNLOAD, agent.getDownload());
    doc.addField(RodaConstants.AGENT_PROVENANCE_INFORMATION, agent.getProvenanceInformation());
    doc.addField(RodaConstants.AGENT_PLATFORMS, agent.getPlatforms());
    doc.addField(RodaConstants.AGENT_EXTENSIONS, agent.getExtensions());
    doc.addField(RodaConstants.AGENT_MIMETYPES, agent.getMimetypes());
    doc.addField(RodaConstants.AGENT_PRONOMS, agent.getPronoms());
    doc.addField(RodaConstants.AGENT_UTIS, agent.getUtis());
    doc.addField(RodaConstants.AGENT_FORMAT_IDS, agent.getFormatIds());
    doc.addField(RodaConstants.AGENT_AGENTS_REQUIRED, agent.getAgentsRequired());

    return doc;
  }

  public static Agent solrDocumentToAgent(SolrDocument doc) {
    Agent agent = new Agent();

    agent.setId(objectToString(doc.get(RodaConstants.AGENT_ID)));
    agent.setName(objectToString(doc.get(RodaConstants.AGENT_NAME)));
    agent.setType(objectToString(doc.get(RodaConstants.AGENT_TYPE)));
    agent.setDescription(objectToString(doc.get(RodaConstants.AGENT_DESCRIPTION)));
    agent.setCategory(objectToString(doc.get(RodaConstants.AGENT_CATEGORY)));
    agent.setVersion(objectToString(doc.get(RodaConstants.AGENT_VERSION)));
    agent.setLicense(objectToString(doc.get(RodaConstants.AGENT_LICENSE)));
    agent.setPopularity(objectToInteger(doc.get(RodaConstants.AGENT_POPULARITY), 0));
    agent.setDeveloper(objectToString(doc.get(RodaConstants.AGENT_DEVELOPER)));
    agent.setInitialRelease(objectToDate(doc.get(RodaConstants.AGENT_INITIAL_RELEASE)));
    agent.setWebsite(objectToString(doc.get(RodaConstants.AGENT_WEBSITE)));
    agent.setDownload(objectToString(doc.get(RodaConstants.AGENT_DOWNLOAD)));
    agent.setProvenanceInformation(objectToString(doc.get(RodaConstants.AGENT_PROVENANCE_INFORMATION)));
    agent.setPlatforms(objectToListString(doc.get(RodaConstants.AGENT_PLATFORMS)));
    agent.setExtensions(objectToListString(doc.get(RodaConstants.AGENT_EXTENSIONS)));
    agent.setMimetypes(objectToListString(doc.get(RodaConstants.AGENT_MIMETYPES)));
    agent.setPronoms(objectToListString(doc.get(RodaConstants.AGENT_PRONOMS)));
    agent.setUtis(objectToListString(doc.get(RodaConstants.AGENT_UTIS)));
    agent.setFormatIds(objectToListString(doc.get(RodaConstants.AGENT_FORMAT_IDS)));
    agent.setAgentsRequired(objectToListString(doc.get(RodaConstants.AGENT_AGENTS_REQUIRED)));

    return agent;
  }

  public static SolrInputDocument formatToSolrDocument(Format format) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.FORMAT_ID, format.getId());
    doc.addField(RodaConstants.FORMAT_NAME, format.getName());
    doc.addField(RodaConstants.FORMAT_DEFINITION, format.getDefinition());
    doc.addField(RodaConstants.FORMAT_CATEGORY, format.getCategory());
    doc.addField(RodaConstants.FORMAT_LATEST_VERSION, format.getLatestVersion());
    doc.addField(RodaConstants.FORMAT_POPULARITY, format.getPopularity());
    doc.addField(RodaConstants.FORMAT_DEVELOPER, format.getDeveloper());
    doc.addField(RodaConstants.FORMAT_INITIAL_RELEASE, format.getInitialRelease());
    doc.addField(RodaConstants.FORMAT_STANDARD, format.getStandard());
    doc.addField(RodaConstants.FORMAT_IS_OPEN_FORMAT, format.isOpenFormat());
    doc.addField(RodaConstants.FORMAT_WEBSITE, format.getWebsite());
    doc.addField(RodaConstants.FORMAT_PROVENANCE_INFORMATION, format.getProvenanceInformation());
    doc.addField(RodaConstants.FORMAT_EXTENSIONS, format.getExtensions());
    doc.addField(RodaConstants.FORMAT_MIMETYPES, format.getMimetypes());
    doc.addField(RodaConstants.FORMAT_PRONOMS, format.getPronoms());
    doc.addField(RodaConstants.FORMAT_UTIS, format.getUtis());

    return doc;
  }

  public static Format solrDocumentToFormat(SolrDocument doc) {
    Format format = new Format();

    format.setId(objectToString(doc.get(RodaConstants.FORMAT_ID)));
    format.setName(objectToString(doc.get(RodaConstants.FORMAT_NAME)));
    format.setDefinition(objectToString(doc.get(RodaConstants.FORMAT_DEFINITION)));
    format.setCategory(objectToString(doc.get(RodaConstants.FORMAT_CATEGORY)));
    format.setLatestVersion(objectToString(doc.get(RodaConstants.FORMAT_LATEST_VERSION)));
    format.setPopularity(objectToInteger(doc.get(RodaConstants.FORMAT_POPULARITY), 0));
    format.setDeveloper(objectToString(doc.get(RodaConstants.FORMAT_DEVELOPER)));
    format.setInitialRelease(objectToDate(doc.get(RodaConstants.FORMAT_INITIAL_RELEASE)));
    format.setStandard(objectToString(doc.get(RodaConstants.FORMAT_STANDARD)));
    format.setOpenFormat(objectToBoolean(doc.get(RodaConstants.FORMAT_IS_OPEN_FORMAT)));
    format.setWebsite(objectToString(doc.get(RodaConstants.FORMAT_WEBSITE)));
    format.setProvenanceInformation(objectToString(doc.get(RodaConstants.FORMAT_PROVENANCE_INFORMATION)));
    format.setExtensions(objectToListString(doc.get(RodaConstants.FORMAT_EXTENSIONS)));
    format.setMimetypes(objectToListString(doc.get(RodaConstants.FORMAT_MIMETYPES)));
    format.setPronoms(objectToListString(doc.get(RodaConstants.FORMAT_PRONOMS)));
    format.setUtis(objectToListString(doc.get(RodaConstants.FORMAT_UTIS)));

    return format;
  }

  public static SolrInputDocument fileToSolrDocument(AIP aip, File file, Binary premisFile, String fulltext) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.FILE_UUID, IdUtils.getFileId(file));
    List<String> path = file.getPath();
    doc.addField(RodaConstants.FILE_PATH, path);
    if (path != null && !path.isEmpty()) {
      String parentFileId = path.get(path.size() - 1);
      List<String> parentFileDirectoryPath = new ArrayList<>();
      if (path.size() > 1) {
        parentFileDirectoryPath.addAll(path.subList(0, path.size() - 1));
      }

      doc.addField(RodaConstants.FILE_PARENT_UUID,
        IdUtils.getFileId(file.getAipId(), file.getRepresentationId(), parentFileDirectoryPath, parentFileId));
    }
    doc.addField(RodaConstants.FILE_AIPID, file.getAipId());
    doc.addField(RodaConstants.FILE_FILEID, file.getId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_UUID,
      IdUtils.getRepresentationId(file.getAipId(), file.getRepresentationId()));
    doc.addField(RodaConstants.FILE_ISDIRECTORY, file.isDirectory());

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getFileStoragePath(file);
      doc.addField(RodaConstants.FILE_STORAGEPATH, filePath.asString());
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index file storage path", e);
    }

    // Add information from PREMIS
    if (premisFile != null) {
      // TODO get entry point from PREMIS or remove it
      // doc.addField(RodaConstants.FILE_ISENTRYPOINT, file.isEntryPoint());
      try {
        doc = PremisV3Utils.updateSolrDocument(doc, premisFile);
      } catch (GenericException e) {
        LOGGER.warn("Could not index file PREMIS information", e);
      }
    }

    if (fulltext != null) {
      doc.addField(RodaConstants.FILE_FULLTEXT, fulltext);

    }

    // indexing active state and permissions
    doc.addField(RodaConstants.ACTIVE, aip.isActive());
    setPermissions(aip.getPermissions(), doc);

    return doc;
  }

  public static IndexedFile solrDocumentToIndexedFile(SolrDocument doc) {
    IndexedFile file = null;
    String uuid = objectToString(doc.get(RodaConstants.FILE_UUID));
    String parentUUID = objectToString(doc.get(RodaConstants.FILE_PARENT_UUID));
    String aipId = objectToString(doc.get(RodaConstants.FILE_AIPID));
    String representationId = objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_ID));
    String representationUUID = objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_UUID));
    String fileId = objectToString(doc.get(RodaConstants.FILE_FILEID));
    List<String> path = objectToListString(doc.get(RodaConstants.FILE_PATH));
    // boolean entryPoint =
    // objectToBoolean(doc.get(RodaConstants.FILE_ISENTRYPOINT));

    String originalName = objectToString(doc.get(RodaConstants.FILE_ORIGINALNAME));
    List<String> hash = objectToListString(doc.get(RodaConstants.FILE_HASH));
    long size = objectToLong(doc.get(RodaConstants.FILE_SIZE), 0L);
    boolean isDirectory = objectToBoolean(doc.get(RodaConstants.FILE_ISDIRECTORY));
    String storagePath = objectToString(doc.get(RodaConstants.FILE_STORAGEPATH));

    // format
    String formatDesignationName = objectToString(doc.get(RodaConstants.FILE_FILEFORMAT));
    String formatDesignationVersion = objectToString(doc.get(RodaConstants.FILE_FORMAT_VERSION));
    String mimetype = objectToString(doc.get(RodaConstants.FILE_FORMAT_MIMETYPE));
    String pronom = objectToString(doc.get(RodaConstants.FILE_PRONOM));
    String extension = objectToString(doc.get(RodaConstants.FILE_EXTENSION));
    // FIXME how to restore format registries
    Map<String, String> formatRegistries = new HashMap<>();

    // technical features
    String creatingApplicationName = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_NAME));
    String creatingApplicationVersion = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION));
    String dateCreatedByApplication = objectToString(doc.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION));
    // String fullText = objectToString(doc.get(RodaConstants.FILE_FULLTEXT));

    // handle other properties
    Map<String, List<String>> otherProperties = new HashMap<String, List<String>>();
    for (String fieldName : doc.getFieldNames()) {
      if (fieldName.endsWith("_txt")) {
        List<String> otherProperty = objectToListString(doc.get(fieldName));
        otherProperties.put(fieldName, otherProperty);
      }

    }

    FileFormat fileFormat = new FileFormat(formatDesignationName, formatDesignationVersion, mimetype, pronom, extension,
      formatRegistries);

    file = new IndexedFile(uuid, parentUUID, aipId, representationId, representationUUID, path, fileId, false,
      fileFormat, originalName, size, isDirectory, creatingApplicationName, creatingApplicationVersion,
      dateCreatedByApplication, hash, storagePath, otherProperties);

    return file;
  }

  public static SolrInputDocument jobReportToSolrDocument(Report jobReport) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.JOB_REPORT_ID, jobReport.getId());
    doc.addField(RodaConstants.JOB_REPORT_JOB_ID, jobReport.getJobId());
    doc.addField(RodaConstants.JOB_REPORT_ITEM_ID, jobReport.getItemId());
    doc.addField(RodaConstants.JOB_REPORT_OTHER_ID, jobReport.getOtherId());
    doc.addField(RodaConstants.JOB_REPORT_TITLE, jobReport.getTitle());
    doc.addField(RodaConstants.JOB_REPORT_DATE_CREATED, jobReport.getDateCreated());
    doc.addField(RodaConstants.JOB_REPORT_DATE_UPDATE, jobReport.getDateUpdated());
    doc.addField(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE, jobReport.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_REPORT_STEPS_COMPLETED, jobReport.getStepsCompleted());
    doc.addField(RodaConstants.JOB_REPORT_TOTAL_STEPS, jobReport.getTotalSteps());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN, jobReport.getPlugin());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_STATE, jobReport.getPluginState());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_DETAILS, jobReport.getPluginDetails());
    doc.addField(RodaConstants.JOB_REPORT_REPORTS, JsonUtils.getJsonFromObject(jobReport.getReports()));

    return doc;
  }

  private static Report solrDocumentToJobReport(SolrDocument doc) {
    Report jobReport = new Report();

    jobReport.setId(objectToString(doc.get(RodaConstants.JOB_REPORT_ID)));
    jobReport.setJobId(objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_ID)));
    jobReport.setItemId(objectToString(doc.get(RodaConstants.JOB_REPORT_ITEM_ID)));
    jobReport.setOtherId(objectToString(doc.get(RodaConstants.JOB_REPORT_OTHER_ID)));
    jobReport.setTitle(objectToString(doc.get(RodaConstants.JOB_REPORT_TITLE)));
    jobReport.setDateCreated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_CREATED)));
    jobReport.setDateUpdated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_UPDATE)));
    jobReport.setCompletionPercentage(objectToInteger(doc.get(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE), 0));
    jobReport.setStepsCompleted(objectToInteger(doc.get(RodaConstants.JOB_REPORT_STEPS_COMPLETED), 0));
    jobReport.setTotalSteps(objectToInteger(doc.get(RodaConstants.JOB_REPORT_TOTAL_STEPS), 0));
    jobReport.setPlugin(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN)));
    jobReport.setPluginState(PluginState.valueOf(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_STATE))));
    jobReport.setPluginDetails(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_DETAILS)));
    try {
      jobReport
        .setReports(JsonUtils.getListFromJson(objectToString(doc.get(RodaConstants.JOB_REPORT_REPORTS)), Report.class));
    } catch (GenericException e) {
      LOGGER.error("Error parsing report in job report", e);
    }

    return jobReport;
  }

  public static <T extends IsIndexed> List<String> suggest(SolrClient index, Class<T> classToRetrieve, String field,
    String queryString) throws GenericException {

    String dictionaryName = field + "Suggester";

    SolrQuery query = new SolrQuery();
    query.setRequestHandler("/suggest");
    query.setParam("suggest", "true");
    query.setParam("suggest.dictionary", dictionaryName);
    query.setParam("suggest.q", queryString);

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      return response.getSuggesterResponse().getSuggestedTerms().get(dictionaryName);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not get suggestions", e);
    }
  }

  public static SolrInputDocument addOtherPropertiesToIndexedFile(String prefix, OtherMetadata otherMetadataBinary,
    ModelService model, SolrClient index)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
      ParserConfigurationException, SAXException, IOException, XPathExpressionException, SolrServerException {
    SolrDocument solrDocument = index.getById(RodaConstants.INDEX_FILE,
      IdUtils.getFileId(otherMetadataBinary.getAipId(), otherMetadataBinary.getRepresentationId(),
        otherMetadataBinary.getFileDirectoryPath(), otherMetadataBinary.getFileId()));

    Binary binary = model.retrieveOtherMetadataBinary(otherMetadataBinary);
    Map<String, List<String>> otherProperties = MetadataFileUtils.parseBinary(binary);

    for (Map.Entry<String, List<String>> entry : otherProperties.entrySet()) {
      solrDocument.setField(prefix + entry.getKey(), entry.getValue());
    }
    return solrDocumentToSolrInputDocument(solrDocument);

  }

  private static SolrInputDocument solrDocumentToSolrInputDocument(SolrDocument d) {
    SolrInputDocument doc = new SolrInputDocument();
    for (String name : d.getFieldNames()) {
      doc.addField(name, d.getFieldValue(name));
    }
    return doc;
  }

}
