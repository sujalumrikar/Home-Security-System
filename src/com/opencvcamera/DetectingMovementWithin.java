package com.opencvcamera;
//package com.opencvcamera;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

//import com.opencvcamera.SendVideoInEmail;

//instructions from https://youtu.be/NUQc7-dYIxA

public class DetectingMovementWithin extends JFrame {
	
	private JLabel cameraScreen;
	private JButton btnCapture;
	private VideoCapture capture; //OpenCV allows an interface to capture live stream with the camera (webcam).
	private Mat image; //The Mat class of OpenCV library is used to store the values of an image.
	private int delay = 0;
	private boolean motionDetected = false;
	
	private double analysis = 1;
	private double averagePerc = 0;
	private double averageDiff = 0;
	
	private int vidFrames = 0;
	
	//this is creating the frame
	public DetectingMovementWithin() {
		setLayout(null);
		
		//creating the screen
		cameraScreen = new JLabel();
		cameraScreen.setBounds(0, 0, 640, 480);
		add(cameraScreen);
		
		//creating the button
		/*
		btnCapture = new JButton("Capture");
		btnCapture.setBounds(300, 480, 80, 40);
		add(btnCapture);
		*/
		
		//Anonymous class within. Captures if button has been pressed.
		/*
		btnCapture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clicked = true;
			}
		});
		*/
		
		setSize(new Dimension(640, 560));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	//create camera
	public void startCamera() {
		ArrayList<byte[]> imagesTaken = new ArrayList<byte[]>();
		ArrayList<Mat> thePictures = new ArrayList<Mat>();
		ArrayList<String> imageNames = new ArrayList<String>();
		capture = new VideoCapture(0);
		image = new Mat();
		byte[] imageData;
		
		ImageIcon icon;
		while (motionDetected == false) {
			System.out.println("Loop started. Delay is currently: " + delay);
			capture.read(image);
			final MatOfByte buf = new MatOfByte();
			Imgcodecs.imencode(".jpg", image, buf);
			
			imageData = buf.toArray();
			//add to JLabel
			//icon displays the webcamera's stream onto the JFrame
			icon = new ImageIcon(imageData);
			cameraScreen.setIcon(icon);
			//capture and save to file
			if (imagesTaken.size() > 30) {
				imagesTaken.remove(0);
				thePictures.remove(0);
				imageNames.remove(0);
			}
			imagesTaken.add(imageData);
			
			Mat newImage = new Mat();
			image.copyTo(newImage);
			thePictures.add(newImage);
			
			imageNames.add(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS").format(new Date()));
			//imageTaken(imageData);
			if (imagesTaken.size() > 7) {
				isThereMotion(thePictures);
				System.out.println("Testing for motion.");
			}
			
			if(motionDetected) {
				captureVideoClip();
				//delay++;
				//System.out.println("Delay loop: " + delay + " has just finished");
			}
			
			sleepMethod();
		}
		
		for (String s: imageNames) {
			imageTaken(s);
		}
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				DetectingMovementWithin camera = new DetectingMovementWithin();
				//start camera in thread
				new Thread(new Runnable() {
					@Override
					public void run() {
						camera.startCamera();
					}
				}).start();
			}
		});
	}

	public void imageTaken(String name) {
		//String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS").format(new Date());
		//write to file
		Imgcodecs.imwrite("images/" + name + ".jpg", image);
		//clicked = false;
		
		/*
		for (byte b : imageData) {
			System.out.println(b);
		}
		*/
	}
	
	public void isThereMotion(ArrayList<Mat> thePictures) {
		/*
		byte[] img1 = imagesTaken.get(imagesTaken.size()-1); //equivalent to imageData
	    byte[] img2 = imagesTaken.get(imagesTaken.size()-2);
		
	    final int picArrayLength;
	    
		if (img1.length > img2.length) {
			picArrayLength = img2.length;
		} else {
			picArrayLength = img1.length;
		}
		
		int differenceSpotted = 0;
		for (int i = 0; i < picArrayLength; i++) {
			if (img1[i] != img2[i]) {
				differenceSpotted++;
			}
		}
		
		double differenceRatio = (double) differenceSpotted/picArrayLength;
		System.out.println("Difference Count is: " + differenceSpotted + " , PicArrayLength is: " + picArrayLength + 
				" , Ratio is: " + differenceRatio);
		
		if (differenceRatio > 0.9999) {
			System.out.println("MOVEMENT!");
			motionDetected = true;
		}
		*/
		
		Mat image1 = thePictures.get(thePictures.size() - 1);
		Mat image2 = thePictures.get(thePictures.size() - 5);
		Mat grayImage1 = new Mat();
		Mat grayImage2 = new Mat();
		Mat diffImage = new Mat();
		Mat thresholdImage = new Mat();

		
		Imgproc.cvtColor(image1, grayImage1, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(image2, grayImage2, Imgproc.COLOR_BGR2GRAY);
		Core.absdiff(grayImage1, grayImage2, diffImage);
		Imgproc.threshold(diffImage, thresholdImage, 0, 255, Imgproc.THRESH_BINARY);
		
		double nonZeroPixels = Core.countNonZero(thresholdImage);
		double totalPixels = thresholdImage.rows() * thresholdImage.cols();
		double percentage = (nonZeroPixels/totalPixels) * 100;
		
		if (analysis < 11) {
			if (analysis < 6) {
				averagePerc += percentage/5;
			} else {
				averageDiff += averagePerc - percentage;
			}
			analysis++;
		}
		
		/*System.out.println("Percentage is: " + percentage + " , Average Percentage is: " + averagePerc 
				+ " , Average Difference is: " + averageDiff);*/
		if (analysis >= 11) {
			if (percentage > averagePerc + averageDiff/5) {
				System.out.println("MOVEMENT");
			}
		}
		
	}
	
	public void sleepMethod() {
		try {
			Thread.sleep(1500);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void captureVideoClip() {
		System.out.println("Video Capturing...");
		VideoCapture capture = new VideoCapture(0);
		if (!capture.isOpened()) {
			System.out.println("Error opening webcam");
			return;
		}
		
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

		String fileName = "images/" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS").format(new Date()) + ".mp4";
		
		VideoWriter writer = new VideoWriter(fileName, VideoWriter.fourcc('H', '2', '6', '4'), 
				capture.get(Videoio.CAP_PROP_FPS), 
				new org.opencv.core.Size((int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH),
				(int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT)), true);
		
		Mat frame = new Mat();
		
		while(true) {
			System.out.println("Entering video while loop...");
			capture.read(frame);
			
			if (frame.empty()) {
				System.out.println("Error capturing frame");
				break;
			}
			
			writer.write(frame);
			
			//SendVideoInEmail emailObject = new SendVideoInEmail(fileName);
			/*
			try {
				emailObject.sendAttachment();
			} catch (Exception e) {
				System.out.println(e);
			}
			*/
			
			//imshow("Webcam", frame);
			
			if (vidFrames > 120) {
				break;
			} else {
				vidFrames++;
				System.out.println(vidFrames);
			}
		}
		
		capture.release();
		writer.release();
		
	}
}