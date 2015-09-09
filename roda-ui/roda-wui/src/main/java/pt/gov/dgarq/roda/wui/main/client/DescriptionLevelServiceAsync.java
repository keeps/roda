package pt.gov.dgarq.roda.wui.main.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DescriptionLevelServiceAsync {

  DescriptionLevelServiceAsync INSTANCE = GWT.create(DescriptionLevelService.class);

  void getDescriptionLevels(AsyncCallback<List<String>> callback);

  void getAllDescriptionLevels(AsyncCallback<DescriptionLevelInfoPack> callback);

}
