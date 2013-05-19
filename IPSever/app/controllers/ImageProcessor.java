package controllers;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageProcessor {
	private BufferedImage Ori_im; // original Image
	private BufferedImage Cropped_im; // cropped Image
	private BufferedImage Resized_im; // small Image
	private BufferedImage Head_im; // test

	private ArrayList<Head> heads;

	private int window_size;
	
	private Point crop_pos1;

	private int width;
	private int height;
	private int width_c;
	private int height_c;
	private int width_r;
	private int height_r;

	private boolean checked[][];

	public ImageProcessor(BufferedImage bi) {
		this.Ori_im = bi;
		this.width = bi.getWidth();
		this.height = bi.getHeight();

		this.window_size = 2;

		this.heads = new ArrayList<Head>();

		this.width_r = countWindowPerLine(this.width, this.window_size);
		this.height_r = countWindowPerLine(this.height, this.window_size);
		this.Resized_im = new BufferedImage(this.width_r, this.height_r,
				BufferedImage.TYPE_INT_RGB);

		make2smallImage(this.Ori_im);

		this.checked = new boolean[this.width_r][this.height_r];
	}

	public ImageProcessor(BufferedImage bi, int window_size) {
		this.Ori_im = bi;
		this.width = bi.getWidth();
		this.height = bi.getHeight();

		this.window_size = window_size;

		this.heads = new ArrayList<Head>();

		this.width_r = countWindowPerLine(this.width, this.window_size);
		this.height_r = countWindowPerLine(this.height, this.window_size);
		this.Resized_im = new BufferedImage(this.width_r, this.height_r,
				BufferedImage.TYPE_INT_RGB);

		make2smallImage(this.Ori_im);

		this.checked = new boolean[this.width_r][this.height_r];

		this.Head_im = new BufferedImage(this.width_r, this.height_r,
				BufferedImage.TYPE_INT_RGB);
	}
	
	public ImageProcessor(BufferedImage bi, int window_size, Point p1, Point p2) {
		this.Ori_im = bi;
		this.width = bi.getWidth();
		this.height = bi.getHeight();

		this.Cropped_im = Ori_im.getSubimage(p1.getX(), p1.getY(), p2.getX() - p1.getX(), p2.getY() - p1.getY());
		this.crop_pos1 = p1;
		
		this.width_c = this.Cropped_im.getWidth();
		this.height_c = this.Cropped_im.getHeight();
		
		this.window_size = window_size;

		this.heads = new ArrayList<Head>();

		this.width_r = countWindowPerLine(this.width_c, this.window_size);
		this.height_r = countWindowPerLine(this.height_c, this.window_size);
		this.Resized_im = new BufferedImage(this.width_r, this.height_r,
				BufferedImage.TYPE_INT_RGB);

		make2smallImage(this.Cropped_im);

		this.checked = new boolean[this.width_r][this.height_r];

		this.Head_im = new BufferedImage(this.width_r, this.height_r,
				BufferedImage.TYPE_INT_RGB);
	}

	public int countWindowPerLine(int line_size, int window_size) {
		// TODO end-corner side processing should be implemented later
		// return line_size % window_size == 0 ? line_size / window_size :
		// line_size / window_size + 1;
		return line_size / window_size;
	}

	public void make2smallImage(BufferedImage im) {
		for (int i = 0; i < this.width_r; i++)
			for (int j = 0; j < this.height_r; j++)
				Resized_im.setRGB(i, j, getAverageRGB(im, i, j));
	}

	public int getAverageRGB(BufferedImage im, int i, int j) {
		int r = 0;
		int g = 0;
		int b = 0;

		for (int x = 0; x < this.window_size; x++) {
			for (int y = 0; y < this.window_size; y++) {
				int argb = im.getRGB(i * this.window_size + x, j
						* this.window_size + y);
				r += (argb >> 16) & 0xFF;
				g += (argb >> 8) & 0xFF;
				b += argb & 0xFF;
			}
		}

		r /= (int) Math.pow(this.window_size, 2);
		g /= (int) Math.pow(this.window_size, 2);
		b /= (int) Math.pow(this.window_size, 2);

		// System.out.println((r << 16 | g << 8 | b));
		return (r << 16 | g << 8 | b);
	}

	public void floodfill() {
		for (int i = 0; i < this.width_r; i++)
			for (int j = 0; j < this.height_r; j++)
				floodfillUnit(i, j);
	}

	public void floodfillUnit(int x, int y) {
		if (!getChecked(x, y)) {
			setChecked(x, y);
			Head head = new Head();
			if (isHead(x, y)) {
				this.heads.add(head);
				if (x < this.width_r - 1)
					floodfillHead(head, x + 1, y);
				if (y < this.height_r - 1)
					floodfillHead(head, x, y + 1);
				if (x > 0)
					floodfillHead(head, x - 1, y);
				if (y > 0)
					floodfillHead(head, x, y - 1);
			}
			setColored(head, x, y);
		} else {
			return;
		}
	}

	public void floodfillHead(Head head, int x, int y) {
		if (!getChecked(x, y)) {
			setChecked(x, y);
			if (isHead(x, y)) {
				if (x < this.width_r - 1)
					floodfillHead(head, x + 1, y);
				if (y < this.height_r - 1)
					floodfillHead(head, x, y + 1);
				if (x > 0)
					floodfillHead(head, x - 1, y);
				if (y > 0)
					floodfillHead(head, x, y - 1);
			}
			setColored(head, x, y);
		} else {
			return;
		}
	}
	
	public void markHeads() {
		ArrayList<Head> remove = new ArrayList<Head>();
		for(Head h : this.heads) {
			Point center = h.findCenter();
			int radius = h.findRadius();
			if (radius < 7) {
				remove.add(h);
				continue;
			}
			
			if (h.pixels.size() < h.findArea() * 0.3) {
				remove.add(h);
				continue;
			}
			
			Point center_re = new Point(center.getX() * this.window_size + this.crop_pos1.getX(), center.getY() * this.window_size + this.crop_pos1.getY());
			int radius_re = radius * this.window_size;
			
			int x1 = center_re.getX() < radius_re ? 0 : center_re.getX() - radius_re;
			int x2 = center_re.getX() + radius_re > width ? width - 1 : center_re.getX() + radius_re;
			int y1 = center_re.getY() < radius_re ? 0 : center_re.getY() - radius_re;
			int y2 = center_re.getY() + radius_re > height ? height - 1 : center_re.getY() + radius_re;
			
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					Point p = new Point(x, y);
					if (p.distance(center_re) == radius_re | p.distance(center_re) == radius_re + 1 | p.distance(center_re) == radius_re - 1)
						this.Ori_im.setRGB(p.getX(), p.getY(), 0xFF0000);
				}
			}
		}
		
		for (Head h : remove) {
			this.heads.remove(h);
		}
	}

	public boolean getChecked(int x, int y) {
		return this.checked[x][y];
	}

	public void setChecked(int x, int y) {
		this.checked[x][y] = true;
	}

	public void setColored(Head head, int x, int y) {
		if (isHead(x, y)) {
			this.Head_im.setRGB(x, y, 0x000000);
			head.addPixel(x, y);
		} else {
			this.Head_im.setRGB(x, y, 0xFFFFFF);
		}
	}

	public boolean isHead(int x, int y) {
		int rgb = this.Resized_im.getRGB(x, y);
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = (rgb) & 0xFF;

		// System.out.println(String.format("%d : %d : %d", r, g, b));

		return (r < 40 && g < 40 && b < 45 && r > 15 && g > 15 && b > 20);
	}

	public void make2File() {
		try {
			ImageIO.write(this.Cropped_im, "png", new File("Cropped_im.png"));
			ImageIO.write(this.Resized_im, "png", new File("Resized_im.png"));
			ImageIO.write(this.Head_im, "png", new File("Head_im.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void show(String title, final BufferedImage img, int x, int y) {
		JFrame f = new JFrame(title);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new JPanel() {
			@Override
			protected void paintChildren(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(img, null, 0, 0);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(img.getWidth(), img.getHeight());
			}
		});
		f.pack();
		f.setLocation(x, y);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			BufferedImage image = ImageIO.read(new File("NCRI.png"));
			ImageProcessor ip = new ImageProcessor(image, 2, new Point(0, 0), new Point(704, 480));
			
			//ip.show("Original Image", ip.Ori_im, 0, 0);
			ip.floodfill();
			ip.markHeads();
			ip.make2File();
			System.out.println(ip.heads.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
