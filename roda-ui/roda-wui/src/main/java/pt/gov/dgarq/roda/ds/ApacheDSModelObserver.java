package pt.gov.dgarq.roda.ds;

import javax.naming.NamingException;

import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.File;
import org.roda.model.ModelObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.harvard.hul.ois.ots.schemas.AES.Use;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchGroupException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.data.v2.User;

public class ApacheDSModelObserver implements ModelObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApacheDSModelObserver.class);
	
	@Override
	public void aipCreated(AIP aip) {
	}

	@Override
	public void aipUpdated(AIP aip) {		
	}

	@Override
	public void aipDeleted(String aipId) {		
	}

	@Override
	public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadataBinary) {		
	}

	@Override
	public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadataBinary) {		
	}

	@Override
	public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {		
	}

	@Override
	public void representationCreated(Representation representation) {		
	}

	@Override
	public void representationUpdated(Representation representation) {		
	}

	@Override
	public void representationDeleted(String aipId, String representationId) {		
	}

	@Override
	public void fileCreated(File file) {		
	}

	@Override
	public void fileUpdated(File file) {		
	}

	@Override
	public void fileDeleted(String aipId, String representationId, String fileId) {		
	}

	@Override
	public void logEntryCreated(LogEntry entry) {		
	}

	@Override
	public void sipReportCreated(SIPReport sipReport) {		
	}

	@Override
	public void sipReportUpdated(SIPReport sipReport) {		
	}

	@Override
	public void sipReportDeleted(String sipReportId) {		
	}

	@Override
	public void userCreated(User user) {
		try {
			UserUtility.getLdapUtility().addUser(user);
		} catch (LdapUtilityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmailAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void userUpdated(User user) {
		try {
			UserUtility.getLdapUtility().modifyUser(user);
		}catch(LdapUtilityException lue){
			
		} catch (EmailAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void userDeleted(String userID) {
		try{
			UserUtility.getLdapUtility().removeUser(userID);
		}catch(LdapUtilityException | IllegalOperationException e){
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void groupCreated(Group group) {
		try {
			UserUtility.getLdapUtility().addGroup(group);
		} catch (LdapUtilityException e) {
			e.printStackTrace();
		} catch (GroupAlreadyExistsException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void groupUpdated(Group group) {
		try {
			UserUtility.getLdapUtility().modifyGroup(group);
		}catch(LdapUtilityException e){
			e.printStackTrace();
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		} catch (NoSuchGroupException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void groupDeleted(String groupID) {
		try{
			UserUtility.getLdapUtility().removeGroup(groupID);
		}catch(LdapUtilityException | IllegalOperationException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void rodaMemberCreated(RODAMember member) {
		try {
			if(member.isUser()){
				UserUtility.getLdapUtility().addUser(new User(member));
			}else{
				UserUtility.getLdapUtility().addGroup(new Group(member));
			}
		} catch (LdapUtilityException e) {
			e.printStackTrace();
		} catch (GroupAlreadyExistsException e) {
			e.printStackTrace();
		} catch (UserAlreadyExistsException e) {
			e.printStackTrace();
		} catch (EmailAlreadyExistsException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void rodaMemberUpdated(RODAMember member) {
		try {
			if(member.isUser()){
				UserUtility.getLdapUtility().modifyUser(new User(member));
			}else{
				UserUtility.getLdapUtility().modifyGroup(new Group(member));
			}
		}catch(LdapUtilityException e){
			e.printStackTrace();
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		} catch (NoSuchGroupException e) {
			e.printStackTrace();
		} catch (EmailAlreadyExistsException e) {
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void rodaMemberDeleted(String memberID) {
		try{
			UserUtility.getLdapUtility().removeGroup(memberID);
			UserUtility.getLdapUtility().removeUser(memberID);
		}catch(LdapUtilityException | IllegalOperationException e){
			e.printStackTrace();
		}
	}

}
