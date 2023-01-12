package com.novation;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;

import static com.novation.LaunchpadControlXLExtension.*;
import static com.novation.LaunchControlXL.KnobType.*;
import static com.novation.LaunchControlXL.ChannelType.*;

public class LaunchControlXL {

  static final int UP_CC = 104;
  static final int DOWN_CC = 105;
  static final int LEFT_CC = 106;
  static final int RIGHT_CC = 107;

  static final int DEV_NOTE = 105;
  static final int MUTE_NOTE = 106;
  static final int SOLO_NOTE = 107;
  static final int ARM_NOTE = 108;

  static final int SEND_A_CC = 13;
  static final int SEND_B_CC = 29;
  static final int DEVICE_CC = 49;
  static final int VOLUME_CC = 77;
   
  static final int XL_COLOUR_OFF = 12;
  static final int XL_COLOUR_RED = 15;
  static final int XL_COLOUR_RED_LOW = 13;
  static final int XL_COLOUR_GREEN = 60;
  static final int XL_COLOUR_GREEN_LOW = 28;
  static final int XL_COLOUR_YELLOW = 63;
  static final int XL_COLOUR_AMBER = 47;
  static final int XL_COLOUR_ORANGE = 31;
  static final int XL_COLOUR_AMBER_LOW = 29;

  static final int MUTE_COLOUR = XL_COLOUR_ORANGE;
  static final int SOLO_COLOUR = XL_COLOUR_YELLOW;
  static final int ARM_COLOUR = XL_COLOUR_RED;
  static final int SELECT_COLOUR = XL_COLOUR_GREEN_LOW;

  static final int[] FOCUS_BUTTONS = {41, 42, 43, 44, 57, 58, 59, 60};
  static final int[] CONTROL_BUTTONS = {73, 74, 75, 76, 89, 90, 91, 92};

  public enum KnobType {
    SEND_KNOB,
    RC_KNOB,
  }

  public enum ChannelType {
    TRACK_CH,
    SEND_CH
  }

  static final ChannelType[] CHANNEL_TYPES = {
    TRACK_CH, TRACK_CH, TRACK_CH, TRACK_CH, TRACK_CH, TRACK_CH, TRACK_CH, SEND_CH
  };

  static final KnobType[][] KNOB_TYPES = {
    {SEND_KNOB, SEND_KNOB, SEND_KNOB, SEND_KNOB, SEND_KNOB, SEND_KNOB, SEND_KNOB, RC_KNOB},
    {SEND_KNOB, SEND_KNOB, SEND_KNOB, SEND_KNOB, RC_KNOB,   SEND_KNOB, SEND_KNOB, RC_KNOB},
    {RC_KNOB,   RC_KNOB,   RC_KNOB,   RC_KNOB,   RC_KNOB,   RC_KNOB,   RC_KNOB,   RC_KNOB}
  };

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

  static final int GRADIENT_LEN = COLOR_GRADIENT.length;

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
    updatePadLEDs();
    updateSideButtonLEDs();
    updateKnobLEDs();
    mDeviceMode = false;

    for (int i = 0; i < 8; i++) {
      final int track_idx = i;
      Track track;
      CursorRemoteControlsPage rc_page;
      if (CHANNEL_TYPES[i] == SEND_CH) {
        track = mSendBank.getItemAt(0);
        rc_page = mSendRemoteControls;
      }
      else {
        track = mTrackBank.getItemAt(i);
        rc_page = mRemoteControls[track_idx];
      } 

      track.solo().addValueObserver(solo -> {
        if (mTrackCtrl == TrackControl.SOLO)
          setColour(4, track_idx, solo ? SOLO_COLOUR : XL_COLOUR_OFF);
      });

      track.mute().addValueObserver(mute -> {
        if (mTrackCtrl == TrackControl.MUTE)
          setColour(4, track_idx, mute ? MUTE_COLOUR : XL_COLOUR_OFF);
      });

      track.arm().addValueObserver(arm -> {
        if (mTrackCtrl == TrackControl.ARM)
          setColour(4, track_idx, arm ? ARM_COLOUR : XL_COLOUR_OFF);
      });

      track.addIsSelectedInMixerObserver(selected -> {
        setColour(3, track_idx, selected ? SELECT_COLOUR : XL_COLOUR_OFF);
      });

      for (int knob = 0; knob < 3; knob++) {
        final int knob_idx = knob;
        if (KNOB_TYPES[knob][track_idx] == RC_KNOB) {
            rc_page.getParameter(2 - knob).value().addValueObserver(GRADIENT_LEN, value -> {
            setColour(knob_idx, track_idx, colourFromValue(value));
          });
        }
        else if (KNOB_TYPES[knob][track_idx] == SEND_KNOB) {
          track.sendBank().getItemAt(knob).value().addValueObserver(GRADIENT_LEN, value -> {
            setColour(knob_idx, track_idx, colourFromValue(value));
          });
        }
      }
    }

