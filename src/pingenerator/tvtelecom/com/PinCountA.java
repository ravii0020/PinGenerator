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

@WebServlet("/PinCountA")
public class PinCountA extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinCountA() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinCountA.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String digit = request.getParameter("digit");
        String patternid = request.getParameter("patternid");
		Connection con = null;
		Statement st1 = null;
		String sql0 = "select * from pattern where patternid = " + patternid;
		String sql1 ="select count(*) c from pin where status = 'A' and serial is null and digit = _digit";			
		ResultSet rs1 = null;
		
		String result="failed";
		long c=0;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			if (patternid != null) {
				rs1 = st1.executeQuery(sql0);
				if (rs1.next()) {
					digit = rs1.getString("PINDIGIT");
				}
				rs1.close();
			}
LOG.log(Level.INFO,"PinCountA digit:{0}",new Object[]{digit});
			sql1 = sql1.replaceAll("_digit",digit);
LOG.log(Level.INFO,"PinCountA sql1:{0}",new Object[]{sql1});
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				c = rs1.getLong("c");
				result = "succeed";
LOG.log(Level.INFO,"PinCountA count:{0}",new Object[]{c});
			}
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
		out.print("{\"result\":\""+result+"\",\"count\":"+c+"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
