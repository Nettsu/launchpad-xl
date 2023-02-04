package com.novation;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;

public class LaunchpadControlXLExtension extends ControllerExtension
{
   static final int NUM_SCENES = 5;
   static final int NUM_TRACKS = 8;
   static final int NUM_SENDS = 8;

   protected LaunchpadControlXLExtension(final LaunchpadControlXLExtensionDefinition definition, final ControllerHost host) {
      super(definition, host);
   }

   @Override
   public void init() {
      mHost = getHost();
      mTransport = mHost.createTransport();
      mTrackBank = mHost.createTrackBank(NUM_TRACKS, NUM_SENDS, NUM_SCENES);
      mSendBank = mHost.createEffectTrackBank(1, NUM_SCENES);

      mSceneBank = mTrackBank.sceneBank();
      mSceneBank.scrollPosition().addValueObserver(idx -> {
         mSendBank.sceneBank().scrollPosition().set(idx);
      });

      mTrackBank.setShouldShowClipLauncherFeedback(true);
      mSendBank.setShouldShowClipLauncherFeedback(true);

      mPadMidiIn = mHost.getMidiInPort(0);
      mPadMidiOut = mHost.getMidiOutPort(0);

      mControlMidiIn = mHost.getMidiInPort(1);
      mControlMidiOut = mHost.getMidiOutPort(1);

      mRemoteControls = new CursorRemoteControlsPage[NUM_TRACKS];
      mDeviceBank = new DeviceBank[NUM_TRACKS];
      mEditorRemoteControls = mHost.createCursorTrack(3, NUM_SCENES).createCursorDevice().createCursorRemoteControlsPage(8);
      mUserControls = mHost.createUserControls(7);
      
      mCursorClip = mHost.createLauncherCursorClip(1, 1);
      mCursorClip.clipLauncherSlot().isSelected().markInterested();
      mCursorClip.clipLauncherSlot().sceneIndex().addValueObserver(idx -> {
         if (mCursorClip.clipLauncherSlot().isSelected().get())
            mSceneBank.scrollPosition().set((idx / NUM_SCENES) * NUM_SCENES);
      });

      mSendDeviceBank = mSendBank.getItemAt(0).createDeviceBank(1);
      mSendDeviceBank.getDevice(0).position().addValueObserver(pos -> {
         mSendDeviceBank.scrollIntoView(0);
      });
      mSendRemoteControls = mSendDeviceBank.getDevice(0).createCursorRemoteControlsPage(8);

      for (int i = 0; i < 8; i++) {
         mSendRemoteControls.getParameter(i).markInterested();
         mEditorRemoteControls.getParameter(i).markInterested();
      }

      for (int col = 0; col < NUM_TRACKS; col++) {
         final int trackIdx = col;
         final Track track = mTrackBank.getItemAt(trackIdx);
         
         mDeviceBank[col] = track.createDeviceBank(1);
         mDeviceBank[col].getDevice(0).position().addValueObserver(pos -> { 
            mDeviceBank[trackIdx].scrollIntoView(0);
         });
         mRemoteControls[col] = mDeviceBank[col].getDevice(0).createCursorRemoteControlsPage(8);

         for (int i = 0; i < 8; i++) mRemoteControls[col].getParameter(i).markInterested();
         for (int i = 0; i < NUM_SENDS; i++) track.sendBank().getItemAt(i).markInterested();

         track.isStopped().markInterested();
         track.isQueuedForStop().markInterested();
         track.color().markInterested();
         track.mute().markInterested();
         track.solo().markInterested();
         track.arm().markInterested();
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

      mSendBank.getItemAt(0).mute().markInterested();
      mSendBank.getItemAt(0).solo().markInterested();
      mSendBank.getItemAt(0).arm().markInterested();

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

   public Launchpad mLaunchpad;
   public LaunchControlXL mLaunchControl;

   public static ControllerHost mHost;
   public static Transport mTransport;

   public static MidiOut mPadMidiOut;
   public static MidiIn mPadMidiIn;

   public static MidiOut mControlMidiOut;
   public static MidiIn mControlMidiIn;

   public static TrackBank mTrackBank;
   public static SceneBank mSceneBank;
   public static TrackBank mSendBank;
   
   public static UserControlBank mUserControls;
   public static CursorRemoteControlsPage[] mRemoteControls;
   public static DeviceBank[] mDeviceBank;
   public static DeviceBank mSendDeviceBank;
   public static CursorRemoteControlsPage mEditorRemoteControls;
   public static CursorRemoteControlsPage mSendRemoteControls;
   public static Clip mCursorClip;

   public static boolean mShift;
}