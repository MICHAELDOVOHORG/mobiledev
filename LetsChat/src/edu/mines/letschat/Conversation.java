package edu.mines.letschat;

import android.content.Context;

import com.orm.SugarRecord;

public class Conversation extends SugarRecord<Conversation> {
	
	String senderID;
	String recipientID;
	String message;
	boolean sent;
	String picture;

	public Conversation(Context arg0) {
		super(arg0);
	}
	
	public Conversation(Context arg, String senderID, String recipientID, String message, boolean sent, String picture) {
		super(arg);
		this.senderID = senderID;
		this.recipientID = recipientID;
		this.message = message;
		this.sent = sent;
		this.picture = picture;
	}

}
