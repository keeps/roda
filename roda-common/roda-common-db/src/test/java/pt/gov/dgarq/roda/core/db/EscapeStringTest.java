package pt.gov.dgarq.roda.core.db;

import java.util.regex.Matcher;

/**
 * @author tobias
 * 
 */
public class EscapeStringTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String value = "C:\\Documents and 'me'";

		System.out.println("Not escaped value: " + value);

		String escapedValue = escapeValue(value);

		System.out.println("Escaped value: " + escapedValue);
	}

	protected static String escapeValue(String value) {

		String escapedValue = value.replaceAll("\\\\", Matcher
				.quoteReplacement("\\\\"));
		escapedValue = escapedValue.replaceAll("'", Matcher
				.quoteReplacement("\\'"));

		return escapedValue;
	}

}
