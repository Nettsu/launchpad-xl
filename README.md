# launchpad-xl

This is a rather quirky Launchpad Mini 3 + Launch Control XL Bitwig script that targets my very specific needs.

Launchpad part:
- The clip launching grid is 8x5 and you can scroll it as usual using the arrow buttons
- Below the grid there is a dedicated STOP button for each track
- Further below are two momentary switches used to control Remote Controls 3 and 4 of the selected device on each track. Holding the button sets the Remote Control to 100% while releasing it resets it to 0%. Great for quick rythmic effects on each track
- Instead of scene launching by default the rightmost column controls the 9th tracks' Remote Controls:
  + First 5 buttons work as a fader to control the first Remote Control
  + The other two control Remote Controls 2 and 3. Pressing them toggles between 0% and 100%
- Holding the bottom right corner button (SHIFT in this script) changes the behaviour of some butttons:
  + first 5 scene buttons launch scenes
  + 6th scene button stops all tracks
  + the remote control buttons for track 1-8 work as a toggle instead of a momentary hold

```
 track  track  track  track  track  track  track  track  track
   1      2      3      4      5      6      7      8      9
+------+------+------+------+------+------+------+------+------+
|  up  | down | left | rght | sess | drum | keys | user |      |
|      |      |      |      |      |      |      |      |      |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | RC 1 |
|      |      |      |      |      |      |      |      | 100% |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | RC 1 |
|      |      |      |      |      |      |      |      |  75% |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | RC 1 |
|      |      |      |      |      |      |      |      |  50% |
+------+------+--CLIP LAUNCHING GRID 5x8--+------+------+------+
|      |      |      |      |      |      |      |      | RC 1 |
|      |      |      |      |      |      |      |      |  25% |
+------+------+------+------+------+------+------+------+------+
|      |      |      |      |      |      |      |      | RC 1 |
|      |      |      |      |      |      |      |      |   0% |
+------+------+------+------+------+------+------+------+------+
| stop | stop | stop | stop | stop | stop | stop | stop | RC 2 |
|      |      |      |      |      |      |      |      |toggle|
+------+------+------+------+------+------+------+------+------+
| RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 | RC 3 |
| hold | hold | hold | hold | hold | hold | hold | hold |toggle|
+------+------+------+------+------+------+------+------+------+
| RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | RC 4 | SHFT |
| hold | hold | hold | hold | hold | hold | hold | hold |      |
+------+------+------+------+------+------+------+------+------+
```

Launch Control XL part:
- The first 7 columns control the tracks, following the selection of the Launchpad
- The 8th column controls a send channel, select which channel with the left and right buttons
- For track channels the first two knobs control the send amounts and the third one controls the first Remote Control of the selected device on each track
- For the send channel the knobs control the first three Remote Controls of the selected device
- The up and down buttons control the tempo
- The Mute, Solo, Record Arm and two rows of buttons at the bottom work as originally intended
- Pressing the Device button toggles on the 'Device' mode. When in 'Device' mode:
  + the third row of knobs control the eight Remote Controls of currently selected device on the currently selected track
  + the left and right buttons select the previous/next Remote Control page

```
+------+------+------+------+------+------+------+------+------+
|track1 track2 track3 track4 track5 track6 track7  send        |
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
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[DEVI]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[MUTE]|
|volume|volume|volume|volume|volume|volume|volume|volume|      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[SOLO]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |      |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |[rARM]|
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |   \  |
|  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |  ||  |   /  |
+------+------+------+------+------+------+------+------+   \  |
| SEL  | SEL  | SEL  | SEL  | SEL  | SEL  | SEL  | SEL  |   /  |
+------+------+------+------+------+------+------+------+   \  |
| CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | CTRL | <~+  |
+------+------+------+------+------+------+------+------+------+
```
