package com.davecoss.smq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Queue {
	
	final static String CREATE_QUEUE_SQL = "create table if not exists queue (id INTEGER PRIMARY KEY, name STRING UNIQUE NOT NULL, description STRING);";
	final static String CREATE_MESSAGE_QUEUE_SQL = "create table if not exists messages (id INTEGER PRIMARY KEY, queueid INTEGER, message STRING NOT NULL, message_type STRING, sender STRING, target STRING, FOREIGN KEY(queueid) REFERENCES queue(id));";
	
	final String dbpath;
	final String name;
	final int queueid;
	private Connection db;
	
	public Queue(String name) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.name = name;
		this.dbpath = name + ".db";
		this.db = initDB(this.dbpath);
		this.queueid = createQueue(name, this.db);
	}
	
	public Queue(String name, String dbpath) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.name = name;
		this.dbpath = dbpath;
		this.db = initDB(this.dbpath);
		this.queueid = createQueue(name, this.db);
	}
	
	protected static Connection initDB(String dbpath) throws SQLException {
		Connection retval = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
		PreparedStatement stat = null;
        try {
        	stat = retval.prepareStatement(CREATE_QUEUE_SQL);
        	stat.execute();
        	stat = retval.prepareStatement(CREATE_MESSAGE_QUEUE_SQL);
        	stat.execute();
        	retval.commit();
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
			db.commit();
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
	
}