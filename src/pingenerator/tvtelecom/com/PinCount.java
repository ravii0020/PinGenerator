package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import javax.sql.DataSource;

@WebServlet("/PinCount")
public class PinCount extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinCount() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinCount.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        
LOG.log(Level.INFO,"PinCount jobId: {0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select count(*) c from pin where jobid = '" + jobId + "'";
		ResultSet rs1 = null;

		Statement st2 = null;
		String sql2 ="select * from job where jobid = '" + jobId + "'";
		ResultSet rs2 = null;
		
		String result="failed";
		long c=0;
		String status = "F";
		String desc1 = "";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				result="failed";
				c = rs1.getLong("c");
				
				st2 = con.createStatement();
				rs2 = st2.executeQuery(sql2);
				
				if (rs2.next()) {
					status = rs2.getString("STATUS");
					desc1 = rs2.getString("DESC1");
				}
				
				result = "succeed";
LOG.log(Level.INFO,"PinCount count: {0} status: {1}",new Object[]{c,status});
			}
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
                if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
                if (rs2 != null) {rs2.close();}if (st2 != null) {st2.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+",\"count\":"+c+",\"status\":\""+status+"\",\"desc1\":\""+desc1+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
