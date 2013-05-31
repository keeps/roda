/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Luis Faria
 *
 */
public interface AdvancedSearchConstants extends Constants {

	// Date Interval Picker
	@DefaultStringValue("Any date")
	public String anyDateInterval();
	@DefaultStringValue("Choose date interval")
	public String chooseDateInterval();
	@DefaultStringValue("from")
	public String from();
	@DefaultStringValue("to")
	public String to();
	
	// * Month Picker
	@DefaultStringValue("january")
	public String january();
	@DefaultStringValue("february")
	public String february();
	@DefaultStringValue("march")
	public String march();
	@DefaultStringValue("april")
	public String april();
	@DefaultStringValue("may")
	public String may();
	@DefaultStringValue("june")
	public String june();
	@DefaultStringValue("july")
	public String july();
	@DefaultStringValue("august")
	public String august();
	@DefaultStringValue("september")
	public String september();
	@DefaultStringValue("october")
	public String october();
	@DefaultStringValue("november")
	public String november();
	@DefaultStringValue("december")
	public String december();
	
	// Keyword Picker
	@DefaultStringValue("All words")
	public String allWords();
	@DefaultStringValue("At least one word")
	public String atLeastOneWord();
	@DefaultStringValue("the exact phrase")
	public String exactPhrase();
	@DefaultStringValue("none of the words")
	public String withoutWords();
	
	@DefaultStringValue("All fields")
	public String allFields();
	@DefaultStringValue("Choose fields")
	public String chooseFields();
	@DefaultStringValue("Please fill the keyword")
	public String addingKeywordButItsEmptyAlert();
	@DefaultStringValue("Please choose a field")
	public String addingKeywordButNoFieldsAlert();
	@DefaultStringValue("Please add a keyword")
	public String searchingButNoKeywordsAlert();
	
	
	// Advanced Search
	@DefaultStringValue("Choose description level")
	public String chooseLevelLabel();
	@DefaultStringValue("Choose a date interval")
	public String chooseDateIntervalLabel();
	@DefaultStringValue("Add keywords to search for")
	public String addKeywordsLabel();
	@DefaultStringValue("Search")
	public String search();
	
	
	
	
}
