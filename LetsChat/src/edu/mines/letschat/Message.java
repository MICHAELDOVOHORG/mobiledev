package edu.mines.letschat;

public class Message {
	public String message;
	private boolean sent;
	public String picture;
	
	public Message(String message, boolean sent, String picture) {
		this.message = message;
		this.sent = sent;
		this.picture = picture;
	}
	
	public boolean isMine() {
		if (sent) {
			return true;
		} else {
			return false;
		}
	}
}
