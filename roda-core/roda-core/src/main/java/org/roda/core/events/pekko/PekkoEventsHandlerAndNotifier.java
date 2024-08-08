/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.events.pekko;

import java.io.IOException;
import java.io.Serial;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Address;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.Terminated;
import org.apache.pekko.cluster.Cluster;
import org.apache.pekko.dispatch.OnComplete;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.pekko.PekkoUtils;
import org.roda.core.common.pekko.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.events.AbstractEventsHandler;
import org.roda.core.events.EventsHandler;
import org.roda.core.events.EventsNotifier;
import org.roda.core.index.utils.ZkController;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class PekkoEventsHandlerAndNotifier extends AbstractEventsHandler implements EventsNotifier {
  @Serial
  private static final long serialVersionUID = 919188071375009042L;
  private static final Logger LOGGER = LoggerFactory.getLogger(PekkoEventsHandlerAndNotifier.class);

  private static final String EVENTS_SYSTEM = "EventsSystem";

  private ActorSystem eventsSystem;
  private ActorRef eventsNotifierAndHandlerActor;
  private String instanceSenderId;
  private boolean shuttingDown = false;

  public PekkoEventsHandlerAndNotifier() {
    Config pekkoConfig = PekkoUtils.getPekkoConfiguration("events.conf");
    eventsSystem = ActorSystem.create(EVENTS_SYSTEM, pekkoConfig);

    List<Address> seedNodesAddresses = getSeedNodesAddresses();
    if (seedNodesAddresses.isEmpty()) {
      LOGGER.warn(
        "Found no seed nodes addresses (in any source, i.e., 1) system properties; 2) env. variables; 3) properties files)");
    }
    Cluster.get(eventsSystem).joinSeedNodes(seedNodesAddresses);

    eventsNotifierAndHandlerActor = instantiateEventsNotifierAndHandlerActor();
    instanceSenderId = eventsNotifierAndHandlerActor.toString();
  }

  private ActorRef instantiateEventsNotifierAndHandlerActor() {
    String writeConsistency = RodaCoreFactory.getProperty("core.events.pekko.writeConsistency", "");
    int writeConsistencyTimeoutInSeconds = RodaCoreFactory
      .getProperty("core.events.pekko.writeConsistencyTimeoutInSeconds", 3);
    return eventsSystem.actorOf(Props.create(PekkoEventsHandlerAndNotifierActor.class, (EventsHandler) this,
      writeConsistency, writeConsistencyTimeoutInSeconds), "eventsNotifierAndHandlerActor");
  }

  @Override
  public void notifyUserCreated(ModelService model, User user) {
    LOGGER.debug("notifyUserCreated '{}'", user);
    eventsNotifierAndHandlerActor.tell(Messages.newEventUserCreated(user, instanceSenderId),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, User updatedUser) {
    LOGGER.debug("notifyUserUpdated '{}'", user);
    eventsNotifierAndHandlerActor.tell(Messages.newEventUserUpdated(user, false, instanceSenderId),
      ActorRef.noSender());
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, User updatedUser) {
    LOGGER.debug("notifyMyUserUpdated '{}'", user);
    eventsNotifierAndHandlerActor.tell(Messages.newEventUserUpdated(user, true, instanceSenderId),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserDeleted(ModelService model, String id) {
    LOGGER.debug("notifyUserDeleted '{}'", id);
    eventsNotifierAndHandlerActor.tell(Messages.newEventUserDeleted(id, instanceSenderId), ActorRef.noSender());
  }

  @Override
  public void notifyGroupCreated(ModelService model, Group group) {
    LOGGER.debug("notifyGroupCreated '{}'", group);
    eventsNotifierAndHandlerActor.tell(Messages.newEventGroupCreated(group, instanceSenderId), ActorRef.noSender());
  }

  @Override
  public void notifyGroupUpdated(ModelService model, Group group, Group updatedGroup) {
    LOGGER.debug("notifyGroupUpdated '{}'", group);
    eventsNotifierAndHandlerActor.tell(Messages.newEventGroupUpdated(group, instanceSenderId), ActorRef.noSender());
  }

  @Override
  public void notifyGroupDeleted(ModelService model, String id) {
    LOGGER.debug("notifyGroupDeleted '{}'", id);
    eventsNotifierAndHandlerActor.tell(Messages.newEventGroupDeleted(id, instanceSenderId), ActorRef.noSender());
  }

  @Override
  public void shutdown() {
    if (!shuttingDown) {
      shuttingDown = true;

      LOGGER.info("Going to shutdown EVENTS actor system");
      Cluster cluster = Cluster.get(eventsSystem);
      cluster.leave(cluster.selfAddress());

      Future<Terminated> terminate = eventsSystem.terminate();
      terminate.onComplete(new OnComplete<Terminated>() {
        @Override
        public void onComplete(Throwable failure, Terminated result) {
          if (failure != null) {
            LOGGER.error("Error while shutting down EVENTS actor system", failure);
          } else {
            LOGGER.info("Done shutting down EVENTS actor system");
          }
        }
      }, eventsSystem.dispatcher());

      try {
        LOGGER.info("Waiting up to 30 seconds for EVENTS actor system  to shutdown");
        Await.result(eventsSystem.whenTerminated(), Duration.create(30, "seconds"));
      } catch (TimeoutException e) {
        LOGGER.warn("EVENTS Actor system shutdown wait timed out, continuing...");
      } catch (Exception e) {
        LOGGER.error("Error while shutting down EVENTS actor system", e);
      }
    }
  }

  private List<Address> getSeedNodesAddresses() {
    List<Address> seedNodes = new ArrayList<>();

    if (RodaCoreFactory.getProperty("core.events.pekko.seeds_via_list", false)) {
      int i = 1;
      while (i != -1) {
        String seed = RodaCoreFactory.getProperty("core.events.pekko.seeds." + i, null);
        if (seed != null) {
          processAndAddSeedNode(seedNodes, seed);
          i++;
        } else {
          i = -1;
        }
      }
    } else {
      ZooKeeper zkClient;
      try {
        String connectString = RodaCoreFactory.getProperty(RodaConstants.CORE_SOLR_CLOUD_URLS, "localhost:2181");
        String zkSeedsNode = RodaCoreFactory.getProperty("core.events.pekko.zk.seeds_path", "/akka/nodes");
        String chRootPath = connectString + zkSeedsNode;
        ZkController.checkChrootPath(chRootPath, true);

        zkClient = new ZooKeeper(connectString, 2000, event -> {
          // do nothing and carry on
        });

        zkClient.getChildren(zkSeedsNode, false).forEach(seed -> {
          processAndAddSeedNode(seedNodes, seed);
        });

        String separator = RodaCoreFactory.getProperty("core.events.pekko.address.separator", ":");

        String hostName = InetAddress.getLocalHost().getHostAddress() + separator + "2552";
        zkClient.create(zkSeedsNode + "/" + hostName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        // When using joinSeedNodes you should not include the node itself except for
        // the node that is supposed to be the first seed node
        if (seedNodes.isEmpty()) {
          processAndAddSeedNode(seedNodes, hostName);
        }

      } catch (IOException | KeeperException | InterruptedException e) {
        // do nothing and carry on
      }
    }

    return seedNodes;
  }

  private void processAndAddSeedNode(List<Address> seedNodes, String node) {
    if (StringUtils.isBlank(node)) {
      return;
    }
    try {
      String separator = RodaCoreFactory.getProperty("core.events.pekko.address.separator", ":");
      String[] nodeParts = node.split(separator, 2);
      seedNodes.add(new Address("pekko.tcp", EVENTS_SYSTEM, nodeParts[0], Integer.parseInt(nodeParts[1])));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      // do nothing and carry on
    }
  }

}
