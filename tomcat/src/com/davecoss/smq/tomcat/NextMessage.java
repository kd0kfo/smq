 package com.davecoss.smq.tomcat;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.davecoss.smq.Message;
import com.davecoss.smq.Queue;

public class NextMessage extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		boolean debug = request.getParameter("debug") != null;
		Queue queue = null;
		try {
			queue = QueueList.loadQueue(request);
		} catch(Exception e) {
			out.println("Error loading queue: " + e.getMessage());
			e.printStackTrace();
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
		
		Message message = null;
		try {
			String recipient = QueueList.getStringParameter(request, "recipient", null);
			if(!queue.hasMessage(recipient))
			{
				out.println("No messages");
				return;
			}
			message = queue.next(recipient);
			if(message == null) {
				out.println("No messages");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("From ");
				sb.append(message.sender);
				sb.append(": ");
				sb.append(message.content);
				out.println(sb.toString());
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error getting message: " + e.getMessage());
		}
		
	}
	
}
