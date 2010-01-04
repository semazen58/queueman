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
package edwardawebb.queueman.core;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.ImageLoader;
import edwardawebb.queueman.classes.NetFlix;


/**
 * @author eddie
 *
 */
public class HomeTitles extends Activity implements OnItemClickListener {
	
	List<Disc> discs;
	TextView noTitles;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.titles_at_home);

	    
	    noTitles = (TextView) findViewById(R.id.no_titles);
	    
	    GridView gridview = (GridView) findViewById(R.id.grid_athome);
	    gridview.setAdapter(new ImageAdapter(this,NetFlix.homeQueue.getDiscs()));
	    gridview.setOnItemClickListener(this);
	    if(NetFlix.homeQueue.getDiscs().size()<1){
	    	Toast.makeText(this, "No Titles at Home", Toast.LENGTH_LONG);
	    }else{
	    	noTitles.setText(NetFlix.homeQueue.getDiscs().size() + " title(s) at home or in transit.\nPlease wait as titles load.");
	    	//noTitles.setVisibility(View.INVISIBLE);
	    }
	    	
	}
	
	
	public class DownloadImage extends AsyncTask<String, Bitmap, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... arg0) {
			Bitmap bitmap = ImageLoader.getBitmap(arg0[0]);					
			return bitmap;
		}

	     protected Bitmap onPostExecute(Bitmap... bitmap) {
	       return bitmap[0];
	     }
	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;
	    private List<Disc> mDiscs;
	 // references to our images
	    public ImageAdapter(Context c,List<Disc> discs) {
	    	mContext = c;
	    	mDiscs=discs;
	    }

	    public int getCount() {
	        return mDiscs.size();
	    }

	    public Disc getItem(int position) {
	        return (Disc) mDiscs.get(position);
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            imageView.setLayoutParams(new GridView.LayoutParams(110, 150));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            imageView.setPadding(8, 8, 8, 8);
	            
	        } else {
	            imageView = (ImageView) convertView;
	        }
	        
	        DownloadImage di = new DownloadImage();
	        di.execute(mDiscs.get(position).getBoxArtUrlLarge());
	        Log.d("HomeTitles",mDiscs.get(position).getBoxArtUrlLarge());
	        try {
				imageView.setImageBitmap(di.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return imageView;
	    }


	    
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// User has clicked an title cover, view the details.
		Intent intent = new Intent(this,
				edwardawebb.queueman.core.MovieDetails.class);
		Bundle b = new Bundle();
		//grab disc to add as bundle sent o details activity
		Disc disc = NetFlix.homeQueue.getDiscs().get(position);
		//netflix.getTitleState(disc.getId());
		b.putSerializable("Disc", disc);
		intent.putExtras(b);
		
			startActivity(intent);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
