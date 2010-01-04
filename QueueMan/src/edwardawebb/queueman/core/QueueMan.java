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

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
/**
 * @author eddie
 *
 */
public class QueueMan extends TabActivity implements OnItemClickListener,
		OnTabChangeListener, OnClickListener {

	/*
	 * Settings name and keys for sticky values
	 */
	public static final String PREFS_NAME = "FlixManAppSettings";
	protected static final String MEMBER_ID_KEY = "member_id";
	protected static final String ACCESS_TOKEN_KEY = "token_id";
	protected static final String ACCESS_TOKEN_SECRET_KEY = "token_secret_id";
	protected static final String WATCH_INSTANT_KEY = "can_watch_instant_id";
	protected static final String TITLE_COUNT_KEY = "titles_to_download_id";
	protected static final String RECOMMEND_COUNT_KEY = "recommends_to_download_id";
	protected static final String DEFAULT_TAB_KEY = "default_tab_id";
	protected static final String RT_KEY = "request_token";
	protected static final String RTS_KEY = "request_token_secret_id";

	/**
	 * what is a user?
	 */
	private static String accessToken;
	private static String userId;
	private static String accessTokenSecret;
	/**
	 * session preferences
	 */
	private static String queueDownloadCount;
	private static String recommendDownloadCount;
	private static String defaultTab;
	protected static boolean canWatchInstant;
	
	
	public static NetFlix netflix;
	// see res/values/download_count_array.xml
	public static final String ALL_TITLES_STRING = "All";

	/*
	 * hopefully will ease the FC issues and confusion around process
	 */
	private static int sessionStatus = 0;
	private static final int SESSION_STARTING =0; //initially sesion is starting.. duh
	private static final int SESSION_ACCESS = 1;  // we have request token, but need access token
	private static final int SESSION_ACTIVE = 2; //we have access token saved locally
	private static final int SESSION_TITLE_ADDED = 3; // they just cmae back from search and we have already called addDisc
	private static final int SESSION_EULA_READ = 4; // tthey still havent accepted EULA, and are just reading it.
	
	/*
	 * Menu Item Order
	 */

	private static final int SEARCH_ID = 1;
	private static final int REFRESH_ID = 2;
	private static final int SIGNOUT_ID = 3;
	private static final int SETTINGS_ID = 4;
	private static final int LICENSE_ID = 5;
	private static final int HOME_ID = 6;
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
	protected static final int EDIT_PREFS = 555; // this will allow us to catch edits and update session prefs.
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
	private static final int TAB_RECOMMEND = 2;
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
	private Button accept;
	private Button decline;
	private Button about;

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
			QueueMan.this.finish();
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
			image.setImageResource(R.drawable.red_icon);

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
			image.setImageResource(R.drawable.red_icon);

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
			image.setImageResource(R.drawable.red_icon);

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
			image.setImageResource(R.drawable.red_icon);

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
		mTabHost.addTab(mTabHost.newTabSpec("discqueue").setIndicator("Netflix\nDiscs",getResources().getDrawable(R.drawable.cd)).setContent(R.id.discqueue));
		mTabHost.addTab(mTabHost.newTabSpec("instantqueue").setIndicator("Netflix\nInstant",getResources().getDrawable(R.drawable.instant2)).setContent(R.id.instantqueue));
		mTabHost.addTab(mTabHost.newTabSpec("recommendations").setIndicator("Recommendations",getResources().getDrawable(R.drawable.heart)).setContent(R.id.recommendqueue));
		mTabHost.setOnTabChangedListener(this);
		// set current defaults
		mTabHost.setCurrentTab(TAB_DISC);
		queueType = NetFlixQueue.QUEUE_TYPE_DISC;

	}

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
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
		
		
		//determine where user is coming from, what our sesion staus is
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
					showLicenseDialog();
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
				mTabHost.setCurrentTab(Integer.valueOf(defaultTab));
				redrawQueue();
				break;
				
			case SESSION_TITLE_ADDED:
				sessionStatus=SESSION_ACTIVE;
				redrawQueue();
				Toast.makeText(QueueMan.this,R.string.message_added_title,Toast.LENGTH_LONG);
				break;
			case SESSION_EULA_READ:
				//they're coming from reading the license - hoep they enjoyed
				sessionStatus=SESSION_STARTING;
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.contains(ACCESS_TOKEN_SECRET_KEY);
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
		saveSettings();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// See which child activity is calling us back.
		Disc disc=null;
		switch (requestCode) {
		case SEARCH_MOVIES:
			//they were searchin and found something they like, A
			sessionStatus=SESSION_TITLE_ADDED;
			disc = (Disc) data.getSerializableExtra("Disc");
			int queueType = data.getIntExtra(ACTION_KEY, (int) 0);
			addNewDisc(disc, queueType);
			break;
		case QueueSearch.ADD_MOVIES:
			sessionStatus=SESSION_TITLE_ADDED;
			disc = (Disc) data.getSerializableExtra("Disc");
			int queueType2 = data.getIntExtra(ACTION_KEY, (int) 0);
			addNewDisc(disc, queueType2);
			if(mTabHost.getCurrentTab() == TAB_RECOMMEND){
				NetFlix.recomemendedQueue.delete(disc);
				redrawQueue();
			}
			break;
		case EDIT_PREFS:
			loadSettings();
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
		menu.add(0, SEARCH_ID, 0, R.string.menu_search)
			.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, REFRESH_ID, 0, R.string.menu_refresh)
		.setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, HOME_ID, 0, R.string.menu_at_home)
		.setIcon(android.R.drawable.ic_menu_send);
		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
			.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, LICENSE_ID, 0, R.string.menu_license)
			.setIcon(android.R.drawable.ic_menu_info_details);
		return result;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_ID:
			netflix.purgeQueue(queueType);
			if(queueType == NetFlixQueue.QUEUE_TYPE_RECOMMEND){
				loadRecommendations();
			}else{			
				loadQueue();
				}
			return true;
		case HOME_ID:
			FlurryAgent.onEvent("Launching At Home");
			//load the at home titles if we habent already
			loadHomeTitles();

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
			startActivityForResult(new Intent(this,
					edwardawebb.queueman.core.EditPreferences.class),EDIT_PREFS);

			return true;
		case LICENSE_ID:
			FlurryAgent.onEvent("Launching License");
			// Start the activity whose result we want to retrieve. The
			startActivity( new Intent(this,
					edwardawebb.queueman.core.ViewLicense.class));

			return true;
		}
		super.onMenuItemSelected(featureId, item);
		return true;
	}

	public void onListItemClick(ListView l, View v, int position, long id) {

		Disc disc = null;
		Intent intent = new Intent(this,
				edwardawebb.queueman.core.MovieDetails.class);
		Bundle b = new Bundle();
		
		switch (mTabHost.getCurrentTab()) {
		case TAB_INSTANT:
			disc = NetFlix.instantQueue.getDiscs().get(position);
			break;
		case TAB_DISC:
			disc = NetFlix.discQueue.getDiscs().get(position);
			break;
		case TAB_RECOMMEND:
			disc = NetFlix.recomemendedQueue.getDiscs().get(position);
			intent.putExtra(QueueMan.ACTION_KEY, QueueMan.ACTION_ADD);
			break;
		}
		//@TODO
		netflix.getTitleState(disc.getId());
		b.putSerializable("Disc", disc);
		intent.putExtras(b);
		if(mTabHost.getCurrentTab() == TAB_RECOMMEND){
			startActivityForResult(intent, QueueSearch.ADD_MOVIES);
		}else{
			startActivity(intent);
		}
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//getSharedPreferences(PREFS_NAME, 0);
		userId = settings.getString(MEMBER_ID_KEY, "");
		accessToken = settings.getString(ACCESS_TOKEN_KEY, "");
		accessTokenSecret = settings.getString(ACCESS_TOKEN_SECRET_KEY, "");
		canWatchInstant = settings.getBoolean(WATCH_INSTANT_KEY, false);
		queueDownloadCount = settings.getString(TITLE_COUNT_KEY, "10");
		recommendDownloadCount = settings.getString(RECOMMEND_COUNT_KEY, "20");
		defaultTab = settings.getString(DEFAULT_TAB_KEY, "0");
		Log.d("Preferences","qdl:"+queueDownloadCount + "  rdl: " + recommendDownloadCount + "    dt: "+defaultTab + "   user: "+ userId + "     at: " + accessToken);

	}

	protected void saveSettings() {
		if(netflix != null && netflix.getUserID() != null){
			// Save user preferences. We need an Editor object to
			// make changes. All objects are from android.context.Context
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			//clear out old request tokens... we have an access token now!
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

			if(queueDownloadCount==null)queueDownloadCount="10";
			if(recommendDownloadCount==null)recommendDownloadCount="10";
			if(defaultTab==null)defaultTab="0";
			editor.putString(TITLE_COUNT_KEY, queueDownloadCount);
			editor.putString(RECOMMEND_COUNT_KEY, recommendDownloadCount);
			editor.putString(DEFAULT_TAB_KEY, defaultTab);
			// commit
			editor.commit();
		}
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
						//save the created Request Token and secret in case of QM being closed in the middle
						saveRequestToken(netflix.getRT(),netflix.getRTS());
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

	protected void saveRequestToken(String rt, String rts) {
		// TODO Auto-generated method stub
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		// values
		editor.putString(RT_KEY, rt);
		editor.putString(RTS_KEY, rts);
		// commit
		editor.commit();
	}

	/*
	 * if queueman is closed, we can reinstantiate Netflix with request tokens
	 */
	protected void loadRequestToken() {
		// TODO Auto-generated method stub
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		netflix = new NetFlix(settings.getString(RT_KEY, ""),settings.getString(RTS_KEY, ""));
	}

	protected void retrieveAccessToken(final String requestToken) {
		// show custom dialog to let them know
		showCustomDialog("Welcome Back",
				"Grabbing Access Key  \n (A one time operation)");
		FlurryAgent.onEvent("retrieveAccessToken");
		if(netflix == null){
		 loadRequestToken();
		}
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
		case TAB_RECOMMEND:
			mListView = (ListView) findViewById(R.id.recommendqueue);
			// @ TODO decide best layout.
			// mListView.setAdapter(new IconicAdapter(this,
			// NetFlix.instantQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, NetFlix.recomemendedQueue
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
		switch(mTabHost.getCurrentTab()){
			case TAB_DISC:
			case TAB_INSTANT:
				if (queueDownloadCount.equals(ALL_TITLES_STRING)) {
					message = "\n\nDownloading "
							+ queueDownloadCount
							+ " titles. \n*This may take a while*\n You may adjust this value in Settings.";
				} else {
					message = "\n\nDownloading titles up to position #" + queueDownloadCount
								+ ". You may adjust this value in Settings.";
				
				}
				break;
			case TAB_RECOMMEND:
				message = "\n\nDownloading " + recommendDownloadCount + " recommendations.\n"
				+ ". You may adjust this value in Settings.";
				break;
		}
		
		text.setText("Patience is a virtue" + message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.red_icon);
		// show message
		dialog.show();

		// now work in background, we will call mupdateQ when done
		Thread t = new Thread() {
			public void run() {
				if (netflix.isOnline()) {
					// get queue will connect to neflix and resave the currentQ
					// vairable
					int result = netflix.getQueue(queueType, getDownloadCount());
					switch (result) {
					case 200:
					case 201:
						mHandler.post(mRedrawQueue);
						break;
					default:
						boolean hasAccess = (netflix.getAccessToken()!=null);
						boolean hasID = (netflix.getUserID()!=null);
						FlurryAgent.onError("ER:72",
								"Failed to Retrieve Queue - "
										+ netflix.lastResponseMessage
										+ "Has Access: "+ hasAccess
										+ "Has ID: "+ hasID,
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
		image.setImageResource(R.drawable.red_icon);

		dialog.show();
	}

	private void showLicenseDialog() {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.license_dialog);
		dialog.setTitle("License Agreement");
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(R.string.dialog_license_start);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setVisibility(View.GONE);
		accept = (Button) dialog.findViewById(R.id.accept);
		accept.setOnClickListener(this);
		decline = (Button) dialog.findViewById(R.id.decline);
		decline.setOnClickListener(this);
		about = (Button) dialog.findViewById(R.id.about);
		about.setOnClickListener(this);
		
		dialog.show();
	}

	private void addNewDisc(final Disc disc, final int mqueueType) {
		Thread t = new Thread() {
			public void run() {
				int result = netflix.addToQueue(disc, mqueueType);
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
			} else if (mTabHost.getCurrentTab() == TAB_RECOMMEND) {
				queueType = NetFlixQueue.QUEUE_TYPE_RECOMMEND;
				loadRecommendations();
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

	
	public void onClick(View v) {
		if(v == accept){
			dialog.dismiss();
			retrieveRequestToken();
		}else if(v == decline){
			this.finish();
		}else if(v == about){
			sessionStatus=SESSION_EULA_READ;
			startActivity( new Intent(this,
					edwardawebb.queueman.core.ViewLicense.class));
		}
		
	}
	
	protected void loadRecommendations(){
		 HashMap<String, String> parameters = new HashMap<String, String>();
	 		parameters.put("Queue Type:", "Recommendations");
	 		FlurryAgent.onEvent("loadRecommendations", parameters);
	 		// show custom dialog to let them know
	 		dialog = new Dialog(this);
	 		dialog.setContentView(R.layout.custom_dialog);
	 		dialog.setTitle("Loading Recommendations");
	 		TextView text = (TextView) dialog.findViewById(R.id.text);
	 		String message = "\nLet's see what Netflix thinks you'll like...";
	 		
	 		text.setText("Patience is a virtue" + message);
	 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
	 		image.setImageResource(R.drawable.red_icon);
	 		// show message
	 		dialog.show();
		new DownloadRecommendations().execute();
	}
	/***
	 * Manage ASync Tasks the Android way
	 * @author eddie
	 *
	 */
	 private class DownloadRecommendations extends AsyncTask<Void, Integer, Integer> {
	     protected Integer doInBackground(Void... arg0) {
	         int result = 900;
	       
			if (netflix.isOnline()) {
				// get queue will connect to neflix and resave the currentQ
				// vairable
				result = netflix.getRecommendations(getDownloadCount());
				switch (result) {
				case 200:
				case 201:
					break;
				default:
					boolean hasAccess = (netflix.getAccessToken()!=null);
					boolean hasID = (netflix.getUserID()!=null);
					FlurryAgent.onError("ER:91",
							"Failed to Retrieve Recommendations - "
									+ netflix.lastResponseMessage
									+ "Has Access: "+ hasAccess
									+ "Has ID: "+ hasID,
							"QueueMan");
				}

			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
				result=901;
			} 	
	         
	         return result;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(Integer result) {
	        dialog.dismiss();
	        redrawQueue();
	     }

	 }

	 

		protected void loadHomeTitles(){
			 HashMap<String, String> parameters = new HashMap<String, String>();
		 		parameters.put("Queue Type:", "Home Titles");
		 		FlurryAgent.onEvent("loadHomeTitles", parameters);
		 		// show custom dialog to let them know
		 		dialog = new Dialog(this);
		 		dialog.setContentView(R.layout.custom_dialog);
		 		dialog.setTitle("Loading  at home titles");
		 		TextView text = (TextView) dialog.findViewById(R.id.text);
		 		String message = "\nTitles at home...";
		 		
		 		text.setText("Patience is a virtue" + message);
		 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		 		image.setImageResource(R.drawable.red_icon);
		 		// show message
		 		dialog.show();
			new DownloadHomeTitles().execute();
		}
		/***
		 * Manage ASync Tasks the Android way
		 * @author eddie
		 *
		 */
		 private class DownloadHomeTitles extends AsyncTask<Void, Integer, Integer> {
		     protected Integer doInBackground(Void... arg0) {
		         int result = 900;
		       
				if (QueueMan.netflix.isOnline()) {
					// get queue will connect to neflix and resave the currentQ
					// vairable
					result = QueueMan.netflix.getHomeTitles();
					switch (result) {
					case 200:
					case 201:
						break;
					default:
						boolean hasAccess = (QueueMan.netflix.getAccessToken()!=null);
						boolean hasID = (QueueMan.netflix.getUserID()!=null);
						FlurryAgent.onError("ER:91",
								"Failed to Retrieve Home titles - "
										+ QueueMan.netflix.lastResponseMessage
										+ "Has Access: "+ hasAccess
										+ "Has ID: "+ hasID,
								"QueueMan");
					}

				} else {
					FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
					result=901;
				} 	
		         
		         return result;
		     }

		     protected void onProgressUpdate(Integer... progress) {
		        //dont have indcators yet
		     }

		     protected void onPostExecute(Integer result) {

					//show grid views of titles.
					startActivity(new Intent(QueueMan.this,
							edwardawebb.queueman.core.HomeTitles.class));
		     }

		 }
		
		
		
			/**
			 * @return the downloadCount based on current tab
			 */
			public String getDownloadCount() {
									
				if(mTabHost.getCurrentTab() == TAB_RECOMMEND){
						return recommendDownloadCount;						
				}else{
						return queueDownloadCount;
				}
			}

	 
	 

}
