package com.novation;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;

public class LaunchpadControlXLExtension extends ControllerExtension
{
   static final int NUM_SCENES = 5;
   static final int NUM_TRACKS = 9;

   protected LaunchpadControlXLExtension(final LaunchpadControlXLExtensionDefinition definition, final ControllerHost host) {
      super(definition, host);
   }

   @Override
   public void init() {
      mHost = getHost();
      mTransport = mHost.createTransport();
      mTrackBank = mHost.createTrackBank(NUM_TRACKS, 8, NUM_SCENES);
      mSceneBank = mTrackBank.sceneBank();

      mPadMidiIn = mHost.getMidiInPort(0);
      mPadMidiOut = mHost.getMidiOutPort(0);

      // mControlMidiIn = mHost.getMidiInPort(1);
      // mControlMidiOut = mHost.getMidiOutPort(1);

      mRemoteControls = new CursorRemoteControlsPage[NUM_TRACKS];
      for (int i = 0; i < NUM_TRACKS; i++)
         mRemoteControls[i] = mTrackBank.getItemAt(i).createDeviceBank(1).getDevice(0).createCursorRemoteControlsPage(8);

      for (int col = 0; col < NUM_TRACKS; col++) {
         final int trackIdx = col;
         final Track track = mTrackBank.getItemAt(trackIdx);
         track.isStopped().markInterested();
         track.isQueuedForStop().markInterested();
         track.color().markInterested();
         final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
         for (int row = 0; row < NUM_SCENES; row++) {
            ClipLauncherSlot slot = slotBank.getItemAt(row);
            slot.color().markInterested();
            slot.isPlaying().markInterested();
            slot.hasContent().markInterested();
            slot.isPlaybackQueued().markInterested();
            slot.isStopQueued().markInterested();
            slot.isRecordingQueued().markInterested();
            slot.isRecording().markInterested();
         }
      }

      launchpad = new Launchpad();
      launchpad.init();

      mHost.showPopupNotification("Launchpad Control XL Initialized");
   }

   @Override
   public void exit() {
      launchpad.exit();
      mHost.showPopupNotification("Launchpad Control XL Exited");
   }

   @Override
   public void flush() {
   }

   public static Launchpad launchpad;

   public static ControllerHost mHost;
   public static Transport mTransport;

   public static MidiOut mPadMidiOut;
   public static MidiIn mPadMidiIn;

   // private MidiOut mControlMidiOut;
   // private MidiIn mControlMidiIn;

   public static TrackBank mTrackBank;
   public static SceneBank mSceneBank;
   
   // private CursorTrack mCursorTrack;
   // private DeviceBank mDeviceBank;
   // private CursorDevice mCursorDevice;
   
   public static CursorRemoteControlsPage[] mRemoteControls;
}
