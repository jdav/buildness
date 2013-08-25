package com.davenport.buildness;

import java.util.Random;

public class Color {

	private short alpha = 0;
	private short red = 0;
	private short green = 0;
	private short blue = 0;
	
	public Color(short alpha, short red, short green, short blue) {
		this.alpha = alpha;
		this.red = red;
		this.green = green;
		this.blue = blue;
		
		if (alpha < 0) alpha = 255;
		if (red < 0) red = 255;
		if (green < 0) green = 255;
		if (blue < 0) blue = 255;
	}
	
	public Color(int rgb) {
		//System.out.println("int = " + rgb);
		String hex = Integer.toHexString(rgb);
		//System.out.println("hex = " + hex);
		//System.out.println("The hex value of this color is: " + hex);
		alpha = Short.parseShort(hex.substring(0, 2), 16);
		red = Short.parseShort(hex.substring(2, 4), 16);
		green = Short.parseShort(hex.substring(4, 6), 16);
		blue = Short.parseShort(hex.substring(6, 8), 16);
		
		if (alpha < 0) alpha = 255;
		if (red < 0) red = 255;
		if (green < 0) green = 255;
		if (blue < 0) blue = 255;
		
		//System.out.println("" + alpha + ", " + red + ", " + green + ", " + blue);
	}

	public short getAlpha() {
		return alpha;
	}

	public void setAlpha(short alpha) {
		this.alpha = alpha;
	}

	public short getRed() {
		return red;
	}

	public void setRed(short red) {
		this.red = red;
	}

	public short getGreen() {
		return green;
	}

	public void setGreen(short green) {
		this.green = green;
	}

	public short getBlue() {
		return blue;
	}

	public void setBlue(short blue) {
		this.blue = blue;
	}
	
	public int getRGB() {
		//This is a bit of kludge. I don't know why these
		//are overflowing...
		if (alpha < 0) alpha = 255;
		if (red < 0) red = 255;
		if (green < 0) green = 255;
		if (blue < 0) blue = 255;
		//System.out.println("a,r,g,b = " + alpha + ":" + red + ":" + green + ":" + blue);
		String hex = Integer.toHexString(alpha);
		hex += Integer.toHexString(red);
		hex += Integer.toHexString(green);
		hex += Integer.toHexString(blue);
		//System.out.println("hex string = " + hex);
		Long temp = Long.parseLong(hex, 16);
		return temp.intValue();
	}
	
	public static Color getRandom() {
		Random random = new Random();
		return new Color((short) random.nextInt(256),
				         (short) random.nextInt(256), 
				         (short) random.nextInt(256), 
				         (short) random.nextInt(256));
	}
	
	public boolean equals(Object o) {
		Color color = (Color) o;
		return 
		this.alpha == color.alpha &&
		this.red == color.red &&
		this.green == color.green &&
		this.blue == color.blue;
	}
	
	public boolean moreIntenseThan(Color c) {
		//System.out.println(this.getAlpha() + " - " + c.getAlpha());
		//System.out.println(this.getRed() + " - " + c.getRed());
		//System.out.println(this.getGreen() + " - " + c.getGreen());
		//System.out.println(this.getBlue() + " - " + c.getBlue());
		return 	this.getAlpha() >= c.getAlpha() &&
				this.getRed() > c.getRed() &&
				this.getGreen() > c.getGreen() &&
				this.getBlue() > c.getBlue();
	}
	
}
