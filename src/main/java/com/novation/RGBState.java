package com.novation;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.MidiOut;

import static com.novation.LaunchpadControlXLExtension.*;

public class RGBState extends InternalHardwareLightState {
   public static final RGBState OFF = new RGBState(0);
   public static final RGBState OFF_BLINK = new RGBState(0, 1);
   public static final RGBState DARKGREY = new RGBState(1);
   public static final RGBState DARKGREY_BLINK = new RGBState(1, 1);
   public static final RGBState GREY = new RGBState(2);
   public static final RGBState WHITE = new RGBState(3);
   public static final RGBState WHITE_BLINK = new RGBState(3, 1);
   public static final RGBState WHITE_PULSE = new RGBState(3, 2);
   public static final RGBState DARKRED = new RGBState(7);
   public static final RGBState DARKRED_BLINK = new RGBState(7, 1);
   public static final RGBState BLUE = new RGBState(79);
   public static final RGBState YELLOW = new RGBState(13);
   public static final RGBState YELLOW_BLINK = new RGBState(13, 1);
   public static final RGBState DARKYELLOW = new RGBState(15);
   public static final RGBState PURPLE = new RGBState(94);
   public static final RGBState ORANGE = new RGBState(9);
   public static final RGBState DARKORANGE = new RGBState(11);
   public static final RGBState RED = new RGBState(72);
   public static final RGBState RED_PULS = new RGBState(72, 2);
   public static final RGBState RED_BLINK = new RGBState(5, 1); // 72 instead of 5
   public static final RGBState GREEN = new RGBState(122);
   public static final RGBState GREEN_PULS = new RGBState(21, 2);
   public static final RGBState GREEN_BLINK = new RGBState(21, 1);

   // TO DO find problematic colors!

