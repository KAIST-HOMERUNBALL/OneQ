package controllers;

import play.*;
import play.mvc.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

import models.*;

public class Application extends Controller {
	private static IMediaListener ml = new MediaListenerAdapter() {
		public void onVideoPicture(IVideoPictureEvent ev) {
			try {
				File f = new File("recent_image.png");
				BufferedImage bi = ev.getImage();
				ImageIO.write(bi, "png", f);
				//System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	public static void index() {
		render();
	}

}