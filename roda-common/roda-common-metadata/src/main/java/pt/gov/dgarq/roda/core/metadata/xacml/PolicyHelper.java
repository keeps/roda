package pt.gov.dgarq.roda.core.metadata.xacml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Text;

import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;
import x0Policy.oasisNamesTcXacml1.ActionMatchType;
import x0Policy.oasisNamesTcXacml1.ApplyType;
import x0Policy.oasisNamesTcXacml1.AttributeDesignatorType;
import x0Policy.oasisNamesTcXacml1.AttributeValueType;
import x0Policy.oasisNamesTcXacml1.EffectType.Enum;
import x0Policy.oasisNamesTcXacml1.PolicyDocument;
import x0Policy.oasisNamesTcXacml1.PolicyType;
import x0Policy.oasisNamesTcXacml1.ResourceMatchType;
import x0Policy.oasisNamesTcXacml1.RuleType;
import x0Policy.oasisNamesTcXacml1.SubjectAttributeDesignatorType;
import x0Policy.oasisNamesTcXacml1.TargetType;

/**
 * This is an helper class for manipulating a XACML Policy XML document. It
 * provides methods to read {@link RODAObjectPermissions} from XACML Policy
 * documents and methods to write {@link RODAObjectPermissions}s to XACML Policy
 * documents.
 * 
 * @author Rui Castro
 */
public class PolicyHelper {
	private static final Logger logger = Logger.getLogger(PolicyHelper.class);

	private static final String XACML_NAMESPACE = "urn:oasis:names:tc:xacml:1.0:policy";

	private static final String SUBJECT_ATTRIBUTE_ID_USERS = "urn:fedora:names:fedora:2.1:subject:loginId";
	private static final String SUBJECT_ATTRIBUTE_ID_GROUPS = "groups";

	private static final String SCHEMA_DATA_TYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";

	private static final String XACML_RULE_COMBINING_ALGORITHM_FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";

	private static final String XACML_FUNCTION_STRING_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final String XACML_FUNCTION_STRING_AT_LEAST_ONE_MEMBER_OF = "urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of";
	private static final String XACML_FUNCTION_STRING_BAG = "urn:oasis:names:tc:xacml:1.0:function:string-bag";

	private static final String FEDORA_RESOURCE_OBJECT_PID = "urn:fedora:names:fedora:2.1:resource:object:pid";
	private static final String FEDORA_RESOURCE_DATASTREAM_ID = "urn:fedora:names:fedora:2.1:resource:datastream:id";

	private static final String FEDORA_ACTION_ID = "urn:fedora:names:fedora:2.1:action:id";
	private static final String FEDORA_ACTION_API = "urn:fedora:names:fedora:2.1:action:api";
	private static final String FEDORA_ACTION_APIA = "urn:fedora:names:fedora:2.1:action:api-a";
	private static final String FEDORA_ACTION_APIM = "urn:fedora:names:fedora:2.1:action:api-m";
	private static final String FEDORA_ACTION_PURGE_OBJECT = "urn:fedora:names:fedora:2.1:action:id-purgeObject";

	private final PolicyDocument policyDocument;

