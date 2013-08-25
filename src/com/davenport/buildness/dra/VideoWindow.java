package com.davenport.buildness.dra;

import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.davenport.buildness.Image;


public class VideoWindow extends JFrame {

	private static final long serialVersionUID = 5142388990039117949L;
	private BufferedImage bufferedImage = null;
	private Image originalImage = null;
	private Image edgedImage = null;
	private short edgeThresholdFactor = 30;
	private static Object[] edgeThresholdFactorChoices = 
		new Object[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
					   "10", "15", "20", "25", "30", "35", "40", "45", 
		               "50", "55", "60", "65", "70", "75", "80", "85" };
	
	public static void main(String[] args) {
		VideoWindow window = new VideoWindow();
		window.setVisible(true);
		window.repaint();
	}
	
	public VideoWindow() {
		setSize(400, 400);
		addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
		
		MenuItem load = new MenuItem();
		load.setLabel("Load Image...");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadImage();
				repaint();
			}});
		
		MenuItem exit = new MenuItem();
		exit.setLabel("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}});
		
		MenuItem viewOriginal = new MenuItem();
		viewOriginal.setLabel("View Original");
		viewOriginal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				prepareToDisplay(originalImage);
				repaint();
			}});
		
		MenuItem viewEdges = new MenuItem();
		viewEdges.setLabel("View Edges");
		viewEdges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				edgedImage = originalImage.clone();
				edgedImage.findEdges(edgeThresholdFactor);
				prepareToDisplay(edgedImage);
				repaint();
			}});
		
		MenuItem viewAsVideo = new MenuItem();
		viewAsVideo.setLabel("View as Video");
		viewAsVideo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					showVideo(edgedImage);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});

		MenuItem setEdgeThresholdFactor = new MenuItem();
		setEdgeThresholdFactor.setLabel("Set Edge Threshold Factor");
		setEdgeThresholdFactor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				edgeThresholdFactor = getEdgeThresholdFactor();
			}});
		
		Menu fileMenu = new Menu();
		fileMenu.setLabel("File");
		fileMenu.add(load);
		fileMenu.add(exit);
		
		Menu viewMenu = new Menu();
		viewMenu.setLabel("View");
		viewMenu.add(viewOriginal);
		viewMenu.add(viewEdges);
		viewMenu.add(viewAsVideo);
		viewMenu.add(setEdgeThresholdFactor);
		
		MenuBar menuBar = new MenuBar();
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		
		this.setMenuBar(menuBar);
	}
	
	public VideoWindow(Image image) {
		this();
		prepareToDisplay(image);
	}
	
	private short getEdgeThresholdFactor() {
		Object selection = 
			JOptionPane.showInputDialog(this, "Set Edge Threshold Factor", "Select", 
				                        JOptionPane.OK_CANCEL_OPTION, null, edgeThresholdFactorChoices, 
				                        "" + edgeThresholdFactor);
		if (selection == null) {
			return edgeThresholdFactor;
		} else {
			return Short.parseShort((String) selection);
		}
	}
	
	private void loadImage() {
		JFileChooser fileChooser = new JFileChooser();
		int response = fileChooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION) {
			File imageFile = fileChooser.getSelectedFile();
			try {
				originalImage = new Image(imageFile.getAbsolutePath());
				prepareToDisplay(originalImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void showVideo(Image newImage) throws InterruptedException {
		setSize(newImage.getWidth(), newImage.getHeight());
		Video video = new Video(newImage);
		Image image = null;
		while ((image = video.take()) != null) {
			bufferedImage = image.toBufferedImage(this);
			repaint();
			Thread.sleep(100);
		}
	}
	
	public void prepareToDisplay(Image newImage) {
		setSize(newImage.getWidth(), newImage.getHeight());
		bufferedImage = newImage.toBufferedImage(this);
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (bufferedImage != null) {
			g.drawImage(bufferedImage, 0, 0, this);
		}
	}

}

