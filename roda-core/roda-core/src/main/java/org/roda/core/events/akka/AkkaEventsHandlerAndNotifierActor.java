/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.events.akka;

import java.io.Serial;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.cluster.Cluster;
import org.apache.pekko.cluster.ddata.DistributedData;
import org.apache.pekko.cluster.ddata.GSet;
import org.apache.pekko.cluster.ddata.GSetKey;
import org.apache.pekko.cluster.ddata.Key;
import org.apache.pekko.cluster.ddata.ORMap;
import org.apache.pekko.cluster.ddata.Replicator;
import org.apache.pekko.cluster.ddata.Replicator.Changed;
import org.apache.pekko.cluster.ddata.Replicator.Update;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.akka.Messages.EventGroupCreated;
import org.roda.core.common.akka.Messages.EventGroupDeleted;
import org.roda.core.common.akka.Messages.EventGroupUpdated;
import org.roda.core.common.akka.Messages.EventUserCreated;
import org.roda.core.common.akka.Messages.EventUserDeleted;
import org.roda.core.common.akka.Messages.EventUserUpdated;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.events.EventsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import scala.concurrent.duration.Duration;

public class AkkaEventsHandlerAndNotifierActor extends AbstractActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEventsHandlerAndNotifierActor.class);

  private static final String CACHE_PREFIX = "cache-";
  private static final String USER_KEY_PREFIX = "user-";
  private static final String GROUP_KEY_PREFIX = "group-";
  private static final String RODA_OBJECT_OTHER_INFO_PASSWORD = "password";

  private final ActorRef replicator = DistributedData.get(context().system()).replicator();
  private final Cluster cluster = Cluster.get(context().system());

  private EventsHandler eventsHandler;
  private String instanceSenderId;

  private final Key<GSet<ObjectKey>> objectKeysKey = GSetKey.create("objectKeys");
  private Set<ObjectKey> objectKeys = new HashSet<>();

  private final Replicator.WriteConsistency writeConsistency;

  public AkkaEventsHandlerAndNotifierActor(final EventsHandler eventsHandler, final String writeConsistency,
    final int writeConsistencyTimeoutInSeconds) {
    this.eventsHandler = eventsHandler;
    this.instanceSenderId = self().toString();
    this.writeConsistency = instantiateWriteConsistency(writeConsistency, writeConsistencyTimeoutInSeconds);
  }

  private Replicator.WriteConsistency instantiateWriteConsistency(String writeConsistency,
    final int writeConsistencyTimeoutInSeconds) {
    if ("WriteAll".equalsIgnoreCase(writeConsistency)) {
      return new Replicator.WriteAll(Duration.create(writeConsistencyTimeoutInSeconds, TimeUnit.SECONDS));
    } else {
      return new Replicator.WriteMajority(Duration.create(writeConsistencyTimeoutInSeconds, TimeUnit.SECONDS));
    }

  }

  @Override
  public void preStart() {
    Replicator.Subscribe<GSet<ObjectKey>> subscribe = new Replicator.Subscribe<>(objectKeysKey, getSelf());
    replicator.tell(subscribe, ActorRef.noSender());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(EventUserCreated.class, e -> handleUserCreated(e))
      .match(EventUserUpdated.class, e -> handleUserUpdated(e)).match(EventUserDeleted.class, e -> handleUserDeleted(e))
      .match(EventGroupCreated.class, e -> handleGroupCreated(e))
      .match(EventGroupUpdated.class, e -> handleGroupUpdated(e))
      .match(EventGroupDeleted.class, e -> handleGroupDeleted(e)).match(Replicator.Changed.class, c -> handleChanged(c))
      .match(Replicator.UpdateSuccess.class, e -> handleUpdateSuccess(e))
      .match(Replicator.UpdateFailure.class, e -> handleUpdateFailure(e))
      .matchAny(msg -> {
        LOGGER.warn("Received unknown message '{}'", msg);
      }).build();
  }

  private void handleUpdateSuccess(Replicator.UpdateSuccess e) {
    // LOGGER.info("handleUpdateSuccess '{}'", e);
    // FIXME 20180925 hsilva: do nothing???
  }

  private void handleUpdateFailure(Replicator.UpdateFailure e) {
    // LOGGER.info("handleUpdateFailure '{}'", e);
    // FIXME 20180925 hsilva: what to do???
  }

  private void handleChanged(Replicator.Changed<?> e) {
    if (e.key().equals(objectKeysKey)) {
      handleObjectKeysChanged((Replicator.Changed<GSet<ObjectKey>>) e);
    } else if (e.key() instanceof ObjectKey) {
      handleObjectChanged((Changed<ORMap<String, CRDTWrapper>>) e);
    }
  }

  private void handleObjectKeysChanged(Replicator.Changed<GSet<ObjectKey>> e) {
    Set<ObjectKey> newKeys = e.dataValue().getElements();
    Set<ObjectKey> keysToSubscribe = new HashSet<>(newKeys);
    keysToSubscribe.removeAll(objectKeys);
    keysToSubscribe.forEach(keyToSubscribe -> {
      // subscribe to get notifications of when objects with this name are
      // added or removed
      replicator.tell(new Replicator.Subscribe<>(keyToSubscribe, self()), self());
    });
    objectKeys = newKeys;

    // 20180925 hsilva: to improve GC
    keysToSubscribe = null;
  }

  private void handleObjectChanged(Replicator.Changed<ORMap<String, CRDTWrapper>> e) {
    String objectId = e.key().id().replaceFirst(CACHE_PREFIX, "");
    Option<CRDTWrapper> option = e.dataValue().get(objectId);
    if (option.isDefined()) {
      CRDTWrapper wrapper = (CRDTWrapper) option.get();
      if (!wrapper.getInstanceId().equals(instanceSenderId)) {
        if (objectId.startsWith(USER_KEY_PREFIX)) {
          try(SecureString password = new SecureString(getUserPasswordFromRodaUserOtherInfoMap(wrapper).toCharArray())) {
            if (!wrapper.isUpdate()) {
              eventsHandler.handleUserCreated(RodaCoreFactory.getModelService(), (User) wrapper.getRodaObject(),
                      password);
            } else {
              eventsHandler.handleUserUpdated(RodaCoreFactory.getModelService(), (User) wrapper.getRodaObject(),
                      password);
            }
          }
        } else if (objectId.startsWith(GROUP_KEY_PREFIX)) {
          if (!wrapper.isUpdate()) {
            eventsHandler.handleGroupCreated(RodaCoreFactory.getModelService(), (Group) wrapper.getRodaObject());
          } else {
            eventsHandler.handleGroupUpdated(RodaCoreFactory.getModelService(), (Group) wrapper.getRodaObject());
          }
        }
      }
    } else {
      // this is a deletion
      if (objectId.startsWith(USER_KEY_PREFIX)) {
        eventsHandler.handleUserDeleted(RodaCoreFactory.getModelService(), objectId.replaceFirst(USER_KEY_PREFIX, ""));
      } else if (objectId.startsWith(GROUP_KEY_PREFIX)) {
        eventsHandler.handleGroupDeleted(RodaCoreFactory.getModelService(),
          objectId.replaceFirst(GROUP_KEY_PREFIX, ""));
      }
    }
  }

  private void handleUserCreated(EventUserCreated e) {
    String key = USER_KEY_PREFIX + e.getUser().getId();
    Map<String, Object> rodaObjectOtherInfo = new HashMap<>();
    putObjectInCache(key, new CRDTWrapper(e.getUser(), rodaObjectOtherInfo,
      false, instanceSenderId, new Date().getTime()));
  }

  private String getUserPasswordFromRodaUserOtherInfoMap(CRDTWrapper wrapper) {
    return (String) wrapper.getRodaObjectOtherInfo().getOrDefault(RODA_OBJECT_OTHER_INFO_PASSWORD, null);
  }

  private Map<String, Object> createRodaUserOtherInfoMapWithUserPassword(String password) {
    Map<String, Object> rodaObjectOtherInfo = new HashMap<>();
    rodaObjectOtherInfo.put(RODA_OBJECT_OTHER_INFO_PASSWORD, password);
    return rodaObjectOtherInfo;
  }

  private void handleUserUpdated(EventUserUpdated e) {
    String key = USER_KEY_PREFIX + e.getUser().getId();
    Map<String, Object> rodaObjectOtherInfo = new HashMap<>();
    putObjectInCache(key, new CRDTWrapper(e.getUser(), rodaObjectOtherInfo,
            true, instanceSenderId, new Date().getTime()));
  }

  private void handleUserDeleted(EventUserDeleted e) {
    String key = USER_KEY_PREFIX + e.getId();
    evictObjectFromCache(key);
  }

  private void handleGroupCreated(EventGroupCreated e) {
    String key = GROUP_KEY_PREFIX + e.getGroup().getId();
    putObjectInCache(key,
      new CRDTWrapper(e.getGroup(), Collections.emptyMap(), false, instanceSenderId, new Date().getTime()));
  }

  private void handleGroupUpdated(EventGroupUpdated e) {
    String key = GROUP_KEY_PREFIX + e.getGroup().getId();
    putObjectInCache(key,
      new CRDTWrapper(e.getGroup(), Collections.emptyMap(), true, instanceSenderId, new Date().getTime()));
  }

  private void handleGroupDeleted(EventGroupDeleted e) {
    String key = GROUP_KEY_PREFIX + e.getId();
    evictObjectFromCache(key);
  }

  private void putObjectInCache(String key, CRDTWrapper value) {
    ObjectKey objectKey = dataKey(key);
    if (!objectKeys.contains(objectKey)) {
      Update<GSet<ObjectKey>> update1 = new Update<>(objectKeysKey, GSet.create(), writeConsistency,
        curr -> curr.add(objectKey));
      replicator.tell(update1, self());
    }

    Update<ORMap<String, CRDTWrapper>> update = new Update<>(dataKey(key), ORMap.create(), writeConsistency,
      curr -> curr.put(cluster.selfUniqueAddress(), key, value));
    replicator.tell(update, self());
  }

  private void evictObjectFromCache(String key) {
    ObjectKey objectKey = dataKey(key);
    if (!objectKeys.contains(objectKey)) {
      Update<GSet<ObjectKey>> update1 = new Update<>(objectKeysKey, GSet.create(), writeConsistency,
        curr -> curr.add(objectKey));
      replicator.tell(update1, self());
    }

    Update<ORMap<String, CRDTWrapper>> update = new Update<>(objectKey, ORMap.create(), writeConsistency,
      curr -> curr.remove(cluster.selfUniqueAddress(), key));
    replicator.tell(update, self());
  }

  private ObjectKey dataKey(String entryKey) {
    return new ObjectKey(CACHE_PREFIX + entryKey);
  }

  public static class ObjectKey extends Key<ORMap<String, CRDTWrapper>> {
    @Serial
    private static final long serialVersionUID = 4859356839497209682L;

    public ObjectKey(String eventKey) {
      super(eventKey);
    }
  }
}