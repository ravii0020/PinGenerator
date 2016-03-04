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
import javax.sql.DataSource;

@WebServlet("/PinGenBatchCSVBKUP")
public class PinGenBatchCSVBKUP extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenBatchCSVBKUP() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenBatchCSVBKUP.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
		//HttpSession session = request.getSession(true);
		//String userId = (String)session.getAttribute("userId");
        String jobId = request.getParameter("jobId");
        
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"PinGenBatchCSV",jobId});

		SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
		String exportJobId = dFormat.format(new Date());

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		Statement st2 = null;
		String sql2 ="select * from pin where jobid = '" + jobId + "'";
		ResultSet rs2 = null;

		Statement st3 = null;
		String sql31 = "insert into job (JOBID,TYPE,DIGIT,AMOUNT,STATUS,DESC1,UPDATEDBY,UPDATEDDATE) values ('" + exportJobId + "','PE',_pinDigit,_pinAmount,'I','" + jobId + "',_updatedby,CURRENT_TIMESTAMP)";
		String sql32 = "update job set status = '_status' where jobid = '" + exportJobId + "'";
		String sql3r = "";
		
		String result="";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			
			
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				int pinDigit = rs1.getInt("DIGIT");
				long pinAmount = rs1.getLong("AMOUNT");
				int updatedBy = rs1.getInt("UPDATEDBY");
				
	            sql3r = sql31.replaceAll("_pinDigit", Integer.toString(pinDigit));
	            sql3r = sql3r.replaceAll("_pinAmount", Long.toString(pinAmount));
	            sql3r = sql3r.replaceAll("_updatedby", Integer.toString(updatedBy));
				st3 = con.createStatement();
				st3.executeUpdate(sql3r);
				
	            sql3r = sql32.replaceAll("_status", "P");
				st3.executeUpdate(sql3r);
				
				st2 = con.createStatement();
				rs2 = st2.executeQuery(sql2);
				while (rs2.next()) {
					result += rs2.getString("PIN")+"\n";
				}
				
	            sql3r = sql32.replaceAll("_status", "S");
				st3.executeUpdate(sql3r);
			}
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"PinGenBatchCSV","result: "+result});
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		        if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
		        if (st2 != null) {st2.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
		    	LOG.log(Level.WARNING, ex.getMessage(), ex);
		    }
		}
		SimpleDateFormat dFileFormat = new SimpleDateFormat("yyMMdd_hhmmss");
		String fileName = "PinGen_"+dFileFormat.format(new Date());
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
