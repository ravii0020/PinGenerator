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

@WebServlet("/JobListResult")
public class JobListResult extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public JobListResult() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(JobListResult.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String jobId = request.getParameter("jobId");
        
LOG.log(Level.INFO,"JobListResult JobId:{0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from pinhist where jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		String result="";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			while (rs1.next()) {
				result += rs1.getString("PIN")+"\n";
			}
LOG.log(Level.INFO,"JobListResult result:{0}",new Object[]{result});
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		        if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
		    	LOG.log(Level.WARNING, ex.getMessage(), ex);
		    }
		}
		String fileName = "JOBID"+jobId;
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+".csv\"");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print(result);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
