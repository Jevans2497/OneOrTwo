/**
 * Created by jonathanevans on 4/23/19.
 */
import java.io.*;
import java.net.*;

class TCPServer{

    //Keeps track of the max streak of all current games being played on this server.
    public static int maxStreak = 0;
    public static String maxStreakName = "TBD";

    public static void main(String argv[]) throws Exception {

        //Sets up the welcome socket
        ServerSocket welcomeSocket = new ServerSocket(10000);

        System.out.println("Waiting for incoming connection Request...");

        //Connects both players and starts a new thread for their game.
        while (true) {
            Socket player1ConnectionSocket = welcomeSocket.accept();
            Socket player2ConnectionSocket = welcomeSocket.accept();

            Game g = new Game(player1ConnectionSocket, player2ConnectionSocket);

            Thread thread = new Thread(g);

            thread.start();
        }
    }

    //We must create this class and implement runnable in order to use multithreading.
    final static class Game implements Runnable {

        //CRLF stands for Carriage Return Line Feed and we need to end every string we send out with it for it to be
        //read successfully by the clients.
        final static String CRLF = "\r\n";
        //Connect the two player's sockets
        Socket player1ConnectionSocket;
        Socket player2ConnectionSocket;
        //Holds the number of the player who is asking 1 or 2
        int askerNum = 0;
        //Holds the number of the player who is guessing 1 or 2
        int guesserNum = 0;
        //Keeps track of the current streak. This number is reset every time the guesser is correct.
        int currentStreak = 0;
        //This keeps track of which player is the asker and is very important. It is used to know which player is which.
        Boolean isPlayer1Asker = true;
        String player1Name;
        String player2Name;

        //Constructor
        public Game(Socket player1Socket, Socket player2Socket) throws Exception {
            this.player1ConnectionSocket = player1Socket;
            this.player2ConnectionSocket = player2Socket;
        }

