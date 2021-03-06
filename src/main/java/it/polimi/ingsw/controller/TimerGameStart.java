package it.polimi.ingsw.controller;

import java.util.TimerTask;

/**
 * @author Gregorio Barzasi
 */
public class TimerGameStart extends TimerTask {

    private Lobby lobby;

    public TimerGameStart(Lobby lobby){
        this.lobby=lobby;
    }

    @Override
    public void run() {
        lobby.setStarted(true);
    }

}