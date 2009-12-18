package edwardawebb.queueman.core;

import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.ImageLoader;
import edwardawebb.queueman.classes.NetFlixQueue;

public class MovieDetails extends Activity {

	/*
	 * shared dialog
	 */
	private static Dialog dialog;
	private Button addButton;
	private Button cancelButton;
	private TextView title;
	private TextView year;
	private TextView rating;
	private ImageView boxart;
	private TextView synopsis;
	private RelativeLayout searchOptions;
	private RadioGroup radioOptions;
	private RadioButton radioAddTop;
	private RadioButton radioAddInstant;
	private RatingBar ratingBar;
	private TextView formats;
	
	private Button noThanks;
	private RatingBar rate;

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
			case QueueMan.ACTION_MOVE:
				setContentView(R.layout.add_movie_detail);

			case QueueMan.ACTION_ADD:
				// set views
				setContentView(R.layout.add_movie_detail);
				addButton = (Button) findViewById(R.id.add_movie);
				addButton.setOnClickListener(new clickr());
				radioOptions = (RadioGroup) findViewById(R.id.add_options);
				radioAddTop = (RadioButton) findViewById(R.id.radio_top);
				radioAddInstant = (RadioButton) findViewById(R.id.radio_instant);

				formats = (TextView) findViewById(R.id.Formats);

				// populate views
				radioAddInstant.setEnabled(false);
				// get smaller image to save space for buttons
				loadBoxArt(disc.getBoxArtUrlMedium());
				// we may need to trim this is searching, or moving to allow
				// room for buttons
				String snippet = null;
				if (disc.getSynopsis().length() > 250) {
					snippet = disc.getSynopsis().substring(0, 250) + "...";
				} else {
					snippet = disc.getSynopsis();
				}
				synopsis = (TextView) findViewById(R.id.synopsis);
				synopsis.setText(Html.fromHtml(snippet));

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

				//ratings!
				/*rate=new RatingBar(ctxt);
				noThanks=new Button(ctxt);
				
				noThanks.setBackgroundResource(R.drawable.no_interest_drk);
				noThanks.setPadding(5,0,0,0);
				noThanks.setWidth(92);
				noThanks.setHeight(16);
				
				rate.setNumStars(5);
				rate.setStepSize(0.1f);			
								
				RatingBar.OnRatingBarChangeListener l=
											new RatingBar.OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar,float rating,boolean fromTouch)	{
						rates[(Integer)ratingBar.getTag()]=rating;
						if(fromTouch) {
							rate.setStepSize(1.0f) ;
							new SetRating().execute(String.valueOf(disc.getId()),String.valueOf((int)rating));
						}
					}
				};
				Button.OnClickListener cl=
					new Button.OnClickListener() {				

						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							noThanks.setBackgroundResource(R.drawable.no_interest_lght);
							rate.setRating(0.0f);
							rate.setEnabled(false);
							new SetRating().execute(String.valueOf(disc.getId()),"not_interested");
							
						}
					};
					
				
				noThanks.setOnClickListener(this);	
				
				rate.setOnRatingBarChangeListener(this);
				*/
				
				break;
			case QueueMan.ACTION_DELETE:
				setContentView(R.layout.add_movie_detail);
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

		ratingBar = (RatingBar) findViewById(R.id.RatingBar01);
		ratingBar.setNumStars(5);
		ratingBar.setRating(disc.getRating().floatValue());
		ratingBar.setFocusable(false);
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

	public void passBack(final int queueTypeDisc) {
		// TODO Auto-generated method stub
		Thread t = new Thread() {
			public void run() {
				Bundle b = new Bundle();

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

}
