package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
//import java.util.concurrent.ThreadLocalRandom;
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

@WebServlet("/PinGenBatchX")
public class PinGenBatchX extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenBatchX() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenBatchX.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinGenBatchX-jobId: ",jobId});
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'I' and jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		PreparedStatement st2 = null;
		String sql2 = "insert into pin (PIN,STATUS,DIGIT,JOBID,UPDATEDBY,UPDATEDDATE) values (?,'A',_digit,'"+jobId+"',"+userId+",CURRENT_TIMESTAMP)";
		
		Statement st3 = null;
		String sql3 = "update job set status = '_status', dupcount = _ratio where jobid = '" + jobId + "'";
		String sql3r = "";
		
		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			int pinDigit; 
			long pinAmount;

			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				result="failed";
				pinDigit = rs1.getInt("DIGIT");
				pinAmount = rs1.getLong("AMOUNT");
				boolean dup;

	            sql3r = sql3.replaceAll("_status", "P");
	            sql3r = sql3r.replaceAll("_ratio", Long.toString(0));
				st3 = con.createStatement();
				st3.executeUpdate(sql3r);

				sql2 = sql2.replaceAll("_digit", Integer.toString(pinDigit));
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinGenBatchX","sql2: " + sql2});
				st2 = con.prepareStatement(sql2);
				long c = 0;
	            for (long i = 1; i <= pinAmount; i++) {
	                do {
	                	dup = false;
	                	try {
	                		st2.setString(1, randomNumber(pinDigit));
	    					st2.executeUpdate();
	                		//st2.addBatch();
	                	} catch (java.sql.SQLIntegrityConstraintViolationException e) {
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"PinGenBatchX","found duplicated pin while generating: " + Long.toString(++c)});
							dup = true;
	                	}
	                } while (dup);
	            }
	            //st2.executeBatch();
	            
	            sql3r = sql3.replaceAll("_status", "S");
	            sql3r = sql3r.replaceAll("_ratio", Long.toString(c));
				//st3 = con.createStatement();
				st3.executeUpdate(sql3r);
				result = "succeed";
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"PinGenBatchX","Done!"});
			}
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (!result.equals("succeed")) {
            		sql3r = sql3.replaceAll("_status", "F");
        			//st3 = con.createStatement();
        			st3.executeUpdate(sql3r);
            	}
                if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
                if (st2 != null) {st2.close();}
                if (st3 != null) {st3.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"jobId\":"+jobId+",\"result\":\""+result+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private String randomNumber(int digit) {
		long l;
		String res;
	    Random randomGenerator = new Random();
	    l = randomGenerator.nextLong();
	    res = Long.toString(l);
	    return res.substring(res.length() - digit);
	}
}
