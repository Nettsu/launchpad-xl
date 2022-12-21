# launchpad-xl

This is a rather quirky Launchpad Mini 3 + Launch Control XL Bitwig script that targets my very specific needs.

- The clip launching grid is 8x5 and you can scroll it as usual using the arrow buttons.
- Below the grid there is a dedicated STOP button for each track
- Furhter below are two momentary switches used to control Remote Controls 3 and 4 of the first device on each track. Holding the button sets the Remote Control to 100% while releasing it resets it to 0%. Great for quick rythmic effects on each track.
- Instead of scene launching the rightmost column control the 9th tracks' Remote Controls:
  + First 5 buttons work as a fader to control the first Remote Control
  + The other two control Remote Controls 2 and 3. Pressing them toggles between 0% and 100%.

```
 track track track track track track track track track
   1     2     3     4     5     6     7     8     9
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|up   |down |left |right|sessi|drums|keys |user |     |
|     |     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|play |play |play |play |play |play |play |play | RC1 |
|     |     |     |     |     |     |     |     | 100%|
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|play |play |play |play |play |play |play |play | RC1 |
|     |     |     |     |     |     |     |     | 75% |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|play |play |play |play |play |play |play |play | RC1 |
|     |     |     |     |     |     |     |     | 50% |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|play |play |play |play |play |play |play |play | RC1 |
|     |     |     |     |     |     |     |     | 25% |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|play |play |play |play |play |play |play |play | RC1 |
|     |     |     |     |     |     |     |     | 0%  |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|stop |stop |stop |stop |stop |stop |stop |stop | RC2 |
|     |     |     |     |     |     |     |     | 0/1 |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
| RC3 | RC3 | RC3 | RC3 | RC3 | RC3 | RC3 | RC3 | RC3 |
|hold |hold |hold |hold |hold |hold |hold |hold | 0/1 |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
| RC4 | RC4 | RC4 | RC4 | RC4 | RC4 | RC4 | RC4 |     |
|hold |hold |hold |hold |hold |hold |hold |hold |     |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+
```
