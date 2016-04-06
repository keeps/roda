package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

public interface RiskMessages extends Messages {
  @DefaultMessage("Risk creation failed {0}")
  String createRiskFailure(String message);
}
