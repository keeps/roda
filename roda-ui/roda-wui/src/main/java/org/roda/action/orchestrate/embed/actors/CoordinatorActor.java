/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.orchestrate.embed.actors;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

public class CoordinatorActor extends UntypedActor {
  private final Logger logger = Logger.getLogger(getClass());

  public CoordinatorActor() {

  }

  @Override
  public void onReceive(Object msg) throws Exception {
    logger.info("message: " + msg);
  }

}