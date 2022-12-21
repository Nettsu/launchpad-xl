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
   public static final RGBState GREY = new RGBState(2);
   public static final RGBState WHITE = new RGBState(3);
   public static final RGBState WHITE_BLINK = new RGBState(3, 1);
   public static final RGBState DARKRED = new RGBState(121);
   public static final RGBState DARKRED_BLINK = new RGBState(121, 1);
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
   public static final RGBState TRACK_ARM = new RGBState(121);
   public static final RGBState GREEN = new RGBState(122);
   public static final RGBState GREEN_PULS = new RGBState(21, 2);
   public static final RGBState GREEN_BLINK = new RGBState(21, 1);

   // TO DO find problematic colors!

   public RGBState(int n) {
      mNumber = n;
      mColor = Color.fromHex(RGB_HEX_COLOR_TABLE[n]);
   }

   public RGBState(int n, int t) {
      mNumber = n;
      mColor = Color.fromHex(RGB_HEX_COLOR_TABLE[n]);
      mType = t;
   }

   public RGBState(Color c) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < 128; i++) {
         //err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i]));
         err = RGBError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i]));
         if (err < bestErr) {
            best = i;
            bestErr = err;
         }
      }
      mNumber = best;
      mHost.println("best: " + best);
      mHost.println("input: " + c.toHex());
      mHost.println("chosen hex: " + Color.fromHex(RGB_HEX_COLOR_TABLE[best]).toHex());

      //mColor = Color.fromHex(Integer.toHexString(RGB_COLOR_TABLE[best][1]));
      mColor = c; // When sending via Sysex
   }

   public RGBState(Color c, int t) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < 128; i++) {
         err = RGBError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i]));
         if (err < bestErr) {
            best = i;
            bestErr = err;
         }
      }
      mNumber = best;
      mColor = Color.fromHex(RGB_HEX_COLOR_TABLE[best]);
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



   private String[] RGB_HEX_COLOR_TABLE = {
      "000000",
      "1e1e1e",
      "7f7f7f",
      "ffffff",
      "ff4c4c",
      "ff0000",
      "590000",
      "190000",
      "FFF3D5",
      "ff5400",
      "591d00",
      "271b00",
      "ffff4c",
      "ffff00",
      "595900",
      "191900",
      "88ff4c",
      "54ff00",
      "1d5900",
      "142b00",
      "4cff4c",
      "00ff00",
      "005900",
      "001900",
      "4cff5e",
      "00ff19",
      "00590d",
      "001902",
      "4cff88",
      "00ff55",
      "EEFC61",
      "001f12",
      "4cffb7",
      "00ff99",
      "005935",
      "001912",
      "4cc3ff",
      "61EEFF",
      "004152",
      "001019",
      "4c88ff",
      "0055ff",
      "001d59",
      "000819",
      "4c4cff",
      "0000ff",
      "000059",
      "000019",
      "874cff",
      "5400ff",
      "190064",
      "0f0030",
      "ff4cff",
      "ff00ff",
      "590059",
      "190019",
      "ff4c87",
      "ff0054",
      "59001d",
      "220013",
      "ff1500",
      "993500",
      "795100",
      "436400",
      "033900",
      "005735",
      "618CD5",
      "0000ff",
      "00454f",
      "2500cc",
      "7f7f7f",
      "202020",
      "ff0000",
      "bdff2d",
      "afed06",
      "64ff09",
      "108b00",
      "00ff87",
      "00a9ff",
      "002aff",
      "3f00ff",
      "7a00ff",
      "b21a7d",
      "402100",
      "ff4a00",
      "88e106",
      "72ff15",
      "00ff00",
      "3bff26",
      "59ff71",
      "38ffcc",
      "5b8aff",
      "A1C2F6",
      "877fe9",
      "d31dff",
      "ff005d",
      "ff7f00",
      "b9b000",
      "90ff00",
      "835d07",
      "392b00",
      "144c10",
      "0d5038",
      "15152a",
      "16205a",
      "693c1c",
      "a8000a",
      "F9B3A1",
      "d86a1c",
      "FFF38C",
      "9ee12f",
      "67b50f",
      "1e1e30",
      "dcff6b",
      "80ffbd",
      "9a99ff",
      "8e66ff",
      "404040",
      "757575",
      "e0ffff",
      "a00000",
      "350000",
      "1ad000",
      "074200",
      "b9b000",
      "3f3100",
      "b35f00",
      "4b1502"      
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
   private int[][] RGB_COLOR_TABLE = {
         { 0, 0 },          { 1, 1973790 },    { 2, 8355711 },    { 3, 16777215 }, 
         { 4, 16731212 },   { 5, 16711680 },   { 6, 5832704 },    { 7, 1638400 },
         { 8, 16760172 },   { 9, 16733184 },   { 10, 5840128 },   { 11, 2562816 },
         { 12, 16777036 },  { 13, 16776960 },  { 14, 5855488 },   { 15, 1644800 },
         { 16, 8978252 },   { 17, 5570304 },   { 18, 1923328 },   { 19, 1321728 },
         { 20, 5046092 },   { 21, 65280 },     { 22, 22784 },     { 23, 6400 },
         { 24, 5046110 },   { 25, 65305 },     { 26, 22797 },     { 27, 6402 },
         { 28, 5046152 },   { 29, 65365 },     { 30, 22813 },     { 31, 7954 },
         { 32, 5046199 },   { 33, 65433 },     { 34, 22837 },     { 35, 6418 },
         { 36, 5030911 },   { 37, 43519 },     { 38, 16722 },     { 39, 4121 },
         { 40, 5015807 },   { 41, 22015 },     { 42, 7513 },      { 43, 2073 },
         { 44, 5000447 },   { 45, 255 },       { 46, 89 },        { 47, 25 },
         { 48, 8867071 },   { 49, 5505279 },   { 50, 1638500 },   { 51, 983088 },
         { 52, 16731391 },  { 53, 16711935 },  { 54, 5832793 },   { 55, 1638425 },
         { 56, 16731271 },  { 57, 16711764 },  { 58, 5832733 },   { 59, 2228243 },
         { 60, 16717056 },  { 61, 10040576 },  { 62, 7950592 },   { 63, 4416512 },
         { 64, 211200 },    { 65, 22325 },     { 66, 21631 },     { 67, 255 },
         { 68, 17743 },     { 69, 2425036 },   { 70, 8355711 },   { 71, 2105376 },
         { 72, 16711680 },  { 73, 12451629 },  { 74, 11529478 },  { 75, 6618889 },
         { 76, 1084160 },   { 77, 65415 },     { 78, 43519 },     { 79, 11007 },
         { 80, 4129023 },   { 81, 7995647 },   { 82, 11672189 },  { 83, 4202752 },
         { 84, 16730624 },  { 85, 8970502 },   { 86, 7536405 },   { 87, 65280 },
         { 88, 3931942 },   { 89, 5898097 },   { 90, 3735500 },   { 91, 5999359 },
         { 92, 3232198 },   { 93, 8880105 },   { 94, 13835775 },  { 95, 16711773 },
         { 96, 16744192 },  { 97, 12169216 },  { 98, 9502464 },   { 99, 8609031 },
         { 100, 3746560 },  { 101, 1330192 },  { 102, 872504 },   { 103, 1381674 },
         { 104, 1450074 },  { 105, 6896668 },  { 106, 11010058 }, { 107, 14569789 },
         { 108, 14182940 }, { 109, 16769318 }, { 110, 10412335 }, { 111, 6796559 },
         { 112, 1973808 },  { 113, 14483307 }, { 114, 8454077 },  { 115, 10131967 },
         { 116, 9332479 },  { 117, 4210752 },  { 118, 7697781 },  { 119, 14745599 },
         { 120, 10485760 }, { 121, 3473408 },  { 122, 1757184 },  { 123, 475648 },
         { 124, 12169216 }, { 125, 4141312 },  { 126, 11755264 }, { 127, 4920578 }
   };

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