    for (int param = 0; param < 8; param++) {
      final int rc_idx = param;
      mEditorRemoteControls.getParameter(param).value().addValueObserver(GRADIENT_LEN, value -> {
        if (!mDeviceMode) return;
        setColour(2, rc_idx, colourFromValue(value));
      });
    }
  }

  public void exit() {
  }

  private int colourFromValue(int value) {
    int red = COLOR_GRADIENT[value][0];
    int green = COLOR_GRADIENT[value][1];
    return 12 + red + 16 * green;
  }

  private void setColour(int row, int col, int colour) {
    int index = row * 8 + col;
    String idx = String.format("%02X", index);
    String c = String.format("%02X", colour);
    mMidiOut.sendSysex("F0 00 20 29 02 11 78 08 " + idx + " " + c + " F7");
  }

  private void setColour(int index, int colour) {
    String idx = String.format("%02X", index);
    String c = String.format("%02X", colour);
    mMidiOut.sendSysex("F0 00 20 29 02 11 78 08 " + idx + " " + c + " F7");
  }

  private void selectTrack(int idx) {
    if (CHANNEL_TYPES[idx] == SEND_CH) mSendBank.getItemAt(0).selectInEditor();
    else mTrackBank.getItemAt(idx).selectInEditor();
  }

  private void controlButtonPress(int idx) {
    Track track;
    if (CHANNEL_TYPES[idx] == SEND_CH)
      track = mSendBank.getItemAt(0);
    else
      track = mTrackBank.getItemAt(idx);

    switch (mTrackCtrl) {
      case SOLO:
        boolean solo_val = track.solo().getAsBoolean();
        track.solo().set(!solo_val);
        break;
      case MUTE:
        boolean mute_val = track.mute().getAsBoolean();
        track.mute().set(!mute_val);
        break;
      case ARM:
        boolean rec_val = track.arm().getAsBoolean();
        track.arm().set(!rec_val);
        break;
    }
  }

  private void processNote(int note, int velocity) {
    if (velocity == 0) return;

    for (int i = 0; i < 8; i++) {
      if (note == FOCUS_BUTTONS[i]) {
        selectTrack(i);
        return;
      }
      else if (note == CONTROL_BUTTONS[i]) {
        controlButtonPress(i);
        return;
      }
    }

    switch(note) {
      case SOLO_NOTE:
        mTrackCtrl = TrackControl.SOLO;
        updateSideButtonLEDs();
        updatePadLEDs();
        break;
      case MUTE_NOTE:
        mTrackCtrl = TrackControl.MUTE;
        updateSideButtonLEDs();
        updatePadLEDs();
        break;
      case ARM_NOTE:
        mTrackCtrl = TrackControl.ARM;
        updateSideButtonLEDs();
        updatePadLEDs();
        break;
      case DEV_NOTE:
        mDeviceMode = !mDeviceMode;
        updateSideButtonLEDs();
        updateKnobLEDs();
        break;
    }
  }

  private void updateSideButtonLEDs() {
    setColour(40, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(41, mTrackCtrl == TrackControl.MUTE ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(42, mTrackCtrl == TrackControl.SOLO ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(43, mTrackCtrl == TrackControl.ARM ? SELECT_COLOUR : XL_COLOUR_OFF);

    setColour(44, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(45, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(46, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
    setColour(47, mDeviceMode ? SELECT_COLOUR : XL_COLOUR_OFF);
  }

  private void updatePadLEDs() {
    for (int i = 0; i < 8; i++) {      
      Track track;
      if (CHANNEL_TYPES[i] == SEND_CH) track = mSendBank.getItemAt(0);
      else track = mTrackBank.getItemAt(i);
      
      int colour = XL_COLOUR_OFF;
      if (mTrackCtrl == TrackControl.SOLO && track.solo().getAsBoolean())
        colour = SOLO_COLOUR; 
      else if (mTrackCtrl == TrackControl.MUTE && track.mute().getAsBoolean())
        colour = MUTE_COLOUR; 
      else if (mTrackCtrl == TrackControl.ARM && track.arm().getAsBoolean())
        colour = ARM_COLOUR;
      setColour(4, i, colour);
    }
  }

  private void updateKnobLEDs() {
    for (int track = 0; track < 8; track++) {
      for (int knob = 0; knob < 3; knob++) {
        double value = 0;
        if (mDeviceMode && knob == 2)
          value = mEditorRemoteControls.getParameter(track).get();
        else if (KNOB_TYPES[knob][track] == SEND_KNOB) {
          if (CHANNEL_TYPES[track] == SEND_CH)
            value = mSendBank.getItemAt(0).sendBank().getItemAt(knob).get();
          else
            value = mTrackBank.getItemAt(track).sendBank().getItemAt(knob).get();
        }
        else if (KNOB_TYPES[knob][track] == RC_KNOB) {
          if (CHANNEL_TYPES[track] == SEND_CH)
            value = mSendRemoteControls.getParameter(2 - knob).get();
          else
            value = mRemoteControls[track].getParameter(2 - knob).get();
        }

        value *= GRADIENT_LEN - 1;
        setColour(knob, track, colourFromValue((int)value));
      }
    }
  }

  public class CCData {
    boolean isAnalog;
    int row;
    int col;

    public CCData(boolean a, int r, int c) {
      this.isAnalog = a;
      this.row = r;
      this.col = c;
    }
  }

  private CCData getCCData(int cc) {
    if (cc >= SEND_A_CC && cc <= SEND_A_CC + 7) return new CCData(true, 0, cc - SEND_A_CC);
    if (cc >= SEND_B_CC && cc <= SEND_B_CC + 7) return new CCData(true, 1, cc - SEND_B_CC);
    if (cc >= DEVICE_CC && cc <= DEVICE_CC + 7) return new CCData(true, 2, cc - DEVICE_CC);
    if (cc >= VOLUME_CC && cc <= VOLUME_CC + 7) return new CCData(true, 3, cc - VOLUME_CC);
    return new CCData(false, 0, 0);
  }

  private void processContinious(int row, int col, int value) {
    if (row == 2 && mDeviceMode) {
      mEditorRemoteControls.getParameter(col).set(value, 128);
    }
    else if (CHANNEL_TYPES[col] == SEND_CH) {
      if (row == 3)
        mSendBank.getItemAt(0).volume().set(value, 161);
      else
        mSendRemoteControls.getParameter(2 - row).set(value, 128);
    }
    else {
      if (row == 3)
        mTrackBank.getItemAt(col).volume().set(value, 161);
      else if (KNOB_TYPES[row][col] == SEND_KNOB)
        mTrackBank.getItemAt(col).sendBank().getItemAt(row).set(value, 128);
      else if (KNOB_TYPES[row][col] == RC_KNOB)
        mRemoteControls[col].getParameter(2 - row).set(value, 128);
    }
  }

  private void processCCButton(int cc, int value) {
    switch (cc) {
      case UP_CC:
        mTransport.tempo().incRaw(1);
        break;
      case DOWN_CC:
        mTransport.tempo().incRaw(-1);
        break;
      case LEFT_CC:
        if (mDeviceMode) mEditorRemoteControls.selectNextPage(true);
        else {
          mSendBank.scrollBackwards();
          mSendBank.getItemAt(0).selectInMixer();
        }
        break;
      case RIGHT_CC:
        if (mDeviceMode) mEditorRemoteControls.selectPreviousPage(true);
        else {
          mSendBank.scrollForwards();
          mSendBank.getItemAt(0).selectInMixer();
        }
        break;
    }
  }

  private void processCC(int cc, int value) {
    CCData data = getCCData(cc);
    if (data.isAnalog)
      processContinious(data.row, data.col, value);
    else if (value == 127)
      processCCButton(cc, value);
  }

  private void onMidi(ShortMidiMessage msg) {
    final int code = msg.getStatusByte() & 0xF0;

    // mHost.println("midi: " + msg.getStatusByte() + ", " + msg.getData1() + ", " + msg.getData2());

    switch (code) {
      // Note on/off
      case 0x80:
      case 0x90:
        processNote(msg.getData1(), msg.getData2());
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
