package pt.gov.dgarq.roda.core.data.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.SearchParameter;

/**
 * This is the default search parameter for the search service. It should be
 * used for fields whose values are a list of words of a phrase.
 * 
 * @author Rui Castro
 */
public class DefaultSearchParameter extends SearchParameter {
	private static final long serialVersionUID = -8868823386339116329L;

	// AT_LEAST_ONE_WORD (k1 or k2)
	// ALL_WORDS (k2 and k2)
	// EXACT_PHRASE ""
	// WITHOUT_WORDS (NOT k1)

	/**
	 * At least one word must appear
	 */
	public final static int MATCH_AT_LEAST_ONE_WORD = 0;

	/**
	 * The exact phrase must appear
	 */
	public final static int MATCH_EXACT_PHRASE = 1;

	/**
	 * Any of the words should not appear
	 */
	public final static int MATCH_WITHOUT_WORDS = 2;

	/**
	 * All words must appear
	 */
	public final static int MATCH_ALL_WORDS = 3;

	private int constraint = MATCH_AT_LEAST_ONE_WORD;
	private List<String> fields = new ArrayList<String>();
	private String value = null;

	/**
	 * Constructs an empty {@link DefaultSearchParameter}.
	 */
	public DefaultSearchParameter() {
	}

	/**
	 * Constructs a new {@link DefaultSearchParameter} cloning an existing
	 * {@link DefaultSearchParameter}.
	 * 
	 * @param searchParameter
	 *            the {@link DefaultSearchParameter} to clone.
	 */
	public DefaultSearchParameter(DefaultSearchParameter searchParameter) {
		this(searchParameter.getFields(), searchParameter.getValue(),
				searchParameter.getConstraint());
	}

	/**
	 * Constructs a new {@link DefaultSearchParameter} with the given
	 * parameters.
	 * 
	 * @param fields
	 *            the fields of the parameter.
	 * @param value
	 *            the value of the parameter.
	 * @param constraint
	 * 
	 * @throws IllegalArgumentException
	 *             if constraint is not one of this values:
	 *             <ul>
	 *             <li> {@link DefaultSearchParameter#MATCH_AT_LEAST_ONE_WORD}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_EXACT_PHRASE}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_WITHOUT_WORDS}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_ALL_WORDS}</li>
	 *             </ul>
	 */
	public DefaultSearchParameter(String[] fields, String value, int constraint)
			throws IllegalArgumentException {
		setFields(fields);
		setValue(value);
		setConstraint(constraint);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof DefaultSearchParameter) {
			DefaultSearchParameter other = (DefaultSearchParameter) obj;
			return this.getConstraint() == other.getConstraint()
					&& this.fields.equals(other.fields)
					&& this.getValue().equals(other.getValue());
		} else {
			return false;
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "DefaultSearchParameter (" + constraintToString(getConstraint())
				+ "," + Arrays.asList(getFields()) + ", " + getValue() + ")";
	}

	private String constraintToString(int constraint2) {

		String constraintString = null;

		switch (constraint2) {
		case MATCH_AT_LEAST_ONE_WORD:
			constraintString = "MATCH_AT_LEAST_ONE_WORD";
			break;
		case MATCH_EXACT_PHRASE:
			constraintString = "MATCH_EXACT_PHRASE";
			break;
		case MATCH_WITHOUT_WORDS:
			constraintString = "MATCH_WITHOUT_WORDS";
			break;
		case MATCH_ALL_WORDS:
			constraintString = "MATCH_ALL_WORDS";
			break;
		}

		return constraintString;
	}

	/**
	 * Returns the fields of this parameter.
	 * 
	 * @return the fields
	 */
	public String[] getFields() {
		return (String[]) fields.toArray(new String[fields.size()]);
	}

	/**
	 * Sets the list of fields.
	 * 
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(String[] fields) {
		this.fields.clear();
		if (fields != null) {
			this.fields.addAll(Arrays.asList(fields));
		}
	}

	/**
	 * Adds a new field name to the list of fields.
	 * 
	 * @param field
	 *            the name of the new field to add.
	 * @return true if the field was added, false otherwise.
	 */
	public boolean addField(String field) {
		return this.fields.add(field);
	}

