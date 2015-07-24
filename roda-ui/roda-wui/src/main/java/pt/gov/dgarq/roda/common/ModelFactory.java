package pt.gov.dgarq.roda.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.roda.index.IndexService;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;

public class ModelFactory {
	private static final Logger logger = Logger.getLogger(ModelFactory.class);

	private static Path storagePath;
	private static Path indexPath;
	private static StorageService storage;
	private static ModelService model;
	private static IndexService index;

	private static boolean INDEX_FROM_CORPORA = false;

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

			storagePath = Paths.get(RODA_HOME, "data", "storage");
			indexPath = Paths.get(RODA_HOME, "data", "index");
			storage = new FileStorageService(storagePath);
			model = new ModelService(storage);

			// Configure Solr
			// FIXME this should be changed to RODA installation dir
			Path solrHome = Paths.get(ModelFactory.class.getResource("/index/").toURI());
			System.setProperty("solr.data.dir", indexPath.toString());
			System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
			System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
			System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
			System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
			System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
			// FIXME added missing cores

			if (!indexPath.toFile().exists()) {
				INDEX_FROM_CORPORA = true;
			}

			// start embedded solr
			final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

			index = new IndexService(solr, model);

			if (INDEX_FROM_CORPORA) {
				indexPath.toFile().mkdirs();
				storagePath.toFile().mkdirs();
				// Configure Solr
				// FIXME this should be changed to RODA installation dir
				URL solrConfigURL = ModelFactory.class.getResource("/index/solr.xml");
				Path solrConfigPath = Paths.get(solrConfigURL.toURI());
				Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
				Path aipSchema = indexPath.resolve("aip");
				Files.createDirectories(aipSchema);
				Files.createFile(aipSchema.resolve("core.properties"));

				// Copy test corpora
				// FIXME this should be removed for the final release
				Path corporaPath = Paths.get(RODA_HOME, "data2");
				StorageService corporaService = new FileStorageService(corporaPath);
				Iterable<Resource> aips = corporaService.listResourcesUnderContainer(DefaultStoragePath.parse("AIP"));
				for (Resource aip : aips) {
					logger.info("Loading AIP: " + aip.getStoragePath());
					model.createAIP(aip.getStoragePath().getName(), corporaService, aip.getStoragePath());
				}
			}
		} catch (IOException e) {
			logger.error(e);
		} catch (StorageActionException e) {
			logger.error(e);
		} catch (URISyntaxException e) {
			logger.error(e);
		} catch (ModelServiceException e) {
			logger.error(e);
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

}
