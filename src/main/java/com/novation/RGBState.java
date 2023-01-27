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
      for (int i = 0; i < RGB_HEX_COLOR_TABLE.length; i++) {
         // err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i][1]));
         err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][1]));
         if (err < bestErr) {
            best = (int)RGB_HEX_COLOR_TABLE[i][0];
            bestErr = err;
            if (err == 0) break;
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
      for (int i = 0; i < RGB_HEX_COLOR_TABLE.length; i++) {
         // err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i][1]));
         err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][1]));
         if (err < bestErr) {
            best = (int)RGB_HEX_COLOR_TABLE[i][0];
            bestErr = err;
            if (err == 0) break;
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

   private Object[][] RGB_HEX_COLOR_TABLE = {
      // Kelvin palette manual map

      {56, "ff6393"}, // 9,1
      {5,  "ff1800"}, // 9,2
      {6,  "db1200"}, // 9,3
      
      {4,  "ff8593"}, // 8,1
      {60, "ff4a00"}, // 8,2
      {127,"db3600"}, // 8,3

      {107,"f4a893"}, // 7,1
      {84, "f27e00"}, // 7,2
      {10, "d05d00"}, // 7,3

      {12, "ffd293"}, // 6,1
      {126,"ffbc00"}, // 6,2
      {61, "db8a00"}, // 6,3

      {109,"f6f69d"}, // 5,1
      {13, "ffff26"}, // 5,2
      {14, "dbbc1c"}, // 5,3

      {32, "6ffeff"}, // 4,1
      {33, "4dfeff"}, // 4,2
      {34, "42bbbc"}, // 4,3

      {37, "5ad9ff"}, // 3,1
      {38, "33c6ff"}, // 3,2
      {39, "2c91bc"}, // 3,3

      {41, "45affc"}, // 2,1
      {42, "1988f9"}, // 2,2
      {43, "1564b7"}, // 2,3

      {66, "458bff"}, // 1,1
      {45, "1953ff"}, // 1,2
      {46, "153dbc"}, // 1,3

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
      {16, "DBFDA0"},
      {17, "C2FF61"},
      {18, "A1DD61"},
      {19, "81B361"},
      {20, "C2FFB3"},
      {21, "61FF61"},
      {22, "61DD61"},
      {23, "61B361"},
      {24, "C2FFC2"},
      {25, "61FF8C"},
      {26, "61DD76"},
      {27, "61B36B"},
      {28, "C2FFCC"},
      {29, "61FFCC"},
      {30, "61DDA1"},
      {31, "61B381"},
      {32, "C2FFF3"},
      {33, "61FFE9"},
      {34, "61DDC2"},
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
      {47, "6161B3"},
      {48, "CCB3FF"},
      {49, "A161FF"},
      {50, "0"},
      {51, "7661B3"},
      {52, "FFB3FF"},
      {53, "FF61FF"},
      {54, "DD61DD"},
      {55, "B361B3"},
      {56, "FFB3D5"},
      {57, "FF61C2"},
      {58, "DD61A1"},
      {59, "B3618C"},
      {60, "FF7661"},
      {61, "993500"},
      {62, "795100"},
      {63, "436400"},
      {64, "033900"},
      {65, "005735"},
      {66, "458bff"},
      {67, "6161FF"},
      {68, "00454f"},
      {69, "8161dd"},
      {70, "7f7f7f"},
      {71, "202020"},
      {72, "ff0000"},
      {73, "bdff2d"},
      {74, "afed06"},
      {75, "64ff09"},
      {76, "108b00"},
      {77, "00ff87"},
      {78, "61E9FF"},
      {79, "002aff"},
      {80, "3f00ff"},
      {81, "7a00ff"},
      {82, "b21a7d"},
      {83, "402100"},
      {84, "ff4a00"},
      {85, "88e106"},
      {86, "72ff15"},
      {87, "00ff00"},
      {88, "3bff26"},
      {89, "59ff71"},
      {90, "38ffcc"},
      {91, "CCE4FF"},
      {92, "A1C2F6"},
      {93, "877fe9"},
      {94, "d31dff"},
      {95, "ff005d"},
      {96, "ff7f00"},
      {97, "b9b000"},
      {98, "90ff00"},
      {99, "835d07"},
      {100, "392b00"},
      {101, "144c10"},
      {102, "0d5038"},
      {103, "15152a"},
      {104, "16205a"},
      {105, "693c1c"},
      {106, "a8000a"},
      {107, "F9B3A1"},
      {108, "d86a1c"},
      {109, "FFF38C"},
      {110, "E9F9A1"},
      {111, "67b50f"},
      {112, "1e1e30"},
      {113, "dcff6b"},
      {114, "80ffbd"},
      {115, "9a99ff"},
      {116, "8e66ff"},
      {117, "404040"},
      {118, "757575"},
      {119, "e0ffff"},
      {120, "a00000"},
      {121, "350000"},
      {122, "1ad000"},
      {123, "074200"},
      {124, "b9b000"},
      {125, "3f3100"},
      {126, "b35f00"},
      {127, "4b1502"}   
   };
}