	/**
	 * Removes a field name to the list of fields.
	 * 
	 * @param field
	 *            the name of the field to remove.
	 * @return <code>true</code> if the field was removed, <code>false</code>
	 *         otherwise.
	 */
	public boolean removeField(String field) {
		return this.fields.remove(field);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the constraint
	 */
	public int getConstraint() {
		return constraint;
	}

	/**
	 * @param constraint
	 *            the constraint to set
	 * 
	 * @throws IllegalArgumentException
	 *             if constraint is not one of this values:
	 *             <ul>
	 *             <li> {@link DefaultSearchParameter#MATCH_AT_LEAST_ONE_WORD}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_EXACT_PHRASE}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_WITHOUT_WORDS}</li>
	 *             <li> {@link DefaultSearchParameter#MATCH_ALL_WORDS}</li>
	 *             </ul>
	 */
	public void setConstraint(int constraint) throws IllegalArgumentException {
		if (constraint == MATCH_EXACT_PHRASE
				|| constraint == MATCH_AT_LEAST_ONE_WORD
				|| constraint == MATCH_WITHOUT_WORDS
				|| constraint == MATCH_ALL_WORDS) {
			this.constraint = constraint;
		} else {
			throw new IllegalArgumentException("unknown constraint "
					+ constraint);
		}
	}

	/**
	 * Get Lucene sub query relative to this parameter
	 * 
	 * @return the sub query
	 */
	public String getSubQuery() {

		String subQuery = "";

		switch (getConstraint()) {
		case DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD:

			if (fields.size() > 1) {
				subQuery += "(";
			}
			for (int i = 0; i < fields.size(); i++) {
				subQuery += ((String) fields.get(i)) + ":(";
				subQuery += escapedKeyword(getValue().trim().replaceAll("\\s+",
						" OR "));
				subQuery += ")";
				if (i + 1 < fields.size()) {
					subQuery += " OR ";
				}
			}
			if (fields.size() > 1) {
				subQuery += ")";
			}

			break;

		case DefaultSearchParameter.MATCH_ALL_WORDS:

			if (fields.size() > 1) {
				subQuery += "(";
			}
			for (int i = 0; i < fields.size(); i++) {
				subQuery += ((String) fields.get(i)) + ":(";
				subQuery += escapedKeyword(getValue().trim().replaceAll("\\s+",
						" AND "));
				subQuery += ")";
				if (i + 1 < fields.size()) {
					subQuery += " OR ";
				}
			}
			if (fields.size() > 1) {
				subQuery += ")";
			}

			break;

		case DefaultSearchParameter.MATCH_EXACT_PHRASE:

			if (fields.size() > 1) {
				subQuery += "(";
			}
			for (int i = 0; i < fields.size(); i++) {
				subQuery += ((String) fields.get(i)) + ":\"";
				subQuery += escapedKeyword(getValue().trim());
				subQuery += "\"";
				if (i + 1 < fields.size()) {
					subQuery += " OR ";
				}
			}
			if (fields.size() > 1) {
				subQuery += ")";
			}

			break;

		case DefaultSearchParameter.MATCH_WITHOUT_WORDS:

			if (fields.size() > 1) {
				subQuery += "(";
			}
			for (int i = 0; i < fields.size(); i++) {
				subQuery += "NOT " + ((String) fields.get(i)) + ":(";
				subQuery += escapedKeyword(getValue().trim().replaceAll("\\s+",
						" OR "));
				subQuery += ")";
				if (i + 1 < fields.size()) {
					subQuery += " AND ";
				}
			}
			if (fields.size() > 1) {
				subQuery += ")";
			}

			break;

		default:
		}

		super.setSubQuery(subQuery);
		return subQuery;
	}

	/**
	 * This method has no effect. To set the subquery use methods
	 * {@link DefaultSearchParameter#setFields(String[])},
	 * {@link DefaultSearchParameter#setValue(String)} and
	 * {@link DefaultSearchParameter#setConstraint(int)}.
	 * 
	 * @param subQuery
	 */
	public void setSubQuery(String subQuery) {
		// super.setSubQuery(subQuery);
	}

	private String escapedKeyword(String keyword) {
		return keyword.replaceAll("\\:", "\\\\:");
	}

}
