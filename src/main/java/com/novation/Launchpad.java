package com.novation;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.api.Color;

import static com.novation.LaunchpadControlXLExtension.*;

public class Launchpad {
  static final String sysExHeader = "F0 00 20 29 02 0D";

  static final int GRID_SIZE = 8;

  static final int UP_CC = 91;
  static final int DOWN_CC = 92;
  static final int LEFT_CC = 93;
  static final int RIGHT_CC = 94;
  static final int[] SCENE_CC = {89, 79, 69, 59, 49, 39, 29, 19};

  static final int[] PAD_REMOTE_CTRL = {2,3};

  static final int FADER_LENGTH = 5;

  static final int STATE_STOPPED = 0;
  static final int STATE_PLAYING = 1;
  static final int STATE_RECORDS = 2;

  static final RGBState STOP_QUEUED_COLOUR = RGBState.WHITE_PULSE;
  static final RGBState INACTIVE_COLOUR = RGBState.DARKGREY;
  static final RGBState PLAY_COLOUR = RGBState.WHITE;
  static final RGBState PLAY_QUEUED_COLOUR = RGBState.WHITE_BLINK;
  static final RGBState REC_COLOUR = RGBState.RED;
  static final RGBState REC_QUEUED_COLOUR = RGBState.RED_BLINK;
  static final RGBState STOP_ACTIVE_COLOUR = RGBState.DARKGREY;
  static final RGBState STOP_INACTIVE_COLOUR = RGBState.OFF;
  static final RGBState STOP_INACTIVE_QUEUED_COLOUR = RGBState.DARKGREY_BLINK;

  public void init() {
    mMidiIn = mPadMidiIn;
    mMidiOut = mPadMidiOut;
    mShift = false;

    mMidiIn.setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi(msg));

    mMidiOut.sendSysex(sysExHeader + " 10 01 F7"); // set to DAW mode
    mMidiOut.sendSysex(sysExHeader + " 00 00 F7"); // set Session layout

    mSceneBank.scrollPosition().addValueObserver(value -> { updateAllLED(); });
    mTrackBank.scrollPosition().addValueObserver(value -> { updateAllLED(); });

    for (int col = 0; col < GRID_SIZE; col++) {
      final int trackIdx = col;
      final Track track = mTrackBank.getItemAt(trackIdx);
      final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

      track.exists().addValueObserver(exists -> { updateAllLED(); });

      track.isStopped().addValueObserver(stopped -> {
        mHost.println("isStopped: " + stopped);
        if (stopped)
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_ACTIVE_COLOUR);
      });

