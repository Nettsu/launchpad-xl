package com.novation;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;

import static com.novation.LaunchpadControlXLExtension.*;

public class LaunchControlXL {

  static final int UP_CC = 91;
  static final int DOWN_CC = 92;
  static final int LEFT_CC = 93;
  static final int RIGHT_CC = 94;

  static final int ARM_CC = 1;
  static final int SOLO_CC = 2;
  static final int MUTE_CC = 3;
  static final int DEV_CC = 4;
   
  static final int XL_COLOUR_OFF = 12;
  static final int XL_COLOUR_RED = 15;
  static final int XL_COLOUR_RED_LOW = 13;
  static final int XL_COLOUR_GREEN = 60;
  static final int XL_COLOUR_GREEN_LOW = 28;
  static final int XL_COLOUR_YELLOW = 62;
  static final int XL_COLOUR_AMBER = 63;
  static final int XL_COLOUR_AMBER_LOW = 29;

  static final int MUTE_COLOUR = XL_COLOUR_AMBER;
  static final int SOLO_COLOUR = XL_COLOUR_YELLOW;
  static final int ARM_COLOUR = XL_COLOUR_RED;
  static final int SELECT_COLOUR = XL_COLOUR_YELLOW;

  static final int[][] COLOR_GRADIENT = {
  // R, G
    {0, 0},
    {0, 1},
    {0, 2},
    {0, 3},
    {1, 3},
    {2, 3},
    {3, 3},
    {3, 2},
    {3, 1},
    {3, 0},
  };

  public enum TrackControl {
    SOLO,
    MUTE,
    ARM 
  }

  public void init() {
    mMidiIn = mControlMidiIn;
    mMidiOut = mControlMidiOut;
    mMidiIn.setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi(msg));
    
    mTrackCtrl = TrackControl.MUTE;
    setCCColour(MUTE_CC, SELECT_COLOUR);
    mDeviceMode = false;

