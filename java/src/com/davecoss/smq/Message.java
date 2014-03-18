package com.davecoss.smq;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Message {
	
	public final String content;
	public final String content_type;
	public final String sender;
	public final String recipient;
	public final Integer id;
	public final Integer queueid;
    
    public Message(String content, String content_type,
    		String sender, String recipient,
            Integer messageid, Integer queueid) {
        this.content = content;
        this.content_type = content_type;
        this.sender = sender;
        this.recipient = recipient;
        this.queueid = queueid;
        this.id = messageid;
    }

    public static Message from_sql_row(ResultSet row) throws SQLException {
        if(row == null)
            return null;
    	Integer id = row.getInt("id");
    	if(row.wasNull())
    		id = null;
    	Integer queueid = row.getInt("queueid");
    	if(row.wasNull())
    		queueid = null;
        return new Message(row.getString("message"), row.getString("message_type"), row.getString("sender"),
                        row.getString("target"), id, queueid);
    }

}