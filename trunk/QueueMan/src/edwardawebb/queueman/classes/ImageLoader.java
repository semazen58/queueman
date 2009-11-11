package edwardawebb.queueman.classes;

/**
 * QueueMan
 * Oct 13, 2009
 *  http://edwardawebb.com/
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Edward A. Webb - http://edwardawebb.com
 * 
 */

public class ImageLoader {

	public static Bitmap getBitmap(String imageUrl) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(imageUrl).openConnection();

			conn.setDoInput(true);

			conn.connect();

			InputStream inStream;
			inStream = conn.getInputStream();

			bitmap = BitmapFactory.decodeStream(inStream);
			inStream.close();
			conn.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}
}
