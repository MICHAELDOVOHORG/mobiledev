package edu.mines.letschat;

public class Message {
	public String message;
	private boolean sent;
	
	public Message(String message, boolean sent) {
		this.message = message;
		this.sent = sent;
	}
	
	public boolean isMine() {
		if (sent) {
			return true;
		} else {
			return false;
		}
	}
}
