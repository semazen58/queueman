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

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.NetFlix;
import edwardawebb.queueman.classes.NetFlixQueue;

/**
 * @author Eddie Webb edwardawebb.com
 * @copyright 2009 Edward A. Webb
 *
 */
public class QueueMan extends TabActivity implements OnItemClickListener,
		OnTabChangeListener {

	/*
	 * Settings name and keys for sticky values
	 */
	protected static final String PREFS_NAME = "FlixManAppSettings";
	protected static final String MEMBER_ID_KEY = "member_id";
	protected static final String ACCESS_TOKEN_KEY = "token_id";
	protected static final String ACCESS_TOKEN_SECRET_KEY = "token_secret_id";
	protected static final String WATCH_INSTANT_KEY = "can_watch_instant_id";
	protected static final String TITLE_COUNT_KEY = "titles_to_download_id";

	/**
	 * what is a user?
	 */
	private static String accessToken;
	private static String userId;
	private static String accessTokenSecret;
	private static String downloadCount;
	// see res/values/download_count_array.xml
	public static final String ALL_TITLES_STRING = "All";
	protected static boolean canWatchInstant;
	public static NetFlix netflix;

	/*
	 * hopefully will ease the FC issues and confusion around process
	 */
	private static int sessionStatus = 0;
	private static final int SESSION_STARTING =0; //initially sesion is starting.. duh
	private static final int SESSION_ACCESS = 1;  // we have request token, but need access token
	private static final int SESSION_ACTIVE = 2; //we have access token saved locally
	private static final int SESSION_TITLE_ADDED = 3; // they just cmae back from search and we have already called addDisc
	
	/*
	 * Menu Item Order
	 */

	private static final int SEARCH_ID = 1;
	private static final int REFRESH_ID = 2;
	private static final int SIGNOUT_ID = 3;
	private static final int SETTINGS_ID = 4;
	/*
	 * context menu item codes
	 */
	private static final int MOVE_ID = 11;
	private static final int DELETE_ID = 12;
	private static final int MOVE_TOP_ID = 13;
	private static final int MOVE_BOTTOM_ID = 14;
	private static final int MOVE_UP_ID = 15;
	private static final int MOVE_DOWN_ID = 16;

	/*
	 * Intent codes
	 */
	protected static final int SEARCH_MOVIES = 444;
	public static final String ACTION_KEY = "QueueMan.Action";
	public static final int ACTION_MOVE = 301;
	public static final int ACTION_ADD = 302;
	public static final int ACTION_DELETE = 303;
	private static final String SCHEME_PREFIX = "flixman";

	/*
	 * Flurry analytics
	 */
	static final String FLURRY_APP_KEY = "J27WUUEP8M8HU61YVJMD";

	/*
	 * ints for knowning which tab
	 */
	private static final int TAB_DISC = 0;
	private static final int TAB_INSTANT = 1;
	private static int queueType = NetFlixQueue.QUEUE_TYPE_DISC;

	/*
	 * error codes passed byunetflix
	 */
	private int errorCode;
	private final static int SAVED_TITLE = 620;

	/*
	 * shared dialog
	 */
	private static Dialog dialog;
	private TabHost mTabHost;

	private ListView mListView;

	// handler for callbacks to UI (primary) thread
	final Handler mHandler = new Handler();
	// redraw q on callback
	final Runnable mRedrawQueue = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			dialog.dismiss();
			redrawQueue();
		}
	};

	/*
	 * called after user negotiates access token for the first time!
	 */
	final Runnable mRetrieveQueue1st = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			sessionStatus=SESSION_ACTIVE;
			loadQueue();
		}
	};
	/*
	 * called after any q updates
	 */
	final Runnable mRetrieveQueue = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			loadQueue();
		}
	};

	/*
	 * called if the above step (getting AcessToken) was not successful
	 */
	final Runnable mAbort27 = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			QueueMan.this.notify("Dev note",
					"Error 27: please report to trac.webbmaster.org");
			QueueMan.this.finish();
		}
	};
	/*
	 * called the first time a user runs Quueue. We just hand off to the browser
	 * and close ourself out.
	 */
	final Runnable mBrowserCalled = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
		}
	};
	final Runnable mBrowserCallFailed = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.setTitle("Authentication Error");
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text
					.setText("Please Report error 13 to trac.webbmaster.org;"
							+ "Details: We were unable to Verify with Netflx and get "
							+ "a 'reuest token' the first step in a three step authentication process.");

			dialog.show();
			QueueMan.this.finish();
		}
	};
	/**
	 * \ NetFlix class was unable to load the requested queue, do not pass go do
	 * not collect....
	 */
	final Runnable mAbort36 = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			dialog = new Dialog(QueueMan.this);
			dialog.setContentView(R.layout.custom_dialog);
			dialog.setTitle("Poor Connection");
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText("Sorry, unable to connect with NetFlix");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.icon);

			dialog.show();
		}
	};
	/*
	 * called when addMovie fails
	 */
	final Runnable mAbort18 = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			dialog = new Dialog(QueueMan.this);
			dialog.setContentView(R.layout.custom_dialog);
			dialog.setTitle("Error Adding Movie");
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text
					.setText("Sorry, I was unable to cadd the title to your queue.");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.icon);

			dialog.show();
		}
	};

	/*
	 * called when move / delete fails
	 */
	final Runnable mAbort63 = new Runnable() {
		public void run() {
			// make changes in UI
			dialog.dismiss();
			dialog = new Dialog(QueueMan.this);
			dialog.setContentView(R.layout.custom_dialog);
			dialog.setTitle("Unable to Adjust Movie");
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text
					.setText("Sorry, but the action failed. Please try refreshing the Queue\n(Menu > Refresh)\n\npress back to close this window.");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.icon);

			dialog.show();
		}
	};

	/*
	 * called when when did nto get the quuwuw
	 */
	final Runnable mAbort72 = new Runnable() {
		public void run() {
			// user data corrupted/expired

			dialog.dismiss();
			dialog = new Dialog(QueueMan.this);
			dialog.setContentView(R.layout.custom_dialog);
			dialog.setTitle("Unable to Retreive Queue Titles");
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text
					.setText("We were not able to download your titles. Please try again later. if the problem persists, please notify the developer.");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.icon);

			dialog.show();

		}
	};

	final Runnable mError = new Runnable() {
		public void run() {
			dialog.dismiss();
			switch (errorCode) {
			case SAVED_TITLE:
				showCustomDialog(
						"Non-Movable Title",
						"The title you have chosen is in your Saved list, and is not movable. See Netflix.com for details\n\nPress Back to return to Queue");
				break;
			default:
				showCustomDialog(
						"Error",
						"An error occured and has been reported. Sorry for the trouble.\n\n Press Back to return to queue");

			}
		}
	};
	final Runnable iconHandler = new Runnable() {
		public void run() {
			dialog.dismiss();
			switch (errorCode) {
			case SAVED_TITLE:
				showCustomDialog(
						"Non-Movable Title",
						"The title you have chosen is in your Saved list, and is not movable. See Netflix.com for details\n\nPress Back to return to Queue");
				break;
			default:
				showCustomDialog(
						"Error",
						"An error occured and has been reported. Sorry for the trouble.\n\n Press Back to return to queue");

			}
		}
	};

	/*
	 * Activity flow methods
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FlurryAgent.onStartSession(this, FLURRY_APP_KEY);
		// prepare and display view and tabs
		setContentView(R.layout.queue_man);
		mTabHost = getTabHost();
		mListView = (ListView) findViewById(R.id.discqueue);
		mTabHost.addTab(mTabHost.newTabSpec("discqueue").setIndicator("Netflix\nDiscs")
				.setContent(R.id.discqueue));
		mTabHost.addTab(mTabHost.newTabSpec("instantqueue").setIndicator(
				"Netflix\nInstant").setContent(R.id.instantqueue));
		mTabHost.setOnTabChangedListener(this);
		// set current defaults
		mTabHost.setCurrentTab(TAB_DISC);
		queueType = NetFlixQueue.QUEUE_TYPE_DISC;

	}

	public void onStart() {
		super.onStart();
		// start analytics session
		FlurryAgent.onStartSession(this, FLURRY_APP_KEY);
		
		//catch restart when linking. If successful they will have passed us the request token with our scheme
		Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(SCHEME_PREFIX)) {
			// they has request token! 
			sessionStatus=SESSION_ACCESS;
		}
		
		
		//logic
		switch(sessionStatus){
			case SESSION_STARTING:
				if(isExistingUser()){
					//user has already linkd with NF, just load settings and run
					loadSettings();
					sessionStatus= SESSION_ACTIVE;
					netflix = new NetFlix();
					netflix.createUser(userId, accessToken, accessTokenSecret);
					loadQueue();
				}else{
					//total newbie, run the full course
					retrieveRequestToken();
				}
				break;
			case SESSION_ACCESS:
				//they have request token (2 out of 3) so lets finish them up
				String rt = uri.getQueryParameter("oauth_token");
					
				//purge intent so we don't repeat this :)
				getIntent().setData(null);
				//retrive handlers will set sessions status and call queue, or report error
								retrieveAccessToken(rt);
				
				break;
				
			case SESSION_ACTIVE:
				//this only occurs if they are coming back from a sub activity, or flipping screen 
				//just be redraw our current queue
				redrawQueue();
				break;
				
			case SESSION_TITLE_ADDED:
				sessionStatus=SESSION_ACTIVE;
				redrawQueue();
				Toast.makeText(this,R.string.message_added_title,Toast.LENGTH_LONG);
				break;
			
			default:
				//report error.
				FlurryAgent.onError("ER:99",	"sessionStatus Invalid - method OnStart: "+sessionStatus, "QueueMan");
		}


	}

	/**
	 * @return
	 */
	private boolean isExistingUser() {
		// TODO Auto-generated method stub
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		return settings.contains(ACCESS_TOKEN_SECRET_KEY);
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
		saveSettings();
	}

	// Listen for results from search screen
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// See which child activity is calling us back.
		switch (resultCode) {
		case SEARCH_MOVIES:
			//they were searchin and found something they like, A
			sessionStatus=SESSION_TITLE_ADDED;
			Disc disc = (Disc) data.getSerializableExtra("Disc");
			int queueType = data.getIntExtra(ACTION_KEY, (int) 0);
			addNewDisc(disc, queueType);
		default:
			break;
		}
	}

	/**
	 * Menu and event Methods
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, SEARCH_ID, 0, R.string.menu_search);
		menu.add(0, REFRESH_ID, 0, R.string.menu_refresh);
		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);
		return result;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_ID:
			netflix.purgeQueue(queueType);
			loadQueue();
			return true;
		case SEARCH_ID:
			FlurryAgent.onEvent("Launching Search");
			// Start the activity whose result we want to retrieve. The
			Intent intent = new Intent(this,
					edwardawebb.queueman.core.QueueSearch.class);
			startActivityForResult(intent, SEARCH_MOVIES);

			return true;
		case SIGNOUT_ID:
			purgeUser();
			notify("User Message",
					"! You will still need to change your Login information with Netflix");
			retrieveRequestToken();

			return true;
		case SETTINGS_ID:
			FlurryAgent.onEvent("Launching Settings");
			// Start the activity whose result we want to retrieve. The
			Intent mintent = new Intent(this,
					edwardawebb.queueman.core.Settings.class);
			startActivity(mintent);

			return true;
		}
		super.onMenuItemSelected(featureId, item);
		return true;
	}

	public void onListItemClick(ListView l, View v, int position, long id) {

		Disc disc = null;
		switch (mTabHost.getCurrentTab()) {
		case TAB_INSTANT:
			disc = NetFlix.instantQueue.getDiscs().get(position);
			break;
		case TAB_DISC:
			disc = NetFlix.discQueue.getDiscs().get(position);
			break;
		}
		netflix.getTitleState(disc.getId());
		Intent intent = new Intent(this,
				edwardawebb.queueman.core.MovieDetails.class);
		Bundle b = new Bundle();
		b.putSerializable("Disc", disc);

		intent.putExtras(b);
		startActivity(intent);
	}

	/*
	 * when movies ar held
	 */
	public void onCreateContextMenu(ContextMenu menu, View vv,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, vv, menuInfo);

		if (mTabHost.getCurrentTab() == TAB_INSTANT) {
			/*
			 * menu.add(0,MOVE_BOTTOM_ID,Menu.FIRST, "Move to Bottom");
			 * menu.add(0,MOVE_TOP_ID,Menu.FIRST+1, "Move to Top");
			 * menu.add(0,MOVE_UP_ID,Menu.FIRST+2, "Move Up");
			 * menu.add(0,MOVE_DOWN_ID,Menu.FIRST+3, "Move Down");
			 */
			menu.add(0, DELETE_ID, Menu.FIRST + 5, "Delete this Movie");
		} else if (mTabHost.getCurrentTab() == TAB_DISC) {
			menu.add(0, MOVE_BOTTOM_ID, Menu.FIRST, "Move to Bottom");
			menu.add(0, MOVE_TOP_ID, Menu.FIRST + 1, "Move to Top");
			menu.add(0, MOVE_UP_ID, Menu.FIRST + 2, "Move Up");
			menu.add(0, MOVE_DOWN_ID, Menu.FIRST + 3, "Move Down");
			menu.add(0, DELETE_ID, Menu.FIRST + 5, "Delete this Movie");
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Disc disc = null;
		int lastPosition = 0;
		switch (mTabHost.getCurrentTab()) {
		case TAB_DISC:
			disc = NetFlix.discQueue.getDiscs().get((int) info.id);
			lastPosition = NetFlix.discQueue.getDiscs().size() + 1;
			break;
		case TAB_INSTANT:
			disc = NetFlix.instantQueue.getDiscs().get((int) info.id);
			lastPosition = NetFlix.instantQueue.getDiscs().size() + 1;
			break;
		}
		final int menuItemId = item.getItemId();
		handleDiscUpdate(menuItemId, disc, lastPosition);

		return true;

	}

	protected void handleDiscUpdate(final int menuItemId, final Disc disc,
			final int lastPosition) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("Menu Id", String.valueOf(menuItemId));
		parameters.put("Movie Item Position", String.valueOf(menuItemId));
		parameters.put("Queue Type", String.valueOf(queueType));
		FlurryAgent.onEvent("handleDiscUpdate", parameters);
		showCustomDialog("Updating Title", "Wait for it, wait for it...");

		int mip = 0;
		switch (queueType) {
		case NetFlixQueue.QUEUE_TYPE_DISC:
			mip = NetFlix.discQueue.positionOf(disc);
			break;
		case NetFlixQueue.QUEUE_TYPE_INSTANT:
			mip = NetFlix.instantQueue.positionOf(disc);
			break;
		}

		// catch saved movies (delete only)
		if (!disc.isAvailable() && menuItemId != DELETE_ID) {
			dialog.dismiss();
			Toast.makeText(this,
					"This is a Saved title.\nYou may only delete it",
					Toast.LENGTH_LONG).show();
			return;
		}

		final int movieItemPosition = mip;
		Thread t = new Thread() {
			public void run() {
				int result = 0;
				switch (menuItemId) {
				case MOVE_ID:
					// call move
					// notify("Dev note","I cant help you with "+l.getItemAtPosition(position));
					Intent intent = new Intent(QueueMan.this,
							edwardawebb.queueman.core.MovieDetails.class);
					Bundle b = new Bundle();
					// b.pu
					b.putInt(ACTION_KEY, ACTION_MOVE);
					b.putSerializable("Disc", disc);

					intent.putExtras(b);
					startActivity(intent);

					break;
				case MOVE_TOP_ID:
					result = netflix.moveInQueue(disc, movieItemPosition, 1);
					break;
				case MOVE_BOTTOM_ID:
					result = netflix.moveInQueue(disc, movieItemPosition,
							lastPosition);
					break;
				case MOVE_UP_ID:
					result = netflix.moveInQueue(disc, movieItemPosition,
							movieItemPosition - 1);
					break;
				case MOVE_DOWN_ID:
					result = netflix.moveInQueue(disc, movieItemPosition,
							movieItemPosition + 1);
					break;
				case DELETE_ID:
					// call delete confirm
					result = netflix.deleteFromQueue(disc, queueType);
					break;
				default:
				}
				switch (result) {
				case 200:
				case 201:
					mHandler.post(mRetrieveQueue);
				default:

				}
			}

		};
		t.start();
	}

	/**
	 * helpers
	 */

	private void loadSettings() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		userId = settings.getString(MEMBER_ID_KEY, "");
		accessToken = settings.getString(ACCESS_TOKEN_KEY, "");
		accessTokenSecret = settings.getString(ACCESS_TOKEN_SECRET_KEY, "");
		canWatchInstant = settings.getBoolean(WATCH_INSTANT_KEY, false);
		downloadCount = settings.getString(TITLE_COUNT_KEY, "10");

	}

	protected void saveSettings() {
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		// values
		userId = netflix.getUserID();
		accessToken = netflix.getAccessToken();
		accessTokenSecret = netflix.getAccessTokenSecret();
		editor.putString(MEMBER_ID_KEY, netflix.getUserID());
		editor.putString(ACCESS_TOKEN_KEY, netflix.getAccessToken());
		editor.putString(ACCESS_TOKEN_SECRET_KEY, netflix
				.getAccessTokenSecret());
		editor.putBoolean(WATCH_INSTANT_KEY, canWatchInstant);
		if (downloadCount == null) {
			downloadCount = "10";
		}
		editor.putString(TITLE_COUNT_KEY, downloadCount);
		// commit
		editor.commit();
	}

	private void retrieveRequestToken() {
		FlurryAgent.onEvent("retrieveRequestToken");
		// create new netflix instance
		// get request token
		// send user to netflix

		// show custom dialog to let them know
		showCustomDialog("Verifing with Netflix", "Patience..Loading");

		netflix = new NetFlix();
		Log.d("QueueMan", "Netflix Instantiated:" + netflix.toString());
		// now spawn new thread to generate auth url and start browser
		Thread t = new Thread() {
			public void run() {
				Uri authUrl = null;
				if (netflix.isOnline()) {
					authUrl = netflix.getRequestLoginUri();
					if (authUrl != null) {
						// now send user off to tell netflix were good peoples
						try {
							Intent i = new Intent(Intent.ACTION_VIEW, authUrl);
							startActivity(i);
						} catch (Exception e) {
							// Log.e("NetApi","Launch of Browser Failed" +
							// e.getMessage())
							e.printStackTrace();
						}
						// no need to callback, we kicked off a browser
						mHandler.post(mBrowserCalled);
					} else {
						FlurryAgent.onError("ER:21",
								"Failed to launch browser", "QueueMan");
						mHandler.post(mBrowserCallFailed);
						// QueueMan.this.notify("User message","Error 21 - please report to trac.webbmaster.org");
					}
				} else {

					showCustomDialog("Unable to Connect",
							"Arrrrgggh matey, there be no internet here.");
				}
			}
		};
		t.start();

	}

	protected void retrieveAccessToken(final String requestToken) {
		// show custom dialog to let them know
		showCustomDialog("Welcome Back",
				"Grabbing Access Key  \n (A one time operation)");
		FlurryAgent.onEvent("retrieveAccessToken");
		Log.d("QueueMan", "Netflix Exists Still:" + netflix.toString());

		Thread t = new Thread() {
			public void run() {
				boolean accessProvided = netflix
						.negotiateAccessToken(requestToken);

				if (accessProvided) {
					canWatchInstant = netflix.getWatchInstant();

					saveSettings();
					mHandler.post(mRetrieveQueue1st);
				} else {
					FlurryAgent.onError("ER:27",
							"Failed to retrieve Access TOken. ", "QueueMan");
					mHandler.post(mAbort27);
				}
			}
		};
		t.start();
	}

	protected void redrawQueue() {
		// just in case
		dialog.dismiss();
		switch (mTabHost.getCurrentTab()) {
		case TAB_DISC:
			mListView = (ListView) findViewById(R.id.discqueue);
			// mListView.setAdapter(new
			// IconicAdapter(this,NetFlix.discQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, NetFlix.discQueue
							.getDiscs()));
			break;
		case TAB_INSTANT:
			mListView = (ListView) findViewById(R.id.instantqueue);
			// @ TODO decide best layout.
			// mListView.setAdapter(new IconicAdapter(this,
			// NetFlix.instantQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, NetFlix.instantQueue
							.getDiscs()));
			break;
		}

		mListView.setTextFilterEnabled(true);
		mListView.setOnItemClickListener(this);
		// register for long hold on menu items
		registerForContextMenu(mListView);
	}

	/**
	 * loadQueue
	 * 
	 * Will make a call to netflix, or load local queue if present. Queuetype is
	 * based on current tab. does not recieve any results, but instead matching
	 * queue is set within netflix class.
	 */
	protected void loadQueue() {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("Queue Type:", String.valueOf(queueType));
		parameters.put("Can Instant:", String.valueOf(canWatchInstant));
		FlurryAgent.onEvent("loadQueue", parameters);
		// show custom dialog to let them know
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle("Downloading Queue");
		TextView text = (TextView) dialog.findViewById(R.id.text);
		String message = "";
		if (downloadCount.equals(ALL_TITLES_STRING)) {
			message = "\n\nDownloading "
					+ downloadCount
					+ " titles. \n*This may take a while*\n You may adjust this value in Settings.";
		} else {
			message = "\n\nDownloading titles up to position #" + downloadCount
					+ ". You may adjust this value in Settings.";
		}
		text.setText("Patience is a virtue" + message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);
		// show message
		dialog.show();

		// now work in background, we will call mupdateQ when done
		Thread t = new Thread() {
			public void run() {
				if (netflix.isOnline()) {
					// get queue will connect to neflix and resave the currentQ
					// vairable
					int result = netflix.getQueue(queueType, downloadCount);
					switch (result) {
					case 200:
					case 201:
						mHandler.post(mRedrawQueue);
						break;
					default:
						FlurryAgent.onError("ER:72",
								"Failed to Retrieve Queue - "
										+ netflix.lastResponseMessage,
								"QueueMan");
						mHandler.post(mAbort72);
					}

				} else {
					FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
					mHandler.post(mAbort36);
				}

			}
		};
		t.start();
	}

	private void notify(String title, String message) {
		// toast
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void purgeUser() {
		accessToken = null;
		accessTokenSecret = null;
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		// values
		editor.clear();
		// commit
		editor.commit();
		// Log.i("NetApi","User Data Destroyed")
	}

	private void showCustomDialog(String title, String message) {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle(title);
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		dialog.show();
	}

	private void addNewDisc(final Disc disc, final int queueType) {
		Thread t = new Thread() {
			public void run() {
				int result = netflix.addToQueue(disc, queueType);
				switch (result) {
				case 200:
				case 201:
					// title added
					mHandler.post(mRedrawQueue);
					break;
				case 620:
					// added to SAVED queue
					FlurryAgent
							.onError(
									"Error:620",
									"The chosen title is a saved title, and nto movable",
									"QueueMan");
					mHandler.post(mError);
					break;
				default:
					FlurryAgent.onError("ER:45",
							"AddNewDisc: Unkown response. SubCode: " + result
									+ " Http:" + netflix.lastResponseMessage,
							"QueueMan");
				}
			}
		};
		t.start();

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// convert to list item click
		if (arg0 == mListView) {
			onListItemClick((ListView) arg0, arg1, arg2, arg3);
		}

	}

	public void onTabChanged(String tabId) {
		//this may get called when setting up layout, so only listen if user's session is active
		if(sessionStatus==SESSION_ACTIVE){
			FlurryAgent.onEvent("onTabChanged");
			if (mTabHost.getCurrentTab() == TAB_INSTANT) {
				if (canWatchInstant) {
					queueType = NetFlixQueue.QUEUE_TYPE_INSTANT;
					loadQueue();
				} else {
					showCustomDialog(
							"Restricted",
							"According to Netflix this user does not have \"Watch Instantly\" rights\nPress 'Back' to return to your Disc Queue");
					mTabHost.setCurrentTab(TAB_DISC);
				}
			} else if (mTabHost.getCurrentTab() == TAB_DISC) {
				queueType = NetFlixQueue.QUEUE_TYPE_DISC;
				loadQueue();
			}
		}
	}

	protected void upgradePreBetaUser() {
		// show custom dialog to let them know
		showCustomDialog("New Instant Features",
				"Checking if you have an instant Queue..  \n (A one time operation)");
		Thread t = new Thread() {
			public void run() {
				canWatchInstant = netflix.getWatchInstant();
				saveSettings();
				mHandler.post(mRetrieveQueue1st);
			}
		};
		t.start();
	}

	public static void updateDownloadCount(String titleCount) {
		// TODO Auto-generated method stub
		downloadCount = titleCount;
	}

	/*	*//***
	 * Make the list rows a little more attarctive
	 * 
	 * @author eddie
	 * 
	 */
	/*
	 * class IconicAdapter extends ArrayAdapter<Disc>{ Activity context;
	 * List<Disc> items; IconicAdapter(Activity context,List<Disc> items){ //@
	 * TODO changing layotu type? adjust layout here
	 * super(context,R.layout.fancy_list,items); this.context=context;
	 * this.items=items; }
	 * 
	 * public View getView(int position, View convertView, ViewGroup parent){
	 * View row = convertView; RatingRowWrapper wrapper= null;
	 * 
	 * if(row == null){ LayoutInflater inflater=context.getLayoutInflater();
	 * 
	 * //@ TODO And here row = inflater.inflate(R.layout.fancy_list, null);
	 * wrapper= new RatingRowWrapper(row); }else{
	 * wrapper=(RatingRowWrapper)row.getTag(); }
	 * 
	 * 
	 * Disc disc= (Disc) items.get(position);
	 * wrapper.getLabel().setText(disc.toString());
	 * 
	 * wrapper.getPosition().setText(""+(position+1)); RatingBar
	 * rb=(RatingBar)wrapper.getStars(); rb.setStepSize(0.1F); rb.setMax(5);
	 * rb.setRating(disc.getRating().floatValue()); rb.setFocusable(false);
	 * 
	 * return (row);
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

}
