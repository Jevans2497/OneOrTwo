/**
 * Created by jonathanevans on 4/23/19.
 */
import java.io.*;
import java.net.*;
class TCPClient {

    public static void main(String argv[]) throws Exception
    {

        Boolean isAsker = false;
        int number;
        final String CRLF = "\r\n";

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("localhost", 10000);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

        while (true) {

            isAsker = inFromServer.readLine().equals("true");

            if (isAsker) {
                System.out.println("Is your number 1... or 2!?!");

                int oneOrTwo = inFromUser.readLine().charAt(0) - 48;

                while (oneOrTwo != 1 && oneOrTwo != 2) {
                    System.out.println("You did not provide a 1 or a 2");
                    oneOrTwo = inFromUser.readLine().charAt(0) - 48;
                }

                outToServer.writeBytes(Integer.toString(oneOrTwo) + CRLF);

                //Receive a confirmation that the server received my 1 or 2
                System.out.println(inFromServer.readLine());

            } else {

                System.out.println("Waiting for number from Asker...");

                //This line does nothing except confirm that the server is ready for a guess number.
                String readyToTransmitGuess = inFromServer.readLine();
                System.out.println("You are The Guesser. Guess your number now!");

                int oneOrTwo = inFromUser.readLine().charAt(0) - 48;

                while (oneOrTwo != 1 && oneOrTwo != 2) {
                    System.out.println("You did not provide a 1 or a 2");
                    oneOrTwo = inFromUser.readLine().charAt(0) - 48;
                }

                outToServer.writeBytes(Integer.toString(oneOrTwo) + CRLF);

                //Receive a confirmation that the server received my 1 or 2
                System.out.println(inFromServer.readLine());
            }
            //These three prints are for the countdown
            System.out.println(inFromServer.readLine());
            System.out.println(inFromServer.readLine());
            System.out.println(inFromServer.readLine());

            System.out.println(inFromServer.readLine());
        }
    }
}


