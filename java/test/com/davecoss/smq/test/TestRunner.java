package com.davecoss.smq.test;

import java.io.File;

import com.davecoss.smq.Message;
import com.davecoss.smq.Queue;

public class TestRunner {
	
	public static void main(String[] args) throws Exception {
		System.out.println("Testing Queue Creation");
		
		File dbfile = new File("test/test.db");
		
		if(dbfile.exists()) {
			System.err.println("Database exists. Remove for test.");
			System.exit(1);
		}
		
		// Create
		Queue queue = new Queue("test", dbfile);
		System.out.println("Created database " + queue.name + " with id " + queue.id);
		System.out.println("SUCCESS");
		
		
		// Send Messages
		System.out.println("Testing send");
		queue.send(new Message("Hello, Alice", "String", "Tester", "Alice", null, null));
		queue.send(new Message("42", "int", "Tester", "Bob", null, null));
		queue.send(new Message("3.14", "float", "Tester", "Alice", null, null));
		System.out.println("SUCCESS");
		
		// Receive Messages
		System.out.println("Testing receive");
		System.out.println("Getting Bob's message");
		System.out.print("Does Bob have a message? ");
		if(queue.hasMessage("Bob"))
			System.out.println("Yes");
		else
		{
			System.out.println("No");
			System.exit(1);
		}
		Message message = queue.next("Bob");
		System.out.println(String.format("Got %s type message: %s", message.content_type, message.content));
		
		System.out.println("Getting Alice's messages using while");
		while(queue.hasMessage("Alice"))
		{
			message = queue.next("Alice");
			System.out.println(String.format("Got %s type message: %s", message.content_type, message.content));
		}
		
		// Clean
		System.out.println("Cleaning Database");
		dbfile.delete();
	}
}