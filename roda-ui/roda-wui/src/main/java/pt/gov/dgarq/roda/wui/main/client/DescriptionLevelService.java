package pt.gov.dgarq.roda.wui.main.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("descriptionlevel")
public interface DescriptionLevelService extends RemoteService {

	List<String> getDescriptionLevels();

	DescriptionLevelInfoPack getAllDescriptionLevels();
}
