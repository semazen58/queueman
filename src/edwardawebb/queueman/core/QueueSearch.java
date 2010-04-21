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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.NetFlixQueue;

public class QueueSearch extends Activity {

	// result code sent by movieDetail activity
	public static final int ADD_MOVIES = 99;
	ListView resultsList;
	EditText searchTextBox;
	Button searchButton, cancelButton;

	Dialog dialog;

	NetFlixQueue results;

	// handler for callbacks to UI (primary) thread
	final Handler mHandler = new Handler();
	// redraw q on callback
	final Runnable displayResults = new Runnable() {
		public void run() {
			dialog.dismiss();
			searchButton.setEnabled(true);
			// TODO Auto-generated method stub
			if (results != null) {
				redrawResults();
			} else {
				// Log.i("QueueSearch","no results")
			}

		}
	};

	protected void redrawResults() {
		resultsList = (ListView) findViewById(R.id.results);
		resultsList.setAdapter(new ArrayAdapter<Disc>(QueueSearch.this,
				android.R.layout.simple_list_item_1, results.getDiscs()));
		// resultsList.setOnClickListener(new clickr());
		resultsList.setTextFilterEnabled(true);
		resultsList.setOnItemClickListener(new clickr());
		resultsList.refreshDrawableState();
		// register for long hold on menu items
		// registerForContextMenu(resultsList);

	}

	private void returnResult(Disc disc) {
		// setup an intent to return a result
		Bundle b = new Bundle();
		// b.pu
		b.putSerializable("Disc", disc);

		Intent resultIntent = new Intent();
		resultIntent.putExtras(b);
		QueueSearch.this.setResult(QueueMan.SEARCH_MOVIES, resultIntent);

		finish();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queuesearch);

		// Capture our button from layout
		searchButton = (Button) findViewById(R.id.ok);
		// Register the onClick listener with the implementation above
		searchButton.setOnClickListener(new clickr());
		searchTextBox = (EditText) findViewById(R.id.entry);
		searchTextBox.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		          // Perform action on key press
		          QueueSearch.this.searchButton.performClick();
		        	return true;
		        }
		        return false;
		    }
		});

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

	private void findResults(final String searchTerm) {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle("Searching Netflix");
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText("....");
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.red_icon);
		// show message
		dialog.show();
		Thread t = new Thread() {
			public void run() {
				results = QueueMan.netflix.getSearchResults(searchTerm.trim());
				mHandler.post(displayResults);

			}
		};
		t.start();
	}

	/**
	 * Call movie details for clicked title
	 * @param selection disc to display
	 */
	private void showDetails(Disc selection) {
		//
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("Disc Id", String.valueOf(selection.getId()));
		parameters.put("Title", String.valueOf(selection.getFullTitle()));
		FlurryAgent.onEvent("ViewSearchedTitle", parameters);
		
		
		// notify("Dev note","I cant help you with "+l.getItemAtPosition(position));
		Intent intent = new Intent(this,
				edwardawebb.queueman.core.MovieDetails.class);
		Bundle b = new Bundle();
		// b.pu
		b.putSerializable("Disc", selection);

		intent.putExtras(b);
		intent.putExtra(QueueMan.ACTION_KEY, QueueMan.ACTION_ADD);
		startActivityForResult(intent, ADD_MOVIES);
	}

	// Listen for results from search screen
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// See which child activity is calling us back.
		switch (resultCode) {
		case ADD_MOVIES:
			FlurryAgent.onEvent("AddSearchedTitle");
			QueueSearch.this.setResult(QueueMan.SEARCH_MOVIES, data);
			finish();
			break;
		default:
		}
	}

	class clickr implements Button.OnClickListener, OnItemClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == searchButton) {
				String searchTerm = searchTextBox.getText().toString();

				findResults(searchTerm);
				searchButton.setEnabled(false);
			} else if (v == cancelButton) {
				//
				QueueSearch.this.finish();
			} else {
				// Log.d("QueueSearch",v.toString())
				// Log.d("QueueSearch","equal: " + new
				// Boolean(v==resultsList).toString())
			}

		}

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			/*
			 * dialog=new Dialog(QueueSearch.this);
			 * dialog.setContentView(R.layout.custom_dialog);
			 * dialog.setTitle("Saving Movie"); TextView text = (TextView)
			 * dialog.findViewById(R.id.text);
			 * text.setText("Adding Movie to your Queue"); ImageView image =
			 * (ImageView) dialog.findViewById(R.id.image);
			 * image.setImageResource(R.drawable.icon); //show message
			 * dialog.show();
			 */
			final Disc selection = (Disc) arg0.getItemAtPosition(arg2);

			Thread t = new Thread() {
				public void run() {

					// Log.d("QueueSearch","Item clicked:"+
					// selection.getShortTitle())
					// returnResult(selection.getTitle(), selection.getId());
					showDetails(selection);
					// returnResult(selection);
				}

			};
			t.start();// TODO Auto-generated method stub

		}

	}

}