	/**
	 * Creates a new instance of a {@link PolicyHelper} for the XACML Policy XML
	 * inside the given {@link File}.
	 * 
	 * @param policyFile
	 *            the XACML Policy XML file.
	 * 
	 * @return a {@link PolicyHelper} for the given XACML Policy XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws PolicyMetadataException
	 *             if the XACML Policy XML document is invalid.
	 */
	public static PolicyHelper newInstance(File policyFile)
			throws PolicyMetadataException, FileNotFoundException, IOException {
		FileInputStream policyInputStream = new FileInputStream(policyFile);
		PolicyHelper instance = newInstance(policyInputStream);
		policyInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link PolicyHelper} for the XACML Policy XML
	 * inside the given {@link InputStream}.
	 * 
	 * @param policyInputStream
	 *            the XACML Policy XML {@link InputStream}.
	 * 
	 * @return a {@link PolicyHelper} for the given XACML Policy XML
	 *         {@link InputStream} .
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws PolicyMetadataException
	 *             if the XACML Policy XML document is invalid.
	 */
	public static PolicyHelper newInstance(InputStream policyInputStream)
			throws PolicyMetadataException, IOException {

		try {

			PolicyDocument document = PolicyDocument.Factory
					.parse(policyInputStream);
			if (document.validate()) {
				return new PolicyHelper(document);
			} else {
				throw new PolicyMetadataException(
						"Error validating XML document");
			}

		} catch (XmlException e) {
			logger.debug("Error parsing XACML Policy - " + e.getMessage(), e);
			throw new PolicyMetadataException("Error parsing XACML Policy - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Constructs a new {@link PolicyHelper} with a new XACML Policy document.
	 */
	public PolicyHelper() {
		this(PolicyDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link PolicyHelper} with a new XACML Policy document
	 * and sets the information inside the given {@link RODAObjectPermissions}.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions}.
	 * 
	 * @throws PolicyMetadataException
	 */
	public PolicyHelper(RODAObjectPermissions permissions)
			throws PolicyMetadataException {
		this();
		setRODAObjectPermissions(permissions);
	}

	/**
	 * Constructs a new {@link PolicyHelper} for the given XACML Policy
	 * document.
	 * 
	 * @param policyDocument
	 *            the XACML Policy document.
	 */
	public PolicyHelper(PolicyDocument policyDocument) {

		this.policyDocument = policyDocument;

		if (getPolicy() == null) {
			getPolicyDocument().addNewPolicy();
		}
	}

	/**
	 * Gets the current XACML Policy document.
	 * 
	 * @return the policyDocument
	 */
	public PolicyDocument getPolicyDocument() {
		return policyDocument;
	}

	/**
	 * Gets the current XACML Policy.
	 * 
	 * @return the current {@link PolicyType}.
	 */
	public PolicyType getPolicy() {
		return getPolicyDocument().getPolicy();
	}

	/**
	 * Gets a {@link RODAObjectPermissions} from the current
	 * {@link PolicyDocument}.
	 * 
	 * @return a {@link RODAObjectPermissions}.
	 */
	public RODAObjectPermissions getRODAObjectPermissions() {

		RODAObjectPermissions permissions = new RODAObjectPermissions();

		permissions.setObjectPID(getPolicy().getPolicyId());

		permissions.setReadUsers(getConditionValuesArray(getRule("readUsers")));
		permissions
				.setReadGroups(getConditionValuesArray(getRule("readGroups")));

		permissions
				.setModifyUsers(getConditionValuesArray(getRule("modifyUsers")));
		permissions
				.setModifyGroups(getConditionValuesArray(getRule("modifyGroups")));

		permissions
				.setRemoveUsers(getConditionValuesArray(getRule("removeUsers")));
		permissions
				.setRemoveGroups(getConditionValuesArray(getRule("removeGroups")));

		permissions
				.setGrantUsers(getConditionValuesArray(getRule("grantUsers")));
		permissions
				.setGrantGroups(getConditionValuesArray(getRule("grantGroups")));

		return permissions;
	}

	/**
	 * Replaces the current XACML XML data for the data inside the given
	 * {@link RODAObjectPermissions}.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions}.
	 * 
	 * @throws PolicyMetadataException
	 */
	public void setRODAObjectPermissions(RODAObjectPermissions permissions)
			throws PolicyMetadataException {

		// Replaces the current policy with a new empty <Policy>
		setPolicy(PolicyType.Factory.newInstance());

		// /Policy/@PolicyId
		getPolicy().setPolicyId(permissions.getObjectPID());
		// /Policy/@RuleCombiningAlgId
		getPolicy().setRuleCombiningAlgId(
				XACML_RULE_COMBINING_ALGORITHM_FIRST_APPLICABLE);

		// /Policy/Target
		addTarget(getPolicy(), permissions.getObjectPID());

		// /Policy/Rule/@RuleId='readUsers'
		addRule(getPolicy(), "readUsers", "Permit", null, null,
				FEDORA_ACTION_API, FEDORA_ACTION_APIA,
				SUBJECT_ATTRIBUTE_ID_USERS, permissions.getReadUsers());
		// /Policy/Rule/@RuleId='readGroups'
		addRule(getPolicy(), "readGroups", "Permit", null, null,
				FEDORA_ACTION_API, FEDORA_ACTION_APIA,
				SUBJECT_ATTRIBUTE_ID_GROUPS, permissions.getReadGroups());

		// /Policy/Rule/@RuleId='removeUsers'
		addRule(getPolicy(), "removeUsers", "Permit", null, null,
				FEDORA_ACTION_ID, FEDORA_ACTION_PURGE_OBJECT,
				SUBJECT_ATTRIBUTE_ID_USERS, permissions.getRemoveUsers());
		// /Policy/Rule/@RuleId='removeGroups'
		addRule(getPolicy(), "removeGroups", "Permit", null, null,
				FEDORA_ACTION_ID, FEDORA_ACTION_PURGE_OBJECT,
				SUBJECT_ATTRIBUTE_ID_GROUPS, permissions.getRemoveGroups());

		// /Policy/Rule/@RuleId='grantUsers'
		addRule(getPolicy(), "grantUsers", "Permit",
				FEDORA_RESOURCE_DATASTREAM_ID, "POLICY", FEDORA_ACTION_API,
				FEDORA_ACTION_APIM, SUBJECT_ATTRIBUTE_ID_USERS,
				permissions.getGrantUsers());
		// /Policy/Rule/@RuleId='grantGroups'
		addRule(getPolicy(), "grantGroups", "Permit",
				FEDORA_RESOURCE_DATASTREAM_ID, "POLICY", FEDORA_ACTION_API,
				FEDORA_ACTION_APIM, SUBJECT_ATTRIBUTE_ID_GROUPS,
				permissions.getGrantGroups());

		// /Policy/Rule/@RuleId='modifyUsers'
		addRule(getPolicy(), "modifyUsers", "Permit", null, null,
				FEDORA_ACTION_API, FEDORA_ACTION_APIM,
				SUBJECT_ATTRIBUTE_ID_USERS, permissions.getModifyUsers());
		// /Policy/Rule/@RuleId='modifyGroups'
		addRule(getPolicy(), "modifyGroups", "Permit", null, null,
				FEDORA_ACTION_API, FEDORA_ACTION_APIM,
				SUBJECT_ATTRIBUTE_ID_GROUPS, permissions.getModifyGroups());

		RuleType ruleDenyByDefault = getPolicy().addNewRule();
		ruleDenyByDefault.setRuleId("deny-by-default");
		ruleDenyByDefault.setEffect(Enum.forString("Deny"));
	}

	/**
	 * Saves the current XACML document to a byte array.
	 * 
	 * @return a <code>byte[]</code> with the contents of the XACML XML file.
	 * 
	 * @throws PolicyMetadataException
	 *             if the XACML document is not valid or if something goes wrong
	 *             with the serialisation.
	 */
	public byte[] saveToByteArray() throws PolicyMetadataException {
		return saveToByteArray(true);
	}

	/**
	 * Saves the current XACML document to a byte array.
	 * 
	 * @param writeXMLDeclaration
	 * 
	 * @return a <code>byte[]</code> with the contents of the XACML XML file.
	 * 
	 * @throws PolicyMetadataException
	 *             if the XACML document is not valid or if something goes wrong
	 *             with the serialisation.
	 */
	public byte[] saveToByteArray(boolean writeXMLDeclaration)
			throws PolicyMetadataException {

		try {

			return MetadataHelperUtility.saveToByteArray(getPolicyDocument(),
					writeXMLDeclaration);

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PolicyMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Saves the current XACML document to a {@link File}.
	 * 
	 * @param policyFile
	 *            the {@link File}.
	 * 
	 * @throws PolicyMetadataException
	 *             if the XACML document is not valid or if something goes wrong
	 *             with the serialisation.
	 * 
	 * @throws FileNotFoundException
	 *             if the specified {@link File} couldn't be opened.
	 * @throws IOException
	 *             if {@link FileOutputStream} associated with the {@link File}
	 *             couldn't be closed.
	 */
	public void saveToFile(File policyFile) throws PolicyMetadataException,
			FileNotFoundException, IOException {
		try {

			MetadataHelperUtility.saveToFile(getPolicyDocument(), policyFile);

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PolicyMetadataException(e.getMessage(), e);
		}
	}

	private void setPolicy(PolicyType policy) {
		getPolicyDocument().setPolicy(policy);
	}

	private RuleType getRule(String ruleId) {

		RuleType resultRule = null;

		List<RuleType> rules = getPolicy().getRuleList();
		if (rules != null) {
			for (RuleType rule : rules) {
				if (rule.getRuleId().equals(ruleId)) {
					resultRule = rule;
					break;
				}
			}
		}

		return resultRule;
	}

	private List<String> getConditionValues(RuleType rule) {
		List<String> values = new ArrayList<String>();

		// Rule/Condition
		if (rule != null && rule.getCondition() != null) {

			// Rule/Condition/Apply
			List<ApplyType> applyList = rule.getCondition().getApplyList();

			if (applyList != null) {

				ApplyType firstApply = applyList.get(0);

				// Rule/Condition/Apply/AttributeValue
				List<AttributeValueType> attributeValueList = firstApply
						.getAttributeValueList();

				if (attributeValueList != null) {

					for (AttributeValueType attributeValue : attributeValueList) {

						values.add(attributeValue.getDomNode().getFirstChild()
								.getNodeValue());
					}

				}
			}
		}

		return values;
	}

	private String[] getConditionValuesArray(RuleType rule) {
		List<String> values = getConditionValues(rule);
		return values.toArray(new String[values.size()]);
	}

	private void addTarget(PolicyType policy, String objectPID)
			throws PolicyMetadataException {

		// /Policy/Target
		TargetType target = policy.addNewTarget();
		// /Policy/Target/Subjects/AnySubject
		target.addNewSubjects().addNewAnySubject();

		// /Policy/Target/Resources/Resource/ResourceMatch
		ResourceMatchType resourceMatch = target.addNewResources()
				.addNewResource().addNewResourceMatch();
		// /Policy/Target/Resources/Resource/ResourceMatch/@MatchId
		resourceMatch.setMatchId(XACML_FUNCTION_STRING_EQUAL);

		// /Policy/Target/Resources/Resource/ResourceMatch/AttributeValue
		AttributeValueType attributeValue = resourceMatch
				.addNewAttributeValue();
		// /Policy/Target/Resources/Resource/ResourceMatch/AttributeValue/@DataType
		attributeValue.setDataType(SCHEMA_DATA_TYPE_STRING);
		// /Policy/Target/Resources/Resource/ResourceMatch/AttributeValue/text()
		setAttributeValueText(attributeValue, objectPID);

		// /Policy/Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator
		AttributeDesignatorType resourceAttributeDesignator = resourceMatch
				.addNewResourceAttributeDesignator();
		// /Policy/Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator/@DataType
		resourceAttributeDesignator.setDataType(SCHEMA_DATA_TYPE_STRING);
		// /Policy/Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator/@AttributeId
		resourceAttributeDesignator.setAttributeId(FEDORA_RESOURCE_OBJECT_PID);

		// /Policy/Target/Actions/AnyAction
		target.addNewActions().addNewAnyAction();
	}

	private void addRule(PolicyType policy, String ruleId, String effect,
			String resourceAttributeId, String resourceAttributeValue,
			String actionAttributeId, String actionAttributeValue,
			String subjectAttributeId, String[] subjectAttributeValues)
			throws PolicyMetadataException {

		// Rule
		RuleType rule = policy.addNewRule();
		// Rule/@RuleId
		rule.setRuleId(ruleId);
		// Rule/@Effect
		rule.setEffect(Enum.forString(effect));

		// Rule/Target
		addRuleTarget(rule, resourceAttributeId, resourceAttributeValue,
				actionAttributeId, actionAttributeValue);

		// Rule/Condition
		addRuleCondition(rule, subjectAttributeId, subjectAttributeValues);

	}

	private void addRuleTarget(RuleType rule, String resourceId,
			String resourceValue, String actionId, String actionValue)
			throws PolicyMetadataException {

		// Target
		TargetType target = rule.addNewTarget();
		// Target/Subjects/AnySubject
		target.addNewSubjects().addNewAnySubject();

		if (resourceId != null && resourceValue != null) {

			// Target/Resources/Resource/ResourceMatch
			ResourceMatchType resourceMatch = target.addNewResources()
					.addNewResource().addNewResourceMatch();
			// Target/Resources/Resource/ResourceMatch/@MatchId
			resourceMatch.setMatchId(XACML_FUNCTION_STRING_EQUAL);

			// Target/Resources/Resource/ResourceMatch/AttributeValue
			AttributeValueType attributeValue = resourceMatch
					.addNewAttributeValue();
			// Target/Resources/Resource/ResourceMatch/AttributeValue/@DataType
			attributeValue.setDataType(SCHEMA_DATA_TYPE_STRING);
			// Target/Resources/Resource/ResourceMatch/AttributeValue/text()
			setAttributeValueText(attributeValue, resourceValue);

			// Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator
			AttributeDesignatorType resourceAttributeDesignator = resourceMatch
					.addNewResourceAttributeDesignator();
			// Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator/@DataType
			resourceAttributeDesignator.setDataType(SCHEMA_DATA_TYPE_STRING);
			// Target/Resources/Resource/ResourceMatch/ResourceAttributeDesignator/@AttributeId
			resourceAttributeDesignator.setAttributeId(resourceId);

		} else {

			// Target/Resources/AnyResource
			target.addNewResources().addNewAnyResource();

		}

		// Target/Actions/Action/ActionMatch
		ActionMatchType actionMatch = target.addNewActions().addNewAction()
				.addNewActionMatch();
		// Target/Actions/Action/ActionMatch/@MatchId
		actionMatch.setMatchId(XACML_FUNCTION_STRING_EQUAL);

		// Target/Actions/Action/ActionMatch/AttributeValue
		AttributeValueType attributeValue = actionMatch.addNewAttributeValue();
		// Target/Actions/Action/ActionMatch/AttributeValue/@DataType
		attributeValue.setDataType(SCHEMA_DATA_TYPE_STRING);
		// Target/Actions/Action/ActionMatch/AttributeValue/text()
		setAttributeValueText(attributeValue, actionValue);

		// Target/Actions/Action/ActionMatch/ActionAttributeDesignator
		AttributeDesignatorType actionAttributeDesignator = actionMatch
				.addNewActionAttributeDesignator();
		// Target/Actions/Action/ActionMatch/ActionAttributeDesignator/@DataType
		actionAttributeDesignator.setDataType(SCHEMA_DATA_TYPE_STRING);
		// Target/Actions/Action/ActionMatch/ActionAttributeDesignator/@AttributeId
		actionAttributeDesignator.setAttributeId(actionId);
	}

	private void addRuleCondition(RuleType rule, String subjectAttributeId,
			String[] subjectAttributeValues) throws PolicyMetadataException {

		// Rule/Condition
		ApplyType condition = rule.addNewCondition();
		// Rule/Condition/@FunctionId
		condition.setFunctionId(XACML_FUNCTION_STRING_AT_LEAST_ONE_MEMBER_OF);

		// Rule/Condition/SubjectAttributeDesignator
		SubjectAttributeDesignatorType subjectAttributeDesignator = condition
				.addNewSubjectAttributeDesignator();
		// Rule/Condition/SubjectAttributeDesignator/@DataType
		subjectAttributeDesignator.setDataType(SCHEMA_DATA_TYPE_STRING);
		// Rule/Condition/SubjectAttributeDesignator/@AttributeId
		subjectAttributeDesignator.setAttributeId(subjectAttributeId);

		// Rule/Condition/Apply
		ApplyType apply = condition.addNewApply();
		// Rule/Condition/Apply/@FunctionId
		apply.setFunctionId(XACML_FUNCTION_STRING_BAG);

		if (subjectAttributeValues != null) {
			for (String value : subjectAttributeValues) {

				// Rule/Condition/Apply/AttributeValue
				AttributeValueType attributeValue = apply
						.addNewAttributeValue();
				// Rule/Condition/Apply/AttributeValue/@DataType
				attributeValue.setDataType(SCHEMA_DATA_TYPE_STRING);
				// Rule/Condition/Apply/AttributeValue/text()
				setAttributeValueText(attributeValue, value);
			}
		}
	}

	private void setAttributeValueText(AttributeValueType attributeValue,
			String text) {
		Text textNode = attributeValue.getDomNode().getOwnerDocument()
				.createTextNode(text);
		attributeValue.getDomNode().appendChild(textNode);
	}
}