   public RGBState(int n) {
      mNumber = n;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[n][1]);
   }

   public RGBState(int n, int t) {
      mNumber = n;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[n][1]);
      mType = t;
   }

   public RGBState(Color c) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < 128; i++) {
         // err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i][1]));
         err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][1]));
         if (err < bestErr) {
            best = i;
            bestErr = err;
         }
      }
      mNumber = best;
      mHost.println("best: " + best);
      mHost.println("input: " + c.toHex());
      mHost.println("chosen hex: " + Color.fromHex((String)RGB_HEX_COLOR_TABLE[best][1]).toHex());

      //mColor = Color.fromHex(Integer.toHexString(RGB_COLOR_TABLE[best][1]));
      mColor = c; // When sending via Sysex
   }

   public RGBState(Color c, int t) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < 128; i++) {
         // err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i][1]));
         err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][1]));
         if (err < bestErr) {
            best = i;
            bestErr = err;
         }
      }
      mNumber = best;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[best][1]);
      mType = t;
   }

   @Override
   public HardwareLightVisualState getVisualState() {
      return HardwareLightVisualState.createForColor(this.getColor());
   }

   public int getMessage() {
      return mNumber;
   }

   public Color getColor() {
      return mColor;
   }

   private Object[][] RGB_HEX_COLOR_TABLE = {
      {0, "000000"},
      {1, "1e1e1e"},
      {2, "7f7f7f"},
      {3, "ffffff"},
      {4, "ff4c4c"},
      {5, "ff0000"},
      {6, "590000"},
      {7, "190000"},
      {8, "FFF3D5"},
      {9, "ff5400"},
      {10, "591d00"},
      {11, "271b00"},
      {12, "FFEEA1"},
      {13, "FFFF61"},
      {14, "595900"},
      {15, "191900"},
      {16, "DCFEA0"},
      {17, "54ff00"},
      {18, "A1DD61"},
      {19, "142b00"},
      {20, "4cff4c"},
      {21, "61FF61"},
      {22, "005900"},
      {23, "001900"},
      {24, "4cff5e"},
      {25, "00ff19"},
      {26, "00590d"},
      {27, "001902"},
      {28, "4cff88"},
      {29, "00ff55"},
      {30, "EEFC61"},
      {31, "001f12"},
      {32, "6ffeff"},
      {33, "4dfeff"},
      {34, "42bbbc"},
      {35, "61B396"},
      {36, "C2F3FF"},
      {37, "5ad9ff"},
      {38, "33c6ff"},
      {39, "2c91bc"},
      {40, "C2DDFF"},
      {41, "45affc"},
      {42, "1988f9"},
      {43, "1564b7"},
      {44, "4c4cff"},
      {45, "1953ff"},
      {46, "153dbc"},
      {0, "6161B3"},
      {0, "874cff"},
      {0, "5400ff"},
      {0, "190064"},
      {0, "0f0030"},
      {0, "ff4cff"},
      {0, "ff00ff"},
      {0, "590059"},
      {0, "190019"},
      {0, "ff4c87"},
      {0, "ff0054"},
      {0, "59001d"},
      {0, "220013"},
      {0, "FF7661"},
      {0, "993500"},
      {0, "795100"},
      {0, "436400"},
      {0, "033900"},
      {0, "005735"},
      {0, "458bff"},
      {0, "0000ff"},
      {0, "00454f"},
      {0, "2500cc"},
      {0, "7f7f7f"},
      {0, "202020"},
      {0, "ff0000"},
      {0, "bdff2d"},
      {0, "afed06"},
      {0, "64ff09"},
      {0, "108b00"},
      {0, "00ff87"},
      {0, "61E9FF"},
      {0, "002aff"},
      {0, "3f00ff"},
      {0, "7a00ff"},
      {0, "b21a7d"},
      {0, "402100"},
      {0, "ff4a00"},
      {0, "88e106"},
      {0, "72ff15"},
      {0, "00ff00"},
      {0, "3bff26"},
      {0, "59ff71"},
      {0, "38ffcc"},
      {0, "CCE4FF"},
      {0, "A1C2F6"},
      {0, "877fe9"},
      {0, "d31dff"},
      {0, "ff005d"},
      {0, "ff7f00"},
      {0, "b9b000"},
      {0, "90ff00"},
      {0, "835d07"},
      {0, "392b00"},
      {0, "144c10"},
      {0, "0d5038"},
      {0, "15152a"},
      {0, "16205a"},
      {0, "693c1c"},
      {0, "a8000a"},
      {0, "F9B3A1"},
      {0, "d86a1c"},
      {0, "FFF38C"},
      {0, "E9F9A1"},
      {0, "67b50f"},
      {0, "1e1e30"},
      {0, "dcff6b"},
      {0, "80ffbd"},
      {0, "9a99ff"},
      {0, "8e66ff"},
      {0, "404040"},
      {0, "757575"},
      {0, "e0ffff"},
      {0, "a00000"},
      {0, "350000"},
      {0, "1ad000"},
      {0, "074200"},
      {0, "b9b000"},
      {0, "3f3100"},
      {0, "b35f00"},
      {0, "4b1502"}   
   };

   public int getType() {
      return mType;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final RGBState other = (RGBState) obj;
      return mNumber == other.mNumber && mType == other.mType;
   }

   public static void send(MidiOut m, int i, RGBState s) {
      // m.sendMidi(0xB0 + 0, i, 0);  
      // m.sendMidi(0x90 + 0, i, 0);   
      m.sendMidi(0xB0 + s.mType, i, s.mNumber);  
      m.sendMidi(0x90 + s.mType, i, s.mNumber);   
   }  

   public static void sendSys(final MidiOut m, int i, RGBState s) {
      send(m, i, s);
   }

   private int mNumber;
   private Color mColor;
   private int mType = 0;

   public static void RGBtoHSV(final double r, final double g, final double b, final double[] hsv) {
      assert r >= 0 && r <= 1;
      assert g >= 0 && g <= 1;
      assert b >= 0 && b <= 1;
      assert hsv != null;
      assert hsv.length == 3;

      double min, max, delta;
      double h, s, v;

      min = Math.min(Math.min(r, g), b);
      max = Math.max(Math.max(r, g), b);
      v = max; // v

      delta = max - min;

      if (max != 0) {
         s = delta / max; // s
      } else {
         // r = g = b = 0 // s = 0, v is undefined
         s = 0;
         h = 0;
         assert h >= 0 && h <= 360;
         assert s >= 0 && s <= 1;
         assert v >= 0 && v <= 1;

         hsv[0] = h;
         hsv[1] = s;
         hsv[2] = v;
         return;
      }

      if (delta == 0) {
         h = 0;
      } else {
         if (r == max) {
            h = (g - b) / delta; // between yellow & magenta
         } else if (g == max) {
            h = 2 + (b - r) / delta; // between cyan & yellow
         } else {
            h = 4 + (r - g) / delta; // between magenta & cyan
         }
      }

      h *= 60; // degrees
      if (h < 0) {
         h += 360;
      }

      assert h >= 0 && h <= 360;
      assert s >= 0 && s <= 1;
      assert v >= 0 && v <= 1;

      hsv[0] = h;
      hsv[1] = s;
      hsv[2] = v;
   }

   private static double computeHsvError(Color c, final Color color) {
      double[] hsv = new double[3];
      RGBtoHSV(c.getRed(), c.getGreen(), c.getBlue(), hsv);
      double[] hsvRef = new double[3];
      RGBtoHSV(color.getRed(), color.getGreen(), color.getBlue(), hsvRef);

      double hueError = (hsv[0] - hsvRef[0]) / 30;
      double sError = (hsv[1] - hsvRef[1]) * 1.6f;
      final double vScale = 1f;
      double vError = (vScale * hsv[2] - hsvRef[2]) / 40;

      final double error = hueError * hueError + vError * vError + sError * sError;

      return error;
   }

   private static double RGBError(Color c, Color r) {
      double RedMean = 0.5*(c.getRed255() + r.getRed255());
      double RedErr = (2 + RedMean/256) * Math.pow(c.getRed() - r.getRed(), 2);
      double GreenErr = 4 * Math.pow(c.getGreen() - r.getGreen(), 2);
      double BlueErr = (2  + (255 - RedMean)/256) * Math.pow(c.getBlue() - r.getBlue(), 2);
      return Math.sqrt(RedErr + GreenErr + BlueErr);
   }

}