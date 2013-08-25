package com.davenport.buildness;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Image {

	private int height = 0;
	private int width = 0;
	private short alpha[][] = null;
	private short red[][] = null;
	private short green[][] = null;
	private short blue[][] = null;

	public Image(int width, int height) {
		this.height = height;
		this.width = width;
		alpha = new short[width][height];
		red = new short[width][height];
		green = new short[width][height];
		blue = new short[width][height];
	}

	public Image(String location) throws IOException {
		BufferedImage bi = ImageIO.read(new File(location));
		this.height = bi.getHeight();
		this.width = bi.getWidth();
		alpha = new short[width][height];
		red = new short[width][height];
		green = new short[width][height];
		blue = new short[width][height];

		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				Color color = new Color(bi.getRGB(x, y));
				alpha[x][y] = color.getAlpha();
				red[x][y] = color.getRed();
				green[x][y] = color.getGreen();
				blue[x][y] = color.getBlue();
			}
		}
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setPixel(Coordinate coordinate, Color color) {
		alpha[coordinate.getX()][coordinate.getY()] = color.getAlpha();
		red[coordinate.getX()][coordinate.getY()] = color.getRed();
		green[coordinate.getX()][coordinate.getY()] = color.getGreen();
		blue[coordinate.getX()][coordinate.getY()] = color.getBlue();
	}

	public Color getPixel(int x, int y) {
		return new Color(alpha[x][y], red[x][y], green[x][y], blue[x][y]);
	}

	public void paintRectangle(Coordinate start, Coordinate end, Color color) {
		int startX = (start.getX() < end.getX() ? start.getX() : end.getX());
		int endX = (start.getX() >= end.getX() ? start.getX() : end.getX());
		int startY = (start.getY() < end.getY() ? start.getY() : end.getY());
		int endY = (start.getY() >= end.getY() ? start.getY() : end.getY());

		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				setPixel(new Coordinate(x, y), color);
			}
		}
	}

	public BufferedImage toBufferedImage(Component component) {
		// Bit of a hack...
		boolean needToChange = !component.isVisible();
		if (needToChange) {
			component.setVisible(true);
		}
		BufferedImage bufferedImage = (BufferedImage) component.createImage(
				width, height);
		if (needToChange) {
			component.setVisible(false);
		}

		// this part is not a hack...
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				bufferedImage.setRGB(x, y, getPixel(x, y).getRGB());
			}
		}

		return bufferedImage;
	}
	
	/*
	 * This method implements the Canny Edge Detector as described in:
	 * http://en.wikipedia.org/wiki/Canny_edge_detector.
	 */
	public void findCannyEdges(int threshold) {
		//STEP 1: Convolve raw image with a Gaussian filter
		short[][] filter = { 	{ 2, 4,  5,  4,  2 },
								{ 4, 9,  12, 9,  4 },	
								{ 5, 12, 15, 12, 5 },				
								{ 4, 9,  12, 9,  4 },	
								{ 2, 4,  5,  4,  2 }	};
		
		convolve(filter, 1.0/159.0);
		
		Image gy = this.clone();
		short[][] filterGy = { 	{ -1,	-2,	-1 	},
								{ 0,	0,	0 	},	
								{ 1, 	2, 	1	}	};
		gy.convolve(filterGy, 1.0);
		
		Image gx = this.clone();
		short[][] filterGx = { 	{ -1,	0,	1 },
								{ -2,	0,	2 },	
								{ -1, 	0, 	1 }	};
		gx.convolve(filterGx, 1.0);
		
		//Now, looping through all of the pixels...
		Collection<Coordinate> edgePoints = new ArrayList<Coordinate>();
		Collection<Coordinate> otherPoints = new ArrayList<Coordinate>();

		for (int x=0; x<getWidth(); x++) {
			for (int y=0; y<getHeight(); y++) {
				Coordinate coordinate = new Coordinate(x, y);
				
				//STEP 2: Finding the intensity gradient of the image
				//double edgeGradient = gx.getEdgeGradient(gy.getPixel(x, y), gx.getPixel(x, y));
				double edgeDirection = getEdgeDirection(gy.getPixel(x, y), gx.getPixel(x, y));
				
				//STEP 3: Suppress non-maximums
				if (isMaximum(coordinate, edgeDirection)) {
					edgePoints.add(coordinate);
				} else {
					otherPoints.add(coordinate);
				}
			}
		}
		
		Color black = new Color((short) 0, (short) 0, (short) 0, (short) 0);
		for (Coordinate edgePoint : edgePoints) {
			setPixel(edgePoint, black);
		}

		Color white = new Color((short) 255, (short) 255, (short) 255, (short) 255);
		for (Coordinate otherPoint : otherPoints) {
			setPixel(otherPoint, white);
		}
	}
	
	public void convolve(short[][] filter, double d) {
		int filterWidth = filter.length;
		int filterHeight = filter[0].length;
				
		if (filterWidth % 2 == 1 && filterWidth == filterHeight) {	
			int centerOffset = filterWidth / 2;
				
			//Iterate through the matrices from top-left to bottom-right,
			//applying the Gaussian filter to each center pixel...
			for (int x=0; x<getWidth()-filterWidth; x++) {
				for (int y=0; y<getHeight()-filterHeight; y++) {
					short[][] alphaSubMatrix = 
						getSubMatrix(alpha, x, y, filterWidth, filterHeight);
					alpha[x+centerOffset][y+centerOffset] = 
						sumOfProducts(alphaSubMatrix, d, filter);
					
					short[][] redSubMatrix = 
						getSubMatrix(red, x, y, filterWidth, filterHeight);
					red[x+centerOffset][y+centerOffset] = 
						sumOfProducts(redSubMatrix, d, filter);
					
					short[][] greenSubMatrix = 
						getSubMatrix(green, x, y, filterWidth, filterHeight);
					green[x+centerOffset][y+centerOffset] = 
						sumOfProducts(greenSubMatrix, d, filter);
					
					short[][] blueSubMatrix = 
						getSubMatrix(blue, x, y, filterWidth, filterHeight);
					blue[x+centerOffset][y+centerOffset] = 
						sumOfProducts(blueSubMatrix, d, filter);
				}
			}
		}
	}
	
	public static float[][] divide(short[][] matrix, int divisor) {
		float[][] result = new float[matrix.length][matrix[0].length];
		
		for (int x=0; x<result.length; x++) {
			for (int y=0; y<result[0].length; y++) {
				result[x][y] = matrix[x][y] / divisor;
				//System.out.println("(" + x + "," + y + ") = " + result[x][y]);
			}
		}
		
		return result;
	}
	
	public static short[][] getSubMatrix(short[][] matrix, int startX, int startY, 
			                          int width, int height) {
		short[][] sub = new short[width][height];
		
		for (int matrixX=startX, subX=0; matrixX<startX+width; matrixX++, subX++) {
			for (int matrixY=startY, subY=0; matrixY<startY+width; matrixY++, subY++) {
				sub[subX][subY] = matrix[matrixX][matrixY];
			}
		}
		
		return sub;
	}
	
	public static short sumOfProducts(short[][] matrix, double coefficient, short[][] filter) {
		short result = 0;
		
		int filterWidth = filter.length;
		int filterHeight = filter[0].length;
		
		if (filter.length == matrix.length && 
			filter[0].length == matrix[0].length) {
			for (int x=0; x<filterWidth; x++) {
				for (int y=0; y<filterHeight; y++) {
					short temp = (short) (matrix[x][y] * coefficient * filter[x][y]);
					result += temp;
				}
			}
		}
		
		return result;
	}
	
	private boolean isMaximum(Coordinate coordinate, double direction) {
		Color thisColor = getPixel(coordinate.getX(), coordinate.getY());
		
		//System.out.println("direction = " + direction);
		
		if (direction == 0.0) {
			boolean moreIntense = true;
			
			if (coordinate.hasEastNeighbor()) {
				Coordinate east = coordinate.getEastNeighbor();
				Color eastColor = getPixel(east.getX(), east.getY());
				moreIntense = thisColor.moreIntenseThan(eastColor);
				//System.out.println("1 " + moreIntense);
			}
			
			if (moreIntense && coordinate.hasWestNeighbor()) {
				Coordinate west = coordinate.getWestNeighbor();
				Color westColor = getPixel(west.getX(), west.getY());
				moreIntense = thisColor.moreIntenseThan(westColor);
				//System.out.println("2 " + moreIntense);
			}
			
			return moreIntense;
		}
		
		if (direction == 45.0) {
			boolean moreIntense = true;
			
			if (coordinate.hasNorthEastNeighbor()) {
				Coordinate northEast = coordinate.getNorthEastNeighbor();
				Color northEastColor = getPixel(northEast.getX(), northEast.getY());
				moreIntense = thisColor.moreIntenseThan(northEastColor);
				//System.out.println("3 " + moreIntense);
			}
			
			if (moreIntense && coordinate.hasSouthWestNeighbor()) {
				Coordinate southWest = coordinate.getSouthWestNeighbor();
				Color southWestColor = getPixel(southWest.getX(), southWest.getY());
				moreIntense = thisColor.moreIntenseThan(southWestColor);
				//System.out.println("4 " + moreIntense);
			}
			
			return moreIntense;
		}
		
		if (direction == 90.0) {
			boolean moreIntense = true;
			
			if (coordinate.hasNorthNeighbor()) {
				Coordinate north = coordinate.getNorthNeighbor();
				Color northColor = getPixel(north.getX(), north.getY());
				moreIntense = thisColor.moreIntenseThan(northColor);
				//System.out.println("5 " + moreIntense);
			}
			
			if (moreIntense && coordinate.hasSouthNeighbor()) {
				Coordinate south = coordinate.getSouthNeighbor();
				Color southColor = getPixel(south.getX(), south.getY());
				moreIntense = thisColor.moreIntenseThan(southColor);
				//System.out.println("6 " + moreIntense);
			}
			
			return moreIntense;
		}
		
		if (direction == 135.0) {
			boolean moreIntense = true;
			
			if (coordinate.hasNorthWestNeighbor()) {
				Coordinate northWest = coordinate.getNorthWestNeighbor();
				Color northWestColor = getPixel(northWest.getX(), northWest.getY());
				moreIntense = thisColor.moreIntenseThan(northWestColor);
				//System.out.println("7 " + moreIntense);
			}
			
			if (moreIntense && coordinate.hasSouthEastNeighbor()) {
				Coordinate southEast = coordinate.getSouthEastNeighbor();
				Color southEastColor = getPixel(southEast.getX(), southEast.getY());
				moreIntense = thisColor.moreIntenseThan(southEastColor);
				//System.out.println("8 " + moreIntense);
			}
			
			return moreIntense;
		}
		
		return false;
	}
	
	private double getEdgeDirection(Color gy, Color gx) {
		double theta = Math.atan(gy.getRGB()/gx.getRGB());
		
		//System.out.println("Theta = " + theta);
		
		if (theta == 0.0 || theta == 45.0 || theta == 90.0 || theta == 135.0) {
			return theta;
		}
		
		if (theta < 0.0) {
			return 0.0;
		}
		
		if (theta > 0.0 && theta < 22.5) {
			return 0.0;
		}
		
		if (theta >= 22.5 && theta < 67.5) {
			return 45.0;
		}
		
		if (theta >= 67.5 && theta < 112.5) {
			return 90.0;
		}
		
		if (theta >= 112.5 && theta < 180.0) {
			return 135.0;
		}
		
		//Should not be reachable...
		return theta;
	}
	
	//private double getEdgeGradient(Color color, Color color2) {
	//	return Math.sqrt((color * color) + (color2 * color2));
	//}
	
	public void shiftLeft() {
		for (int x = 0; x < getWidth() - 1; x++) {
			for (int y = 0; y < getHeight() - 1; y++) {
				Color sourceColor = getPixel(x + 1, y + 1);
				setPixel(new Coordinate(x, y), sourceColor);
			}
		}
	}
	
	public void findEdges(int threshold) {
		Collection<Coordinate> edgePoints = new ArrayList<Coordinate>();
		Collection<Coordinate> otherPoints = new ArrayList<Coordinate>();

		for (int x = 1; x < getWidth() - 1; x++) {
			for (int y = 1; y < getHeight() - 1; y++) {
				if (Math.abs(alpha[x][y] - alpha[x - 1][y - 1]) > threshold
						|| Math.abs(alpha[x][y] - alpha[x][y - 1]) > threshold
						|| Math.abs(alpha[x][y] - alpha[x - 1][y]) > threshold
						|| Math.abs(red[x][y] - red[x - 1][y - 1]) > threshold
						|| Math.abs(red[x][y] - red[x][y - 1]) > threshold
						|| Math.abs(red[x][y] - red[x - 1][y]) > threshold
						|| Math.abs(green[x][y] - green[x - 1][y - 1]) > threshold
						|| Math.abs(green[x][y] - green[x][y - 1]) > threshold
						|| Math.abs(green[x][y] - green[x - 1][y]) > threshold
						|| Math.abs(blue[x][y] - blue[x - 1][y - 1]) > threshold
						|| Math.abs(blue[x][y] - blue[x][y - 1]) > threshold
						|| Math.abs(blue[x][y] - blue[x - 1][y]) > threshold) {
					edgePoints.add(new Coordinate(x, y));
				} else {
					otherPoints.add(new Coordinate(x, y));
				}
			}
		}

		Color black = new Color((short) 0, (short) 0, (short) 0, (short) 0);
		for (Coordinate edgePoint : edgePoints) {
			setPixel(edgePoint, black);
		}

		Color white = new Color((short) 255, (short) 255, (short) 255, (short) 255);
		for (Coordinate otherPoint : otherPoints) {
			setPixel(otherPoint, white);
		}
	}

	public Image clone() {
		Image clone = new Image(this.width, this.height);
		
		for (int x=0; x<getWidth(); x++) {
			for (int y=0; y<getHeight(); y++) {
				clone.alpha[x][y] = this.alpha[x][y];
				clone.red[x][y] = this.red[x][y];
				clone.green[x][y] = this.green[x][y];
				clone.blue[x][y] = this.blue[x][y];
			}
		}
		
		return clone;
	}

	public void findShapes(short threshold) {
		Collection<Coordinate> edgePoints = new ArrayList<Coordinate>();
		Collection<Coordinate> nonEdgePoints = new ArrayList<Coordinate>();

		for (int x = 1; x < getWidth() - 1; x++) {
			for (int y = getHeight() - 2; y > 0; y--) {
				if (Math.abs(alpha[x][y] - alpha[x - 1][y - 1]) > threshold
						|| Math.abs(alpha[x][y] - alpha[x][y - 1]) > threshold
						|| Math.abs(alpha[x][y] - alpha[x - 1][y]) > threshold
			            || Math.abs(red[x][y] - red[x - 1][y - 1]) > threshold
						|| Math.abs(red[x][y] - red[x][y - 1]) > threshold
						|| Math.abs(red[x][y] - red[x - 1][y]) > threshold
						|| Math.abs(green[x][y] - green[x - 1][y - 1]) > threshold
						|| Math.abs(green[x][y] - green[x][y - 1]) > threshold
						|| Math.abs(green[x][y] - green[x - 1][y]) > threshold
						|| Math.abs(blue[x][y] - blue[x - 1][y - 1]) > threshold
						|| Math.abs(blue[x][y] - blue[x][y - 1]) > threshold
						|| Math.abs(blue[x][y] - blue[x - 1][y]) > threshold) {
					edgePoints.add(new Coordinate(x, y));
				} else {
					nonEdgePoints.add(new Coordinate(x, y));
				}
			}
		}
		
		Color black = new Color((short) 0, (short) 0, (short) 0, (short) 0);
		Color white = new Color((short) 255, (short) 255, (short) 255, (short) 255);
		
		int counter = 0;
		while (++counter < 5000 && !edgePoints.isEmpty()) {
			Line line = extractLine(edgePoints);
			Color color = Color.getRandom();
			for (Coordinate point : line) {
				setPixel(point, color);
			}
		}
		
		for (Coordinate point : edgePoints) {
			setPixel(point, black);
		}
		
		for (Coordinate point : nonEdgePoints) {
			setPixel(point, white);
		}

		/*		
		Collection<Coordinate> coordinatesAlreadyInAShape = 
		  new ArrayList<Coordinate>();
		int counter = 0;
		long startTime = System.currentTimeMillis();
		boolean timeEstimated = false;
		for (Coordinate nonEdgePoint : nonEdgePoints) {
			if (nonEdgePoint.isInsideAShape(coordinatesAlreadyInAShape, edgePoints)) {
				setPixel(nonEdgePoint, green);
			} else {
				setPixel(nonEdgePoint, white);
			}
			
			if (!timeEstimated && (++counter * 100 / nonEdgePoints.size()) > 0) {
				long timeEstimate = (System.currentTimeMillis() - startTime) / 10;
				JOptionPane.showMessageDialog(new JFrame(), "This task will take " + timeEstimate + " seconds. Press OK to continue...");
				timeEstimated = true;
			}
		}
	    */
	}
	
	private Line extractLine(Collection<Coordinate> edgePoints) {
		Line line = new Line();
		
		Coordinate seed = edgePoints.iterator().next();
		line.add(seed);
		edgePoints.remove(seed);
		
		for (Coordinate edgePoint : edgePoints) {
			if (line.contains(edgePoint.getEastNeighbor()) ||
				line.contains(edgePoint.getNorthEastNeighbor()) ||
				line.contains(edgePoint.getNorthNeighbor()) ||
				line.contains(edgePoint.getNorthWestNeighbor()) ||
				line.contains(edgePoint.getWestNeighbor()) ||
				line.contains(edgePoint.getSouthWestNeighbor()) ||
				line.contains(edgePoint.getSouthNeighbor()) ||
				line.contains(edgePoint.getSouthEastNeighbor())) {
					
				line.add(edgePoint);
			}
		}
		
		edgePoints.removeAll(line);
		if (line.size() == 1) {
			line.remove(seed);
		}
		
		return line;
	}
	
	public void buildColorBlockedImage(int width, int height) {
		Image image = new Image(width, height);
		image.paintRectangle(new Coordinate(0, 0), new Coordinate(width / 3,
				height / 3), Color.getRandom());
		image.paintRectangle(new Coordinate(width / 3 + 1, height / 3 + 1),
				new Coordinate(width / 2, height / 2), Color.getRandom());
		image.paintRectangle(new Coordinate(width / 2 + 1, height / 2 + 1),
				new Coordinate(width, height), Color.getRandom());
	}

	public void buildNoiseImage(int width, int height) {
		Image image = new Image(width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setPixel(new Coordinate(x, y), Color.getRandom());
			}
		}
	}
	
	public Image getImage() {
		return this;
	}

	public class Coordinate {
		private int x;
		private int y;
		
		public boolean myEquals(Object o) {
			return o instanceof Coordinate && 
			       ((Coordinate) o).x == this.x &&
			       ((Coordinate) o).y == this.y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coordinate other = (Coordinate) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}
		
		public boolean isInsideAShape(Collection<Coordinate> coordinatesInsideShape, 
				                      Collection<Coordinate> edgeCoordinates) {
			
			if (this.hasEastNeighbor() && 
				coordinatesInsideShape.contains(this.getEastNeighbor())) {
				return true;
			}
			
			if (this.hasNorthEastNeighbor() && 
				coordinatesInsideShape.contains(this.getNorthEastNeighbor())) {
				return true;
			}
			
			if (this.hasNorthNeighbor() && 
				coordinatesInsideShape.contains(this.getNorthNeighbor())) {
				return true;
			}
			
			if (this.hasNorthWestNeighbor() && 
				coordinatesInsideShape.contains(this.getNorthWestNeighbor())) {
				return true;
			}
			
			if (this.hasSouthEastNeighbor() && 
				coordinatesInsideShape.contains(this.getSouthEastNeighbor())) {
				return true;
			}
			
			if (this.hasSouthNeighbor() && 
				coordinatesInsideShape.contains(this.getSouthNeighbor())) {
				return true;
			}
			
			if (this.hasSouthWestNeighbor() && 
				coordinatesInsideShape.contains(this.getSouthWestNeighbor())) {
				return true;
			}
			
			if (this.hasWestNeighbor() && 
				coordinatesInsideShape.contains(this.getWestNeighbor())) {
				return true;
			}
			
			boolean hasNorth = false;
			boolean hasSouth = false;
			boolean hasWest = false;
			boolean hasEast = false;
			
			int borders = 0;
			
			for (Coordinate coordinate : edgeCoordinates) {
				if (coordinate.isSouthOf(this)) {
					if (!hasSouth) borders++;
					hasSouth = true;
				}
				
				if (coordinate.isNorthOf(this)) {
					if (!hasNorth) borders++;
					hasNorth = true;
				}
				
				if (coordinate.isEastOf(this)) {
					if (!hasEast) borders++;
					hasEast = true;
				}
				
				if (coordinate.isWestOf(this)) {
					if (!hasWest) borders++;
					hasWest = true;
				}
			}
			
			//return hasNorth && hasSouth && hasWest && hasEast;
			return borders > 2;
		}
		
		public boolean isSouthOf(Coordinate point) {
			return this.getX() == point.getX() && this.getY() > point.getY();
		}

		public boolean isNorthOf(Coordinate point) {
			return this.getX() == point.getX() && this.getY() < point.getY();
		}

		public boolean isEastOf(Coordinate point) {
			return this.getY() == point.getY() && this.getX() > point.getX();
		}

		public boolean isWestOf(Coordinate point) {
			return this.getY() == point.getY() && this.getX() < point.getX();
		}
		
		public boolean hasNorthNeighbor() {
			return y > 0;
		}
		
		public Coordinate getNorthNeighbor() {
			if (!hasNorthNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x, y-1);
		}
		
		public boolean hasNorthWestNeighbor() {
			return x > 0 && y > 0;
		}
		
		public Coordinate getNorthWestNeighbor() {
			if (!hasNorthWestNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x-1, y-1);
		}
		
		public boolean hasWestNeighbor() {
			return x > 0;
		}

		public Coordinate getWestNeighbor() {
			if (!hasWestNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x-1, y);
		}
		
		public boolean hasSouthWestNeighbor() {
			return x > 0 && y < getImage().height - 1;
		}

		public Coordinate getSouthWestNeighbor() {
			if (!hasSouthWestNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x-1, y+1);
		}
		
		public boolean hasSouthNeighbor() {
			return y < getImage().height - 1;
		}

		public Coordinate getSouthNeighbor() {
			if (!hasSouthNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x, y+1);
		}
		
		public boolean hasSouthEastNeighbor() {
			return x < getImage().width - 1 && y < getImage().height - 1;
		}

		public Coordinate getSouthEastNeighbor() {
			if (!hasSouthEastNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x+1, y+1);
		}
		
		public boolean hasEastNeighbor() {
			return x < getImage().width - 1;
		}

		public Coordinate getEastNeighbor() {
			if (!hasEastNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x+1, y);
		}
		
		public boolean hasNorthEastNeighbor() {
			return x < getImage().width - 1 && y > 0;
		}

		public Coordinate getNorthEastNeighbor() {
			if (!hasNorthEastNeighbor()) {
				throw new NoSuchElementException();
			}
			
			return new Coordinate(x+1, y-1);
		}

		private Image getOuterType() {
			return Image.this;
		}
		
		public boolean isNeighbor(Coordinate other) {
			return isNeighbor(other, 2);
		}
		
		public boolean isNeighbor(Coordinate other, int maxDistance) {
			return Math.abs(this.x - other.x) < maxDistance && 
			   Math.abs(this.y - other.y) < maxDistance;
		}
	}
	
	
}