    for (int i = 0; i < NUM_TRACKS; i++) {
      final int trackIdx = i;

      mTrackBank.getItemAt(i).solo().addValueObserver(solo -> {
        if (mTrackCtrl == TrackControl.SOLO)
          setPadColour(1, trackIdx, solo ? SOLO_COLOUR : XL_COLOUR_OFF);
      });

      mTrackBank.getItemAt(i).mute().addValueObserver(mute -> {
        if (mTrackCtrl == TrackControl.MUTE)
          setPadColour(1, trackIdx, mute ? MUTE_COLOUR : XL_COLOUR_OFF);
      });

      mTrackBank.getItemAt(i).arm().addValueObserver(arm -> {
        if (mTrackCtrl == TrackControl.ARM)
          setPadColour(1, trackIdx, arm ? ARM_COLOUR : XL_COLOUR_OFF);
      });

      mTrackBank.getItemAt(i).addIsSelectedInMixerObserver(selected -> {
        setPadColour(0, trackIdx, selected ? SELECT_COLOUR : XL_COLOUR_OFF);
      });
    }
  }

  public void exit() {
  }

  private void setPadColour(int row, int col, int colour) {
    // TODO
  }

  private void setCCColour(int cc, int colour) {
    mMidiOut.sendMidi(176, cc, colour);
  }

  private int msgToRow(ShortMidiMessage msg) {
    // TODO
    return 0;
  }

  private int msgToCol(ShortMidiMessage msg) {
    // TODO
    return 0;
  }

  private void processNote(int row, int col, boolean noteOn, int velocity) {
    if (row == 0) {
      mTrackBank.getItemAt(col).selectInMixer();
    }
    else if (row == 1) {
      switch (mTrackCtrl) {
        case SOLO:
          boolean solo_val = mTrackBank.getItemAt(col).solo().getAsBoolean();
          mTrackBank.getItemAt(col).solo().set(!solo_val);
          break;
        case MUTE:
          boolean mute_val = mTrackBank.getItemAt(col).mute().getAsBoolean();
          mTrackBank.getItemAt(col).mute().set(!mute_val);
          break;
        case ARM:
          boolean rec_val = mTrackBank.getItemAt(col).arm().getAsBoolean();
          mTrackBank.getItemAt(col).arm().set(!rec_val);
          break;
      }
    }
  }

  private void updateSideButtons() {
    setCCColour(SOLO_CC, mTrackCtrl == TrackControl.SOLO ? SELECT_COLOUR : XL_COLOUR_OFF);
    setCCColour(MUTE_CC, mTrackCtrl == TrackControl.MUTE ? SELECT_COLOUR : XL_COLOUR_OFF);
    setCCColour(ARM_CC, mTrackCtrl == TrackControl.ARM ? SELECT_COLOUR : XL_COLOUR_OFF);
    setCCColour(DEV_CC, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
  }

  private void updatePads() {
    for (int i = 0; i < 8; i++) {
      Track track = mTrackBank.getItemAt(i);
      int colour = XL_COLOUR_OFF;
      if (mTrackCtrl == TrackControl.SOLO && track.solo().getAsBoolean())
        colour = SOLO_COLOUR; 
      else if (mTrackCtrl == TrackControl.MUTE && track.mute().getAsBoolean())
        colour = MUTE_COLOUR; 
      else if (mTrackCtrl == TrackControl.ARM && track.arm().getAsBoolean())
        colour = ARM_COLOUR;
      setPadColour(1, i, colour);
    }
  }

  private boolean isAnalogCC(int cc) {
    // TODO
    return false;
  }

  private int rowFromCC(int cc) {
    // TOOD
    return 0;
  }

  private int colFromCC(int cc) {
    // TODO
    return 0;
  }

  private void processContinious(int cc, int value) {
    int row = rowFromCC(cc);
    int col = colFromCC(cc);

    if (row == 2 && mDeviceMode) {
      mEditorRemoteControls.getParameter(col).set(value, 128);
    }
    else if (col == 7) {
      switch (row) {
        case 0:
          mSendRemoteControls.getParameter(2).set(value, 128);
          break;
        case 1:
          mSendRemoteControls.getParameter(1).set(value, 128);
          break;
        case 2:
          mSendRemoteControls.getParameter(0).set(value, 128);
          break;
        case 3:
          mSendBank.getItemAt(0).volume().set(value, 128);
          break;
      }
    }
    else {
      switch (row) {
        case 0:
          mTrackBank.getItemAt(col).sendBank().getItemAt(row).set(value, 128);
          break;
        case 1:
          mTrackBank.getItemAt(col).sendBank().getItemAt(row).set(value, 128);
          break;
        case 2:
          mRemoteControls[col].getParameter(0).set(value, 128);
          break;
        case 3:
          mTrackBank.getItemAt(col).volume().set(value, 128);
          break;
      }
    }
  }

  private void processButton(int cc, int value) {
    switch (cc) {
      case UP_CC:
        mTransport.tempo().incRaw(1);
        break;
      case DOWN_CC:
        mTransport.tempo().incRaw(-1);
        break;
      case LEFT_CC:
        if (mDeviceMode) mEditorRemoteControls.selectNextPage(true);
        else mSendBank.scrollBackwards();
        break;
      case RIGHT_CC:
        if (mDeviceMode) mEditorRemoteControls.selectPreviousPage(true);
        else mSendBank.scrollForwards();
        break;
      case SOLO_CC:
        mTrackCtrl = TrackControl.SOLO;
        updateSideButtons();
        updatePads();
        break;
      case MUTE_CC:
        mTrackCtrl = TrackControl.MUTE;
        updateSideButtons();
        updatePads();
        break;
      case ARM_CC:
        mTrackCtrl = TrackControl.ARM;
        updateSideButtons();
        updatePads();
        break;
      case DEV_CC:
        mDeviceMode = !mDeviceMode;
        updateSideButtons();
        break;
    }
  }

  private void processCC(int cc, int value) {
    if (isAnalogCC(cc))
      processContinious(cc, value);
    else if (value == 127)
      processButton(cc, value);
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

  private boolean mDeviceMode;
  private TrackControl mTrackCtrl;
}
