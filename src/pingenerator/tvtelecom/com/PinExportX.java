package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

@WebServlet("/PinExportX")
public class PinExportX extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinExportX() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinExportX.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"PinExportX jobId:{0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'I' and jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		Statement st2 = null;
		String sql2 = "update job set status = '_status' where jobid = '" + jobId + "'";
		String sql2r = "";
		
		Statement st3 = null;
		String sql3 = "select count(pin) c from pin where status = 'A' and serial is null and digit = _digit";
		//String sql31 = "select * from pin where status = 'A' and serial is null FETCH FIRST _amount ROWS ONLY";	
		String sql31 = "select * from pin where status = 'A' and serial is null and digit = _digit FETCH FIRST 1 ROWS ONLY";	
		ResultSet rs3 = null;

		PreparedStatement st4 = null;
		String sql4 = "update pin set status = 'E', jobid = '" + jobId + "', updatedby = "+userId+", updateddate = CURRENT_TIMESTAMP where pin = ?";

		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			int digit;
			long amount;
			
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				result="failed";
				digit = rs1.getInt("DIGIT");
				amount = rs1.getLong("AMOUNT");
					
	            sql2r = sql2.replaceAll("_status", "P");
				st2 = con.createStatement();
				st2.executeUpdate(sql2r);

				st3 = con.createStatement();
				long cAmount = amount;
				sql3 = sql3.replaceAll("_digit", Integer.toString(digit));
				rs3 = st3.executeQuery(sql3);
				if (rs3.next()) {
					long cAvailablePin = rs3.getLong("c");
					if (cAvailablePin >= cAmount) {
LOG.log(Level.INFO,"PinExportX sql4:{0}",new Object[]{sql4});
						st4 = con.prepareStatement(sql4);
						String pin = "";
					for (int i = 1;i <= cAmount; i++) {
						//sql31 = sql31.replaceAll("_amount", Long.toString(cAmount));
						sql31 = sql31.replaceAll("_digit", Integer.toString(digit));
						rs3 = st3.executeQuery(sql31);
						while (rs3.next()) {
							pin = rs3.getString("PIN");
LOG.log(Level.INFO,"PinExportX pin:{0} count:{1}",new Object[]{pin,i});
							st4.setString(1, pin);
							st4.executeUpdate();
						}
					}
			            sql2r = sql2.replaceAll("_status", "S");
						st2.executeUpdate(sql2r);
						result = "succeed";
					} else {
						sql2 = "update job set status = 'F', desc1 = 'Available PIN is not enough' where jobid = '" + jobId + "'";
						st2.executeUpdate(sql2);
						result = "failed";
					}
				}
			}
LOG.log(Level.INFO,"PinExportX Done!",new Object[]{});	
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (!result.equals("succeed")) {
    	            sql2r = sql2.replaceAll("_status", "F");
    	            st2 = con.createStatement();
    				st2.executeUpdate(sql2r);
            	}
                if (rs1 != null) {rs1.close();}
                if (st1 != null) {st1.close();}
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
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
