package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public Login() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(Login.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String userName = request.getParameter("userName").trim().toUpperCase();
        String password = request.getParameter("password").trim();
//LOG.log(Level.INFO,"userName:{0} password:{1}",new Object[]{userName,password});

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte byteData[] = md.digest();
			
			//convert the byte to hex format method 1
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
			 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			password = sb.toString();
//LOG.log(Level.INFO,"userName:{0} password:{1}",new Object[]{userName,password});			
			//convert the byte to hex format method 2
			/*
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<byteData.length;i++) {
				String hex=Integer.toHexString(0xff & byteData[i]);
			    	if(hex.length()==1) hexString.append('0');
			    	hexString.append(hex);
			}
			System.out.println("Digest(in hex format):: " + hexString.toString());
			*/
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from usr where username = '" + userName + "' and password = '" + password + "'";
		ResultSet rs1 = null;

		int userId = -1;
		String name = "";
		int roleId = -1;
		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
//LOG.log(Level.INFO,"sql1:{0}",new Object[]{sql1});	
			if (rs1.next()) {
				result="failed";
				userId = rs1.getInt("USERID");
				name = rs1.getString("NAME");
				roleId = rs1.getInt("ROLEID");

				HttpSession session = request.getSession(true);
				session.setAttribute("userId", userId);
				session.setAttribute("name", name);
				session.setAttribute("roleId", roleId);
LOG.log(Level.INFO,"Login session userId:{0} name:{1} roleId: {2}",new Object[]{session.getAttribute("userId"),session.getAttribute("name"),session.getAttribute("roleId")});
				result = "succeed";
			} else {
				result="failed";
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
		out.print("{\"result\":\""+result+"\",\"name\":\""+name+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
