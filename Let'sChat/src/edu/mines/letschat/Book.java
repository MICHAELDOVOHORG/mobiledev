package edu.mines.letschat;

import android.content.Context;

import com.orm.SugarRecord;

public class Book extends SugarRecord<Book> {
	String title;
	String edition;

	public Book(Context ctx){
		super(ctx);
	}

	public Book(Context ctx, String title, String edition){
		super(ctx);
		this.title = title;
		this.edition = edition;
	}
}