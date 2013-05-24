package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Camera extends Model {

	public String ip;
	public String streaming_url;

	public String name;
	public String loc;

	public String opentime;
	public String closetime;

	@OneToMany
	public List<Waiting_data> datas;

	public Camera(String name, String loc, String ip, String streaming_url) {
		this.name = name;
		this.loc = loc;
		this.ip = ip;
		this.streaming_url = streaming_url;
	}

	public Camera(String name, String loc, String ip, String streaming_url,
			String ot, String ct) {
		this.name = name;
		this.loc = loc;
		this.ip = ip;
		this.streaming_url = streaming_url;
		this.opentime = ot;
		this.closetime = ct;
	}

}
