package pt.gov.dgarq.roda.plugins.ingest.virus;

import java.io.File;

/**
 * This is an interface for any anti-virus software.
 * 
 * @author Rui Castro
 */
public interface AntiVirus {

	/**
	 * Performs a virus check on the specified file or directory.
	 * 
	 * @param file
	 *            a file or directory to scan.
	 * 
	 * @return the results of the virus check as a {@link VirusCheckResult}.
	 * 
	 * @throws RuntimeException
	 *             if some problem prevented the virus check from run a normal
	 *             test.
	 */
	public VirusCheckResult checkForVirus(File file) throws RuntimeException;

}
