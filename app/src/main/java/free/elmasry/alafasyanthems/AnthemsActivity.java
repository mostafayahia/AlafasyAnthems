/*
 * Copyright (C) 2018 Yahia H. El-Tayeb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * El-Masry
 * Free application
 * this is a simple application for playing some anthems for alafasy
 */

package free.elmasry.alafasyanthems;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class AnthemsActivity extends Activity implements OnClickListener, OnCompletionListener {
	
	// N represents number of anthems
	private final int N = 7;
	
	// choosing value -1 because we increase this value to get anthem index
	private int currentAnthemIndex = -1;
	
	private MediaPlayer mPlayer;
	private CheckBox[] checkBoxes;
	private TextView[] textViews;
	private Button play;
	private Button repetition;
	
	// constants for adjusting play button
	private final int READY_FOR_PLAYING = 0;
	private final int READY_FOR_PAUSING = 1;
	private final int READY_FOR_RESUMING = 2;
	
	private int playButtonState;
	
	// constants for repetition state
	private final int AFTER_FINISH_REPEAT = 0;
	private final int AFTER_FINISH_CLOSE = 1;
	
	private int repetitionState;

	private SharedPreferences mPrefs;
	
	// constant for saving in shared preferences
	private final String STR_REPETITION = "repetition";

	/**
	 * running when first time activity created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set the root view for this activity
		setContentView(R.layout.activity_anthems);
		
		// get the views in this activity
		play = (Button)findViewById(R.id.play);
		final Button next = (Button)findViewById(R.id.next);
		final Button previous = (Button)findViewById(R.id.previous);
		repetition = (Button)findViewById(R.id.repetition);
		checkBoxes = getCheckBoxes();
		textViews = getTextViews();
		
		// set event handler for the buttons
		play.setOnClickListener(this);
		next.setOnClickListener(this);
		previous.setOnClickListener(this);
		repetition.setOnClickListener(this);
		
		// initialize play button state
		playButtonState = READY_FOR_PLAYING;
		
		// get mPrefs
	    mPrefs = getPreferences(Context.MODE_PRIVATE);
	    
	    // restore check boxes status from the preferences
	    for (int i = 0; i < N; i++) {
	    	boolean checked = mPrefs.getBoolean(getCheckBoxFakeName(i), false); 
	    	if (checked)
	    		checkBoxes[i].setChecked(true);
	    }
	    
	    // restore repetition status from the preferences
	    repetitionState = mPrefs.getInt(STR_REPETITION, AFTER_FINISH_CLOSE);
	    if (repetitionState == AFTER_FINISH_REPEAT) 
			repetition.setText(getString(R.string.after_finish_repeat_str));
		else
			repetition.setText(getString(R.string.after_finish_close_str));
	 
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		savePreferences();
	}

	/**
	 * save preferences according to the user choices
	 */
	private void savePreferences() {
		SharedPreferences.Editor ed = mPrefs.edit();
		// save the check boxes status
		// we use fake names for check boxes for storing and retrieving
		// their status
		for (int i = 0; i < N; i++) {
			ed.putBoolean(getCheckBoxFakeName(i), checkBoxes[i].isChecked());
		}
		// save repetition status
		ed.putInt(STR_REPETITION, repetitionState);
		
		// must be added to save the changes we made
		ed.commit();
	    
	}

	/**
	 * called when the activity completely destroyed
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// stop and release the resources of the current playing sound 
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	/**
	 * play next anthem according to anthem index and check boxes status
	 * @return false means there is no next anthem to play
	 */
    private void prepareNextAnthem() {
    	int previousAnthemIndex = currentAnthemIndex;
    	moveAnthemIndexForward();
    	// bold the current anthem index and clear bold from the previous one
    	boldAnthemName(currentAnthemIndex);
    	if (previousAnthemIndex != -1) 
    		unboldAnthemName(previousAnthemIndex);
	}

    /**
	 * play previous anthem according to anthem index and check boxes status
	 * @return false means there is no previous anthem to play
	 */
    private void preparePreviousAnthem() {
    	int previousAnthemIndex = currentAnthemIndex;
    	moveAnthemIndexBackword();
    	// bold the current anthem index and clear bold from the previous one
    	boldAnthemName(currentAnthemIndex);
    	if (previousAnthemIndex != -1) 
    		unboldAnthemName(previousAnthemIndex);
	}

    /**
     * play next anthem according to the current anthem index
     */
    private void playNextAnthem() {
    	prepareNextAnthem();
    	playAnthem();
    }
    
    /**
     * play previous anthem according to the current anthem index
     */
    private void playPreviousAnthem() {
    	preparePreviousAnthem();
    	playAnthem();
    }
    
	/**
     * play anthem after making appropriate setting for the media player
     */
	private void playAnthem() {
		
		// clear memory and release resources before making new one
		if (mPlayer != null)
			mPlayer.release();
		
		// create media player object according to anthem index
		int anthemResid = getAnthemResid();
	    mPlayer = MediaPlayer.create(this, anthemResid);
	    
        // set event handler when sound audio finishes
	 	mPlayer.setOnCompletionListener(this);
	 	
	    mPlayer.setLooping(false);
	    mPlayer.start();
	}


	/**
     * move anthem index forward
     */
	private void moveAnthemIndexForward() {
		for (int i = currentAnthemIndex + 1; i < N; i++) {
			if (checkBoxes[i].isChecked()) {
				currentAnthemIndex = i;
				break;
			}
		}
	}
	
	/**
     * move anthem index backword
     */
	private void moveAnthemIndexBackword() {
		for (int i = currentAnthemIndex - 1; i >= 0; i--) {
			if (checkBoxes[i].isChecked()) {
				currentAnthemIndex = i;
				break;
			}
		}
	}
	
	/**
	 * check if we have next check box to play anthem
	 * @return true if there is next anthem to play
	 */
	private boolean hasNextAnthem() {
		for (int i = currentAnthemIndex + 1; i < N; i++) {
			if (checkBoxes[i].isChecked()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if we have previous check box to play anthem
	 * @return true if there is previous anthem to play
	 */
	private boolean hasPreviousAnthem() {
		for (int i = currentAnthemIndex - 1; i >= 0; i--) {
			if (checkBoxes[i].isChecked()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * making anthem name bold according to anthem index
	 * @param anthemIndex anthem index to make its name bold
	 */
	private void boldAnthemName(int anthemIndex) {
		// make bold text view corresponding to anthem to indicate that 
		// this one is now playing 
		// NOTE: bold not working in ginger bird 2.2.3 for Arabic text (English works well)
		// so we add italic also
		textViews[anthemIndex].setTypeface(null, Typeface.BOLD_ITALIC);
		
	}
	
	/**
	 * remove bold for the specified anthem index
	 * @param anthemIndex index of anthem for removing bold
	 */
	private void unboldAnthemName(int anthemIndex) {
		textViews[anthemIndex].setTypeface(null, Typeface.NORMAL);
		
	}
	
	/**
	 * show message to displayed as a toast in the activity
	 * @param message to display in activity
	 */
	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	
	/**
	 * event handlers for the buttons
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play:
			handlePlayButton();
			break;
		case R.id.next:
			if (hasNextAnthem()) {
				// similar functionality for play button for ("ready for playing" state)
				playButtonState = READY_FOR_PLAYING;
				handlePlayButton();
			} else 
				showMessage("لا يوجد نشيد تالى لتشغيله");
			break;
		case R.id.previous:
			if (hasPreviousAnthem()) {
				playPreviousAnthem();
				playButtonState = READY_FOR_PAUSING;
				play.setText(getString(R.string.pause_str));
			} else 
				showMessage("لا يوجد نشيد سابق لتشغيله");
			break;

		case R.id.repetition:
		default:
			// once the user click the button, we toggle repetition state
			if (repetitionState == AFTER_FINISH_CLOSE) {
				repetitionState = AFTER_FINISH_REPEAT;
				repetition.setText(getString(R.string.after_finish_repeat_str));
			} else {
				repetitionState = AFTER_FINISH_CLOSE;
				repetition.setText(getString(R.string.after_finish_close_str));
			}
			break;
		}
		
	}
	
	/**
	 * handling play button according to its state
	 */
	private void handlePlayButton() {
		switch (playButtonState) {
		case READY_FOR_PLAYING:
			if (hasNextAnthem()) {
				playNextAnthem();
				playButtonState = READY_FOR_PAUSING;
				play.setText(getString(R.string.pause_str));
			}
			else { 
				if (currentAnthemIndex == -1)
					showMessage("لم يتم أختيار أى نشيد");
			}
			break;
		case READY_FOR_PAUSING:
			mPlayer.pause();
			playButtonState = READY_FOR_RESUMING;
			play.setText(getString(R.string.play_str));
			break;
		case READY_FOR_RESUMING:
		default:
			mPlayer.start();
			playButtonState = READY_FOR_PAUSING;
			play.setText(getString(R.string.pause_str));
			break;		
		}
	}


	/**
	 * event handler for media player after completion
	 * @param mp
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		mPlayer.release();
		if (hasNextAnthem()) {
			playNextAnthem();
		}
		else {
			mPlayer = null;
			
			// initialize many settings to be ready for replaying from the user or repetition anthems
			unboldAnthemName(currentAnthemIndex);
			currentAnthemIndex = -1;
			
			if (repetitionState == AFTER_FINISH_REPEAT) {
				if (hasNextAnthem()) playNextAnthem();
			} else { 
				// closing activity after finishing playing anthems
				finish(); 
				return;
				// the next code is used in case we did not decide to close activity after finishing anthems
				/*
			    playButtonState = READY_FOR_PLAYING;
			    play.setText(getString(R.string.replay_str));
			    */
			}
		}
	}
	
	//================ IMPORTANT ===============================
	// any change or (rearrangement) in any of these methods must be correspond change in all of them
	// IN ADDITION TO the names of anthems in STRINGS.XML 
	
	/**
	 * getting anthem resource id of anthem audio according to anthem index
	 * @return resid of anthem audio
	 */
	private Integer getAnthemResid() {
		checkAnthemIndex(currentAnthemIndex);
		Integer resid;
		switch (currentAnthemIndex) {
		case 0:
			resid = R.raw.no_god_except_allah;
			//resid = R.raw.sample0;
			break;
		case 1:
			resid = R.raw.moon_appeared;
			//resid = R.raw.sample1;
			break;
		case 2:
			resid = R.raw.rahman;
			//resid = R.raw.sample2;
			break;
		case 3:
			resid = R.raw.my_mother;
			//resid = R.raw.sample3;
			break;
		case 4:
			resid = R.raw.i_am_slave;
			//resid = R.raw.sample4;
			break;
		case 5:
			resid = R.raw.disappear;
			//resid = R.raw.sample5;
			break;
		case 6:
			resid = R.raw.not_stranger;
			//resid = R.raw.sample6;
			break;
		default:
			resid = null;
		}
		return resid;
	}

	// ================================= helper methods =============================================
	/**
	 * getting anthem fake name according to anthem index
	 * @return anthem name form strings.xml file
	 */
	private String getCheckBoxFakeName(int index) {
		checkAnthemIndex(index);
		String checkBoxName;
		switch (index) {
		case 0:
			checkBoxName = "checkBox0";
			break;
		case 1:
			checkBoxName = "checkBox1";
			break;
		case 2:
			checkBoxName = "checkBox2";
			break;
		case 3:
			checkBoxName = "checkBox3";
			break;
		case 4:
			checkBoxName = "checkBox4";
			break;
		case 5:
			checkBoxName = "checkBox5";
			break;
		case 6:
			checkBoxName = "checkBox6";
			break;
		default:
			checkBoxName = null;
			break;
		}
		return checkBoxName;
	}
	
	/**
	 * check if anthem index is out of boundary or not
	 */
	private void checkAnthemIndex(int index) {
		if (index >= N || index < 0)
			throw new RuntimeException("anthemIndex: " + currentAnthemIndex + " is out of boundary (max is " + (N-1) + ")");
	}
	
	// ========================== get views resides in the activity =======================================
	/**
	 * getting all check boxes which represents anthems
	 * 
	 * @return array of check boxes objects
	 */
	private CheckBox[] getCheckBoxes() {
		// TODO Auto-generated method stub
		CheckBox[] checkBoxes = new CheckBox[N];
		checkBoxes[0] = (CheckBox)findViewById(R.id.checkBox0);
		checkBoxes[1] = (CheckBox)findViewById(R.id.checkBox1);
		checkBoxes[2] = (CheckBox)findViewById(R.id.checkBox2);
		checkBoxes[3] = (CheckBox)findViewById(R.id.checkBox3);
		checkBoxes[4] = (CheckBox)findViewById(R.id.checkBox4);
		checkBoxes[5] = (CheckBox)findViewById(R.id.checkBox5);
		checkBoxes[6] = (CheckBox)findViewById(R.id.checkBox6);
		return checkBoxes;
	}

    /**
     * getting all text views which represents anthems' names	
     * @return array of text views objects
     */
	private TextView[] getTextViews() {
		TextView[] textViews = new TextView[N];
		textViews[0] = (TextView)findViewById(R.id.textView0);
		textViews[1] = (TextView)findViewById(R.id.textView1);
		textViews[2] = (TextView)findViewById(R.id.textView2);
		textViews[3] = (TextView)findViewById(R.id.textView3);
		textViews[4] = (TextView)findViewById(R.id.textView4);
		textViews[5] = (TextView)findViewById(R.id.textView5);
		textViews[6] = (TextView)findViewById(R.id.textView6);
		return textViews;
	}
}
