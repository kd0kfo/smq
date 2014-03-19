package com.davecoss.smq;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Queue {
	
	final static String CREATE_QUEUE_SQL = "create table if not exists queue (id INTEGER PRIMARY KEY, name STRING UNIQUE NOT NULL, description STRING);";
	final static String CREATE_MESSAGE_QUEUE_SQL = "create table if not exists messages (id INTEGER PRIMARY KEY, queueid INTEGER, message STRING NOT NULL, message_type STRING, sender STRING, target STRING, FOREIGN KEY(queueid) REFERENCES queue(id));";
	
	public final File dbfile;
	public final String name;
	public final int id;
	private Connection db;
	
	public Queue(String name) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.name = name;
		this.dbfile = new File(name + ".db");
		this.db = initDB(this.dbfile);
		this.id = createQueue(name, this.db);
	}
	
	public Queue(String name, File dbpath) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.name = name;
		this.dbfile = dbpath;
		this.db = initDB(this.dbfile);
		this.id = createQueue(name, this.db);
	}
	
	public void close() throws SQLException {
		if(db != null)
			db.close();
	}
	
	protected static Connection initDB(File dbpath) throws SQLException {
		Connection retval = DriverManager.getConnection("jdbc:sqlite:" + dbpath.getAbsolutePath());
		PreparedStatement stat = null;
        try {
        	stat = retval.prepareStatement(CREATE_QUEUE_SQL);
        	stat.execute();
        	stat = retval.prepareStatement(CREATE_MESSAGE_QUEUE_SQL);
        	stat.execute();
        	return retval;
        } finally {
        	if(stat != null)
        		stat.close();
        }
	}
	
	protected static int createQueue(String name, Connection db) throws SQLException {
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			stat = db.prepareStatement("insert or ignore into queue values (NULL, ?, NULL);");
			stat.setString(1, name);
			stat.execute();
			stat = db.prepareStatement("select id from queue where name = ?;");
			stat.setString(1, name);
			result = stat.executeQuery();
			return result.getInt("id");
		} finally {
			if(stat != null)
				stat.close();
			if(result != null)
				result.close();
		}
	}
	
	public void send(Message message) throws SQLException {
		PreparedStatement stat = null;
        try {
        	stat = db.prepareStatement("insert into messages values (NULL, ?, ?, ?, ?, ?);");
        	stat.setInt(1, id);
        	stat.setString(2, message.content);
        	stat.setString(3, message.content_type);
        	stat.setString(4, message.sender);
        	stat.setString(5, message.recipient);
        	stat.execute();
        } finally {
            if(stat != null)
            	stat.close();
        }
	}
	
	public Message peek(String recipient) throws SQLException {
		PreparedStatement stat = null;
        try {
        	if(recipient != null && recipient.length() != 0) {
        		stat = db.prepareStatement("select * from messages where queueid == ? and target == ? order by id asc limit 1;");
        		stat.setInt(1, id);
        		stat.setString(2, recipient);
        	} else {
        		stat = db.prepareStatement("select * from messages where queueid == ? order by id asc limit 1;");
        		stat.setInt(1, id);
        	}
        	ResultSet result = stat.executeQuery();
        	return Message.from_sql_row(result);
        } finally {
            if(stat != null)
            	stat.close();
        }
	}
	
	public Message next(String recipient) throws SQLException {
		Message message = peek(recipient);
		if(message == null)
			return null;
		
		PreparedStatement stat = null;
		try {
			stat = db.prepareStatement("delete from messages where id == ?;");
			stat.setInt(1, message.id);
			stat.execute();
			return message;
		} finally {
			if(stat != null)
				stat.close();
		}
	}
	
	public int countMessages(String recipient) throws SQLException {
		PreparedStatement stat = null;
		try {
			if(recipient != null && recipient.length() != 0) {
				stat = db.prepareStatement("select count(*) from messages where queueid == ? and target == ? order by id asc limit 1;");
				stat.setInt(1, id);
				stat.setString(2, recipient);
			} else {
				stat = db.prepareStatement("select count(*) from messages where queueid == ? order by id asc limit 1;");
				stat.setInt(1, id);
			}
			ResultSet result = stat.executeQuery();
			if(result == null)
				return 0;
			return result.getInt(1);
		} finally {
			if(stat != null)
				stat.close();
		}
	}
	
	public boolean hasMessage(String recipient) throws SQLException {
		return countMessages(recipient) != 0;
	}

}