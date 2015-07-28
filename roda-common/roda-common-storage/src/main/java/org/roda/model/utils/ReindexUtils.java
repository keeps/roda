package org.roda.model.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.index.IndexModelObserver;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;

public class ReindexUtils {

	public static void reindexAIP(ModelService model,SolrClient index) throws ModelServiceException{
		IndexModelObserver observer = new IndexModelObserver(index, model);
		Iterable<AIP> aips =  model.listAIPs();
		for(AIP aip : aips){
			observer.aipCreated(aip);
		}
	}
	
	public static void reindexActionLog(ModelService model,SolrClient index) throws ModelServiceException, StorageActionException{
		IndexModelObserver observer = new IndexModelObserver(index, model);
		Iterable<Resource> actionLogs = model.getStorage().listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG));
		Iterator<Resource> it = actionLogs.iterator();
		while(it.hasNext()){
			Resource r = it.next();
			try{
				Binary b = model.getStorage().getBinary(r.getStoragePath());
				java.io.File f = new java.io.File(b.getContent().getURI().getPath());
				try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				       LogEntry entry = ModelUtils.getLogEntry(line);
				       observer.logEntryCreated(entry);
				    }
				}
			}catch(IOException e){
				
			}
		}
	}
	
}
