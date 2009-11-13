package edwardawebb.queueman.core;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class ViewLicense extends Activity {

    private static final String LOGTAG = "LicenseFile";

        private TextView readOutput;
	    private Button gotoReadXMLResource;

	        @Override
		    public void onCreate(final Bundle icicle) {
		            super.onCreate(icicle);
			            this.setContentView(R.layout.read_license_file);

				            this.readOutput = (TextView) findViewById(R.id.license_text);

					            Resources resources = getResources();
						            InputStream is = null;
							            try {
								                is = resources.openRawResource(R.raw.gpl);
										            byte[] reader = new byte[is.available()];
											                while (is.read(reader) != -1) {
													            }
														                this.readOutput.setText(new String(reader));
																        } catch (IOException e) {
																	            Log.e(LOGTAG, e.getMessage(), e);
																		            } finally {
																			                if (is != null) {
																					                try {
																							                    is.close();
																									                    } catch (IOException e) {
																											                        // swallow
																														//                 }
																														//                             }
																														//                                     } 
																														//
																														//                                         }
																														//                                         }
																														//
