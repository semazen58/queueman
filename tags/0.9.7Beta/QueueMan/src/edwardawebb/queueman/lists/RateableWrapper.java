/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package edwardawebb.queueman.lists;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.widget.ListAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.core.QueueMan;

public class RateableWrapper extends AdapterWrapper {
	Context ctxt=null;
	float[] rates=null;
	
	public RateableWrapper(Context ctxt, ListAdapter delegate) {
		super(delegate);
		
		this.ctxt=ctxt;
		this.rates=new float[delegate.getCount()];
		
		for (int i=0;i<delegate.getCount();i++) {
			this.rates[i]=2.0f;
		}
	}
	
	public View getView(int position, View convertView,
											ViewGroup parent) {
		ViewWrapper wrap=null;
		View row=convertView;
														
		if (convertView==null) {
			LinearLayout layout=new LinearLayout(ctxt);
			RatingBar rate=new RatingBar(ctxt);
			
			rate.setNumStars(5);
			rate.setStepSize(1.0f);
			final Disc disc=(Disc)delegate.getItem(position);
			rate.setRating(disc.getRating().floatValue());
			
			View guts=delegate.getView(position, null, parent);
		
			layout.setOrientation(LinearLayout.VERTICAL); 
					
			guts.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.FILL_PARENT));
			rate.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.FILL_PARENT));
			
			RatingBar.OnRatingBarChangeListener l=
										new RatingBar.OnRatingBarChangeListener() {
				public void onRatingChanged(RatingBar ratingBar,float rating,boolean fromTouch)	{
					rates[(Integer)ratingBar.getTag()]=rating;
					if(fromTouch) {
						QueueMan.netflix.setRating(disc.getId(),rating);
					}
				}
			};
			
			rate.setOnRatingBarChangeListener(l);
					
			layout.addView(rate); 					
			layout.addView(guts);
			
			wrap=new ViewWrapper(layout);
			wrap.setGuts(guts);
			layout.setTag(wrap);
			
			rate.setTag(new Integer(position));
			rate.setRating(rates[position]);
				
			row=layout;				
		}
		else {
			wrap=(ViewWrapper)convertView.getTag();
			wrap.setGuts(delegate.getView(position, wrap.getGuts(),
																		parent));
			wrap.getRatingBar().setTag(new Integer(position));
			wrap.getRatingBar().setRating(rates[position]);
		}
		
		return(row);
	}		
}