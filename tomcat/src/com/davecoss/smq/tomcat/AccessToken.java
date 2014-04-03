package com.davecoss.smq.tomcat;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

public class AccessToken {

	public static boolean authorize(File dbpath, String username, String authtoken) throws SQLException {
		if(dbpath == null || username == null || authtoken == null)
			return false;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new SQLException("No JDBC for sqlite found", e);
		}
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet result = null;
        try {
        	conn = DriverManager.getConnection("jdbc:sqlite:" + dbpath.getAbsolutePath());
    		stat = conn.prepareStatement("select authtoken from authtokens where username = ?;);");
    		stat.setString(1, username);
        	result = stat.executeQuery();
        	String canonicalToken = result.getString("authtoken");
        	return canonicalToken.equals(authtoken);
        } finally {
        	if(result != null)
        		result.close();
        	if(stat != null)
        		stat.close();
        	if(conn != null)
        		conn.close();
        }
	}
	
	public static boolean authorizeRequest(HttpServletRequest request, File dbpath) throws SQLException {
		String username = QueueList.getStringParameter(request, "username", null);
		String authtoken = QueueList.getStringParameter(request, "authtoken", null);
		return authorize(dbpath, username, authtoken);
	}
}
