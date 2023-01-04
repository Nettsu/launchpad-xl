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

  static final int SCENE_1_CC = 89;
  static final int SCENE_2_CC = 79;
  static final int SCENE_3_CC = 69;
  static final int SCENE_4_CC = 59;
  static final int SCENE_5_CC = 49;
  static final int SCENE_6_CC = 39;
  static final int SCENE_7_CC = 29;
  static final int SCENE_8_CC = 19;

  static final int[] PAD_REMOTE_CTRL = {2,3};

  static final int FADER_LENGTH = 5;

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

    mSceneBank.scrollPosition().addValueObserver(value -> {
      updateAllLED();
    });

    mTrackBank.scrollPosition().addValueObserver(value -> {
      updateAllLED();
    });

    for (int col = 0; col < GRID_SIZE; col++) {
      final int trackIdx = col;
      final Track track = mTrackBank.getItemAt(trackIdx);
      final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

      for (int pad_param = 0; pad_param < PAD_REMOTE_CTRL.length; pad_param++) {
        final int pad_param_f = pad_param;
        final int rc_idx = PAD_REMOTE_CTRL[pad_param];
        mRemoteControls[trackIdx].getParameter(rc_idx).value().addValueObserver(2, value -> {
          final int row = GRID_SIZE - PAD_REMOTE_CTRL.length + pad_param_f;
          if (value > 0)
            RGBState.send(mMidiOut, posToNote(row, trackIdx), PLAY_COLOUR);
          else
            RGBState.send(mMidiOut, posToNote(row, trackIdx), RGBState.OFF);
        });
      }

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

      track.exists().addValueObserver(exists -> {
        updateAllLED();
      });

      slotBank.addColorObserver((idx, r, g, b) -> {
        RGBState.send(mMidiOut, posToNote(idx, trackIdx), new RGBState(Color.fromRGB(r, g, b)));
      });

      slotBank.addHasContentObserver((idx, hasContent) -> {
        if (hasContent) {
          ClipLauncherSlot slot = slotBank.getItemAt(idx);
          RGBState.send(mMidiOut, posToNote(idx, trackIdx), new RGBState(slot.color().get()));
        } else
          RGBState.send(mMidiOut, posToNote(idx, trackIdx), RGBState.OFF);
      });

      slotBank.addPlaybackStateObserver((idx, state, queued) -> {
        ClipLauncherSlot slot = slotBank.getItemAt(idx);
        updateClipLED(idx, trackIdx, slot, state, queued);
      });
    }

    // extra 9th track controlled by the scene launch buttons (first 3 RCs)

    mTrackBank.getItemAt(GRID_SIZE).color().addValueObserver((r,g,b) -> {
      updateTrackLED(GRID_SIZE);
    });

    mRemoteControls[GRID_SIZE].getParameter(0).value().addValueObserver(FADER_LENGTH, value -> {
      RGBState track_colour = new RGBState(mTrackBank.getItemAt(GRID_SIZE).color().get());
      for (int i = 0; i < FADER_LENGTH; i++)
        setPadCCColour(SCENE_1_CC - i * 10, value == 4 - i ? track_colour : INACTIVE_COLOUR);
    });
    
    for (int i = 1; i < 3; i++) {
      final int idx = i;
      mRemoteControls[GRID_SIZE].getParameter(i).value().addValueObserver(2, value -> {
        RGBState track_colour = new RGBState(mTrackBank.getItemAt(GRID_SIZE).color().get());
        setPadCCColour(SCENE_5_CC - 10 * idx, value > 0 ? track_colour : RGBState.OFF);
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
    RGBState.send(mMidiOut, posToNote(row, col), new RGBState(slot.color().get()));
    switch (state) {
      case 0: // stopped
        if (queued)
          RGBState.send(mMidiOut, posToNote(row, col), STOP_QUEUED_COLOUR);
        break;
      case 1: // playing
        if (queued)
          RGBState.send(mMidiOut, posToNote(row, col), PLAY_QUEUED_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(row, col), PLAY_COLOUR);
        break;
      case 2: // recording
        if (queued)
          RGBState.send(mMidiOut, posToNote(row, col), REC_QUEUED_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(row, col), REC_COLOUR);
        break;
    }
  }

  private void updateRemoteControlLED(int trackIdx) {
    if (trackIdx < GRID_SIZE) {
      for (int i = 0; i < PAD_REMOTE_CTRL.length; i++) {
        double rc_val = mRemoteControls[trackIdx].getParameter(PAD_REMOTE_CTRL[i]).value().get();
        int row = GRID_SIZE - PAD_REMOTE_CTRL.length + i;
        if ((int)Math.round(rc_val) > 0)
          RGBState.send(mMidiOut, posToNote(row, trackIdx), PLAY_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(row, trackIdx), RGBState.OFF);
      }
    } else if (trackIdx == GRID_SIZE) {
      RGBState track_colour = new RGBState(mTrackBank.getItemAt(trackIdx).color().get());
      int rc_val_0 = (int)Math.round(mRemoteControls[trackIdx].getParameter(0).value().get() * 4);
      int rc_val_1 = (int)Math.round(mRemoteControls[trackIdx].getParameter(1).value().get());
      int rc_val_2 = (int)Math.round(mRemoteControls[trackIdx].getParameter(2).value().get());
      for (int i = 0; i < FADER_LENGTH; i++)
        setPadCCColour(SCENE_1_CC - i * 10, rc_val_0 == 4 - i ? track_colour : INACTIVE_COLOUR);

      setPadCCColour(SCENE_6_CC, rc_val_1 > 0 ? track_colour : RGBState.OFF);
      setPadCCColour(SCENE_7_CC, rc_val_2 > 0 ? track_colour : RGBState.OFF);
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
    if (trackIdx == GRID_SIZE && mShift) {
      setPadCCColour(SCENE_8_CC, PLAY_COLOUR);
      for (int i = SCENE_1_CC; i > SCENE_6_CC; i -= 10)
        setPadCCColour(i, PLAY_COLOUR);
      setPadCCColour(SCENE_6_CC, REC_COLOUR);
    }
    else if (trackIdx < GRID_SIZE) {
      Track track = mTrackBank.getItemAt(trackIdx);
      ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
      for (int i = 0; i < NUM_SCENES; i++) {
        ClipLauncherSlot slot = slotBank.getItemAt(i);
        if (slot.isPlaybackQueued().get())
          updateClipLED(i, trackIdx, slot, 1, true);
        else if (slot.isPlaying().get())
          updateClipLED(i, trackIdx, slot, 1, false);
        else if (slot.isStopQueued().get())
          updateClipLED(i, trackIdx, slot, 0, true);
        else if (slot.isRecordingQueued().get())
          updateClipLED(i, trackIdx, slot, 2, true);
        else if (slot.isRecording().get())
          updateClipLED(i, trackIdx, slot, 2, false);
        else
          updateClipLED(i, trackIdx, slot, 0, false);
      }
      if (track.isQueuedForStop().get())
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_QUEUED_COLOUR);
      else if (track.isStopped().get())
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_INACTIVE_COLOUR);
      else
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, trackIdx), STOP_ACTIVE_COLOUR);
      updateRemoteControlLED(trackIdx);
      setPadCCColour(SCENE_8_CC, RGBState.OFF);
    }
    else {
      updateRemoteControlLED(trackIdx);
      setPadCCColour(SCENE_8_CC, RGBState.OFF);
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
    if (value == 0) {
      if (cc == SCENE_8_CC) exitShift();
      return;
    }
    switch (cc) {
      case UP_CC:
        mSceneBank.scrollPageBackwards();
        mSendBank.sceneBank().scrollPageBackwards();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        mSceneBank.getItemAt(0).showInEditor();
        break;
      case DOWN_CC:
        mSceneBank.scrollPageForwards();
        mSendBank.sceneBank().scrollPageForwards();
        mSceneBank.getItemAt(0).showInEditor();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        break;
      case LEFT_CC:
        mTrackBank.scrollBackwards();
        mTrackBank.getItemAt(7).makeVisibleInMixer();
        break;
      case RIGHT_CC:
        mTrackBank.scrollForwards();
        mTrackBank.getItemAt(0).makeVisibleInMixer();
        break;
      // scene launch buttons control 3 RCs of the 9th track
      case SCENE_1_CC:
      case SCENE_2_CC:
      case SCENE_3_CC:
      case SCENE_4_CC:
      case SCENE_5_CC:
        if (!mShift)
          mRemoteControls[GRID_SIZE].getParameter(0).value().set(cc / 10 - 4, 5);
        else
          mSceneBank.launch(8 - cc / 10);
        break;
      case SCENE_6_CC:
        if (!mShift) {
          int rc_val_1 = (int)Math.round(mRemoteControls[GRID_SIZE].getParameter(1).value().get());
          mRemoteControls[GRID_SIZE].getParameter(1).value().set(rc_val_1 == 0 ? 1 : 0, 2);
        }
        else {
          for (int i = 0; i < NUM_TRACKS; i++)
            mTrackBank.getItemAt(i).stop();
        }
        break;
      case SCENE_7_CC:
        int rc_val_2 = (int)Math.round(mRemoteControls[GRID_SIZE].getParameter(2).value().get());
        mRemoteControls[GRID_SIZE].getParameter(2).value().set(rc_val_2 == 0 ? 1 : 0, 2);
        break;
      case SCENE_8_CC:
        enterShift();
        break;
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
