/**
 * 
 */
package edwardawebb.queueman.core;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/**
 * @author eddie
 * 
 */
public class Settings extends Activity implements OnClickListener {
	private Button saveButton;
	private Spinner countSpinner;
	private String titleCount;
	private ArrayAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		countSpinner = (Spinner) findViewById(R.id.spinner);
		adapter = ArrayAdapter.createFromResource(this,
				R.array.download_counts, android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countSpinner.setAdapter(adapter);
		loadSettings();

		saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(this);
	}

	/**
	 * Adds this preference to existing preferences.
	 */
	protected void saveSettings() {
		SharedPreferences settings = getSharedPreferences(QueueMan.PREFS_NAME,
				0);
		SharedPreferences.Editor editor = settings.edit();
		// values
		titleCount = (String) countSpinner.getSelectedItem();
		editor.putString(QueueMan.TITLE_COUNT_KEY, titleCount);
		QueueMan.updateDownloadCount(titleCount);
		// commit
		editor.commit();
	}

	private void loadSettings() {
		SharedPreferences settings = getSharedPreferences(QueueMan.PREFS_NAME,
				0);
		if (settings.contains(QueueMan.TITLE_COUNT_KEY)) {
			titleCount = settings.getString(QueueMan.TITLE_COUNT_KEY, "");
			countSpinner.setSelection(adapter.getPosition(titleCount));
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		saveSettings();
		this.finish();
	}
}
