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

@WebServlet("/MainDashboard")
public class MainDashboard extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public MainDashboard() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(MainDashboard.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);

		Connection con = null;
		Statement st1 = null;
		String sql1 = "select count(*) cA from pin where status = 'A' and serial is null";
		String sql2 = "select count(*) cM from pin where status = 'M'";
		
		
		ResultSet rs1 = null;

		
		String result="failed";
		long cA = 0;
		long cM = 0;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				cA = rs1.getLong("cA");
			}
			rs1.close();
			rs1 = st1.executeQuery(sql2);
			if (rs1.next()) {
				cM = rs1.getLong("cM");
			}
			result = "succeed";
LOG.log(Level.INFO,"MainDashboard countA: {0} countB: {1}",new Object[]{cA,cM});
			
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

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"cA\":"+cA+",\"cM\":"+cM+"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
