package org.academiadecodigo.bootcamp.Server;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameHandler {

    private ExecutorService cachedPool;
    private Queue<ClientHandler> listOfClients;
    private List<Game> listOfGames;

    public GameHandler(){
        cachedPool = Executors.newCachedThreadPool();
        listOfClients = new LinkedList<>();
        listOfGames = new LinkedList<>();
    }

    public void clientJoin(ClientHandler client){
        listOfClients.offer(client);
        if(listOfClients.size() >= 2){
            startGame(listOfClients.poll(), listOfClients.poll());
        }
    }

    private void startGame(ClientHandler clientHandler1, ClientHandler clientHandler2) {
        cachedPool.submit(new Runnable() {
            @Override
            public void run() {
                Game game = new Game(clientHandler1, clientHandler2);
                listOfGames.add(game);
                game.start();
            }
        });
    }

}
