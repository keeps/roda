package pt.gov.dgarq.roda.core.services;

/**
 * @author Rui Castro
 */
public class StringFormatTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		float percentage = 100.0f;
		
		System.out.println(String.format(
				"UPDATE SIPs SET state='%s', complete=%b, percentage=%f, pid=%s, parent_pid=%s"
						+ " WHERE id='%s'", "XXX", true, percentage, "NULL", "NULL",
				"yyy"));
	}

}
