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
      mSendBank = mHost.createEffectTrackBank(1, NUM_SCENES);

      mTrackBank.setShouldShowClipLauncherFeedback(true);
      mSendBank.setShouldShowClipLauncherFeedback(true);

      mPadMidiIn = mHost.getMidiInPort(0);
      mPadMidiOut = mHost.getMidiOutPort(0);

      mControlMidiIn = mHost.getMidiInPort(1);
      mControlMidiOut = mHost.getMidiOutPort(1);

      mRemoteControls = new CursorRemoteControlsPage[NUM_TRACKS];
      mDeviceBank = new DeviceBank[NUM_TRACKS];
      mEditorRemoteControls = mHost.createCursorTrack(3, NUM_SCENES).createCursorDevice().createCursorRemoteControlsPage(8);
      mSendRemoteControls = mSendBank.getItemAt(0).createCursorDevice("SendDevice").createCursorRemoteControlsPage(8);

      for (int col = 0; col < NUM_TRACKS; col++) {
         final int trackIdx = col;
         final Track track = mTrackBank.getItemAt(trackIdx);
         
         mDeviceBank[col] = track.createDeviceBank(1);
         mDeviceBank[col].getDevice(0).position().addValueObserver(pos -> { 
            mDeviceBank[trackIdx].scrollIntoView(0);
         });
         mRemoteControls[col] = mDeviceBank[col].getDevice(0).createCursorRemoteControlsPage(8);

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

      mLaunchpad = new Launchpad();
      mLaunchpad.init();

      mLaunchControl = new LaunchControlXL();
      mLaunchControl.init();

      mHost.showPopupNotification("Launchpad Control XL Initialized");
   }

   @Override
   public void exit() {
      mLaunchpad.exit();
      mHost.showPopupNotification("Launchpad Control XL Exited");
   }

   @Override
   public void flush() {
   }

   public static Launchpad mLaunchpad;
   public static LaunchControlXL mLaunchControl;

   public static ControllerHost mHost;
   public static Transport mTransport;

   public static MidiOut mPadMidiOut;
   public static MidiIn mPadMidiIn;

   public static MidiOut mControlMidiOut;
   public static MidiIn mControlMidiIn;

   public static TrackBank mTrackBank;
   public static SceneBank mSceneBank;
   public static TrackBank mSendBank;
   
   public static CursorRemoteControlsPage[] mRemoteControls;
   public static DeviceBank[] mDeviceBank;
   public static CursorRemoteControlsPage mEditorRemoteControls;
   public static CursorRemoteControlsPage mSendRemoteControls;

   public static boolean mShift;
}