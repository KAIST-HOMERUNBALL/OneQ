import controllers.Application;
import play.jobs.*;

@Every("15s")
//@OnApplicationStart
public class Deamon extends Job {
	
	@Override
	public void doJob() {
		System.out.println("Deamon start");
		System.out.println(Application.imageProcess(1));
	}
}
