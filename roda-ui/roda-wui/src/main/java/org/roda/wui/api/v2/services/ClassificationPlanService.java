package org.roda.wui.api.v2.services;

import org.roda.core.common.ClassificationPlanUtils;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

@Service
public class ClassificationPlanService {

  public ConsumesOutputStream retrieveClassificationPlan(User user, String filename)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return ClassificationPlanUtils.retrieveClassificationPlan(user, filename);
  }
}
