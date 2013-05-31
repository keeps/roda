/**
 * Search.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Search extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.SearchResult basicSearch(java.lang.String query, int firstResultIndex, int maxResults, int snippetsMax, int fieldMaxLength) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SearchException;
    public pt.gov.dgarq.roda.core.data.SearchResult advancedSearch(pt.gov.dgarq.roda.core.data.SearchParameter[] searchParameters, int firstResultIndex, int maxResults, int snippetsMax, int fieldMaxLength) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SearchException;
}
