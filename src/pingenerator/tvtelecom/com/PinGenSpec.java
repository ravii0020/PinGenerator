package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet("/PinGenSpec")
public class PinGenSpec extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenSpec() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenSpec.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        int pinCount = Integer.parseInt(request.getParameter("pinCount"));
        String status = request.getParameter("s");
        String jobId = request.getParameter("jobid");
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		if (status.equals("P")) {
			SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
			jobId = dFormat.format(new Date());
		}
		
LOG.log(Level.INFO,"userId:{0} pinCount:{1} jobId:{2}",new Object[]{userId,pinCount,jobId});

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "insert into job (JOBID,TYPE,AMOUNT,STATUS,UPDATEDBY,UPDATEDDATE) values ('" + jobId + "','PS'," + pinCount + ",'P',"+ userId + ",CURRENT_TIMESTAMP)";
		if (!status.equals("P")) {sql = "UPDATE job SET STATUS = '"+status+"' WHERE jobid = '"+jobId+"'";}
		String result="failed";
		
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st = con.createStatement();
LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
			st.executeUpdate(sql);
			result = "succeed";
		} catch(NamingException | SQLException ex) {
LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		    	if (rs != null) {rs.close();}
		        if (st != null) {st.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
LOG.log(Level.WARNING, ex.getMessage(), ex);
				result = "failed";
		    }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
