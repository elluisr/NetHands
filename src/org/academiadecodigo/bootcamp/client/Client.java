package org.academiadecodigo.bootcamp.client;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.client.promptview.GameMenu;
import org.academiadecodigo.bootcamp.client.promptview.LobbyMenu;
import org.academiadecodigo.bootcamp.client.promptview.MainMenu;
import org.academiadecodigo.bootcamp.enums.GameState;
import org.academiadecodigo.bootcamp.enums.ServerResponse;
import org.academiadecodigo.bootcamp.messages.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import static org.academiadecodigo.bootcamp.enums.GameState.*;
import static org.academiadecodigo.bootcamp.messages.Messages.WELCOME;

public class Client {
    private Prompt prompt;
    private Socket clientSocket;
    private GameState gameState;
    private boolean guest;

    public Client() {
        prompt = new Prompt(System.in, System.out);
        gameState = MAIN;
        guest = true;
    }

    public static void main(String[] args) {

        Client client = new Client();
        client.init();
        client.run();
    }

    private void init() {
        String serverAddress = PromptView.askServerAddress(prompt);
        Integer serverPort = PromptView.askServerPort(prompt);

        try {
            clientSocket = new Socket(InetAddress.getByName(serverAddress), serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {

        System.out.println(WELCOME);

        try {
            while (clientSocket.isConnected()) {

                switch (gameState) {
                    case MAIN:
                        inMain();
                        break;

                    case LOBBY:
                        inLobby();
                        break;

                    case LOGIN:
                        inLogin();
                        break;

                    case GAME:
                        inGame();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reactionToServer(Integer input) {

        ServerResponse response = ServerResponse.values()[input];

        switch (response) {
            case PLAY:
                gameState = GAME;
                break;

            case SCORE:
                gameState = SCORE;
                break;

            case LOGIN:
            case INVALID_USER:
            case USER_EXISTS:
                gameState = LOGIN;
                break;

            case LOBBY:
                gameState = LOBBY;
                break;

            case QUIT:
                break;
        }
    }

    private void inLobby() throws IOException {
        LobbyMenu lobbyMenu = new LobbyMenu(prompt);
        Integer option = lobbyMenu.show();

        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        output.println(option);

        String message = input.readLine();
        System.out.println(message);

        Integer inputOption = Integer.parseInt(input.readLine());

        reactionToServer(inputOption);
    }

    private void inMain() throws IOException {
        MainMenu mainMenu = new MainMenu(prompt);
        Integer option = mainMenu.show();

        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        output.println(option);

        Integer inputOption = Integer.parseInt(input.readLine());

        reactionToServer(inputOption);
    }

    private void inLogin() throws IOException {
        String username = PromptView.askUsername(prompt);

        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        output.println(username);

        String message = input.readLine();
        System.out.println(message);

        if (message.equals(Messages.INVALID_USERNAME)) {
            gameState = LOGIN;
            return;
        }

        guest = false;
        gameState = LOBBY;
    }

    private void inGame() throws IOException {
        GameMenu gameMenu = new GameMenu(prompt);
        Integer option;
        String inputOption;

        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        inputOption = input.readLine();
        System.out.println(inputOption);

        inputOption = input.readLine();

        while (!inputOption.equals(Messages.GAME_OVER)) {

            System.out.println(Messages.NEW_LINE + inputOption);

            option = gameMenu.show();
            System.out.println(Messages.WAITING_FOR_PLAY);
            output.println(option);

            inputOption = input.readLine();
            System.out.println(Messages.NEW_LINE + inputOption);

            inputOption = input.readLine();
        }

        System.out.println(Messages.NEW_LINE + inputOption);

        inputOption = input.readLine();
        System.out.println(Messages.NEW_LINE + inputOption);

        if (guest) {
            gameState = MAIN;
        } else {
            gameState = LOBBY;
        }

    }
}

