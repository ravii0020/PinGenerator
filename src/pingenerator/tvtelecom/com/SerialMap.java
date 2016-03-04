package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

@WebServlet("/SerialMap")
public class SerialMap extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMap() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMap.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);       
        String serialPattern = request.getParameter("serialPattern");
        String pinAmount = request.getParameter("pinAmount");


		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
		String jobId = dFormat.format(new Date());
		
LOG.log(Level.INFO,"userId:{0} serialPattern:{1} pinAmount:{2} jobId:{3}",new Object[]{userId,serialPattern,pinAmount,jobId});


		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql0 = "SELECT * FROM PATTERN WHERE PATTERNID = "+serialPattern;
		String sql = "insert into job (JOBID,TYPE,DIGIT,AMOUNT,PATTERNID,STATUS,UPDATEDBY,UPDATEDDATE) values ('" + jobId + "','SM',_DIGIT," + pinAmount + ","+serialPattern+",'I',"+ userId + ",CURRENT_TIMESTAMP)";
		
		String result="failed";
		
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st = con.createStatement();
LOG.log(Level.INFO,"sql0:{0}",new Object[]{sql0});
			rs = st.executeQuery(sql0);
			if (rs.next()) {
				//String CHANNEL = rs.getString("CHANNEL");
				//int DIGIT = rs.getInt("DIGIT");
				int PINDIGIT = rs.getInt("PINDIGIT");
				sql = sql.replaceAll("_DIGIT", Integer.toString(PINDIGIT));
LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
				st.executeUpdate(sql);
				result = "succeed";
			}
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

		if (!result.equals("failed")) {
			URLConnection urlcon;
			try {
LOG.log(Level.INFO,"SerialMap call SerialMapX url:{0}",new Object[]{request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"SerialMapX?jobId="+jobId+"&userId="+userId});
				URL url = new URL(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"SerialMapX?jobId="+jobId+"&userId="+userId);
				urlcon = url.openConnection();
				urlcon.setConnectTimeout(100);
				urlcon.setReadTimeout(100);
LOG.log(Level.INFO,"call SerialMapX: {0}",new Object[]{urlcon.getDate()});
			} catch (MalformedURLException e) { 
				LOG.log(Level.SEVERE, e.getMessage(), e);
				result = "failed";
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
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
