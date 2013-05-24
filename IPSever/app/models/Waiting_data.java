package models;

import java.util.*;
import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import play.libs.MimeTypes;

import play.db.jpa.*;

@Entity
public class Waiting_data extends Model {

	public String datetime;
	public Blob original_image;
	public Blob infographic_image;
	public int number_of_people;
	public int waiting_time;

	@ManyToOne
	public Camera camera;

	public Waiting_data(Camera camera, String datetime, int number_of_people,
			int waiting_time) {

		this.camera = camera;
		this.datetime = datetime;
		this.number_of_people = number_of_people;
		this.waiting_time = waiting_time;
	}

	public Waiting_data(Long cameraId, int number_of_people, int waiting_time,
			File original_image, File infographic_image)
			throws FileNotFoundException {
		Date cur = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Camera c = Camera.findById(cameraId);

		this.camera = c;
		this.datetime = df.format(cur);
		this.number_of_people = number_of_people;
		this.waiting_time = waiting_time;
		this.original_image = new Blob();
		this.original_image.set(new FileInputStream(original_image),
				MimeTypes.getContentType(original_image.getName()));
		this.infographic_image = new Blob();
		this.infographic_image.set(new FileInputStream(infographic_image),
				MimeTypes.getContentType(infographic_image.getName()));
	}

	public Waiting_data(Camera camera, int number_of_people, int waiting_time) {
		Date cur = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		this.camera = camera;
		this.datetime = df.format(cur);
		this.number_of_people = number_of_people;
		this.waiting_time = waiting_time;
	}

	public Waiting_data current_waiting_data(Camera camera) {
		return Waiting_data.find("camera = ? order by datetime desc", camera)
				.first();
	}

	public List<Waiting_data> get_waiting_data(Camera camera, String datetime) {
		return Waiting_data.find("camera = ? and datetime like ?", camera,
				datetime).fetch();
	}

	public int get_average_time_after5(Camera camera) {
		int sum = 0;
		List<Waiting_data> data = Waiting_data
				.find("camera = ? and DATE_FORMAT(datetime,'%H %i') = DATE_FORMAT(ADDTIME(SYSDATE(),'0:5:0'),'%H %i')",
						camera).fetch();
		for (int i = 0; i < data.size(); i++) {
			sum += data.get(i).waiting_time;
		}
		return sum / data.size();
	}

	public Waiting_data getTest() {
		return Waiting_data.find("byId", 1).first();
	}
}
