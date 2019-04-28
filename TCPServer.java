/**
 * Created by jonathanevans on 4/23/19.
 */
import java.io.*;
import java.net.*;
import java.util.*;

class TCPServer{

    public static void main(String argv[]) throws Exception {

        ServerSocket welcomeSocket = new ServerSocket(10000);

        System.out.println("Waiting for incoming connection Request...");

        while (true) {
            Socket player1ConnectionSocket = welcomeSocket.accept();
            Socket player2ConnectionSocket = welcomeSocket.accept();

            Game g = new Game(player1ConnectionSocket, player2ConnectionSocket);

            Thread thread = new Thread(g);

            thread.start();
        }
    }

    final static class Game implements Runnable {

        final static String CRLF = "\r\n";
        Socket player1ConnectionSocket;
        Socket player2ConnectionSocket;
        int askerNum = 0;
        int guesserNum = 0;
        Boolean isPlayer1Asker = true;

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
            //Get the two clients, the guesser and the asker.

            BufferedReader inFromClientPlayer1 = new BufferedReader
                    (new InputStreamReader(player1ConnectionSocket.getInputStream()));

            DataOutputStream outToClientPlayer1 = new DataOutputStream(player1ConnectionSocket.getOutputStream());

            BufferedReader inFromClientPlayer2 = new BufferedReader
                    (new InputStreamReader(player2ConnectionSocket.getInputStream()));

            DataOutputStream outToClientPlayer2 = new DataOutputStream(player2ConnectionSocket.getOutputStream());

            while (true)

            {

                //Tell each player whether they are the asker or the guesser based on the isPlayer1Asker Boolean
                if (isPlayer1Asker) {
                    outToClientPlayer1.writeBytes("true" + CRLF);
                    outToClientPlayer2.writeBytes("false" + CRLF);
                } else {
                    outToClientPlayer1.writeBytes("false" + CRLF);
                    outToClientPlayer2.writeBytes("true" + CRLF);
                }

                System.out.println("Waiting for number from Asker");

                //If player1 is the asker, receive its number, otherwise, query player2 for its asking number
                if (isPlayer1Asker) {
                    String askerIntConverter = inFromClientPlayer1.readLine();
                    askerNum = Integer.parseInt(askerIntConverter);
                    outToClientPlayer1.writeBytes("You have selected: " + askerNum + CRLF);
                    outToClientPlayer2.writeBytes("Ready for guess transmission!" + CRLF);
                } else {
                    String askerIntConverter = inFromClientPlayer2.readLine();
                    askerNum = Integer.parseInt(askerIntConverter);
                    outToClientPlayer2.writeBytes("You have selected: " + askerNum + CRLF);
                    outToClientPlayer1.writeBytes("Ready for guess transmission!" + CRLF);
                }

                System.out.println("Waiting for number from Guesser");

                //If player2 is the guesser, receive its number, otherwise, query player1 for its guessing number
                if (isPlayer1Asker) {
                    String guesserIntConverter = inFromClientPlayer2.readLine();
                    guesserNum = Integer.parseInt(guesserIntConverter);
                    outToClientPlayer2.writeBytes("You have selected: " + guesserNum + CRLF);
                } else {
                    String guesserIntConverter = inFromClientPlayer1.readLine();
                    guesserNum = Integer.parseInt(guesserIntConverter);
                    outToClientPlayer1.writeBytes("You have selected: " + guesserNum + CRLF);
                }

                //Handle the results. If the asker and guesser said the same number, they switch roles.
                //Otherwise, they play another round exactly the same. Do a countdown to make it exciting.
                int countDown = 3;
                long startTime = System.currentTimeMillis();
                while (countDown >= 0) {
                    long curTime = System.currentTimeMillis();
                    if (curTime - startTime > 1000) {
                        if (countDown != 0) {
                            outToClientPlayer1.writeBytes(countDown + CRLF);
                            outToClientPlayer2.writeBytes(countDown + CRLF);
                        }
                        startTime = curTime;
                        countDown -= 1;
                    }
                }

                if (askerNum == guesserNum) {
                    outToClientPlayer1.writeBytes("The GUESSER was CORRECT. You will now switch roles!" + CRLF);
                    outToClientPlayer2.writeBytes("The GUESSER was CORRECT. You will now switch roles!" + CRLF);
                    isPlayer1Asker = !isPlayer1Asker;
                } else {
                    outToClientPlayer1.writeBytes("The GUESSER was INCORRECT. Play another round!" + CRLF);
                    outToClientPlayer2.writeBytes("The GUESSER was INCORRECT. Play another round!" + CRLF);
                }
            }
        }
    }
}

/*
Pick a starting guesser and a starting asker based on who connects first or a random boolean
Wait for the asker to submit their number
Once the asker has submitted their number, query the guesser for theirs
When both have submitted their numbers, compare them and return if the guesser was correct
If the guesser was correct, swap the guesser and the asker and notify the clients of this.
 */