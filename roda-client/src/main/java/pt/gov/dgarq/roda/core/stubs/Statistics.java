/**
 * Statistics.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Statistics extends java.rmi.Remote {
    public void insertStatisticData(pt.gov.dgarq.roda.core.data.StatisticData statisticData) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.StatisticsException;
    public void insertStatisticDataList(pt.gov.dgarq.roda.core.data.StatisticData[] statisticData) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.StatisticsException;
}
