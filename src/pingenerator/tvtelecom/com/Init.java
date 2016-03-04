package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/Init")
public class Init extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Init() {
        super();
    }
    
/*
CONNECT 'jdbc:derby:PinGen;create=true';
CONNECT 'jdbc:derby:PinGen';

//job type: PG = Pin gen, PS = Pin spec, SM = Serial Map, PE = Pin export, PC = Pin Compare
//job status: I = initial, P = processing, S = success, F = fail, D=deleted

//pin status: A = Available, M = Mapped

SELECT pin, COUNT(*) FROM PIN GROUP BY pin HAVING COUNT(*) > 1;

*/
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger LOG = Logger.getLogger(Init.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String s = request.getParameter("s");
        if (s == null) {throw new IllegalArgumentException("Wrong argument!!");}
        String clean = request.getParameter("clean");
        if (clean == null) {clean = "";}
LOG.log(Level.INFO,"{0} {1}",new Object[]{"Init","Start"});  

		Connection con = null;
		Statement st1 = null;
		ResultSet rs1 = null;

		String sql01 = "DROP TABLE USR";
		String sql02 = "DROP TABLE JOB";
		String sql03 = "DROP TABLE PIN";
		String sql032 = "DROP TABLE PINHIST"; 
		String sql04 = "DROP TABLE SERIAL";
		String sql05 = "DROP TABLE PATTERN";

		String sql11 = "SELECT * FROM USR";
		String sql12 = "SELECT * FROM JOB";
		String sql13 = "SELECT * FROM PIN";
		String sql132 = "SELECT * FROM PINHIST";
		String sql14 = "SELECT * FROM SERIAL";
		String sql15 = "SELECT * FROM PATTERN";
		
		String sql21 = "CREATE TABLE USR (USERID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), NAME VARCHAR(80), USERNAME VARCHAR(40), PASSWORD VARCHAR(40), ROLEID INT, UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		String sql22 = "CREATE TABLE JOB (JOBID VARCHAR(12) PRIMARY KEY, TYPE VARCHAR(5), DIGIT INT, AMOUNT BIGINT, PATTERNID INT, DUPCOUNT INT, STATUS VARCHAR(5), DESC1 VARCHAR(200), DESC2 VARCHAR(200), UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		String sql23 = "CREATE TABLE PIN (PIN VARCHAR(15) PRIMARY KEY, SERIAL VARCHAR(15), DIGIT INT, STATUS VARCHAR(5), JOBID VARCHAR(12), UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		String sql232 = "CREATE TABLE PINHIST (PHID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), PIN VARCHAR(15), SERIAL VARCHAR(15), DIGIT INT, STATUS VARCHAR(5), JOBID VARCHAR(12), UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		String sql233 = "CREATE TRIGGER PIN_I AFTER INSERT ON PIN REFERENCING NEW AS NV FOR EACH ROW MODE DB2SQL INSERT INTO PINHIST (PIN, SERIAL, DIGIT, STATUS, JOBID, UPDATEDBY, UPDATEDDATE) VALUES (NV.PIN, NV.SERIAL, NV.DIGIT, NV.STATUS, NV.JOBID, NV.UPDATEDBY, NV.UPDATEDDATE)";
		String sql234 = "CREATE TRIGGER PIN_U AFTER UPDATE ON PIN REFERENCING NEW AS NV FOR EACH ROW MODE DB2SQL INSERT INTO PINHIST (PIN, SERIAL, DIGIT, STATUS, JOBID, UPDATEDBY, UPDATEDDATE) VALUES (NV.PIN, NV.SERIAL, NV.DIGIT, NV.STATUS, NV.JOBID, NV.UPDATEDBY, NV.UPDATEDDATE)";
		String sql24 = "CREATE TABLE SERIAL (SERIAL VARCHAR(15) PRIMARY KEY, PATTERNID INT, STATUS VARCHAR(5),JOBID VARCHAR(12), UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		String sql25 = "CREATE TABLE PATTERN (PATTERNID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), CHANNEL VARCHAR(10), DIGIT INT, PINDIGIT INT, UPDATEDBY INT NOT NULL, UPDATEDDATE TIMESTAMP NOT NULL)";
		
		String sql251 = "INSERT INTO PATTERN (CHANNEL, DIGIT, PINDIGIT, UPDATEDBY, UPDATEDDATE) VALUES ('24', 12, 8, 1, CURRENT_TIMESTAMP)";
		String sql252 = "INSERT INTO PATTERN (CHANNEL, DIGIT, PINDIGIT, UPDATEDBY, UPDATEDDATE) VALUES ('04', 8, 12, 1, CURRENT_TIMESTAMP)";

		String sql253 = "INSERT INTO USR (NAME, USERNAME, PASSWORD, ROLEID, UPDATEDBY, UPDATEDDATE) VALUES ('Administrator', 'ADMIN', 'e020590f0e18cd6053d7ae0e0a507609', 1, 1, CURRENT_TIMESTAMP)"; //password = admin11

		String result = "";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			try {
				if (!clean.isEmpty()) {st1.executeUpdate(sql01);result += "Drop USR\n";}
				rs1 = st1.executeQuery(sql11);if (rs1 != null) {rs1.close();}
			} catch (java.sql.SQLSyntaxErrorException e) {
				result += "Create USR\n";
				st1.executeUpdate(sql21);
			}
			try {
				if (!clean.isEmpty()) {st1.executeUpdate(sql02);result += "Drop JOB\n";}
				rs1 = st1.executeQuery(sql12);if (rs1 != null) {rs1.close();}
			} catch (java.sql.SQLSyntaxErrorException e) {
				result += "Create JOB\n";
				st1.executeUpdate(sql22);
			}
			try {
				if (!clean.isEmpty()) {st1.executeUpdate(sql03);result += "Drop PIN\n";}
				rs1 = st1.executeQuery(sql13);if (rs1 != null) {rs1.close();}
			} catch (java.sql.SQLSyntaxErrorException e) {
				result += "Create PIN\n";
				st1.executeUpdate(sql23);
			}
				try {
					if (!clean.isEmpty()) {st1.executeUpdate(sql032);result += "Drop PINHIST\n";}
					rs1 = st1.executeQuery(sql132);if (rs1 != null) {rs1.close();}
				} catch (java.sql.SQLSyntaxErrorException e) {
					result += "Create PINHIST\n";
					st1.executeUpdate(sql232);
				}
				if (!clean.isEmpty()) {
					result += "Create PIN_I\n";
					st1.executeUpdate(sql233);
					result += "Create PIN_U\n";
					st1.executeUpdate(sql234);
				}
			try {
				if (!clean.isEmpty()) {st1.executeUpdate(sql04);result += "Drop SERIAL\n";}
				rs1 = st1.executeQuery(sql14);if (rs1 != null) {rs1.close();}
			} catch (java.sql.SQLSyntaxErrorException e) {
				result += "Create SERIAL\n";
				st1.executeUpdate(sql24);
			}
			try {
				if (!clean.isEmpty()) {st1.executeUpdate(sql05);result += "Drop PATTERN\n";}
				rs1 = st1.executeQuery(sql15);if (rs1 != null) {rs1.close();}
			} catch (java.sql.SQLSyntaxErrorException e) {
				result += "Create PATTERN\n";
				st1.executeUpdate(sql25);
				st1.executeUpdate(sql251);st1.executeUpdate(sql252);st1.executeUpdate(sql253);
			}
			if (result.isEmpty()) {result = "Do nothing\n";}
		} catch(Exception ex) {        //} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
		    try {
		        if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
		    	LOG.log(Level.WARNING, ex.getMessage(), ex);
		    }
		}
LOG.log(Level.INFO,"{0} {1}",new Object[]{"Init-result:\n",result.trim()});
		response.getWriter().append("result:\n" + result);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
