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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.queues.BrowsableQueue;
import edwardawebb.queueman.queues.DiscQueue;
import edwardawebb.queueman.queues.HomeQueue;
import edwardawebb.queueman.queues.InstantQueue;
import edwardawebb.queueman.queues.Queue;
import edwardawebb.queueman.queues.RecommendedQueue;


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
	
	
	public static Netflix netflix=Netflix.getInstance();
	// see res/values/download_count_array.xml
	public static final String ALL_TITLES_STRING = "All";

	/*
	 * hopefully will ease the FC issues and confusion around process
	 */
	private static int sessionStatus = 0;
	private static final int SESSION_STARTING =0; //initially sesion is starting.. duh
	private static final int SESSION_ACCESS = 1;  // we have request token, but need access token
	private static final int SESSION_ACTIVE = 2; //we have access token saved locally
	private static final int SESSION_BACKGROUND = 5; //user has quit (onstop) (does not explicitly kill, and we'll let adnroid kill us if needed
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
	private static final int FILTER_ID = 17;
	private static final int NEXT_PAGE_ID = 18;

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
	public static final String FLURRY_APP_KEY = "J27WUUEP8M8HU61YVJMD";

	/*
	 * ints for knowning which tab
	 */
	private static final int TAB_DISC = 0;
	private static final int TAB_INSTANT = 1;
	private static final int TAB_RECOMMEND = 2;
	protected static final String POSITION_KEY = null;


	/*
	 * shared dialog
	 */
	private static Dialog dialog;
	private TabHost mTabHost;
	private Button accept;
	private Button decline;
	private Button about;
