package com.davenport.buildness.dra;

import java.util.concurrent.LinkedBlockingQueue;

import com.davenport.buildness.Image;


public class Video extends LinkedBlockingQueue<Image> {

	private static final long serialVersionUID = 1L;
	
	public Video(Image image) throws InterruptedException {
		this.put(image);

	}

}
