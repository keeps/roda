package pt.gov.dgarq.roda.ds;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.roda.model.ModelService;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * Servlet implementation class DS
 */
public class DS extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(DS.class);
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DS() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.error("DS!!!!!!!!!!!!!!!!");
		try{
			ModelService ms = RodaCoreFactory.getModelService();
			User user = new User();
			user.setActive(true);
			user.setEmail("sleroux@keep.pt");
			user.setGuest(false);
			user.setId("sleroux");
			user.setName("Sébastien Leroux");
			LOGGER.error("ADDING MEMBER");
			ms.addUser(user);
			LOGGER.error("DELETING MEMBER");
			ms.deleteUser("sleroux");
			
			user.setEmail("sleroux3386@gmail.com");
			LOGGER.error("ADDING MEMBER");
			ms.addUser(user);
			user.setEmail("benficamania@gmail.com");
			LOGGER.error("UPDATING MEMBER");
			ms.updateUser(user);
			
			Group group = new Group();
			group.setActive(true);
			group.setId("g1");
			group.setName("Grupo1");
			ms.addGroup(group);
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
