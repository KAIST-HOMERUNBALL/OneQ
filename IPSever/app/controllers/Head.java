package controllers;

import java.util.ArrayList;

public class Head {
	ArrayList<Point> pixels;
	Point center;
	int radius;
	
	public Head() {
		this.pixels = new ArrayList<Point>();
		this.center = new Point();
		this.radius = 0;
	}
	
	public void addPixel(int x, int y) {
		Point posi = new Point(x, y);
		this.pixels.add(posi);
	}
	
	public Point findCenter() {
		int x = 0;
		int y = 0;
		for (Point pixel : this.pixels) {
			x += pixel.getX();
			y += pixel.getY();
		}
		
		this.center.setX(x / this.pixels.size());
		this.center.setY(y / this.pixels.size());
		
		return this.center;
	}
	
	public int findRadius() {
		//Point p = pixels.get(0);
		//radius = center.distance(p);
		this.radius = (findMaxHeight() + findMaxWidth()) / 4;
		
		return radius;
	}
	
	public int findMaxHeight() {
		int max = this.pixels.get(0).getY();
		int min = this.pixels.get(0).getY();
		for (Point p : this.pixels) {
			if (max < p.getY()) max = p.getY();
			else if (min > p.getY()) min = p.getY();
		}
		
		return max - min;
	}
	
	public int findMaxWidth() {
		int max = this.pixels.get(0).getX();
		int min = this.pixels.get(0).getX();
		for (Point p : this.pixels) {
			if (max < p.getX()) max = p.getX();
			else if (min > p.getX()) min = p.getX();
		}
		
		return max - min;
	}
	
	public int findArea() {
		return (int)(Math.pow(this.radius, 2) * 3.14);
	}
}
