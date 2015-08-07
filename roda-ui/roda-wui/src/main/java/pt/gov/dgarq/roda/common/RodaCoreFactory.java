package pt.gov.dgarq.roda.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.roda.index.IndexServiceException;
import org.roda.index.IndexService;
import org.roda.model.ModelService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;

public class RodaCoreFactory {
	private static final Logger LOGGER = Logger.getLogger(RodaCoreFactory.class);

	private static Path storagePath;
	private static Path indexPath;
	private static Path dataPath;
	private static Path logPath;
	private static Path configPath;
	private static StorageService storage;
	private static ModelService model;
	private static IndexService index;
	private static EmbeddedSolrServer solr;

	static {
		try {
			String RODA_HOME;
			if (System.getProperty("roda.home") != null) {
				RODA_HOME = System.getProperty("roda.home");
			} else if (System.getenv("RODA_HOME") != null) {
				RODA_HOME = System.getenv("RODA_HOME");
			} else {
				RODA_HOME = null;
			}

			dataPath = Paths.get(RODA_HOME, "data");
			logPath = dataPath.resolve("log");
			configPath = Paths.get(RODA_HOME, "config");

			storagePath = dataPath.resolve("storage");
			indexPath = dataPath.resolve("index");
			
			storage = new FileStorageService(storagePath);
			model = new ModelService(storage);

			// Configure Solr
			Path solrHome = Paths.get(RODA_HOME, "config", "index");
			if (!Files.exists(solrHome)) {
				solrHome = Paths.get(RodaCoreFactory.class.getResource("/index/").toURI());
			}

			System.setProperty("solr.data.dir", indexPath.toString());
			System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
			System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
			System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
			System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
			System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
			System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
			// FIXME added missing cores

			// start embedded solr
			solr = new EmbeddedSolrServer(solrHome, "test");

			index = new IndexService(solr, model);

		} catch (StorageServiceException e) {
			LOGGER.error(e);
		} catch (URISyntaxException e) {
			LOGGER.error(e);
		}
	}

	public static StorageService getStorageService() {
		return storage;
	}

	public static ModelService getModelService() {
		return model;
	}

	public static IndexService getIndexService() {
		return index;
	}

	public static Path getConfigPath() {
		return configPath;
	}

	public static Path getDataPath() {
		return dataPath;
	}
	
	public static Path getLogPath() {
		return logPath;
	}

	public static void closeSolrServer() {
		try {
			solr.close();
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public static void main(String[] argsArray) {
		List<String> args = Arrays.asList(argsArray);
		if (Arrays.asList("reindex").equals(args)) {
			try {
				index.reindexAIPs();
			} catch (IndexServiceException e) {
				System.err.println("An error has occured while reindexing");
				e.printStackTrace();
			}
		} else {
			System.err.println("Syntax: [java -jar ...] reindex");
		}
		System.exit(0);
	}

}
