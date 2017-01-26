/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;

public class AkkaDistributedPluginWorker extends AkkaDistributedPlugin {
  // XXX this uses a single threaded worker (as opposite to
  // AkkaEmbeddedPluginOrchestrator which is multi-threaded)
  private ActorSystem workerSystem;
  private ActorRef clusterClient;
  private ActorRef worker;

  public AkkaDistributedPluginWorker(String clusterHostname, String clusterPort, String hostname, String port) {
    super();

    String clusterSystemName = "ClusterSystem";
    String clusterSystemPath = clusterSystemName + "@" + clusterHostname + ":" + clusterPort;

    Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)
      .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port))
      .withFallback(ConfigFactory.parseString(
        "akka.cluster.client.initial-contacts=[\"akka.tcp://" + clusterSystemPath + "/system/receptionist\"]"))
      .withFallback(ConfigFactory.load("config/orchestrator/worker"));

    workerSystem = ActorSystem.create("WorkerSystem", conf);

    clusterClient = workerSystem.actorOf(ClusterClient.props(ClusterClientSettings.create(workerSystem)),
      "clusterClient");
    worker = workerSystem.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");
  }

}
