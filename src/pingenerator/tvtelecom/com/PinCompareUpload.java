package pingenerator.tvtelecom.com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

@WebServlet("/PinCompareUpload")
@MultipartConfig
public class PinCompareUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public PinCompareUpload() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinCompareUpload.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);       

        Part fileINPart = request.getPart("fileINHidden");
        String fileINName = getSubmittedFileName(fileINPart);
        InputStream fileIN = fileINPart.getInputStream();
        
        Part filePinGenPart = request.getPart("filePinGenHidden");
        String filePinGenName = getSubmittedFileName(filePinGenPart);
        InputStream filePinGen = filePinGenPart.getInputStream();
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
		String jobId = dFormat.format(new Date());
		
		String uploadFolder = getServletContext().getInitParameter("uploadFolder"); 
		
LOG.log(Level.INFO,"PinCompareUpload uploadFolder:{0}",new Object[]{uploadFolder});

		File file1 = new File(uploadFolder + fileINName);
		File file2 = new File(uploadFolder + filePinGenName);

		Files.copy(fileIN, file1.toPath());
		Files.copy(filePinGen, file2.toPath());
		
		
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "insert into job (JOBID,TYPE,STATUS,UPDATEDBY,UPDATEDDATE) values ('" + jobId + "','PC','I',"+ userId + ",CURRENT_TIMESTAMP)";
		
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

		if (!result.equals("failed")) {
			URLConnection urlcon;
			try {
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"test",request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"PinCompareX?jobId="+jobId+"&userId="+userId});
				URL url = new URL(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"PinCompareX?jobId="+jobId+"&userId="+userId);
				urlcon = url.openConnection();
				urlcon.setConnectTimeout(100);
				urlcon.setReadTimeout(100);
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"call PinCompareX",urlcon.getDate()});
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

	private static String getSubmittedFileName(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
}
