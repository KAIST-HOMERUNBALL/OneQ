package controllers;

import play.*;
import play.db.DB;
import play.mvc.*;

import groovy.ui.text.FindReplaceUtility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

import models.*;

public class Application extends Controller {
	public static final double SECONDS_BETWEEN_FRAMES = 0;

	/** The number of nano-seconds between frames. */

	public static final long NANO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);

	/** Time of last frame write. */

	private static long mLastPtsWrite = Global.NO_PTS;

	public static final String NORTH_CAFE_RTSP_URL = "rtsp://admin:admin@testline0943.iptime.org:554/Slave-0";
	public static final String NORTH_MEILU_RTSP_URL = "rtsp://admin:admin@testline0943.iptime.org:5555/Slave-0";
	public static final String EAST_CAFE_RTSP_URL = "rtsp://admin:admin@iptime0943.iptime.org:554/Slave-0";
	public static final String WEST_CAFE_RTSP_URL = "rtsp://admin:admin@iptime0909.iptime.org:554/Slave-0";

	public static final String NORTH_CAFE_RECENT_IMAGE = "NCRI";
	public static final String NORTH_MEILU_RECENT_IMAGE = "NMRI";
	public static final String EAST_CAFE_RECENT_IMAGE = "ECRI";
	public static final String WEST_CAFE_RECENT_IMAGE = "WCRI";

	public static BufferedImage bi = null;
	public static File mFile = null;
	public static int which = 1;
	public static int pictureNum = 0;

	public static void index() {
		// System.out.println("start");
		// Waiting_data wt = Waiting_data.find("byID", 1).first();
		// System.out.println(wt.number_of_people);

		render();
	}

	@SuppressWarnings("deprecation")
	public static String imageProcess(int which, Point p1, Point p2) {
		System.out.println("Image Processing start : " + which);
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

		if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
			throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler" + " support) for this demo to work");

		// create a Xuggler container object

		IContainer container = IContainer.make();

		// open up the container

		if (container.open(url, IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("could not open file: " + url);

		System.out.println("[SUCCESS] open connection");

		// query how many streams the call to open found

		int numStreams = container.getNumStreams();

		// and iterate through the streams to find the first video stream

		int videoStreamId = -1;
		IStreamCoder videoCoder = null;
		for (int i = 0; i < numStreams; i++) {
			// find the stream object

			IStream stream = container.getStream(i);

			// get the pre-configured decoder that can decode this stream;

			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoCoder = coder;
				break;
			}
		}

		if (videoStreamId == -1)
			throw new RuntimeException("could not find video stream in container: " + url);

		// Now we have found the video stream in this file. Let's open up
		// our decoder so it can do work

		if (videoCoder.open() < 0)
			throw new RuntimeException("could not open video decoder for container: " + url);

		IVideoResampler resampler = null;
		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
			// if this stream is not in BGR24, we're going to need to
			// convert it. The VideoResampler does that for us.

			resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24, videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
			if (resampler == null)
				throw new RuntimeException("could not create color space resampler for: " + url);
		}

		// Now, we start walking through the container looking at each packet.

		IPacket packet = IPacket.make();
		while (container.readNextPacket(packet) >= 0) {

			// Now we have a packet, let's see if it belongs to our video strea

			if (packet.getStreamIndex() == videoStreamId) {
				// We allocate a new picture to get the data out of Xuggle

				IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());

				int offset = 0;
				while (offset < packet.getSize()) {
					// Now, we decode the video, checking for any errors.

					int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
					if (bytesDecoded < 0)
						throw new RuntimeException("got error decoding video in: " + url);
					offset += bytesDecoded;

					// Some decoders will consume data in a packet, but will not
					// be able to construct a full video picture yet. Therefore
					// you should always check if you got a complete picture
					// from
					// the decode.

					if (picture.isComplete()) {
						IVideoPicture newPic = picture;

						// If the resampler is not null, it means we didn't get
						// the
						// video in BGR24 format and need to convert it into
						// BGR24
						// format.

						if (resampler != null) {
							// we must resample
							newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
							if (resampler.resample(newPic, picture) < 0)
								throw new RuntimeException("could not resample video from: " + url);
						}

						if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
							throw new RuntimeException("could not decode video as BGR 24 bit data in: " + url);
						
						//if(pictureNum < 5) {
						//	pictureNum++;
						//	continue;
						//}

						// convert the BGR24 to an Java buffered image

						BufferedImage javaImage = Utils.videoPictureToImage(newPic);

						// process the video frame

						if (newPic.isKeyFrame()) {
							bi = javaImage;
							mFile = processFrame(newPic, javaImage);
							pictureNum = 0;
							
							break;
						}
					}
				}
				//System.out.println("breaked1");
			} else {
				// This packet isn't part of our video stream, so we just
				// silently drop it.
				do {
				} while (false);
			}
			if (mFile != null) {
				
				videoCoder.close();
				container.close();
				break;
			}
		}
		//System.out.println("breaked2");

		File ori_file = make2FileByName(makeFileName(which, ""), bi);
		ImageProcessor ip = new ImageProcessor(bi, 2, p1, p2);
		ip.floodfill();
		ip.markHeads();
		File info_file = make2FileByName(makeFileName(which, "INFO"), ip.getOri_im());

		try {
			int people = ip.getHeads().size();
			Waiting_data wd = new Waiting_data((long) which, people, makeWaitingTime(which, people), info_file, ori_file);
			wd.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		bi = null;
		mFile = null;

		return "Image Processing Done : " + which;
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
		return postfix.equals("") ? file + ".png" : file + "_" + postfix + ".png";
	}
	
	public static File make2FileByName(String name, BufferedImage bi) {
		try {
			File mFile = new File(name);
			ImageIO.write(bi, "png", mFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return mFile;
	}
	
	public static int makeWaitingTime(int which, int numOfPeople) {
		return 0;
	}

	private static File processFrame(IVideoPicture picture, BufferedImage image) {
		try {
			// if uninitialized, backdate mLastPtsWrite so we get the very
			// first frame

			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = picture.getPts() - NANO_SECONDS_BETWEEN_FRAMES;

			// if it's time to write the next frame

			if (picture.getPts() - mLastPtsWrite >= NANO_SECONDS_BETWEEN_FRAMES) {
				// Make a temorary file name

				File file = null;//= File.createTempFile("frame", ".png");
				file = new File("frmae.png");

				// write out PNG

				ImageIO.write(image, "png", file);

				// indicate file written

				double seconds = ((double) picture.getPts()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n", seconds, file);

				// update last write time

				mLastPtsWrite += NANO_SECONDS_BETWEEN_FRAMES;

				return file;
			}

			return null;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}
}