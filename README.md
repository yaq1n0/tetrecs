# TetrECS

Tetris game project for Programming 2 (1st year) coursework 

# Compile and Run
Technically this can be run using just maven cli and javafx cli. But the easiest way is to compile and run is to open the project with intellij IDEA (IDE used for this project) and create a run configuration that does `maven clean install` and ``javafx:run` for you. 

[example_intellij_config.jpg](example_intellij_config.jpg)

# Issues
The game was meant to connect to a server on the University of Southampton's internal network, which requires you to use the University's VPN to connect to. So when you try to launch the game, it will throw errors trying to connect and enable the multiplayer functionality. I might fork this repository and make a single player version of this game if I feel like it at some point but in theory, bypassing the multiplayer connection code should work.   
