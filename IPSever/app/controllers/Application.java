package controllers;

import play.*;
import play.mvc.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IError;

import models.*;

public class Application extends Controller {
	public static final String NORTH_CAFE_RTSP_URL = "rtsp://admin:admin@testline0943.iptime.org/Slave-0";
	public static final String NORTH_MEILU_RTSP_URL = "rtsp://admin:admin@testline0943.iptime.org/Slave-0";
	public static final String EAST_CAFE_RTSP_URL = "rtsp://admin:admin@iptime0943.iptime.org/Slave-0";
	public static final String WEST_CAFE_RTSP_URL = "rtsp://admin:admin@iptime0909.iptime.org/Slave-0";

	public static final String NORTH_CAFE_RECENT_IMAGE = "NCRI";
	public static final String NORTH_MEILU_RECENT_IMAGE = "NMRI";
	public static final String EAST_CAFE_RECENT_IMAGE = "ECRI";
	public static final String WEST_CAFE_RECENT_IMAGE = "WCRI";
	
	public static boolean save_flag = false;
	public static BufferedImage bi = null;
	public static int which = 1;

	public static void index() {
		render();
	}

	public static String imageProcess(int which) {
		System.out.println("Image Processing start");
		Application.which = which;
		
		String url;
		switch (which) {
		case 1:
			url = NORTH_CAFE_RTSP_URL;
			break;
		case 2:
			url = NORTH_MEILU_RTSP_URL;
			break;
		case 3:
			url = EAST_CAFE_RTSP_URL;
			break;
		case 4:
			url = WEST_CAFE_RTSP_URL;
			break;
		default:
			url = NORTH_CAFE_RTSP_URL;
			break;
		}

		IMediaListener ml = new MediaListenerAdapter() {
			public void onVideoPicture(IVideoPictureEvent ev) {
				try {
					System.out.println("Image loading");
					File f = new File(Application.makeFileName(Application.which));
					Application.bi = ev.getImage();
					ImageIO.write(Application.bi, "png", f);
					Application.save_flag = true;
					System.out.println("Image loading done");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		IMediaReader mr = ToolFactory.makeReader(url);
		mr.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		mr.setQueryMetaData(false);
		mr.addListener(ml);
		
		while(true) {
			IError err = null;
			if (mr != null) {
				err = mr.readPacket();
			}
			
			if (save_flag)
				break;
			
			if (err != null) {
				System.out.println("Error : " + err);
				break;
			}
		}
		mr.close();
		save_flag = false;
		
		return "Image Processing Done";
	}
	

	public static String makeFileName(int which) {
		String file;
		switch (which) {
		case 1:
			file = NORTH_CAFE_RECENT_IMAGE;
			break;
		case 2:
			file = NORTH_MEILU_RECENT_IMAGE;
			break;
		case 3:
			file = EAST_CAFE_RECENT_IMAGE;
			break;
		case 4:
			file = WEST_CAFE_RECENT_IMAGE;
			break;
		default:
			file = NORTH_CAFE_RECENT_IMAGE;
			break;
		}
		return file + ".png";
	}
}