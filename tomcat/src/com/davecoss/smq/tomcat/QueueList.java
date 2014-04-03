package com.davecoss.smq.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.davecoss.smq.Message;
import com.davecoss.smq.Queue;

public class QueueList extends HttpServlet {
	
	private static final String DEFAULT_QUEUE_FILENAME = "tomcat.db";
	private static final String DEFAULT_QUEUE_NAME = "tomcat";
	private static final long serialVersionUID = 1L;

	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		boolean debug = getStringParameter(request, "debug", null) != null;
		
		Queue queue = null;
		try {
			queue = loadQueue(request);
		} catch(Exception e) {
			out.println("Error loading queue: " + e.getMessage());
			if(debug)
				e.printStackTrace(out);
			return;
		}
		try {
			if(!AccessToken.authorizeRequest(request, queue.dbfile)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			}
		} catch(SQLException sqle) {
			if(debug) {
				out.println("DB Error: ");
				sqle.printStackTrace(out);
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error authenticating: " + sqle.getMessage());
			}
		}
		String recipient = getStringParameter(request, "recipient", null);
		Collection<Message> messages;
		try {
			messages = queue.list(recipient);
		} catch (SQLException e) {
			out.println("Error loading queue list: " + e.getMessage());
			if(debug)
				e.printStackTrace(out);
			return;
		}
		Iterator<Message> it = messages.iterator();
		out.println("<ul>");
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()) {
			Message message = it.next();
			sb.append("<li>From ");
			sb.append(message.sender);
			sb.append(": ");
			sb.append(message.content);
			sb.append("</li>");
			out.println(sb.toString());
			sb.setLength(0);
		}
		out.println("</ul>");
	}
	
	public static String getStringParameter(HttpServletRequest request, String key, String defaultValue) {
		String retval = request.getParameter(key);
		if(retval == null)
			return defaultValue;
		return retval;
	}
	
	public static Queue loadQueue(HttpServletRequest request) throws ClassNotFoundException, SQLException {
		File queueFile = new File(getStringParameter(request, "queuefile", DEFAULT_QUEUE_FILENAME));
		if(!queueFile.exists()) {
			throw new SQLException("DB not initialized");
		}
		String queueName = getStringParameter(request, "queue", DEFAULT_QUEUE_NAME);
		return new Queue(queueName, queueFile);
	}
}
		
