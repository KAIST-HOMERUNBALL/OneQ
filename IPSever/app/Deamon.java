import controllers.Application;
import controllers.Point;
import play.jobs.*;

@Every("1mn")
//@OnApplicationStart
public class Deamon extends Job {
	
	@Override
	public void doJob() {
		System.out.println("Deamon start");
		Point p1 = new Point(200, 240);
		Point p2 = new Point(700, 480);
		System.out.println(Application.imageProcess(1, p1, p2));
		p1 = new Point(0, 100);
		p2 = new Point(704, 360);
		System.out.println(Application.imageProcess(2, p1, p2));
		p1 = new Point(50, 50);
		p2 = new Point(640, 480);
		System.out.println(Application.imageProcess(3, p1, p2));
		p1 = new Point(100, 200);
		p2 = new Point(700, 430);
		System.out.println(Application.imageProcess(4, p1, p2));
	}
}
