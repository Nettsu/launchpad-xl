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

  static final RGBState STOP_QUEUED_COLOUR = RGBState.OFF_BLINK;
  static final RGBState PLAY_COLOUR = RGBState.WHITE;
  static final RGBState PLAY_QUEUED_COLOUR = RGBState.WHITE_BLINK;
  static final RGBState REC_COLOUR = RGBState.RED;
  static final RGBState REC_QUEUED_COLOUR = RGBState.RED_BLINK;
  static final RGBState STOP_ACTIVE_COLOUR = RGBState.DARKRED;
  static final RGBState STOP_INACTIVE_COLOUR = new RGBState(71);
  static final RGBState STOP_INACTIVE_QUEUED_COLOUR = RGBState.DARKRED_BLINK;

  public void init() {
    mMidiIn = mPadMidiIn;
    mMidiOut = mPadMidiOut;

    mMidiIn.setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi(msg));
    mMidiIn.setSysexCallback((String data) -> onSysex(data));

    mMidiOut.sendSysex(sysExHeader + " 10 01 F7"); // set to DAW mode
    mMidiOut.sendSysex(sysExHeader + " 00 00 F7"); // set Session layout

    mTrackBank.setShouldShowClipLauncherFeedback(true);

    mSceneBank.scrollPosition().addValueObserver(value -> {
      updateAllLED();
    });

    mTrackBank.scrollPosition().addValueObserver(value -> {
      updateAllLED();
    });

    for (int i = 0; i < GRID_SIZE; i++) {
      final int track = i;
      for (int param = 0; param < 8; param++) {
        for (int pad_param = 0; pad_param < PAD_REMOTE_CTRL.length; pad_param++) {
          final int pad_param_f = pad_param;
          if (param == PAD_REMOTE_CTRL[pad_param]) {
            mRemoteControls[track].getParameter(param).value().addValueObserver(2, value -> {
              mPadRCValue[pad_param_f][track] = value;
              updateRemoteControlLED(track);
            });
          }
        }
      }
    }

    for (int col = 0; col < GRID_SIZE; col++) {
      final int trackIdx = col;
      final Track track = mTrackBank.getItemAt(trackIdx);
      final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

      track.exists().addValueObserver(exists -> {
        updateAllLED();
      });

      slotBank.addColorObserver((idx, red, green, blue) -> {
        RGBState.send(mMidiOut, posToNote(idx, trackIdx), new RGBState(Color.fromRGB(red, green, blue)));
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
    for (int i = 0; i < 3; i++) {
      final int rc = i;
      int granularity = i == 0 ? FADER_LENGTH : 2;
      mRemoteControls[GRID_SIZE].getParameter(rc).value().addValueObserver(granularity, value -> {
        m9thRCValue[rc] = value;
        updateRemoteControlLED(GRID_SIZE);
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
        if (queued) {
          RGBState.send(mMidiOut, posToNote(row, col), PLAY_QUEUED_COLOUR);
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, col), STOP_INACTIVE_QUEUED_COLOUR);
        } else {
          RGBState.send(mMidiOut, posToNote(NUM_SCENES, col), STOP_INACTIVE_COLOUR);
        }
        break;
      case 1: // playing
        if (queued)
          RGBState.send(mMidiOut, posToNote(row, col), PLAY_QUEUED_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(row, col), PLAY_COLOUR);
        RGBState.send(mMidiOut, posToNote(NUM_SCENES, col), STOP_ACTIVE_COLOUR);
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
        if (mPadRCValue[i][trackIdx] > 0)
          RGBState.send(mMidiOut, posToNote(GRID_SIZE - PAD_REMOTE_CTRL.length + i, trackIdx), PLAY_COLOUR);
        else
          RGBState.send(mMidiOut, posToNote(GRID_SIZE - PAD_REMOTE_CTRL.length + i, trackIdx), RGBState.OFF);
      }
    } else if (trackIdx == GRID_SIZE) {
      for (int i = 0; i < FADER_LENGTH; i++)
        setPadCCColour(SCENE_1_CC - i * 10, m9thRCValue[0] >= 4 - i ? PLAY_COLOUR : RGBState.OFF);

      setPadCCColour(SCENE_6_CC, m9thRCValue[1] > 0 ? PLAY_COLOUR : RGBState.OFF);
      setPadCCColour(SCENE_7_CC, m9thRCValue[2] > 0 ? PLAY_COLOUR : RGBState.OFF);
    }
  }

  private void updateAllLED() {
    for (int i = 0; i < GRID_SIZE; i++)
      updateTrackLED(i);
  }

  private void updateTrackLED(int trackIdx) {
    if (trackIdx < GRID_SIZE) {
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
    }
    updateRemoteControlLED(trackIdx);
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
      if (velocity == 127)
        mPadRCValue[rc_idx][col] = 1;
      else
        mPadRCValue[rc_idx][col] = 0;
      mRemoteControls[col].getParameter(PAD_REMOTE_CTRL[rc_idx]).value().set(mPadRCValue[rc_idx][col], 2);
    }
  }

  private void processCC(int cc, int value) {
    if (value == 0)
      return;

    switch (cc) {
      case UP_CC:
        mSceneBank.scrollPageBackwards();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        break;
      case DOWN_CC:
        mSceneBank.scrollPageForwards();
        mSceneBank.getItemAt(0).showInEditor();
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
        int rcValue = cc / 10 - 4;
        m9thRCValue[0] = rcValue;
        mRemoteControls[GRID_SIZE].getParameter(0).value().set(rcValue, 5);
        break;
      case SCENE_6_CC:
        m9thRCValue[1] = m9thRCValue[1] > 0 ? 0 : 1;
        mRemoteControls[GRID_SIZE].getParameter(1).value().set(m9thRCValue[1], 2);
        break;
      case SCENE_7_CC:
        m9thRCValue[2] = m9thRCValue[2] > 0 ? 0 : 1;
        mRemoteControls[GRID_SIZE].getParameter(2).value().set(m9thRCValue[2], 2);
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

  private void onSysex(final String data) {
    // MMC Transport Controls:
    if (data.equals("f07f7f0605f7"))
      mTransport.rewind();
    else if (data.equals("f07f7f0604f7"))
      mTransport.fastForward();
    else if (data.equals("f07f7f0601f7"))
      mTransport.stop();
    else if (data.equals("f07f7f0602f7"))
      mTransport.play();
    else if (data.equals("f07f7f0606f7"))
      mTransport.record();
  }

  private MidiOut mMidiOut;
  private MidiIn mMidiIn;

  // remember remote control values for the RC controlled by the lower two rows of
  // pads
  private int[][] mPadRCValue = new int[2][GRID_SIZE];

  // remember remote control values for the 4 RCs controlled by scene buttons (9th
  // column)
  private int[] m9thRCValue = new int[3];
}
