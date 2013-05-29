package controllers;

import play.*;
import play.db.DB;
import play.mvc.*;

import groovy.ui.text.FindReplaceUtility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
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

	public static int image_num = 0;
	public static boolean save_flag = false;
	public static BufferedImage bi = null;
	public static int which = 1;

	public static void index() {
		// System.out.println("start");
		// Waiting_data wt = Waiting_data.find("byID", 1).first();
		// System.out.println(wt.number_of_people);
		/*Waiting_data wd;
		try {
			wd = new Waiting_data(1L, 4, 3, new File("NCRI.png"), new File(
					"ECRI.png"));
			wd.save();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		render();
	}

	public static String imageProcess(int which, Point p1, Point p2) {
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
					if (Application.image_num < 3) {
						Application.image_num += 1;
					} else {
						System.out.println("Image loading");
						File f = new File(Application.makeFileName(
								Application.which, ""));
						Application.bi = ev.getImage();
						ImageIO.write(Application.bi, "png", f);
						Application.save_flag = true;
						System.out.println("Image loading done");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		IMediaReader mr = ToolFactory.makeReader(url);
		mr.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		mr.setQueryMetaData(false);
		mr.addListener(ml);

		while (true) {
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
		image_num = 0;

		ImageProcessor ip = new ImageProcessor(bi, 2, p1, p2);
		ip.floodfill();
		ip.markHeads();
		ip.make2FileByName(makeFileName(which, "INFO"), ip.getOri_im());

		try {
			Waiting_data wd = new Waiting_data((long) which, ip.getHeads()
					.size(), ip.getHeads().size() * 1, new File(makeFileName(
					which, "INFO")), new File(makeFileName(which, "")));
			wd.save();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Image Processing Done";
	}

	public static String makeFileName(int which, String postfix) {
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
		return file + "_" + postfix + ".png";
	}
}