# launchpad-xl

This is a rather quirky Launchpad Mini 3 + Launch Control XL Bitwig script that targets my very specific needs.

### Launchpad part:
- The clip launching grid is 8x5 and you can scroll it as usual using the arrow buttons
- Below the grid there is a dedicated STOP button for each track
- Further below are two momentary switches used to control Remote Controls 3 and 4 of the first device on each track. Holding the button sets the Remote Control to 100% while releasing it resets it to 0%. Great for quick rythmic effects on each track
- Instead of scene launching by default the rightmost column controls 7 User Controls, freely mappable in Bitwig GUI, toggling them between 0% and 100%
- Holding the bottom right corner button (SHIFT in this script) changes the behaviour of some butttons:
  + first 5 scene buttons launch scenes
  + 6th scene button stops all tracks
  + 7th scene button toggles play/stop
  + the remote control buttons for track 1-8 work as a toggle instead of a momentary hold

```
 track  track  track  track  track  track  track  track
   1      2      3      4      5      6      7      8
+------+------+------+------+------+------+------+------+------+
|  up  | down | left | rght | sess | drum | keys | user |      |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | UC 1 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | UC 2 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | UC 3 |
|      |      |      |      |      |      |      |      |      |
+------+------+--CLIP LAUNCHING GRID 8x5--+------+------+------+
|      |      |      |      |      |      |      |      | UC 4 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | UC 5 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
| stop | stop | stop | stop | stop | stop | stop | stop | UC 6 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
| RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | UC 7 |
| hold | hold | hold | hold | hold | hold | hold | hold |      |
+------+------+------+------+------+------+------+------+------+
| RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | SHFT |
| hold | hold | hold | hold | hold | hold | hold | hold |      |
+------+------+------+------+------+------+------+------+------+
```

### Launch Control XL part:
- The first 7 columns control the tracks, following the selection of the Launchpad
- The 8th column controls a send channel, select which channel with the left and right buttons
- For track channels the first two knobs control the send amounts and the third one controls the first Remote Control of the first device on each track
  + an exception is made for track 5, which has two RC knobs and one send knob instead (since that is the only MIDI track in my live setup)
- For the send channel the knobs control the first three Remote Controls of the first device
- The LEDs colour under each knob indicate the value of the parameter in the DAW
- The up and down buttons control the tempo
- The Mute, Solo, Record Arm and two rows of buttons at the bottom work as originally intended
- Pressing the Device button toggles on/off the DEVICE MODE. When in DEVICE MODE:
  + the third row of knobs control the eight Remote Controls of the device currently selected in the GUI
  + the left and right buttons select the previous/next Remote Control page of that device
- The Bitwig volume faders match more closely with the volume printed on the controler, with the exception that +6db at the controller is actually 0db in Bitwig and 0db on the controller maps to -3db in Bitwig

```
+------+------+------+------+------+------+------+------+------+
|track1 track2 track3 track4 track5 track6 track7  SEND        |
|                                                              |
|-------------------------------------------------------       |
|/send\ /send\ /send\ /send\ /send\ /send\ /send\ / RC \ [_][_]|
|\  1 / \  1 / \  1 / \  1 / \  1 / \  1 / \  1 / \  3 /  U  F |
|-------------------------------------------------------       |
|/send\ /send\ /send\ /send\ / RC \ /send\ /send\ / RC \ [<][>]|
|\  2 / \  2 / \  2 / \  2 / \  2 / \  2 / \  2 / \  2 /   BPM |
|-------------------------------------------------------       |
|/ RC \ / RC \ / RC \ / RC \ / RC \ / RC \ / RC \ / RC \ [<][>]|
|\  1 / \  1 / \  1 / \  1 / \  1 / \  1 / \  1 / \  1 /  L  R |
|                                                              |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[DMOD]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[MUTE]|
|volume|volume|volume|volume|volume|volume|volume|volume|      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[SOLO]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[ ARM]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |   \  |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |   /  |
+------+------+------+------+------+------+------+------+   \  |
| SEL  | SEL  | SEL  | SEL  | SEL  | SEL  | SEL  | SEL  |   /  |
+------+------+------+------+------+------+------+------+   \  |
| CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | <~+  |
+------+------+------+------+------+------+------+------+------+
```
