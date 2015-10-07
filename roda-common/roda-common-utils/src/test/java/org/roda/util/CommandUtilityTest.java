package org.roda.util;

import org.roda.util.CommandException;
import org.roda.util.CommandUtility;

/**
 * @author Rui Castro
 * 
 */
public class CommandUtilityTest {

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {

      String output = CommandUtility.execute(args);

      //			System.out.println("Output:\n" + output); //$NON-NLS-1$
      System.out.println("Output length: " + output.length()); //$NON-NLS-1$

    } catch (CommandException e) {
      e.printStackTrace();
    }
  }

}
