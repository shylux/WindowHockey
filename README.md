WindowHockey
============

WindowHockey is an air hockey like game for your pc.

It is played by align two computers screen to screen. The game board is then both of your screens and the air hockey puck is will travel from one screen to the other.

This project is hosted on GitHub: https://github.com/shylux/WindowHockey

How to play
-----------

1. **Make sure both machines are in the same network.**
2. Check the position of your machine. If your machine is on the left side use ```-left```. For the right machine ```-right```.
3. Run the program. ```java -jar WindowHockey.jar -left``` (or ```-right```)

Troubleshooting
---------------

#### The game doesn't start!
Ping the other machine! **Make sure both machines are in the same network.**

If you checked and all is good your network might not support udp broadcast. In this case connect manually by providing the ```-host``` parameter.

#### There is an error with **JVM_Bind**...
WindowHockey uses port ```8228``` by default. This error message means that there is already something running on this port

1. Did you start WindowHockey a second time? Check the TaskManager for a java process.

2. Another program may use the same port. You can switch port with ```-port 1234```. **But the other machine has to use the same port!**

Command line arguments
----------------------

Game settings like ```-slow``` or ```-fast``` are defined by the machine who started WindowHockey first.

| Command | Description |
| --- | --- |
|```-left -right```|Define on which side your machine is located.|
|```-h -host```|Specify the machine to connect to.|
|```-p -port```|Specify custom port.|
|```-slow -fast```|Game speed.|
|```-continue```|Starts searching for new game after old one ends.|

### Debug Arguments
| Command | Description |
| --- | --- |
|```-log```| Log level. Levels are ALL, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL. Default: INFO |
|```-tcp``` ```-udp```| Start instance with only tcp or udp. Used when debugging on one machine. First start the ```-udp``` instance, then the ```-tcp``` one. The ```-udp``` instance defines the game settings. |
|```-stop```| Stops puck on cursor contact. |
