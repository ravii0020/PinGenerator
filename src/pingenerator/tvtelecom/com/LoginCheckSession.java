package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginCheckSession")
public class LoginCheckSession extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginCheckSession() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginCheckSession.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String name = "";
		String result="failed";
        try {
    		HttpSession session = request.getSession(false);
    		String userId = ((Integer)session.getAttribute("userId")).toString();
    		name = (String)session.getAttribute("name");
    		String roleId = ((Integer)session.getAttribute("roleId")).toString();
LOG.log(Level.INFO,"LoginCheckSession userId:{0} name:{1} roleId: {2}",new Object[]{userId,name,roleId});
			result = "succeed";
        } catch (java.lang.NullPointerException e) {
        	result = "failed";
        } finally {
    		response.setContentType("application/json");
    		response.setCharacterEncoding(Utils.CharacterEncoding);
    		PrintWriter out = response.getWriter();
    		out.print("{\"result\":\""+result+"\",\"name\":\""+name+"\"}");
    		out.flush();
        }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
