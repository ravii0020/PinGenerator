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

@WebServlet("/LoginSignout")
public class LoginSignout extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginSignout() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginSignout.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
		String result="failed";
        try {
        	HttpSession session = request.getSession(false);
        	if (session != null) {session.invalidate();}
			result = "succeed";
        } catch (Exception e) {
        	result = "failed";
        } finally {
    		response.setContentType("application/json");
    		response.setCharacterEncoding(Utils.CharacterEncoding);
    		PrintWriter out = response.getWriter();
    		out.print("{\"result\":\""+result+"\"}");
    		out.flush();
LOG.log(Level.INFO,"LoginSignout result:{0}",new Object[]{result});
        }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
