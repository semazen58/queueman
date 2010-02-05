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
 package edwardawebb.queueman.core;

import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.ImageLoader;
import edwardawebb.queueman.classes.NetFlix;
import edwardawebb.queueman.classes.NetFlixQueue;

public class MovieDetails extends Activity implements OnRatingBarChangeListener, OnClickListener {

	/*
	 * shared dialog
	 */
	private static Dialog dialog;
	private Button addButton;
	private TextView title;
	private TextView year;
	private ImageView boxart;
	private TextView synopsis;
	private RadioButton radioAddTop;
	private RadioButton radioAddBottom;
	private RadioButton radioAddInstant;
	private RatingBar avgRatingBar;
	private TextView formats;
	
	private Button rateMe;
	private Button noThanks;

	private Bitmap bitmap;
	private int action;

	private Disc disc;

	public static final String SEARCH = "isSearch";

	public static String INSTANT_DISC_FORMAT = "instant";

	/*
	 * Activity flow methods
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		disc = (Disc) intent.getSerializableExtra("Disc");
		action = intent.getIntExtra(QueueMan.ACTION_KEY, 0);

		if (action > 0) {
			switch (action) {
			/*case QueueMan.ACTION_MOVE:
				setContentView(R.layout.add_title_details);*/

			case QueueMan.ACTION_ADD:
				// set views
				setContentView(R.layout.add_movie_detail);
				addButton = (Button) findViewById(R.id.add_movie);
				addButton.setOnClickListener(new clickr());
							
				//radioOptions = (RadioGroup) findViewById(R.id.add_options);
				radioAddTop = (RadioButton) findViewById(R.id.radio_top);
				radioAddBottom = (RadioButton) findViewById(R.id.radio_bottom);
				radioAddInstant = (RadioButton) findViewById(R.id.radio_instant);

				formats = (TextView) findViewById(R.id.Formats);

				// populate views
				radioAddInstant.setEnabled(false);
				// get smaller image to save space for buttons
				loadBoxArt(disc.getBoxArtUrlLarge());
			
				synopsis = (TextView) findViewById(R.id.synopsis);
				
				synopsis.setText(Html.fromHtml(disc.getSynopsis()));

				// availability
				StringBuilder formatsText = new StringBuilder();
				formatsText.append("Availability: ");
				Iterator<String> it = disc.getFormats().iterator();
				while (it.hasNext()) {
					String str = it.next();
					formatsText.append(str);
					if (it.hasNext()) {
						formatsText.append(", ");
					}
					if (str.equals(INSTANT_DISC_FORMAT)
							&& QueueMan.canWatchInstant) {
						radioAddInstant.setEnabled(true);
					}
				}
				// formatsText.append("\n" + disc.getAvailibilityText());
				formats.setText(formatsText);				
				
