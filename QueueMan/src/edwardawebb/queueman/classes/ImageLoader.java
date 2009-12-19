/**
 *     This file is part of QueueMan.
 *
 *        QueueMan is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    QueueMan is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with QueueMan.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
