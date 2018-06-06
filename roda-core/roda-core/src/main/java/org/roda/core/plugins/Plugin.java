/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;

/**
 * This interface should be implemented by any class that want to be a RODA
 * plugin.
 * 
 * @author Luis Faria<lfaria@keep.p>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public interface Plugin<T extends IsRODAObject> {

  /**
   * Returns the name of this {@link Plugin}.
   * 
   * @return a {@link String} with the name of this {@link Plugin}.
   */
  String getName();

  /**
   * Returns the version of this {@link Plugin}.
   * 
   * @return a <code>String</code> with the version number for this
   *         {@link Plugin}.
   */
  String getVersion();

  /**
   * Returns description of this {@link Plugin}.
   * 
   * @return a {@link String} with the description of this {@link Plugin}.
   */
  String getDescription();

  /**
   * Returns the type of the agent linked to this {@link Plugin}.
   * 
   * @return a {@link PreservationAgentType} with the type of the agent of this
   *         {@link Plugin}.
   */
  PreservationAgentType getAgentType();

  /**
   * Returns the type of the execution preservation event linked to this
   * {@link Plugin}.
   * 
   * @return a {@link PreservationEventType} with the type of the execution event
   *         of this {@link Plugin}.
   */
  PreservationEventType getPreservationEventType();

  /**
   * Returns the description of the execution preservation event linked to this
   * {@link Plugin}.
   * 
   * @return a {@link String} with the description of the execution event of this
   *         {@link Plugin}.
   */
  String getPreservationEventDescription();

  /**
   * Returns the success message of the execution preservation event linked to
   * this {@link Plugin}.
   * 
   * @return a {@link String} with the success message of the execution event of
   *         this {@link Plugin}.
   */
  String getPreservationEventSuccessMessage();

  /**
   * Returns the failure message of the execution preservation event linked to
   * this {@link Plugin}.
   * 
   * @return a {@link String} with the failure message of the execution event of
   *         this {@link Plugin}.
   */
  String getPreservationEventFailureMessage();

  /**
   * Returns the {@link List} of {@link PluginParameter}s necessary to run this
   * {@link Plugin}.
   * 
   * @return a {@link List} of {@link PluginParameter} with the parameters.
   */
  List<PluginParameter> getParameters();

  /**
   * Gets the parameter values inside a {@link Map} with attribute names and
   * values.
   * 
   * @return a {@link Map} with the parameters name and value.
   */
  Map<String, String> getParameterValues();

  /**
   * Sets the parameters returned by a previous call to
   * {@link Plugin#getParameters()}.
   * 
   * @param parameters
   *          a {@link List} of parameters.
   * 
   * @throws InvalidParameterException
   */
  void setParameterValues(Map<String, String> parameters) throws InvalidParameterException;

  /**
   * Method to return Plugin type (so it can be grouped for different purposes)
   */
  PluginType getType();

  /**
   * Method to return Plugin categories
   */
  List<String> getCategories();

  /**
   * Method used by PluginManager to obtain a new instance of a plugin, from the
   * current loaded Plugin, to provide to PluginOrchestrator
   */
  Plugin<T> cloneMe();

  /**
   * Method that validates the parameters provided to the Plugin
   * 
   * FIXME this should be changed to return a report
   */
  boolean areParameterValuesValid();

  /*
   * "Working methods"
   * ___________________________________________________________________________________________________________
   */

  /**
   * Initializes this {@link Plugin}. This method is called by the
   * {@link PluginManager} before any other methods in the plugin.
   * 
   * @throws PluginException
   */
  void init() throws PluginException;

  List<Class<T>> getObjectClasses();

  /**
   * Method to be invoked by the PluginOrchestrator to inject the job plugin info
   * to be used by the plugin
   */
  void injectJobPluginInfo(JobPluginInfo jobPluginInfo);

  /**
   * Method retrieve job plugin info of a certain class
   */
  <T1 extends JobPluginInfo> T1 getJobPluginInfo(Class<T1> jobPluginInfoClass);

  /**
   * Method executed by {@link PluginOrchestrator} before splitting the workload
   * (if it makes sense) by N workers
   * 
   * @throws PluginException
   */
  Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException;

  /**
   * Executes the {@link Plugin}.
   * 
   * @return a {@link Report} of the actions performed.
   * 
   * @throws PluginException
   */
  Report execute(IndexService index, ModelService model, StorageService storage, List<LiteOptionalWithCause> list)
    throws PluginException;

  /**
   * Method executed by {@link PluginOrchestrator} after all workers have finished
   * their work
   * 
   * @throws PluginException
   */
  Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException;

  /**
   * Stops all {@link Plugin} activity. This is the last method to be called by
   * {@link PluginManager} on the {@link Plugin}.
   */
  void shutdown();

}