				break;
			/*case QueueMan.ACTION_DELETE:
				setContentView(R.layout.add_title_details);*/
			}

		} else {
			// no action, just viewugn on in current q
			setContentView(R.layout.view_movie_details);
			// grab largest icon
			loadBoxArt(disc.getBoxArtUrlLarge());
			synopsis = (TextView) findViewById(R.id.synopsis);
			synopsis.setText(Html.fromHtml(disc.getSynopsis()));
		}

		title = (TextView) findViewById(R.id.mtitle);
		year = (TextView) findViewById(R.id.myear);

		//rating button
		rateMe= (Button) findViewById(R.id.RateMe);
		rateMe.setText("Rate Title");
		//rateMe.setHeight(20);
		rateMe.setOnClickListener(this);
		
		//current rating indicator
		avgRatingBar = (RatingBar) findViewById(R.id.DetailRatingBar01);
		if(disc.hasUserRating()){
			avgRatingBar.setRating(disc.getUserRating().floatValue());
			rateMe.setText(R.string.rate_button_rerate);
			
		}else{
			avgRatingBar.setRating(disc.getAvgRating().floatValue());
		}
		
		
		// set values based on disc
		title.setText(disc.getFullTitle());
		title.setTextSize(24);
		title.setTextColor(0xFF0000FF);

		year.setText(disc.getYear());
	}

	public void onStart() {
		super.onStart();
		// start analytics session
		FlurryAgent.onStartSession(this, QueueMan.FLURRY_APP_KEY);

	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	/**
	 * Background Thread handler and listeners
	 */

	// handler for callbacks to UI (primary) thread
	final Handler mHandler = new Handler();

	/*
	 * callbacks for bg thread that loads boxart
	 */
	final Runnable threadHandlerLoadBoxArtSuccess = new Runnable() {
		public void run() {
			boxart = (ImageView) findViewById(R.id.boxart);
			boxart.setImageBitmap(bitmap);
			boxart.setAdjustViewBounds(true); // set the ImageView bounds to
												// match the Drawable's
												// dimensions
			

		}
	};
	final Runnable threadHandlerLoadBoxArtFailed = new Runnable() {
		public void run() {
			// make changes in UI
			Toast.makeText(MovieDetails.this,
					"Unable to Load Disc Cover Image", Toast.LENGTH_LONG)
					.show();

		}
	};

	private void loadBoxArt(final String imageUrl) {
		// TODO Auto-generated method stub
		Thread t = new Thread() {
			public void run() {

				bitmap = ImageLoader.getBitmap(imageUrl);
				if (bitmap != null) {
					mHandler.post(threadHandlerLoadBoxArtSuccess);
				} else {
					mHandler.post(threadHandlerLoadBoxArtFailed);
				}

			}
		};
		t.start();

	}

	/**
	 * Event Listener
	 */
	class clickr implements Button.OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == addButton) {
				if (MovieDetails.this.radioAddTop.isChecked()) {
					// pass a yes back to search
					// setup an intent to return a result
					passBack(NetFlixQueue.QUEUE_TYPE_DISC);
				} else if (MovieDetails.this.radioAddInstant.isChecked()) {
					passBack(NetFlixQueue.QUEUE_TYPE_INSTANT);
					// toast no worky
				} else if (MovieDetails.this.radioAddBottom.isChecked()) {
					passBack(NetFlixQueue.QUEUE_TYPE_DISC,NetFlix.BOTTOM);
					// toast no worky
				} else {
					MovieDetails.this.radioAddTop.setChecked(true);
					passBack(NetFlixQueue.QUEUE_TYPE_DISC);
				}
			}

		}

		/*
		 * public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		 * long arg3) {
		 * 
		 * dialog=new Dialog(MovieDetails.this);
		 * dialog.setContentView(R.layout.custom_dialog);
		 * dialog.setTitle("Saving Movie"); TextView text = (TextView)
		 * dialog.findViewById(R.id.text);
		 * text.setText("Adding Movie to your Queue"); ImageView image =
		 * (ImageView) dialog.findViewById(R.id.image);
		 * image.setImageResource(R.drawable.icon); //show message
		 * dialog.show(); final Disc selection = (Disc)
		 * arg0.getItemAtPosition(arg2);
		 * 
		 * Thread t = new Thread(){ public void run(){
		 * 
		 * //Log.d("QueueSearch","Item clicked:"+ selection.getShortTitle())
		 * //returnResult(selection.getTitle(), selection.getId());
		 * 
		 * returnResult(selection);
		 * 
		 * } }; t.start();// TODO Auto-generated method stub
		 * 
		 * 
		 * }
		 */

	}


	private void showRatingDialog(String title) {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.active_rating_bar);
		dialog.setTitle(title);
		
		noThanks=(Button)dialog.findViewById(R.id.no_thanks);
		noThanks.setOnClickListener(this);
		((RatingBar)dialog.findViewById(R.id.ActiveRatingBar)).setOnRatingBarChangeListener(this);
		
		dialog.show();
	}
	public void passBack(int queueTypeDisc){
		passBack(queueTypeDisc, NetFlix.TOP);
	}
	
	public void passBack(final int queueTypeDisc, final int position) {
		// TODO Auto-generated method stub
		Thread t = new Thread() {
			public void run() {
				Bundle b = new Bundle();
				disc.setPosition(position);
				b.putSerializable("Disc", disc);

				Intent resultIntent = new Intent();
				resultIntent.putExtras(b);
				resultIntent.putExtra(QueueMan.ACTION_KEY, queueTypeDisc);
				MovieDetails.this.setResult(QueueSearch.ADD_MOVIES,
						resultIntent);

				finish();
			}
		};
		t.run();
	}

	public void onRatingChanged(RatingBar arg0, float arg1, boolean arg2) {
		// TODO Auto-generated method stub
		dialog.dismiss();
		disc.setUserRating(arg0.getRating());
		new SetRatings().execute(disc);
		avgRatingBar.setRating(arg0.getRating());	
		
	}

	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view==rateMe){
			showRatingDialog(disc.getShortTitle());
		}else if(view==noThanks){
			dialog.dismiss();
			avgRatingBar.setRating(0.0f);
			disc.setUserRating(0);
			new SetRatings().execute(disc);
		}
	}

	
	 private class SetRatings extends AsyncTask<Disc, Integer, Integer> {

		@Override
		protected Integer doInBackground(Disc... modifiedDisc) {
			int result;

			result=QueueMan.netflix.setRating(modifiedDisc[0]);
			// TODO Auto-generated method stub
			return result;
		}
		
		protected void onPostExecute(Integer result){
			switch(result){
				case 200:
				case 201:
					//successful rating
					//continue
				case 422:
					//already rated
					Toast.makeText(MovieDetails.this, "Title Rated!", Toast.LENGTH_SHORT).show();
					rateMe.setText(R.string.rate_button_rerate);
					break;
				default:
					FlurryAgent.onError("SetRatings-Failed", "Error:"+result + " | HTTP: " + QueueMan.netflix.lastResponseMessage + " | " + QueueMan.netflix.lastNFResponseMessage, "QueueMan");
					Toast.makeText(MovieDetails.this, "Oops: " + QueueMan.netflix.lastResponseMessage, Toast.LENGTH_SHORT).show();
					
					
			}
		}
		 
	 }
	 
	 /**
	  * 
	  * @param dips
	  * @return pixels
	  */
	 private int getPixels(int dips){
		// The gesture threshold expressed in dip
		 final float GESTURE_THRESHOLD_DIP = 16.0f;

		 // Convert the dips to pixels
		 final float scale = getBaseContext().getResources().getDisplayMetrics().density;
		return (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);

		 // Use mGestureThreshold as a distance in pixels
	 }
		 
}
