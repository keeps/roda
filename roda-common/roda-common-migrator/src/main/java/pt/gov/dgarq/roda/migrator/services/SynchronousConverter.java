package pt.gov.dgarq.roda.migrator.services;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.InvalidRepresentationException;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;

/**
 * This interface should be implemented by all RODA Converters.
 * 
 * @author Rui Castro
 */
public interface SynchronousConverter extends Remote {

	/**
	 * Get the agent used in this migration
	 * 
	 * @return the agent
	 * @throws ConverterException
	 * @throws RemoteException
	 */
	public AgentPreservationObject getAgent() throws ConverterException,
			RemoteException;

	/**
	 * Convert/Migrate the representation
	 * 
	 * @param representation
	 *            the representation to be converted/migrated
	 * @return the converted/migrated representation
	 * @throws ConverterException
	 * @throws RemoteException
	 */
	public ConversionResult convert(RepresentationObject representation)
			throws RepresentationAlreadyConvertedException,
			InvalidRepresentationException, WrongRepresentationTypeException,
			WrongRepresentationSubtypeException, ConverterException,
			RemoteException;

}
