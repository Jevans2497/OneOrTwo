/**
 * Created by jonathanevans on 4/23/19.
 */
import java.io.*;
import java.net.*;
class TCPClient {

    public static void main(String argv[]) throws Exception
    {
        Boolean isAsker = false;
        int oneOrTwo = 0;
        final String CRLF = "\r\n";

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        //Substitute  in the IPAddress of the server here.
        Socket clientSocket = new Socket("172.18.58.240", 10000);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

        //Gets the users name from the server.
        System.out.println(inFromServer.readLine());
        String name = inFromUser.readLine();
        //Checks to make sure that the user entered a value
        while (name.equals("")) {
            System.out.println("You must provide a name!");
            name = inFromUser.readLine();
        }
        outToServer.writeBytes(name + CRLF);

        //Prints out the name of the opponent.
        System.out.println(inFromServer.readLine());

        while (true) {

            //Tells this client whether it is the asker or the guesser.
            isAsker = inFromServer.readLine().equals("true");

            System.out.println((isAsker) ? "Is your number 1... or 2!?!" : "Is your guess 1... or 2!?!");

            //Reads in the user's number
            oneOrTwo = getOneOrTwo(inFromUser);

            //Sends the number (oneOrTwo) to the server.
            outToServer.writeBytes(Integer.toString(oneOrTwo) + CRLF);

            //Prints a confirm from the server that it received the client's number.
            System.out.println(inFromServer.readLine());

            //Prints the countdown
            countdownAndResults(inFromServer);
        }
    }

    /*
    Returns either a 1 or a 2. Makes sure the user entered only a 1 or a 2 and nothing else.
     */
    public static int getOneOrTwo(BufferedReader inFromUser) {
        int oneOrTwo = 0;
        while (oneOrTwo != 1 && oneOrTwo != 2) {
            //Use a try/catch block since entering a null value throws an exception and terminates the program (we don't want this).
            try {
                oneOrTwo = inFromUser.readLine().charAt(0) - 48;
                //If the user selected something that wasn't 1 or 2.
                if (oneOrTwo != 1 && oneOrTwo != 2) {
                    System.out.println("You did not provide a 1 or a 2");
                }
            } catch (Exception e) {
                System.out.println("You can't enter nothing! Enter a 1 or a 2");
                //Set the value to 0 so that the value is not null. Otherwise, when the while statements checks the value,
                //it will throw an exception.
                oneOrTwo = 0;
            }
        }
        return oneOrTwo;
    }

    /*
    Prints out the countdown and the end results of the game including the currentStreak, maxStreak, and who won.
     */
    public static void countdownAndResults(BufferedReader inFromServer) throws Exception {
        //Prints 3...
        System.out.println(inFromServer.readLine());
        //Prints 2...
        System.out.println(inFromServer.readLine());
        //Prints 1...
        System.out.println(inFromServer.readLine());

        //Prints the result of the game
        System.out.println(inFromServer.readLine());

        //prints the MAX streak.
        System.out.println(inFromServer.readLine());
    }
}