      track.isQueuedForStop().addValueObserver(stopQueued -> {
        mHost.println("isQueuedForStop: " + stopQueued);
        if (stopQueued) {
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_COLOUR);
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_QUEUED_COLOUR);
        }
        else
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_COLOUR);
      });
      
      slotBank.addColorObserver((idx, r, g, b) -> { updateTrackLED(trackIdx); });

      slotBank.addHasContentObserver((idx, hasContent) -> {
        RGBState slot_colour = new RGBState(slotBank.getItemAt(idx).color().get());
        RGBState.send(mMidiOut, posToNote(idx, trackIdx), hasContent ? slot_colour : RGBState.OFF);
      });

      slotBank.addPlaybackStateObserver((idx, state, queued) -> {
        ClipLauncherSlot slot = slotBank.getItemAt(idx);
        updateClipLED(idx, trackIdx, slot, state, queued);
      });

      for (int pad_param = 0; pad_param < PAD_REMOTE_CTRL.length; pad_param++) {
        final int pad_param_f = pad_param;
        final int rc_idx = PAD_REMOTE_CTRL[pad_param];

        mRemoteControls[trackIdx].getParameter(rc_idx).value().addValueObserver(2, value -> {
          final int row = GRID_SIZE - PAD_REMOTE_CTRL.length + pad_param_f;
          final int pos_note = posToNote(row, trackIdx);
          RGBState.send(mMidiOut, pos_note, value > 0 ? PLAY_COLOUR : RGBState.OFF);
        });
      }
    }

    // extra 9th track controlled by the scene launch buttons (first 3 RCs)

    mTrackBank.getItemAt(GRID_SIZE).color().addValueObserver((r,g,b) -> {
      updateTrackLED(GRID_SIZE);
    });

    mRemoteControls[GRID_SIZE].getParameter(0).value().addValueObserver(FADER_LENGTH, value -> {
      RGBState track_colour = new RGBState(mTrackBank.getItemAt(GRID_SIZE).color().get());
      for (int i = 0; i < FADER_LENGTH; i++)
        setPadCCColour(SCENE_CC[i], value == 4 - i ? track_colour : INACTIVE_COLOUR);
    });
    
    for (int i = 1; i < 3; i++) {
      final int idx = i;
      mRemoteControls[GRID_SIZE].getParameter(i).value().addValueObserver(2, value -> {
        RGBState track_colour = new RGBState(mTrackBank.getItemAt(GRID_SIZE).color().get());
        setPadCCColour(SCENE_CC[FADER_LENGTH - 1 + idx], value > 0 ? track_colour : RGBState.OFF);
      });
    }
  }

  public void exit() {
    mMidiOut.sendSysex(sysExHeader + " 10 00 F7"); // set to standalone mode
  }

  // rows are counted top to bottom (starting at 0) throughout the script
  private int msgToRow(ShortMidiMessage msg) {
    return GRID_SIZE - msg.getData1() / 10;
  }

  private int msgToCol(ShortMidiMessage msg) {
    return msg.getData1() % 10 - 1;
  }

  private int posToNote(int row, int col) {
    return 10 * (GRID_SIZE - row) + (col + 1);
  }

  private void setPadCCColour(int cc, RGBState colour) {
    mMidiOut.sendMidi(176, cc, colour.getMessage());
  }

  private void updateClipLED(int row, int col, ClipLauncherSlot slot, int state, boolean queued) {
    RGBState slot_colour = new RGBState(slot.color().get());
    switch (state) {
      case STATE_STOPPED:
        RGBState.send(mMidiOut, posToNote(row, col), queued ? STOP_QUEUED_COLOUR : slot_colour);
        break;
      case STATE_PLAYING:
        RGBState.send(mMidiOut, posToNote(row, col), queued ? PLAY_QUEUED_COLOUR : PLAY_COLOUR);
        break;
      case STATE_RECORDS:
        RGBState.send(mMidiOut, posToNote(row, col), queued ? REC_QUEUED_COLOUR : REC_COLOUR);
        break;
    }
  }

  private void updateRemoteControlLED(int trackIdx) {
    CursorRemoteControlsPage rc_page = mRemoteControls[trackIdx];

    if (trackIdx < GRID_SIZE) {
      // tracks 1-8 have two momentary remote controls
      for (int i = 0; i < PAD_REMOTE_CTRL.length; i++) {
        int row = GRID_SIZE - PAD_REMOTE_CTRL.length + i;
        int rc_val = (int)Math.round(rc_page.getParameter(PAD_REMOTE_CTRL[i]).value().get());
        RGBState.send(mMidiOut, posToNote(row, trackIdx), rc_val > 0 ? PLAY_COLOUR : RGBState.OFF);
      }
    } else if (trackIdx == GRID_SIZE) {
      // track 9 has one fader and two toggle remote controls
      int max_fader_val = FADER_LENGTH - 1;
      int rc_val_0 = (int)Math.round(rc_page.getParameter(0).value().get() * max_fader_val);
      int rc_val_1 = (int)Math.round(rc_page.getParameter(1).value().get());
      int rc_val_2 = (int)Math.round(rc_page.getParameter(2).value().get());
      RGBState track_colour = new RGBState(mTrackBank.getItemAt(trackIdx).color().get());
      
      setPadCCColour(SCENE_CC[5], rc_val_1 > 0 ? track_colour : RGBState.OFF);
      setPadCCColour(SCENE_CC[6], rc_val_2 > 0 ? track_colour : RGBState.OFF);

      for (int i = 0; i < FADER_LENGTH; i++) {
        final int button_val = max_fader_val - i;
        final int button_cc = SCENE_CC[i];
        setPadCCColour(button_cc, rc_val_0 == button_val ? track_colour : INACTIVE_COLOUR);
      }
    }
  }

  private void updateAllLED() {
    for (int i = 0; i < NUM_TRACKS; i++)
      updateTrackLED(i);
    setPadCCColour(UP_CC, INACTIVE_COLOUR);
    setPadCCColour(DOWN_CC, INACTIVE_COLOUR);
    setPadCCColour(LEFT_CC, INACTIVE_COLOUR);
    setPadCCColour(RIGHT_CC, INACTIVE_COLOUR);
  }

  private void updateTrackLED(int trackIdx) {

    if (trackIdx < GRID_SIZE) {
      Track track = mTrackBank.getItemAt(trackIdx);
      ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
      for (int i = 0; i < NUM_SCENES; i++) {
        ClipLauncherSlot slot = slotBank.getItemAt(i); 
        if (slot.isPlaybackQueued().get())
          updateClipLED(i, trackIdx, slot, STATE_PLAYING, true);
        else if (slot.isPlaying().get())
          updateClipLED(i, trackIdx, slot, STATE_PLAYING, false);
        else if (slot.isStopQueued().get())
          updateClipLED(i, trackIdx, slot, STATE_STOPPED, true);
        else if (slot.isRecordingQueued().get())
          updateClipLED(i, trackIdx, slot, STATE_RECORDS, true);
        else if (slot.isRecording().get())
          updateClipLED(i, trackIdx, slot, STATE_RECORDS, false);
        else
          updateClipLED(i, trackIdx, slot, STATE_STOPPED, false);
      }
      if (track.isQueuedForStop().get())
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_QUEUED_COLOUR);
      else if (track.isStopped().get())
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_COLOUR);
      else
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_ACTIVE_COLOUR);
      updateRemoteControlLED(trackIdx);
    }
    else if (mShift) {
      if (trackIdx == GRID_SIZE) {
        for (int i = 0; i < NUM_SCENES; i++)
          setPadCCColour(SCENE_CC[i], PLAY_COLOUR);
        setPadCCColour(SCENE_CC[5], REC_COLOUR);
        setPadCCColour(SCENE_CC[7], PLAY_COLOUR);
      }
    }
    else {
      updateRemoteControlLED(trackIdx);
      setPadCCColour(SCENE_CC[7], RGBState.OFF);  
    }
  }

  private void processNote(int row, int col, boolean noteOn, int velocity) {
    Track track = mTrackBank.getItemAt(col);
    if (row < NUM_SCENES && noteOn) {
      ClipLauncherSlot slot = track.clipLauncherSlotBank().getItemAt(row);
      slot.launch();
    } else if (row == NUM_SCENES && noteOn) {
      track.stop();
    } else {
      int rc_idx = row + (PAD_REMOTE_CTRL.length - GRID_SIZE);
      int rc_num = PAD_REMOTE_CTRL[rc_idx];
      if (mShift && velocity == 127) {
        int rc_val = (int)Math.round(mRemoteControls[col].getParameter(rc_num).value().get());
        mRemoteControls[col].getParameter(rc_num).value().set(rc_val == 0 ? 1 : 0, 2);
      }
      else if (!mShift) {
        int value = velocity == 127 ? 1 : 0;
        mRemoteControls[col].getParameter(rc_num).value().set(value, 2);
      }
    }
  }

  private void enterShift() {
    mShift = true;
    updateTrackLED(GRID_SIZE);
  }

  private void exitShift() {
    mShift = false;
    updateTrackLED(GRID_SIZE);
  }

  private void processCC(int cc, int value) {
    // value == 0 means the button was released, not pressed
    if (value == 0) {
      if (cc == SCENE_CC[7]) exitShift();
      return;
    }
    switch (cc) {
      case UP_CC:
        mSceneBank.scrollPageBackwards();
        mSendBank.sceneBank().scrollPageBackwards();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        mSceneBank.getItemAt(0).showInEditor();
        return;
      case DOWN_CC:
        mSceneBank.scrollPageForwards();
        mSendBank.sceneBank().scrollPageForwards();
        mSceneBank.getItemAt(0).showInEditor();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        return;
      case LEFT_CC:
        mTrackBank.scrollBackwards();
        mTrackBank.getItemAt(7).makeVisibleInMixer();
        return;
      case RIGHT_CC:
        mTrackBank.scrollForwards();
        mTrackBank.getItemAt(0).makeVisibleInMixer();
        return;
    }
    // scene launch buttons control 3 RCs of the 9th track
    for (int i = 0; i < 8; i++)
      if (SCENE_CC[i] == cc) {
        if (i < 5) {
          if (!mShift)
            mRemoteControls[GRID_SIZE].getParameter(0).value().set(cc / 10 - 4, 5);
          else
            mSceneBank.launch(8 - cc / 10);
        }
        else if (i == 5 && mShift) {
          for (int col = 0; col < NUM_TRACKS; col++)
            mTrackBank.getItemAt(col).stop();
        }
        else if (i == 5 || i == 6) {
          int max_fader_val = FADER_LENGTH - 1;
          RemoteControl rc = mRemoteControls[GRID_SIZE].getParameter(i - max_fader_val);
          int rc_val = (int)Math.round(rc.value().get());
          rc.value().set(rc_val == 0 ? 1 : 0, 2);
        }
        else if (i == 7) {
          enterShift();
        }
      }
  }

  private void onMidi(ShortMidiMessage msg) {
    final int code = msg.getStatusByte() & 0xF0;

    switch (code) {
      // Note on/off
      case 0x80:
      case 0x90:
        final int row = msgToRow(msg);
        final int col = msgToCol(msg);
        final boolean noteOn = msg.isNoteOn();
        final int velocity = msg.getData2();
        processNote(row, col, noteOn, velocity);
        break;

      // CC
      case 0xB0:
        processCC(msg.getData1(), msg.getData2());
        break;

      default:
        mHost.println("Unhandled midi status: " + msg.getStatusByte());
        break;
    }
  }

  private MidiOut mMidiOut;
  private MidiIn mMidiIn;
}
