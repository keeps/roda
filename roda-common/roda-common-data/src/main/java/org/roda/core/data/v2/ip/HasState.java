package org.roda.core.data.v2.ip;

public interface HasState extends HasStateFilter{

  AIPState getState();

  void setState(AIPState state);

}
