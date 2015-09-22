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