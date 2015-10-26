/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.statistics.client;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Luis Faria
 * 
 */
public class StatisticFunction implements Serializable {

  private static final long serialVersionUID = -7985028054716773679L;

  /**
   * Available functions
   * 
   * @author Luis Faria
   * 
   */
  public static enum FunctionType {
    /**
     * Aggregate function, aggregates a series of entries over a time interval
     * into a single entry on that interval
     */
    AGGREGATION_ADD, AGGREGATION_LAST, AGGREGATION_AVERAGE,
    /**
     * Calculates new entries based on the difference between two consecutive
     * entries
     */
    DELTA,
    /**
     * Fill the empty spaces with a given statistics type and value
     */
    FILLER,
    /**
     * Set maximum number of results, using sampling to reduce number
     */
    SAMPLING
  }

  private FunctionType function;
  private String[] arguments;

  /**
   * Empty constructor
   */
  public StatisticFunction() {
  }

  /**
   * Create a new statistic function information package
   * 
   * @param function
   *          the selected function {@link FunctionType}
   * @param arguments
   *          the function arguments
   */
  public StatisticFunction(FunctionType function, String... arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  /**
   * Get the function
   * 
   * @return the selected function {@link FunctionType}
   */
  public FunctionType getFunction() {
    return function;
  }

  /**
   * Set the function
   * 
   * @param function
   */
  public void setFunction(FunctionType function) {
    this.function = function;
  }

  /**
   * Get the function arguments
   * 
   * @return a list with the arguments
   */
  public String[] getArguments() {
    return arguments;
  }

  /**
   * Set the function arguments
   * 
   * @param arguments
   */
  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

  public String toString() {
    return "StatisticFunction(function=" + function + ", args=" + Arrays.toString(arguments) + ")";
  }
}