        public void run() {
            try {
                playGame();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        private void playGame() throws Exception {
            //Establish input and output from and to the clients.


            BufferedReader inFromClientPlayer1 = new BufferedReader
                    (new InputStreamReader(player1ConnectionSocket.getInputStream()));

            DataOutputStream outToClientPlayer1 = new DataOutputStream(player1ConnectionSocket.getOutputStream());

            BufferedReader inFromClientPlayer2 = new BufferedReader
                    (new InputStreamReader(player2ConnectionSocket.getInputStream()));

            DataOutputStream outToClientPlayer2 = new DataOutputStream(player2ConnectionSocket.getOutputStream());

            getPlayerNamesAndDisplayOpponentToClients(outToClientPlayer1, outToClientPlayer2, inFromClientPlayer1, inFromClientPlayer2);

            while (true)
            {
                notifyClientsOnWhoIsAsker(outToClientPlayer1, outToClientPlayer2);

                receiveNumbersAndSendConfirmation(outToClientPlayer1, outToClientPlayer2, inFromClientPlayer1, inFromClientPlayer2);

                startCountDown(outToClientPlayer1, outToClientPlayer2);

                sendGameResultsToClient(outToClientPlayer1, outToClientPlayer2);
            }
        }

        /*
        This method gets the names of each player from their respective client and then tells the opposite client
        who they're playing against.
        Players are prompted in the client to provide their name. While this is a nice feature to begin with imo,
        it is especially useful when we utilize multi-threading as it allows a bunch of players to know who they
        are playing against.
         */
        private void getPlayerNamesAndDisplayOpponentToClients(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2,
                                                               BufferedReader inFromClientPlayer1, BufferedReader inFromClientPlayer2) throws Exception {
            outToClientPlayer1.writeBytes("What is your name?" + CRLF);
            outToClientPlayer2.writeBytes("What is your name?" + CRLF);

            player1Name = inFromClientPlayer1.readLine();
                player2Name = inFromClientPlayer2.readLine();

                //Tells the players who their opponent is.
                outToClientPlayer1.writeBytes("You are playing against " + player2Name + CRLF);
                outToClientPlayer2.writeBytes("You are playing against " + player1Name + CRLF);
        }

        /*
        This method tells the clients playing the game which one of them is the Asker and which is the Guesser.
         */
        private void notifyClientsOnWhoIsAsker(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2) throws Exception {
            //Tell each client whether they are the asker or the guesser based on the isPlayer1Asker Boolean
            //This information is not explicitly told to the player but is used by the client to know its role.
            outToClientPlayer1.writeBytes(isPlayer1Asker.toString() + CRLF);
            //You can't use '!' in front of a boolean and call toString() on it so we set the string here instead.
            Boolean isP1Opposite = !isPlayer1Asker;
            outToClientPlayer2.writeBytes(isP1Opposite.toString() + CRLF);
        }

        /*
        This method is a bit long but it does the same thing twice so I figured that was alright. It receives the numbers from
        the clients and notifies the clients that it properly received the numbers.
         */
        private void receiveNumbersAndSendConfirmation(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2,
                                                       BufferedReader inFromClientPlayer1, BufferedReader inFromClientPlayer2) throws Exception {
            //If player1 is the asker, receive its number, otherwise, query player2 for its asking number
            String askerIntConverter = (isPlayer1Asker) ? inFromClientPlayer1.readLine() : inFromClientPlayer2.readLine();
            askerNum = Integer.parseInt(askerIntConverter);

            //Confirm with the guesser that they have selected their number
            if (isPlayer1Asker) {
                outToClientPlayer1.writeBytes("You have selected: " + askerNum + CRLF);
            } else {
                outToClientPlayer2.writeBytes("You have selected: " + askerNum + CRLF);
            }

            //If player2 is the guesser, receive its number, otherwise, query player1 for its guessing number
            String guesserIntConverter = (isPlayer1Asker) ? inFromClientPlayer2.readLine() : inFromClientPlayer1.readLine();
            guesserNum = Integer.parseInt(guesserIntConverter);

            //Confirm with the guesser that they have selected their number
            if (isPlayer1Asker) {
                outToClientPlayer2.writeBytes("You have selected: " + guesserNum + CRLF);
            } else {
                outToClientPlayer1.writeBytes("You have selected: " + guesserNum + CRLF);
            }
        }

        /*
        This method is just a fun feature I added. After both clients have entered their numbers, the server counts down
        from 3 and then the results are printed out afterwards.
         */
        public void startCountDown(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2) throws Exception {
            //Implements a countdown that appears as
            //"3...
            //2...
            //1..."
            int countDown = 3;
            //Get the start time so we can check how many seconds have passed since the start.
            long startTime = System.currentTimeMillis();
            //While the countdown is greater than 0, print the three numbers
            while (countDown >= 0) {
                long curTime = System.currentTimeMillis();
                //If a second has passed, print the current value of countdown to both players
                if (curTime - startTime > 1000) {
                    if (countDown != 0) {
                        outToClientPlayer1.writeBytes(countDown + "..." + CRLF);
                        outToClientPlayer2.writeBytes(countDown + "..." + CRLF);
                    }
                    //Set the new value of our startTime to curTime so that we wait for the next second to pass.
                    startTime = curTime;
                    countDown -= 1;
                }
            }
        }

        /*
        This method handles printing out all the results of the game including who won, what the currentStreak is, and what
        the maxStreak is. It's a lot of if statements and prints so it's relatively easy to follow except for the fact
        that there is a lot going on.
         */
        private void sendGameResultsToClient(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2) throws Exception {
            //This connects the names of the guessers and askers with their correct player names.
            //This makes it much easier to print out the results of the game without having to use even more if statements.
            String askerName = (isPlayer1Asker) ? player1Name : player2Name;
            String guesserName = (isPlayer1Asker) ? player2Name : player1Name;

            //If the numbers are the same, then the guesser was correct and the players now switch their roles.
            //Otherwise, the game continues with the players in the same roles.
            if (askerNum == guesserNum) {
                guesserWasCorrect(outToClientPlayer1, outToClientPlayer2, guesserName);
            } else {
                guesserWasWrong(outToClientPlayer1, outToClientPlayer2, guesserName, askerName);
            }

            handleMaxStreak(outToClientPlayer1, outToClientPlayer2, askerName);
        }

        /*
        This method is called when the guesser guessed the correct number. We reset the value of the currentStreak and
        notify the clients that the guesser was correct. We also switch the roles of the players.
         */
        public void guesserWasCorrect(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2, String guesserName) throws Exception {
            currentStreak = 0;
            outToClientPlayer1.writeBytes(guesserName + " was CORRECT. You will now switch roles!" + CRLF);
            outToClientPlayer2.writeBytes(guesserName + " was CORRECT. You will now switch roles!" + CRLF);
            //Switch the roles.
            isPlayer1Asker = !isPlayer1Asker;
        }

        /*
        This method is called when the guesser was wrong. We increment the currentStreak and then send out the proper response
        to the clients based on the value of currentStreak.
         */
        public void guesserWasWrong(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2, String guesserName, String askerName) throws Exception {
            //Since the guesser failed, the asker gets a point.
            currentStreak += 1;
            //Since the currentStreak is only 1, it is unnecessary to tell the player their streak so we don't.
            //Everything here is pretty self-explanatory, these statements just print based on the streak.
            //Also, Java doesn't allow switch statements to have conditionals, otherwise I would have used them.
            if (currentStreak == 1) {
                outToClientPlayer1.writeBytes(guesserName + " was INCORRECT. Play another round!" + CRLF);
                outToClientPlayer2.writeBytes(guesserName + " was INCORRECT. Play another round!" + CRLF);
            } else if (currentStreak < 3) {
                outToClientPlayer1.writeBytes(guesserName + " was INCORRECT. The current streak is " + currentStreak + ". Play another round!" + CRLF);
                outToClientPlayer2.writeBytes(guesserName + " was INCORRECT. The current streak is " + currentStreak + ". Play another round!" + CRLF);
            } else {
                outToClientPlayer1.writeBytes(guesserName + " was INCORRECT. " + askerName.toUpperCase() + " IS ON FIRE!!! " + currentStreak + " in a row!" + CRLF);
                outToClientPlayer2.writeBytes(guesserName + " was INCORRECT. " + askerName.toUpperCase() + " IS ON FIRE!!! " + currentStreak + " in a row!" + CRLF);
            }
        }

        /*
        This method handles everything to do with the maxStreak value. It sets it when the currentStreak is high enough and
        notifies the clients after each round.
         */
        private void handleMaxStreak(DataOutputStream outToClientPlayer1, DataOutputStream outToClientPlayer2, String askerName) throws Exception {
            //If the currentStreak is greater than the maxStreak, replace the value of maxStreak. Same with maxStreak Name.
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak;
                maxStreakName = askerName;
            }
            //Tell the players what the current max streak is.
            outToClientPlayer1.writeBytes(maxStreakName + " holds the max streak with a streak of " + maxStreak + CRLF);
            outToClientPlayer2.writeBytes(maxStreakName + " holds the max streak with a streak of " + maxStreak + CRLF);
        }
    }
}