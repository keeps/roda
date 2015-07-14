package org.roda;

import java.nio.file.Paths;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.fedora.FedoraStorageService;
import org.roda.storage.fs.FileStorageService;

public class TestFileSystemToFedoraToSolr {

	public static void main(String[] args) throws StorageActionException {
		try {
			FedoraStorageService fedora = new FedoraStorageService(
					"http://localhost:8383/fcrepo/rest/");
			FileStorageService fss = new FileStorageService(
					Paths.get("/home/sleroux/Development/roda-worker/roda-corpora/"));
			ModelService ms = new ModelService(fedora);
			IndexService is = new IndexService(new HttpSolrClient(
					"http://localhost:8984/solr/"), ms);
			ms.createAIP("AIP_1", fss, DefaultStoragePath.parse("AIP/AIP_1"));
			// is.descriptiveMetadataBinaryDeleted("AIP_1", "ead-c.xml");
			AIP aip = is.retrieveAIP("AIP_1");
			if (aip.getDescriptiveMetadataIds() != null) {
				System.out.println("BINARIES: "
						+ aip.getDescriptiveMetadataIds().size());
			} else {
				System.out.println("BINARIES NULL");
			}
			// DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
			// AIP aipNew = new AIP(aip.getId(), aip.isActive(),
			// df.parse("20-01-1980"), df.parse("20-01-1982"),
			// aip.getDescriptiveMetadataIds(),
			// aip.getRepresentationIds());
			// is.aipUpdated(aipNew);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
