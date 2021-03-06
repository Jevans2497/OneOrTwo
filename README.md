# Rules

There are two players, player1 and player2. One player is the asker and the other is the guesser. First, the asker is prompted to pick a number, either 1 or 2. Then, after the asker receives a confirmation, the guesser is queried for their number, either 1 or 2. If the guesser guessed the right number, they win and become the new asker. Otherwise, the roles are maintained and another round is played. Play until you get bored!


# How it works:

The program uses the standard client-server architecture. I added multi-threading capabilities so that multiple users can play the game with each other at the same time. Every game has exactly 2 players, so when more players join, they must be in even numbers. Players are connected automatically in the order that they connect to the server so if 6 players join the game, the first two to connect play against each other, the 3rd and 4th to connect play against each other, and the 5th and 6th play against each other. 

First, the server is started. Two clients connect and then are queried for their names. The names are used to notify players of who their opponent is and for printing the results of the game later. Then, users are asked for their number, either 1 or 2. Once both numbers have been received by the server, it executes a countdown by sending out the Strings "3...", "2...", and "1..." to the client, which reads the strings and prints each of them on a new line. Then, if the asker number and the guesser number are the same, the clients are notified with a short message, the value of currentStreak is set to 0, and the roles of the players are swapped. If both players chose different numbers then currentStreak is incremented, and a message is sent to both players based on the value of currentStreak. Then, both players are notified of the max streak which is determined by the highest currentStreak attained by any player in any of the games. Then, the game begins again. 
