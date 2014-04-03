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

public class SendMessage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		boolean debug = request.getParameter("debug") != null;
		String sender = request.getParameter("username");
		String recipient = QueueList.getStringParameter(request, "recipient", null);
		String content = QueueList.getStringParameter(request, "content", "");
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
			queue.send(new Message(content, "string", sender, recipient));
			out.println("Message sent");
		} catch(SQLException sqle) {
			if(debug) {
				out.println("DB Error: ");
				sqle.printStackTrace(out);
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error Sending Message: " + sqle.getMessage());
			}
		}
		
	}
}
