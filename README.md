# launchpad-xl

This is a rather quirky Launchpad Mini 3 + Launch Control XL Bitwig script that targets my very specific needs.

### Launchpad part:
- The clip launching grid is 8x5 and you can scroll it as usual using the arrow buttons.
- Below the grid there is a dedicated STOP button for each track.
- Further below are two momentary switches used to control Remote Control 2 of each track. Holding the top button sets the Remote Control to 66%, holding the bottom one sets it to 33% while holding both sets it to 100%. Great for quick rythmic effects on each track.
- Instead of scene launching by default the rightmost column controls 7 Project Remotes, toggling them between 0% and 100%.
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
|      |      |      |      |      |      |      |      | PR 1 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | PR 2 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | PR 3 |
|      |      |      |      |      |      |      |      |      |
+------+------+--CLIP LAUNCHING GRID 8x5--+------+------+------+
|      |      |      |      |      |      |      |      | PR 4 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | PR 5 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
| stop | stop | stop | stop | stop | stop | stop | stop | PR 6 |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
| 66%  | 66%  | 66%  | 66%  | 66%  | 66%  | 66%  | 66%  | PR 7 |
| hold | hold | hold | hold | hold | hold | hold | hold |      |
+------+------+------+------+------+------+------+------+------+
| 33%  | 33%  | 33%  | 33%  | 33%  | 33%  | 33%  | 33%  | SHFT |
| hold | hold | hold | hold | hold | hold | hold | hold |      |
+------+------+------+------+------+------+------+------+------+
```

### Launch Control XL part:
- The first 7 columns control the tracks, following the selection of the Launchpad
- The 8th column controls a send channel, select which channel with the left and right buttons
- For track channels the first two knobs control the send amounts and the third one controls the first Remote Control of each track
- For the send channel the knobs control the first three Remote Controls
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
|/send\ /send\ /send\ /send\ /send\ /send\ /send\ / RC \ [<][>]|
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
