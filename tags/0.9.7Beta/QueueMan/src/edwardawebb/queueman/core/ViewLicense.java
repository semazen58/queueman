package edwardawebb.queueman.core;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class ViewLicense extends Activity {

	private static final String LOGTAG = "LicenseFile";

	private TextView readOutput;

	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);
		this.setContentView(R.layout.read_license_file);

		this.readOutput = (TextView) findViewById(R.id.license_text);

		Resources resources = getResources();
		InputStream is = null;
		try {
			is = resources.openRawResource(R.raw.gpl_3);
			byte[] reader = new byte[is.available()];
			while (is.read(reader) != -1) {
			}
			//this.readOutput.setText(Html.fromHtml(Html.toHtml(new String(reader))));
			this.readOutput.setText(new String(reader));
		} catch (IOException e) {
			Log.e(LOGTAG, e.getMessage(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// swallow
				}
			}
		}

	}
}
