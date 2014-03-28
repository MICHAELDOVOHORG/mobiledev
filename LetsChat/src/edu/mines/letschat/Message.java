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
		return sent;
	}
	
	public boolean hasPicture() {
		if (picture == null || picture.isEmpty()) {
			return false;
		}
		return true;
	}
}
