/**
 *     This file is part of QueueMan.
 *
 *	  QueueMan is free software: you can redistribute it and/or modify
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author Eddie
 *
 */
public class JSON2Queue {
	
	
	/**
	 * Adds moies found in stream to athome queue
	 * @param is
	 * @return #of discs added
	 */
	public static int parseAtHome(InputStream is){
	
		String json;
		int i=0;
			
		try {
			json = Utils.convertStreamToString(is);
			Log.d("NetFlixResponse","json: " + json);
			JSONObject obj = new JSONObject(json);
			JSONObject atHome =obj.getJSONObject("at_home");
			
			
			/*JSONArray questions = interview.getJSONArray("questions");
			x += "Number of questions: " + questions.length()  + "nn";
		*/
			for (i=0;i<Integer.parseInt(atHome.getString("number_of_results"));i++)
			{
				JSONObject title = atHome.getJSONObject("at_home_item");
				Disc tempMovie = 
					new Disc(
							title.getString("id"),getUniqueId(title), title.getJSONObject("title").getString("short"),
							title.getJSONObject("title").getString("regular"),
							title.getJSONObject("box_art").getString("small"), 0D,
						getSynopsis(title), title.getString("release_year"), true);
				// this will add mppaa or tv rating
				addCategories(title, tempMovie);
				/*new Disc(
						id,uniqueID, stitle, ftitle, boxArtUrl, rating,
					synopsis, year, isAvailable);*/
				tempMovie.setAvailibilityText("");
				//tempMovie.setMpaaRating(new String(rating));
				tempMovie.setQueueType(NetFlixQueue.QUEUE_TYPE_HOME);
				NetFlix.homeQueue.add(tempMovie);
			}
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}	
	
	
	
	/**
	 * Adds movies found in stream to reccomended queue
	 * @param is
	 * @return #of discs added
	 */
	public static int parseRecommended(InputStream is){
	
		String json;
		int i=0;
			
		try {
			json = Utils.convertStreamToString(is);
			Log.d("NetFlixResponse","json: " + json);
			JSONObject obj = new JSONObject(json);
			JSONObject atHome =obj.getJSONObject("recommendations");
			
			
			/*JSONArray questions = interview.getJSONArray("questions");
			x += "Number of questions: " + questions.length()  + "nn";
		*/
			for (i=0;i<Integer.parseInt(atHome.getString("number_of_results"));i++)
			{
				JSONObject title = atHome.getJSONObject("recommendation");
				Disc tempMovie = 
					new Disc(
							title.getString("id"),getUniqueId(title), title.getJSONObject("title").getString("short"),
							title.getJSONObject("title").getString("regular"),
							title.getJSONObject("box_art").getString("small"), 0D,
						getSynopsis(title), title.getString("release_year"), true);
				// this will add mppaa or tv rating
				//addCategories(title, tempMovie);
				/*new Disc(
						id,uniqueID, stitle, ftitle, boxArtUrl, rating,
					synopsis, year, isAvailable);*/
				tempMovie.setAvailibilityText("");
				//tempMovie.setMpaaRating(new String(rating));
				tempMovie.setQueueType(NetFlixQueue.QUEUE_TYPE_RECOMMEND);
				NetFlix.recomemendedQueue.add(tempMovie);
			}
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Helper clasees
	 * 
	 */
	
	
	
	/***
	 * 
	 */
	private static String getUniqueId(JSONObject title){
		// only way i could get id to ocompare acrost queues
		try {
			return (String) title.getString("id").subSequence(0,title.getString("id").lastIndexOf("/"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}
	public static String getSynopsis(JSONObject queueTitle){
		String synopsis="";
		try {
			JSONArray links = queueTitle.getJSONArray("link");
			for(int i = 0;i<links.length();i++){
				
					if (links.getJSONObject(i).getString("title").equals("synopsis")){
							synopsis=links.getJSONObject(i).getString("synopsis");
					}				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return synopsis;
	}
	
	/**
	 * this will all genres and rating(MPAA/TV) to array
	 */
	private static void addCategories(JSONObject title,Disc tempMovie){
		// get ratings and genres from 'category' array
		//HashMap categories=new HashMap<String, String>();
		try {
			JSONArray jsoncategories = title.getJSONArray("category");
			for(int i=0;i<jsoncategories.length();i++){
			
				if (jsoncategories.getJSONObject(i).getString("scheme").equals("http://api.netflix.com/categories/tv_ratings")
						||jsoncategories.getJSONObject(i).getString("scheme").equals("http://api.netflix.com/categories/mpaa_ratings")){
					//categories.add("rating",(object)=jsoncategories.getJSONObject(i).getString("label"));
					tempMovie.setMpaaRating(new String(jsoncategories.getJSONObject(i).getString("label")));
					
				}
			} 
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
