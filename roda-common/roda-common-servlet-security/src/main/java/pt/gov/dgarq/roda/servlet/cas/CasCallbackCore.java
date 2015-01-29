package pt.gov.dgarq.roda.servlet.cas;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class CasCallback
 */
public class CasCallbackCore extends HttpServlet {
	static final private Logger logger = Logger.getLogger(CasCallbackCore.class);
	private static final long serialVersionUID = 1L;
	
	private static Map<String,String> iouToGt;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CasCallbackCore() {
        super();
        if(iouToGt==null || iouToGt.size()==0){
        	iouToGt = new Hashtable<String, String>();
        }
    }
    
    public static String getPGTFromIOU(String iou){
    	if(iouToGt!=null){
	    	for(int i=0; i<3; i++){
	    		if(iouToGt.containsKey(iou)){
	    			String PGT = iouToGt.get(iou);
	    			iouToGt.remove(iou);
	    			return PGT;
	    		}
	    		try{
	    			Thread.sleep(500);
	    		}catch(Exception e){
	    		}
	    	}
    	}
    	
    	return null;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		String pgtIou = request.getParameter("pgtIou");
		String pgtId = request.getParameter("pgtId");
		if(pgtId!=null && pgtIou!=null){
			iouToGt.put(pgtIou, pgtId);
		}else{
			String iou = request.getParameter("iou");
			
			if(iou!=null){
				if(iouToGt.containsKey(iou)){
					pw.write(iouToGt.get(iou));
				}
			}
		}
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
