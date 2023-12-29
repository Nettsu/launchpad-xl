package com.novation;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class LaunchpadControlXLExtensionDefinition extends ControllerExtensionDefinition {
   private static final UUID DRIVER_ID = UUID.fromString("ccbfffb2-32c6-4840-9f62-ff2b6081ca16");
   
   public LaunchpadControlXLExtensionDefinition() {
   }

   @Override
   public String getName() {
      return "Launchpad Control XL";
   }
   
   @Override
   public String getAuthor() {
      return "Netsu";
   }

   @Override
   public String getVersion() {
      return "0.2";
   }

   @Override
   public UUID getId() {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor() {
      return "Novation";
   }
   
   @Override
   public String getHardwareModel() {
      return "Launchpad Control XL";
   }

   @Override
   public int getRequiredAPIVersion() {
      return 18;
   }

   @Override
   public int getNumMidiInPorts() {
      return 2;
   }

   @Override
   public int getNumMidiOutPorts() {
      return 2;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType) {
      if (platformType == PlatformType.WINDOWS) {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         list.add(new String[]{"Input Launchpad", "Input Launch Control XL"}, new String[]{"Output Launchpad", "Output LaunchControl XL"});
      }
      else if (platformType == PlatformType.MAC) {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         list.add(new String[]{"Input Launchpad", "Input Launch Control XL"}, new String[]{"Output Launchpad", "Output LaunchControl XL"});
      }
      else if (platformType == PlatformType.LINUX) {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         list.add(new String[]{"Input Launchpad", "Input Launch Control XL"}, new String[]{"Output Launchpad", "Output LaunchControl XL"});
      }
   }

   @Override
   public LaunchpadControlXLExtension createInstance(final ControllerHost host) {
      return new LaunchpadControlXLExtension(this, host);
   }
}
