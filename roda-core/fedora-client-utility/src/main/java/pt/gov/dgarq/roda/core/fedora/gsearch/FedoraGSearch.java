package pt.gov.dgarq.roda.core.fedora.gsearch;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import noNamespace.Field;
import noNamespace.GfindObjects;
import noNamespace.Object;
import noNamespace.ResultPageDocument;
import noNamespace.ResultPageDocument.ResultPage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;
import pt.gov.dgarq.roda.util.StringUtility;
import dk.defxws.fedoragsearch.server.Operations;
import dk.defxws.fedoragsearch.server.OperationsServiceLocator;

/**
 * Utility class to use the Fedora Generic Search service.
 * 
 * @author Rui Castro
 */
public class FedoraGSearch {

	static final private Logger logger = Logger.getLogger(FedoraGSearch.class);

	static final private String serviceURLPath = "/services/FgsOperations";

	private User user = null;

	private Operations fedoraGSearch = null;

	/**
	 * Constructs a new {@link FedoraGSearch} instance for the given service
	 * URL.
	 * 
	 * @param serviceURL
	 *            the URL of the Fedora Generic Search service.
	 * @param user
	 *            the User going the search
	 * 
	 * @throws FedoraGSearchException
	 */
	public FedoraGSearch(URL serviceURL, User user)
			throws FedoraGSearchException {
		this.user = user;
		try {

			setServiceURL(serviceURL);

		} catch (ServiceException e) {
			throw new FedoraGSearchException(
					"Error initializing Fedora Generic Search service - "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Update the search indexes for the given objects.
	 * 
	 * @param PIDs
	 *            the PIDs of the objects to update.
	 */
	public void updateDescriptionObjectsIndex(final List<String> PIDs) {

		for (String pid : PIDs) {
			try {

				this.fedoraGSearch.updateIndex("fromPid", pid, "RODA",
						"RODAOnLucene", "", "");

				logger.trace("Updated index of object " + pid);

			} catch (RemoteException e) {
				logger.warn("Error updating object " + pid + " - "
						+ e.getMessage(), e);
			}
		}

	}

	/**
	 * Search for description objects using the specified keywords. The query
	 * submitted to Fedora Generic Search will be the "AND-conjunction" of all
	 * the keywords. Ex: keyword1 AND keyword2.
	 * 
	 * @param keywords
	 *            the keywords to search in the default fields.
	 * @param firstResultIndex
	 *            the index of the first result. Indexes start at 0.
	 * @param maxResults
	 *            the maximum number of results to return.
	 * @param snippetsMax
	 *            the number of snippets in the result fields.
	 * @param fieldMaxLength
	 *            the maximum size of the fields to return.
	 * 
	 * @return a {@link SearchResult} with the results of this search.
	 * 
	 * @throws FedoraGSearchException
	 */
	public SearchResult basicSearch(String keywords, int firstResultIndex,
			int maxResults, int snippetsMax, int fieldMaxLength)
			throws FedoraGSearchException {

		if (StringUtils.isNotBlank(keywords)) {

			List<String> words = getQuotedWords(keywords);
			String simpleQuery = StringUtility.join(words, " AND ");

			return search(simpleQuery, firstResultIndex, maxResults,
					snippetsMax, fieldMaxLength);

		} else {
			return getEmptySearchResult();
		}
	}

	/**
	 * Search for description objects using the {@link SearchParameter}s given
	 * as argument to construct the query.
	 * 
	 * @param searchParameters
	 *            the {@link SearchParameter}s.
	 * @param firstResultIndex
	 *            the index of the first result. Indexes start at 0.
	 * @param maxResults
	 *            the maximum number of results to return.
	 * @param snippetsMax
	 *            the number of snippets in the result fields.
	 * @param fieldMaxLength
	 *            the maximum size of the fields to return.
	 * 
	 * @return a {@link SearchResult} with the results of this search.
	 * @throws FedoraGSearchException
	 */
	public SearchResult advancedSearch(SearchParameter[] searchParameters,
			int firstResultIndex, int maxResults, int snippetsMax,
			int fieldMaxLength) throws FedoraGSearchException {

		logger.debug("advancedSearch( " + Arrays.asList(searchParameters)
				+ " )");

		String query = "";

		if (searchParameters != null && searchParameters.length > 0) {

			query += searchParameters[0].getSubQuery();

			for (int i = 1; i < searchParameters.length; i++) {
				query += " AND " + searchParameters[i].getSubQuery();
			}

		} else {
			// Empty query.
		}

		logger.debug("advancedSearch( " + query + " )");

		return search(query, firstResultIndex, maxResults, snippetsMax,
				fieldMaxLength);
	}

	/**
	 * @param serviceURL
	 *            the serviceURL to set (ex:
	 *            http://localhost:8080/fedoragsearch)
	 * 
	 * @throws ServiceException
	 *             if the service could not be created.
	 */
	private void setServiceURL(URL serviceURL) throws ServiceException {
		OperationsServiceLocator operationServiceLocator = new OperationsServiceLocator();
		operationServiceLocator.setFgsOperationsEndpointAddress(serviceURL
				+ serviceURLPath);

		this.fedoraGSearch = operationServiceLocator.getFgsOperations();
	}

	/**
	 * Search for description objects using the query specified as argument.
	 * 
	 * @param query
	 *            the query
	 * @param firstResultIndex
	 *            the index of the first result. Indexes start at 0.
	 * @param maxResults
	 *            the maximum number of results to return.
	 * @param snippetsMax
	 *            the number of snippets in the result fields.
	 * @param fieldMaxLength
	 *            the maximum size of the fields to return.
	 * 
	 * @return a {@link SearchResult} with the results of this search.
	 * @throws FedoraGSearchException
	 */
	private SearchResult search(String query, int firstResultIndex,
			int maxResults, int snippetsMax, int fieldMaxLength)
			throws FedoraGSearchException {

		int firstResult = firstResultIndex + 1;

		String indexName = "RODAOnLucene";
		String resultPageXslt = "gfindObjectsToResultPage";

		query = String.format("(%1$s) AND %2$s", query,
				getPermissionConditions());

		logger.debug(String.format(
				"search (%1$s, %2$d, %3$d, %4$d, %5$d, %6$s, %7$s)", query,
				firstResult, maxResults, snippetsMax, fieldMaxLength,
				indexName, resultPageXslt));

		try {

			String searchResult = this.fedoraGSearch.gfindObjects(query,
					firstResult, maxResults, snippetsMax, fieldMaxLength,
					indexName, resultPageXslt);

			logger.trace("Search result: " + searchResult);

			if (searchResult == null || searchResult.length() == 0) {
				logger.debug("Search result is null or empty");
				return getEmptySearchResult();
			} else {
				ResultPageDocument resultPageDocument = ResultPageDocument.Factory
						.parse(searchResult);
				return getSearchResult(resultPageDocument.getResultPage());
			}

		} catch (RemoteException e) {
			logger
					.debug("Remote exception doing search - " + e.getMessage(),
							e);
			throw new FedoraGSearchException("Remote exception doing search - "
					+ e.getMessage(), e);
		} catch (XmlException e) {
			logger.debug(
					"Exception parsing search results - " + e.getMessage(), e);
			throw new FedoraGSearchException(
					"Exception parsing search results - " + e.getMessage(), e);
		}
	}

	private String getPermissionConditions() {

		String conditions = "permissions.read.user:" + this.user.getName();

		for (String groupName : this.user.getAllGroups()) {
			conditions = String.format("%1$s OR permissions.read.group:%2$s",
					conditions, groupName);
		}

		return "(" + conditions + ")";
	}

	/**
	 * Returns an empty {@link SearchResult}.
	 * 
	 * @return an empty {@link SearchResult}.
	 */
	private SearchResult getEmptySearchResult() {

		SearchResult searchResult = new SearchResult();

		searchResult.setDatetime(DateParser.getIsoDate(Calendar.getInstance()
				.getTime()));
		searchResult.setHitPageSize(0);
		searchResult.setHitPageStart(0);
		searchResult.setHitTotal(0);
		// searchResult.setIndexName("");
		// searchResult.setResultPageXslt("");
		searchResult.setResultCount(0);
		searchResult.setSearchResultObjects(new SearchResultObject[0]);

		return searchResult;
	}

	/**
	 * Gets {@link SearchResult} from a ResultPage.
	 * 
	 * @param resultPage
	 * @return
	 */
	private SearchResult getSearchResult(ResultPage resultPage) {

		SearchResult searchResult = new SearchResult();

		searchResult.setDatetime(resultPage.getDateTime());
		searchResult.setIndexName(resultPage.getIndexName());

		GfindObjects gfindObjects = resultPage.getGfindObjects();

		searchResult.setHitPageStart(gfindObjects.getHitPageStart());
		searchResult.setHitPageSize(gfindObjects.getHitPageSize());
		searchResult.setHitTotal(gfindObjects.getHitTotal());
		searchResult.setResultPageXslt(gfindObjects.getResultPageXslt());

		List<Object> objectList = gfindObjects.getObjects().getObjectList();

		if (objectList != null) {

			searchResult.setResultCount(objectList.size());

			for (Object object : objectList) {
				searchResult
						.addSearchResultObject(getSearchResultObject(object));
			}

		} else {
			searchResult.setResultCount(0);
		}

		return searchResult;
	}

	/**
	 * Gets a {@link SearchResultObject} from an Object from a ResultPage.
	 * 
	 * @param object
	 * @return
	 */
	private SearchResultObject getSearchResultObject(Object object) {

		DescriptionObject descriptionObject = new DescriptionObject();

		List<Field> fieldList = object.getFieldList();

		for (int i = 0; fieldList != null && i < fieldList.size(); i++) {
			setDescriptionObjectField(descriptionObject, fieldList.get(i));
		}

		return new SearchResultObject(object.getNo(), object.getScore(),
				descriptionObject);
	}

	private void setDescriptionObjectField(DescriptionObject descriptionObject,
			Field field) {

		String fieldName = field.getName();

		if ("PID".equalsIgnoreCase(fieldName)) {
			descriptionObject.setPid(getValueWithoutSnippets(field));
		} else

		if ("fgs.label".equalsIgnoreCase(fieldName)) {
			descriptionObject.setLabel(getValueWithoutSnippets(field));
		} else

		if ("fgs.contentModel".equalsIgnoreCase(fieldName)) {
			descriptionObject.setContentModel(getValueWithoutSnippets(field));
		} else

		// Complete reference
		if (EadcSearchFields.COMPLETE_REFERENCE.equalsIgnoreCase(fieldName)) {
			descriptionObject
					.setCompleteReference(getValueWithoutSnippets(field));
		} else

		/*
		 * Identificação
		 */

		// @level
		if (EadcSearchFields.LEVEL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setLevel(new DescriptionLevel(
					getValueWithoutSnippets(field)));
		} else

		// did/unitid/@repositorycode
		if (EadcSearchFields.REPOSITORYCODE.equalsIgnoreCase(fieldName)) {

			String countryRepositoryCode = getValueWithoutSnippets(field);

			if (!StringUtils.isBlank(countryRepositoryCode)) {

				String[] values = countryRepositoryCode.split("-");

				if (values.length == 2) {
					descriptionObject.setCountryCode(values[0]);
					descriptionObject.setRepositoryCode(values[1]);
				} else {
					logger
							.warn("Invalid countryRepositoryCode in GSearch index '"
									+ countryRepositoryCode + "'");
				}
			}
		} else

		// did/unitid/text()
		if (EadcSearchFields.UNITID.equalsIgnoreCase(fieldName)) {
			descriptionObject.setId(getValueWithoutSnippets(field));
		} else

		// did/unittitle/text()
		if (EadcSearchFields.UNITTITLE.equalsIgnoreCase(fieldName)) {
			descriptionObject.setTitle(getValueWithSnippets(field));
		} else

		// did/unitdate/@normal
		if (EadcSearchFields.UNITDATE_INITIAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setDateInitial(getDateValue(field));
		} else if (EadcSearchFields.UNITDATE_FINAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setDateFinal(getDateValue(field));
		} else

		// did/physdesc/p/text()
		if (EadcSearchFields.PHYSDESC.equalsIgnoreCase(fieldName)) {
			descriptionObject.setPhysdesc(getValueWithoutSnippets(field));
		} else

		// physdesc/dimensions/text()
		if (EadcSearchFields.PHYSDESC_DIMENSIONS.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescDimensions = descriptionObject
					.getPhysdescDimensions();

			if (physdescDimensions == null) {
				physdescDimensions = new PhysdescElement();
			}

			physdescDimensions.setValue(getValueWithoutSnippets(field));

			descriptionObject.setPhysdescDimensions(physdescDimensions);
		} else

		// physdesc/dimensions/@unit
		if (EadcSearchFields.PHYSDESC_DIMENSIONS_UNIT
				.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescDimensions = descriptionObject
					.getPhysdescDimensions();

			if (physdescDimensions == null) {
				physdescDimensions = new PhysdescElement();
			}

			physdescDimensions.setUnit(getValueWithoutSnippets(field));

			descriptionObject.setPhysdescDimensions(physdescDimensions);
		} else

		// did/physdesc/date/@normal
		if (EadcSearchFields.PHYSDESC_DATE_INITIAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setPhysdescDateInitial(getDateValue(field));
		}
		if (EadcSearchFields.PHYSDESC_DATE_FINAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setPhysdescDateFinal(getDateValue(field));
		}

		// did/physdesc/extent/text()
		if (EadcSearchFields.PHYSDESC_EXTENT.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescExtent = descriptionObject
					.getPhysdescExtent();
			if (physdescExtent == null) {
				physdescExtent = new PhysdescElement();
			}
			physdescExtent.setValue(getValueWithoutSnippets(field));
			descriptionObject.setPhysdescExtent(physdescExtent);

		} else
		// did/physdesc/extent/@unit
		if (EadcSearchFields.PHYSDESC_EXTENT_UNIT.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescExtent = descriptionObject
					.getPhysdescExtent();
			if (physdescExtent == null) {
				physdescExtent = new PhysdescElement();
			}
			physdescExtent.setUnit(getValueWithoutSnippets(field));
			descriptionObject.setPhysdescExtent(physdescExtent);
		}

		/*
		 * Contexto
		 */

		// did/origination/text()
		if (EadcSearchFields.ORIGINATION.equalsIgnoreCase(fieldName)) {
			descriptionObject.setOrigination(getValueWithSnippets(field));
		} else

		// bioghist/p/text()
		if (EadcSearchFields.BIOGHIST.equalsIgnoreCase(fieldName)) {
			descriptionObject.setBioghist(getValueWithSnippets(field));
		} else

		// bioghist/chronlist (ead.bioghist.chronitem.event)
		// if (fieldName.equalsIgnoreCase("")) {
		// descriptionObject.setBioghistCronlist(getValueWithoutSnippets(field));
		// } else

		// custodhist/p/text()
		if (EadcSearchFields.CUSTODHIST.equalsIgnoreCase(fieldName)) {
			descriptionObject.setCustodhist(getValueWithSnippets(field));
		} else

		// acqinfo/p/text()
		if (EadcSearchFields.ACQINFO.equalsIgnoreCase(fieldName)) {
			descriptionObject.setAcqinfo(getValueWithSnippets(field));
		} else

		/*
		 * Conteúdo e estructura
		 */

		// scopecontent/p/text()
		if (EadcSearchFields.SCOPECONTENT.equalsIgnoreCase(fieldName)) {
			descriptionObject.setScopecontent(getValueWithSnippets(field));
		} else

		// appraisal/p/text()
		if (EadcSearchFields.APPRAISAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setAppraisal(getValueWithSnippets(field));
		} else

		// accruals/p/text()
		if (EadcSearchFields.ACCRUALS.equalsIgnoreCase(fieldName)) {
			descriptionObject.setAccruals(getValueWithSnippets(field));
		} else

		// arrangement/p/text()
		if (EadcSearchFields.ARRANGEMENT.equalsIgnoreCase(fieldName)) {
			descriptionObject.setArrangement(getValueWithSnippets(field));
		} else

		// if (fieldName.equalsIgnoreCase("")) {
		// descriptionObject
		// .setArrangementTable(getValueWithoutSnippets(field));
		// } else

		/*
		 * Condições de accesso e utilização
		 */

		// accessrestrict/p/text()
		if (EadcSearchFields.ACCESSRESTRICT.equalsIgnoreCase(fieldName)) {
			descriptionObject.setAccessrestrict(getValueWithSnippets(field));
		} else

		// userestrict/p/text()
		if (EadcSearchFields.USERESTRICT.equalsIgnoreCase(fieldName)) {
			descriptionObject.setUserestrict(getValueWithSnippets(field));
		} else

		// did/langmaterial/language
		if (EadcSearchFields.LANGMATERIAL_LANGUAGE.equalsIgnoreCase(fieldName)) {

			LangmaterialLanguages langmaterialLanguages = descriptionObject
					.getLangmaterialLanguages();

			// the list of languages in this Description Object (empty at first)
			List<String> languages = new ArrayList<String>();

			if (langmaterialLanguages != null
					&& langmaterialLanguages.getLangmaterialLanguages() != null) {

				// If we already have languages, add them to the list.
				languages.addAll(Arrays.asList(langmaterialLanguages
						.getLangmaterialLanguages()));
			}

			// Add the new language to the list.
			languages.add(getValueWithoutSnippets(field));

			// Set the new languages.
			descriptionObject
					.setLangmaterialLanguages(new LangmaterialLanguages(
							languages.toArray(new String[languages.size()])));
		} else

		// phystech/p/text()
		if (EadcSearchFields.PHYSTECH.equalsIgnoreCase(fieldName)) {
			descriptionObject.setPhystech(getValueWithSnippets(field));
		} else

		// did/materialspec/text()
		if (EadcSearchFields.MATERIALSPEC.equalsIgnoreCase(fieldName)) {
			descriptionObject.setMaterialspec(getValueWithSnippets(field));
		} else

		// did/physdesc/physfacet/text()
		if (EadcSearchFields.PHYSDESC_PHYSFACET.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescPhysfacet = descriptionObject
					.getPhysdescPhysfacet();
			if (physdescPhysfacet == null) {
				physdescPhysfacet = new PhysdescElement();
			}
			physdescPhysfacet.setValue(getValueWithoutSnippets(field));
			descriptionObject.setPhysdescPhysfacet(physdescPhysfacet);

		} else
		// did/physdesc/physfacet/@unit
		if (EadcSearchFields.PHYSDESC_PHYSFACET_UNIT
				.equalsIgnoreCase(fieldName)) {

			PhysdescElement physdescPhysfacet = descriptionObject
					.getPhysdescPhysfacet();
			if (physdescPhysfacet == null) {
				physdescPhysfacet = new PhysdescElement();
			}
			physdescPhysfacet.setUnit(getValueWithoutSnippets(field));
			descriptionObject.setPhysdescPhysfacet(physdescPhysfacet);

		}

		// otherfindaid/p/text()
		if (EadcSearchFields.OTHERFINDAID.equalsIgnoreCase(fieldName)) {
			descriptionObject.setOtherfindaid(getValueWithSnippets(field));
		} else

		/*
		 * Documentação associada
		 */

		// relatedmaterial/p/text()
		if (EadcSearchFields.RELATEDMATERIAL.equalsIgnoreCase(fieldName)) {
			descriptionObject.setRelatedmaterial(getValueWithSnippets(field));
		} else

		// bibliography/p/text()
		if (EadcSearchFields.BIBLIOGRAPHY.equalsIgnoreCase(fieldName)) {
			descriptionObject.setBibliography(getValueWithSnippets(field));
		} else

		/*
		 * Notas
		 */

		// note/p
		if (EadcSearchFields.NOTE.equalsIgnoreCase(fieldName)) {
			descriptionObject.setNote(getValueWithSnippets(field));
		} else

		/*
		 * Controlo de descrição
		 */

		// processinfo/p/text()
		// if (EadcSearchFields.PROCESSINFO.equalsIgnoreCase(fieldName)) {
		// descriptionObject.setProcessinfo(getValueWithSnippets(field));
		// } else
		// prefercite/p/text()
		if (EadcSearchFields.PREFERCITE.equalsIgnoreCase(fieldName)) {
			descriptionObject.setPrefercite(getValueWithSnippets(field));
		}

		// descriptionObject.setSubElementsCount();
		descriptionObject.setDescription(descriptionObject.getBioghist());
	}

	private String getValueWithSnippets(Field field) {

		XmlOptions xmlOptions = new XmlOptions()
				.setSaveSyntheticDocumentElement(new QName("xml-fragment"));

		String xmlFragment = field.xmlText(xmlOptions);
		logger.trace("xmlText for " + field.getName() + "=" + xmlFragment);

		// Remove the start and end tags from the xml-fragment
		String value = xmlFragment.replaceFirst("\\<xml-fragment [^\\>]*\\>",
				"").replaceAll("\\</xml-fragment\\>", "").replaceAll("\n", "")
				.replaceAll("\\s+", " ").replaceAll("#quot;", "&quot;");

		logger.trace(field.getName() + "=" + value);

		return value;
	}

	private String getValueWithoutSnippets(Field field) {

		String xmlFragment = field.xmlText();

		// Remove all tags from the xml-fragment
		String value = xmlFragment.replaceAll("\\<[^\\>]*\\>", "").replaceAll(
				"\n", "").replaceAll("\\s+", " ")
				.replaceAll("#quot;", "&quot;");

		logger.trace(field.getName() + "=" + value);

		return value;
	}

	private String getDateValue(Field field) {
		String xmlFragment = field.xmlText();

		// Remove all tags from the xml-fragment
		String value = xmlFragment.replaceAll("\\<[^\\>]*\\>", "").replaceAll(
				"\n", "").replaceAll("\\s+", " ")
				.replaceAll("#quot;", "&quot;");

		String date = "";

		String year = value.substring(0, 4);
		date += year;

		String month = null;
		String day = null;
		if (value.length() > 4) {
			month = value.substring(4, 6);
			date += "-" + month;
		}
		if (value.length() > 6) {
			day = value.substring(6, 8);
			date += "-" + day;
		}

		logger.trace(field.getName() + "=" + date);

		return date;
	}

	private List<String> getQuotedWords(String keywords) {

		List<String> words = new ArrayList<String>();

		Pattern patternFindWords = Pattern.compile("(\"[^\"]+\")|([^\"\\s]+)");
		Matcher matcher = patternFindWords.matcher(keywords);

		while (matcher.find()) {
			String word = matcher.group().replaceAll("\"", "");
			words.add('"' + word + '"');
		}

		return words;
	}

}