/*
 * Navigation bar
 */
	private View navigationPanel;
	private Button btnFilterInstant;
	
	
	
	private ListView mListView;
	private int firstVisibleItem=0;
	
	
	
	static DiscQueue discQueue = new DiscQueue(netflix);
	private InstantQueue instantQueue = new InstantQueue(netflix);
	private HomeQueue homeQueue = new HomeQueue(netflix);
	private RecommendedQueue recommendedQueue = new RecommendedQueue(netflix);
	/** set and changed by {@link #onTabChanged(String)} **/
	private Queue currentQueue; //just a reference to the visible queue (one of above)

	

	
	

	/*
	 * Activity flow methods
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("QueueMan","onCreate()>>>");
	
		//@ TODO DEBUGGIN ONLY - DONT LOG
		//FlurryAgent.setLogEnabled(false);
		
		//start logigng
		FlurryAgent.onStartSession(this, FLURRY_APP_KEY);
		// prepare and display view and tabs
		setContentView(R.layout.main_tabs_screen);
		mTabHost = getTabHost();
		mListView = (ListView) findViewById(R.id.discqueue);
		mTabHost.addTab(mTabHost.newTabSpec("discqueue").setIndicator("Netflix\nDiscs",getResources().getDrawable(R.drawable.cd)).setContent(R.id.discqueue));
		mTabHost.addTab(mTabHost.newTabSpec("instantqueue").setIndicator("Netflix\nInstant",getResources().getDrawable(R.drawable.instant2)).setContent(R.id.instantqueue));
		mTabHost.addTab(mTabHost.newTabSpec("recommendations").setIndicator("Recommendations",getResources().getDrawable(R.drawable.heart)).setContent(R.id.recommendqueue));
		mTabHost.setOnTabChangedListener(this);
		// set current defaults		
		mTabHost.setCurrentTab(TAB_DISC);

		Log.d("QueueMan","onCreate()<<<");
	}

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {

		Log.d("QueueMan","onStart()>>>");
		super.onStart();
		// start analytics session
		//FlurryAgent.onStartSession(this, FLURRY_APP_KEY);
		
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
					netflix = Netflix.getInstance();
					netflix.setExistingUser(userId, accessToken, accessTokenSecret);
					//load user's preferred tab from preferences
					mTabHost.setCurrentTab(Integer.valueOf(defaultTab));
					if(Integer.valueOf(defaultTab) == TAB_DISC) {
						currentQueue=discQueue; // needed by some methods like onClick , elimates need for more case statements
						loadQueue(discQueue) ;// this is missed by "onTabChange" since it is Disc -> Disc, and not really a change
					}
					//retrieve and display current tab's queue
					//loadQueue();
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
				Log.d("QueueMan",""+mTabHost.isDrawingCacheEnabled());
				Log.d("QueueMan",""+mTabHost.isFocusable());
				Log.d("QueueMan",""+mTabHost.isInTouchMode());
				Log.d("QueueMan",""+mTabHost.isLayoutRequested());
				Log.d("QueueMan",""+mTabHost.isShown());
				if(mTabHost.isLayoutRequested()) {					
					mTabHost.setCurrentTab(Integer.valueOf(defaultTab));
				}
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
		
		

		Log.d("QueueMan","onStart()<<<");

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
			if(resultCode > 0){
				//they were searchin and found something they like, A
				sessionStatus=SESSION_TITLE_ADDED;
				disc = (Disc) data.getSerializableExtra("Disc");
				disc.setQueueType(data.getIntExtra(ACTION_KEY, (int) 0));
				//@ TODO new AddTitleTask().execute(disc);
			}else{
				//user cancelled search, just let it slide
			}
			break;
		case QueueSearch.ADD_MOVIES:
			if(resultCode > 0){
				sessionStatus=SESSION_TITLE_ADDED;
				disc = (Disc) data.getSerializableExtra("Disc");
				int qt=data.getIntExtra(ACTION_KEY, (int) 0);
				disc.setQueueType(data.getIntExtra(ACTION_KEY, (int) 0));
				if(mTabHost.getCurrentTab() == TAB_RECOMMEND){
					recommendedQueue.delete(disc);
					redrawQueue();
				}
				//@ TODO new AddTitleTask().execute(disc);
			}
			break;
		case EDIT_PREFS:
			loadSettings();
			//because rec. has paging based on max dl, we need to get up to snuff with settings 
			//if # did not change Netflix class will use same q
			if(mTabHost.getCurrentTab() == TAB_RECOMMEND) loadQueue(recommendedQueue);
			
		default:
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
			return refreshCurrentQueue(discQueue);
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
					edwardawebb.queueman.core.AboutHelp.class));

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
		
		// list is already loaded, so we can just call the quick retrieve.
		try{
		switch (mTabHost.getCurrentTab()) {
		case TAB_INSTANT:
			disc = instantQueue.retreiveQueue().get(position);
			break;
		case TAB_DISC:
			disc = discQueue.retreiveQueue().get(position);
			break;
		case TAB_RECOMMEND:
			disc = recommendedQueue.retreiveQueue().get(position);
			intent.putExtra(QueueMan.ACTION_KEY, QueueMan.ACTION_ADD);
			break;
		}
		}catch(NullPointerException npe){
			//most likely the queue was not done loading and the user tappedn the screen
			FlurryAgent.onError("NA", "NPE onListItem click", "QueueMan");
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
		firstVisibleItem=position;
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
		} else if (mTabHost.getCurrentTab() == TAB_RECOMMEND) {
			//menu.add(0, FILTER_ID, Menu.FIRST, "Filter Instant only");
			//menu.add(0, NEXT_PAGE_ID, Menu.FIRST+1, "See Next " + getDownloadCount() + " titles.");
		} 
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Disc disc = null;
		int lastPosition = 0;
		switch (mTabHost.getCurrentTab()) {
		case TAB_DISC:
			disc = discQueue.retreiveQueue().get((int) info.id);
			lastPosition = (discQueue.getTotalTitles());
			break;
		case TAB_INSTANT:
			disc =instantQueue.retreiveQueue().get((int) info.id);
			lastPosition = instantQueue.getTotalTitles();
			break;
			
		case TAB_RECOMMEND:
			
			
		}
		final int menuItemId = item.getItemId();
		//@ TODO handleDiscUpdate(menuItemId, disc, lastPosition);

		return true;

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
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("CurrentTab", mTabHost.getCurrentTabTag());
			parameters.put("New Tab", tabId);
			FlurryAgent.onEvent("onTabChanged",parameters);
			
			//hide navigation paenl as it is not yet avaible on all fields.
			if(navigationPanel != null)navigationPanel.setVisibility(View.GONE);
			
			if (mTabHost.getCurrentTab() == TAB_INSTANT) {
				if (canWatchInstant) {
					currentQueue=instantQueue;
					loadQueue(instantQueue);
					//((ViewStub) findViewById(R.id.stub_paginate)).setVisibility(View.GONE);
				} else {
					showCustomDialog(
							"Restricted",
							"According to Netflix this user does not have \"Watch Instantly\" rights\nPress 'Back' to return to your Disc Queue");
					mTabHost.setCurrentTab(TAB_DISC);
					currentQueue=discQueue;
					
				}
			} else if (mTabHost.getCurrentTab() == TAB_DISC) {
				currentQueue=discQueue;
				  loadQueue(discQueue);
				//((ViewStub) findViewById(R.id.stub_paginate)).setVisibility(View.GONE);

			} else if (mTabHost.getCurrentTab() == TAB_RECOMMEND) {
				currentQueue=recommendedQueue;
				loadQueue(recommendedQueue);
			}
		}
	}


	/*	protected void upgradePreBetaUser() {
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
	}*/
	
	
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
		}else if(v == btnFilterInstant){
			//only thing we do here is filter
			btnFilterInstant.setEnabled(false);
			
			((BrowsableQueue)currentQueue).filterInstantOnly();
			redrawQueue();
			
		}/*else if (v == btnNextPage){
			//increment starindeex, so they can see next set
			
			currentQueue.setStartIndex(currentQueue.getStartIndex()+Integer.valueOf(getDownloadCount()));
			loadQueue(currentQueue);
			//unlock filter button for another round
			btnFilterInstant.setEnabled(true);
		}*/
	
		
	}


	/*protected void handleDiscUpdate(final int menuItemId, final Disc disc,
			final int lastPosition) {
		
		int mip = 0;
		int newPosition = 0;
		
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
				newPosition=1;
				break;
			case MOVE_BOTTOM_ID:
				newPosition = lastPosition + 1;
				break;
			case MOVE_UP_ID:
				newPosition = mip - 1;
				break;
			case MOVE_DOWN_ID:
				newPosition = mip + 1;
				break;
			case DELETE_ID:
				// call delete confirm
				new DiscDeleteTask().execute(disc);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("Menu Id", String.valueOf(menuItemId));
				parameters.put("Queue Type", NetFlixQueue.queueTypeText[queueType]);
				FlurryAgent.onEvent("handleDiscUpdate-delete", parameters);
				break;
			default:
		}
		//if a new pos. was specified, update using disc, old and new
		if(newPosition>0){
			new DiscMoveTask().execute(disc, mip, newPosition, queueType);
			
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("Menu Id", String.valueOf(menuItemId));
			parameters.put("Current Item Position", ""+String.valueOf(disc.getPosition()));
			parameters.put("New Item Position", String.valueOf(newPosition));
			parameters.put("Queue Type", NetFlixQueue.queueTypeText[queueType]);
			FlurryAgent.onEvent("handleDiscUpdate-move", parameters);
		}
	}*/
	
	/***
	 * DiscMoveTask class spawns a BG thread to handle the movement or current discs in instant and disc q
	 * @author eddie
	 *
	 */
	/* private class DiscMoveTask extends AsyncTask<Object, Integer, Integer> {
	     protected void onPreExecute(){
	    	 showCustomDialog("Moving Title", "Attempting to reorder queue...");
	     }
		 
	     
	      * object array ( Disc, position, newPosition, queueType )
	      
		 protected Integer doInBackground(Object... oArr) {
	    	 int result=36;
	    	
				
			if (netflix.getNewETag(queueType)) {
				result=netflix.moveInQueue((Disc)oArr[0],(Integer)oArr[1],(Integer)oArr[2],(Integer)oArr[3]);
			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
			} 	
	         
	         return result;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(Integer result) {

	    	 dialog.dismiss();
	        switch (result) {
	            case 36:
	        	//not online
	            	break;
				case 200:
				case 201:
					
					redrawQueue();
					break;
					
				case 502:
					//yeah! its happening here too now - Arrgggh!
					Toast.makeText(mTabHost.getCurrentTabView().getContext(),"I blew it! - You may have a poor connection "  , Toast.LENGTH_LONG).show();
					Toast.makeText(mTabHost.getCurrentTabView().getContext(),"But just in case, this issue has been reported - Sorry"  , Toast.LENGTH_LONG).show();
					
					break;
				case  NetFlix.MOVED_OUTSIDE_CURRENT_VIEW:
					//horiible, but this is a long message show back to back toasts allow the user to finish reading
					Toast.makeText(mTabHost.getCurrentTabView().getContext(),"Success - This disc was moved beyond the # of titles shown, hence *this title will no longer be visible in the current range*"  , Toast.LENGTH_LONG).show();
					Toast.makeText(mTabHost.getCurrentTabView().getContext(),"Success - This disc was moved beyond the # of titles shown, hence *this title will no longer be visible in the current range*"  , Toast.LENGTH_LONG).show();
					redrawQueue();
					break;
				default:
					FlurryAgent.onError("ER:73",
							"Failed to MOve title - "
							+ netflix.lastResponseMessage,
							"QueueMan");
						
				}
	     }

	 }
*/
		/***
		 * DiscDeleteTask class spawns a BG thread to handle the movement or current discs in instant and disc q
		 * @author eddie
		 *
		 */
		/* private class DiscDeleteTask extends AsyncTask<Disc, Integer, Integer> {
		     protected void onPreExecute(){
	    	 showCustomDialog("Removing Title", "I like to purge-it, purge-it.\nI like to purge-it, purge-it.\n  Purge-it!!");
		     }
			 
		     
		      * object array ( Disc, position, newPosition, queueType )
		      
			 protected Integer doInBackground(Disc... oArr) {
		    	 int result=901;
		    	
					
				if (netflix.getNewETag(queueType)) {
					result=netflix.deleteFromQueue(oArr[0], queueType);
				} else {
					FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
				} 	
		         
		         return result;
		     }

		     protected void onProgressUpdate(Integer... progress) {
		        //dont have indcators yet
		     }

		     protected void onPostExecute(Integer result) {

		    	 dialog.dismiss();
		        switch (result) {
					case 200:
					case 201:
						redrawQueue();
						break;
					default:
						FlurryAgent.onError("ER:73",
								"Failed to Delete title - "
								+ netflix.lastResponseMessage,
								"QueueMan");
							showCustomDialog("Error - Please Report", "Although we are connected, I was unable to remove that title.\n Reason COde:"+netflix.lastResponseMessage);
			        
					}
		     }

		 }
*/

	/**
	 * helpers
	 */

	private void loadSettings() {

		Log.d("QueueMan","loadSettings()>>>");
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

		Log.d("QueueMan","loadSettings()<<<");
	}

	protected void saveSettings() {
		if(netflix != null && QueueMan.netflix.getUser() != null){
			// Save user preferences. We need an Editor object to
			// make changes. All objects are from android.context.Context
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			//clear out old request tokens... we have an access token now!
			editor.clear();
			//the magic 3 user values - well get thres t each time (cheap)
			userId = QueueMan.netflix.getUser().getUserId();
			accessToken = QueueMan.netflix.getUser().getAccessToken();
			accessTokenSecret = QueueMan.netflix.getUser().getAccessTokenSecret();
			canWatchInstant=QueueMan.netflix.getUser().isCanWatchInstant();
			editor.putString(MEMBER_ID_KEY, QueueMan.netflix.getUser().getUserId());
			editor.putString(ACCESS_TOKEN_KEY, QueueMan.netflix.getUser().getAccessToken());
			editor.putString(ACCESS_TOKEN_SECRET_KEY, QueueMan.netflix.getUser().getAccessTokenSecret());
			editor.putBoolean(WATCH_INSTANT_KEY, QueueMan.netflix.getUser().isCanWatchInstant());
			//prefernces
			if(queueDownloadCount==null)queueDownloadCount="10";
			if(recommendDownloadCount==null)recommendDownloadCount="10";
			if(defaultTab==null)defaultTab="0";
			editor.putString(TITLE_COUNT_KEY, queueDownloadCount);
			editor.putString(RECOMMEND_COUNT_KEY, recommendDownloadCount);
			editor.putString(DEFAULT_TAB_KEY, defaultTab);
			
			
			


			// Write object out to disk

			// commit
			editor.commit();
		}
	}

	private void retrieveRequestToken() {
		//this is called fro very first =time., or new users
		FlurryAgent.onEvent("retrieveRequestToken");
		
		// show custom dialog to let them know they will be passed to netflix to authorize us
		showCustomDialog(R.string.pass_to_netflix_title, R.string.pass_to_netflix_text);

		//we need an instance of netflix to manage this authorization
		netflix = Netflix.getInstance();
		Log.d("QueueMan", "Netflix Instantiated:" + netflix.toString());
		// now spawn worker class to generate auth url and start browser
		new RequestTokenTask().execute();

	}
	/***
	 * AddTitle class spawns a BG thread to handle the addition of titles to instant of disc queues
	 * @author eddie
	 *
	 */
	 private class RequestTokenTask extends AsyncTask<Void, Integer, Integer> {
	     protected void onPreExecute(){
	    	 
	     }
		 
		 protected Integer doInBackground(Void... arg0) {
			 int result=901;
				
			if (isOnline()) {
				
				Uri authUrl = netflix.getRequestLoginUri();
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
					result = 200;
				} else {
					FlurryAgent.onError("ER:21",
							"Failed to launch browser", "QueueMan");
					result= 21;
				}

			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
				result=36;
			} 	
	         
	         return result;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(Integer result) {
	        dialog.dismiss();

			switch (result) {
			case 200:
				// got token !  - close queman and pass to browser
				Toast.makeText(QueueMan.this, "Just a moment now", Toast.LENGTH_LONG).show();
				QueueMan.this.finish();
				break;
			case 21:// error getting token, booo
				showCustomDialog("Error - Please Report", "Snap! I was unable to negotiate a Request Token with Netflix (Step 1 of 3). \n Please restart the application and report if error repeates");
				break;
			default:
				showCustomDialog("Error", "Unable to Connect to the internet, please check connection and try again.");
				
			}	
	     }
	 }
	

	/*
	 * saveRequestToken saves the temp token to preferences allwoing succesful resrt and link
	 */
	protected void saveRequestToken(String rt, String rts) {
		//first time user just got request token from NF. 
		//this save allows quewman to restart if closed while user is authneticating with netflix
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.contains(RT_KEY)){
			netflix = Netflix.getInstance();
			 netflix.setRequestTokens(settings.getString(RT_KEY, ""),settings.getString(RTS_KEY, ""));
		}else{
			showCustomDialog("Error  - Please Report", "I could not load your Rquest Token - please close out and try to link with Netflix again");
		}
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
		new AccessTokenTask().execute(requestToken);
	}
	/***
	 * AddTitle class spawns a BG thread to handle the addition of titles to instant of disc queues
	 * @author eddie
	 *
	 */
	 private class AccessTokenTask extends AsyncTask<String, Integer, Integer> {
	     protected void onPreExecute(){
	    }
		 
		 protected Integer doInBackground(String... arg0) {
			 String rt=arg0[0];
	    	 int result=36;
				
			if (isOnline()) {
				boolean accessProvided = netflix
						.negotiateAccessToken(rt);

				if (accessProvided) {
					//populate user for the first time!
					netflix.getUserDetails();				

					saveSettings();
					result=200;
				} else {
					FlurryAgent.onError("ER:27",
							"Failed to retrieve Access TOken. ", "QueueMan");
					result=27;
				}

			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
			} 	
	         
	         return result;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(Integer result) {
	        dialog.dismiss();

			switch (result) {
			case 200:
			case 201:
				// got token ! hooray, load q
				sessionStatus=SESSION_ACTIVE;
				loadQueue(discQueue);
				break;
			default:
				// error getting token, booo
				showCustomDialog("Error  - Please Report", "Snap! I was unable to negotiate an Access Token with Netflix. \n Please restart the application and report if error repeates");
				
			}
	     }

	 }
	 
	 

	protected void redrawQueue() {
		// just in casedialog.dismiss();
		
		switch (mTabHost.getCurrentTab()) {
		case TAB_DISC:
			mListView = (ListView) findViewById(R.id.discqueue);
			// mListView.setAdapter(new
			// IconicAdapter(this,NetFlix.discQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter<Disc>(this,
					android.R.layout.simple_list_item_1,discQueue
							.retreiveQueue()));
			break;
		case TAB_INSTANT:
			mListView = (ListView) findViewById(R.id.instantqueue);
			// @ TODO decide best layout.
			// mListView.setAdapter(new IconicAdapter(this,
			// NetFlix.instantQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter<Disc>(this,
					android.R.layout.simple_list_item_1, instantQueue
							.retreiveQueue()));
			break;
		case TAB_RECOMMEND:
			mListView = (ListView) findViewById(R.id.recommendqueue);
			// @ TODO decide best layout.
			// mListView.setAdapter(new IconicAdapter(this,
			// NetFlix.instantQueue.getDiscs()));
			mListView.setAdapter(new ArrayAdapter<Disc>(this,
					android.R.layout.simple_list_item_1, recommendedQueue
					.retreiveQueue()
							));
			break;
		}

		mListView.setTextFilterEnabled(true);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(new ScrollHandler());
		mListView.setSelection(firstVisibleItem);
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
	protected void loadQueue(Queue queue) {

		Log.d("QueueMan","loadQueue()>>>");
		HashMap<String, String> parameters = new HashMap<String, String>();
		//parameters.put("Queue Type:", NetFlixQueue.queueTypeText[queueType]);
		parameters.put("Can Instant:", String.valueOf(canWatchInstant));
		FlurryAgent.onEvent("loadQueue", parameters);
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
		showCustomDialog("Retrieving Queue", message);
				// now work in background, and redraw after if successful
		new LoadQueueTask().execute(queue);

		Log.d("QueueMan","loadQueue()<<<");
	}

	/***
	 * AddTitle class spawns a BG thread to handle the addition of titles to instant of disc queues
	 * @author eddie
	 *
	 */
	 private class LoadQueueTask extends AsyncTask<Queue, Integer, Queue > {
	     
		 /*protected void onPreExecute(){
	 		Log.d("QueueMan","LoadQueueTask()");
	    	// showCustomDialog("Adding Title", "Just a sec as I try to add this title to your queue");
	     }*/
		 
		 protected Queue doInBackground(Queue... arg1) {
			 Log.d("QueueMan","LoadQueueTask() | doInBackground()>>>");
		    //default error, not connected 901
			Queue queue = (Queue) arg1[0];
			if (isOnline()) {
					// get queue will connect to neflix and resave the currentQ
				// vairable
				queue.retreiveQueue();
							
			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
					
			}
			Log.d("QueueMan","LoadQueueTask() | doInBackground()<<<");
	    	
			return queue;
			 
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(final Queue queue) {
	    	 Log.d("QueueMan","LoadQueueTask() | PostExecute");
		     dialog.dismiss();
		     switch (queue.getResultCode()) {
				case 200:
				case 201:
				case Netflix.NF_ERROR_NO_MORE:
				case Netflix.SUCCESS_FROM_CACHE:	
					redrawQueue();
					//show navigation panel (instant filter, next)
					//@ TODO  - only show filter instant for recommends and search Queuesa.
					if(mTabHost.getCurrentTab()==TAB_RECOMMEND){
					            if (navigationPanel == null) {
					            	//navigationPanel = ((ViewStub) findViewById(R.id.stub_import)).inflate();
					            	navigationPanel = ((ViewStub) findViewById(R.id.stub_import)).inflate();
									//provide some references
					            	btnFilterInstant = (Button) navigationPanel.findViewById(R.id.filter_instant);
									//register for clika
									btnFilterInstant.setOnClickListener(QueueMan.this);
								} 
					            //although inflate makes view visible, on subsequent trips will need to show it.
					            navigationPanel.setVisibility(View.VISIBLE);
								//the btnNextPage has two uses, so it is set depenind on end of results switch below              
					          
					}// end block for recommended tab
					
					 break;				 
					
				default:
					Toast.makeText(QueueMan.this, "Sorry, we had an error, please refresh", Toast.LENGTH_LONG).show();
					boolean hasAccess = (netflix.getUser().getAccessToken()!=null);
					boolean hasID = (netflix.getUser().getUserId()!=null);
					FlurryAgent.onError("ER:91",
							"Failed to Retrieve Recommendations - "
									+ queue.getNetflixCode()
									+ "Has Access: "+ hasAccess
									+ "Has ID: "+ hasID,
							"QueueMan");
				}	
	     	
		     Log.d("QueueMan","LoadQueueTask() | postExecute()<<<");
		    	
	     }

	 }

	

	private void notify(String title, String message) {
		// toast
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void purgeUser() {
		accessToken = null;
		accessTokenSecret = null;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		// values
		editor.remove(MEMBER_ID_KEY);
		editor.remove(ACCESS_TOKEN_KEY);
		editor.remove(ACCESS_TOKEN_SECRET_KEY);
		editor.remove(WATCH_INSTANT_KEY);
		// commit
		editor.commit();
		// Log.i("NetApi","User Data Destroyed")
	}

	
	/**
	 * 
	 * @param title - the string id to use as a titel
	 * @param message - ditto
	 */
	private void showCustomDialog(int title, int message ){
		dialog = new Dialog(mTabHost.getContext());
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle(title);
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.red_icon);

		dialog.show();
	}
	
	/**
	 * 
	 * @param title the string to use as a title
	 * @param message ditto
	 */
	private void showCustomDialog(String title, String message) {
		dialog = new Dialog(mTabHost.getContext());
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

	
	/***
	 * AddTitle class spawns a BG thread to handle the addition of titles to instant of disc queues
	 * @author eddie
	 *
	 */
	 /*private class AddTitleTask extends AsyncTask<Disc, Integer, Integer> {
	     protected void onPreExecute(){
	    	 Toast.makeText(QueueMan.this, "Just a sec as I try to add this title to your queue",Toast.LENGTH_LONG).show();
	    	}
		 
		 protected Integer doInBackground(Disc... discArr) {
	    	int result=36;
	    	try{
		    	HashMap<String, String> parameters = new HashMap<String, String>();
		 		parameters.put("Queue Type:", );
		 		parameters.put("User ID:", ""+userId);
				parameters.put("Disc ID:", ""+discArr[0].getId() );
				parameters.put("Position:", ""+discArr[0].getPosition());
				parameters.put("Availability:", ""+ discArr[0].isAvailable() + ", " + discArr[0].getAvailibilityText());
				FlurryAgent.onEvent("AddTitleTask", parameters);
			 }catch(Exception e){
				 // empty disc, or bad values - just prevent FC
			 }
			Disc disc=(Disc)discArr[0];
			Queue queue=
			if (netflix.getNewETag(disc.getQueueType())) {
				// get queue will connect to neflix and resave the currentQ
				// vairable
				result = netflix.addToQueue(disc,disc.getQueueType());
			
			} else {
				FlurryAgent.onError("ER:36", "Not Connected", "QueueMan");
			} 	
	         
	         return result;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	        //dont have indcators yet
	     }

	     protected void onPostExecute(Integer result) {

	    	 dialog.dismiss();
	    	 switch (result) {
	    	 	case 36:
	    	 		//not online
	    	 		Toast.makeText(mListView.getContext(), "Unable to connect - Please check your connection, or try again", Toast.LENGTH_LONG).show();
					
	    	 		break;
				case 200:
				case 201:
					Toast.makeText(mListView.getContext(), ""+netflix.lastNFResponseMessage, Toast.LENGTH_LONG).show();
					redrawQueue();
					break;
				case 412:
				case 710:
					//title already exists! cant add
					Toast.makeText(mListView.getContext(), ""+netflix.lastNFResponseMessage, Toast.LENGTH_LONG).show();
					break;
				case 502:
					showCustomDialog("Error - Bad Gateway", "I'm sorry, but I was unable to add your movie due to a issue connecting. Please try again over Wi-Fi, or from a different location"+ "\n\n hit 'Back' to continue");
			        
					break;
				case 620:
					// added to SAVED queue - double post lolonger message
					Toast.makeText(mListView.getContext(), "Title Saved - This title is not currently available, but was added to your Saved queue", Toast.LENGTH_LONG).show();
					Toast.makeText(mListView.getContext(), "Title Saved - This title is not currently available, but was added to your Saved queue", Toast.LENGTH_LONG).show();
					FlurryAgent
							.onError(
									"Error:620",
									"The chosen title is a saved title, and nto movable",
									"QueueMan");
					break;
				default:
					FlurryAgent.onError("ER:45",
							"AddNewDisc: Unkown response. Result: " + result
									+ " Http:" + netflix.lastResponseMessage,
							"QueueMan");
						showCustomDialog("Error - Please Report", "Unable to add new title!\n Reason COde:"+netflix.lastResponseMessage + "\n\n hit 'Back' to continue");
		        
				}
	     }

	 }

	*/
 

		protected void loadHomeTitles(){
			 HashMap<String, String> parameters = new HashMap<String, String>();
		 		parameters.put("Queue Type:", "Home Titles");
		 		FlurryAgent.onEvent("loadHomeTitles", parameters);
		 		// show custom dialog to let them know
		 /*		dialog = new Dialog(this);
		 		dialog.setContentView(R.layout.custom_dialog);
		 		dialog.setTitle("Loading  at home titles");
		 		TextView text = (TextView) dialog.findViewById(R.id.text);
		 		String message = "\nTitles at home...";
		 		
		 		text.setText("Patience is a virtue" + message);
		 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		 		image.setImageResource(R.drawable.red_icon);
		 		// show message
		 		dialog.show();*/
		 		Toast t = Toast.makeText(mListView.getContext()
		 				,"Please wait - at home titles loading"
		 				,Toast.LENGTH_LONG);
		 		t.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
		 		t.show();
		 		new DownloadHomeTitles().execute();
		}
		/***
		 * Manage ASync Tasks the Android way
		 * @author eddie
		 *
		 */
		 private class DownloadHomeTitles extends AsyncTask<Void, Integer, Integer> {
		    protected void onPreExecute(){
		    	if(!isOnline()) this.cancel(true);
		    }
			 protected Integer doInBackground(Void... arg0) {
		        int result = 900;
		       
				
					if(!this.isCancelled()){
				 // get queue will connect to neflix and resave the currentQ
					// vairable
					//result = QueueMan.netflix.getHomeTitles();
					switch (result) {
					case 200:
					case 201:
						break;
					default:
						boolean hasAccess = (QueueMan.netflix.getUser().getAccessToken()!=null);
						boolean hasID = (QueueMan.netflix.getUser().getUserId()!=null);
						FlurryAgent.onError("ER:91",
								"Failed to Retrieve Home titles - "
										//+ QueueMan.netflix.lastResponseMessage
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
	
	public boolean isOnline() {
		//ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		//return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		return netflix.isConnected();
	}
	
	
	/**
	 * Uses current tab to purgew cucrrent queue and request new one
	 * @return
	 */
	private boolean refreshCurrentQueue(Queue queue) {
		queue.purgeQueue();
		loadQueue(queue);		
		return true;
	}


	
	
	public class ScrollHandler implements OnScrollListener{
		private boolean isEndOfLine = false; // scroll state is not adequate for us
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			Log.d("QueueMan","Viewing " + firstVisibleItem + " through " + (visibleItemCount+firstVisibleItem) + " of "+ totalItemCount);
			if ((firstVisibleItem+visibleItemCount) == totalItemCount) {
				 isEndOfLine = true;
			}else{
				isEndOfLine = false;
			}
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			 Log.d("QueueMan","ScrollState: " + scrollState);
			 if(scrollState == OnScrollListener.SCROLL_STATE_IDLE && isEndOfLine) {
				
			 
				//if this is the end of the line, prevent them asking fo more
				if(currentQueue.getTotalTitles() == 500 
						|| currentQueue.getStartIndex() + currentQueue.getMaxTitles() >= currentQueue.getTotalTitles()){
					Toast.makeText(QueueMan.this, "That's it! only " + currentQueue.getTotalTitles() + " results.", Toast.LENGTH_LONG).show();
					
				}else{
					//format next to "grab next XX titles"
					firstVisibleItem=currentQueue.getStartIndex()+Integer.valueOf(getDownloadCount());
					currentQueue.setStartIndex(currentQueue.getStartIndex()+Integer.valueOf(getDownloadCount()));
					loadQueue(currentQueue);
				}
				
			}
		}
	}
	
  


	 
}
