//Submission of Malhar Parikh - 1916382273

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class ImageDisplay {

  JFrame frame;
  JLabel lbIm1;
  BufferedImage imgOne;
  int width = 7680;
  int height = 4320;
  boolean ctrlPressed = false;
  int overlaySize; 

  // Read 16xHD image
  private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
    // BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//trying without anti-aliasing first
    try {
      int frameLength = width * height * 3;
      File file = new File(imgPath);
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      raf.seek(0);
      byte[] bytes = new byte[frameLength];
      raf.read(bytes);

      int ind = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          byte r = bytes[ind];
          byte g = bytes[ind + height * width];
          byte b = bytes[ind + height * width * 2];
          int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
          img.setRGB(x, y, pix);
          ind++;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void showIms(String[] args, double scaleFactor, int antialias, int overlaySize) {
    frame = new JFrame();
	imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
	readImageRGB(width, height, args[0], imgOne);
    int scaledWidth = (int) (width * scaleFactor);
    int scaledHeight = (int) (height * scaleFactor);
    BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
	for (int y = 0; y < scaledHeight; y++) {
		for (int x = 0; x < scaledWidth; x++) {
			int originalX = (int) (x / scaleFactor);
			int originalY = (int) (y / scaleFactor);
	
			if (antialias == 1) {
			int sumR = 0, sumG = 0, sumB = 0;
			int count = 0;
	
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -1; dx <= 1; dx++) {
				int sampleX = originalX + dx;
				int sampleY = originalY + dy;
	
				if (sampleX >= 0 && sampleX < width && sampleY >= 0 && sampleY < height) {
					int pixel = imgOne.getRGB(sampleX, sampleY);
					sumR += (pixel >> 16) & 0xFF;
					sumG += (pixel >> 8) & 0xFF;
					sumB += pixel & 0xFF;
					count++;
				}
				}
			}
	
		int avgR = sumR / count;
		int avgG = sumG / count;
		int avgB = sumB / count;
		int avgPixel = 0xff000000 | (avgR << 16) | (avgG << 8) | avgB;
		scaledImage.setRGB(x, y, avgPixel);
		} else {
		if (originalX >= 0 && originalX < width && originalY >= 0 && originalY < height) {
			int pixel = imgOne.getRGB(originalX, originalY);
			scaledImage.setRGB(x, y, pixel);
		}
		}
	}
	}
    
    // Draw scaled original image
    // Graphics2D g = scaledImage.createGraphics();
    // g.drawImage(imgOne, 0, 0, scaledWidth, scaledHeight, null);
    // g.dispose();

    // Set frame size
    frame.setSize(scaledWidth, scaledHeight);

    // Add scaled image to frame
    frame.add(new JLabel(new ImageIcon(scaledImage)));

    frame.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (ctrlPressed) {
          int mouseX = e.getX();
          int mouseY = e.getY();

          int x = mouseX - overlaySize / 2;
          int y = mouseY - overlaySize / 2;

          if (x < 0) x = 0;
          if (y < 0) y = 0;
          if (x + overlaySize > scaledWidth) x = scaledWidth - overlaySize;
          if (y + overlaySize > scaledHeight) y = scaledHeight - overlaySize;
		//here I took help from chatgpt as my anti-aliasing was not working
          BufferedImage overlayedImage = imgOne.getSubimage(x * (width / scaledWidth), y * (height / scaledHeight), overlaySize, overlaySize);
          Graphics2D g = scaledImage.createGraphics();
          g.drawImage(imgOne, 0, 0, scaledWidth, scaledHeight, null);
          g.drawImage(overlayedImage, x, y, null);
          g.dispose();
          frame.repaint();
        }
      }
    });

    frame.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
          ctrlPressed = true;
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
          ctrlPressed = false;
          Graphics2D g = scaledImage.createGraphics();
          g.drawImage(imgOne, 0, 0, scaledWidth, scaledHeight, null);
          g.dispose();
          frame.repaint();
        }
      }
    });

    frame.setVisible(true);
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: java ImageDisplay <image_path> <scale_factor> <antialias> <window size>");
      System.exit(1);
    }

    String imagePath = args[0];
    double scaleFactor = Double.parseDouble(args[1]);
    int antialias = Integer.parseInt(args[2]);
	int overlaySize = Integer.parseInt(args[3]);

    ImageDisplay app = new ImageDisplay();
    app.showIms(new String[]{imagePath}, scaleFactor, antialias, overlaySize);
  }
}