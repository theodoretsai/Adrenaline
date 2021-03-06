package it.polimi.ingsw.view.command_line_view;

import it.polimi.ingsw.connection.ConnectionTech;
import it.polimi.ingsw.view.virtual_model.VirtualLobby;
import it.polimi.ingsw.view.virtual_model.VirtualPlayer;

import static it.polimi.ingsw.Color.*;
import static it.polimi.ingsw.view.command_line_view.CliMessages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;

/**
 * class used to log into the game
 * @author Gregorio Barzasi
 */
public class CliLobby{

    private Thread t;
    private VirtualLobby lobby;
    private ConnectionTech c;
    private BufferedReader sc;
    private VirtualPlayer p;

    public CliLobby(ConnectionTech c, BufferedReader sc, VirtualPlayer p){
        this.c=c;
        this.sc=sc;
        this.p=p;
    }

    public void startLobby()throws IOException{
        do {
            gameSetup();
            clearScreen();
        }while(ynAsk(PREFERENCE_Q,PREFERENCE_Y,PREFERENCE_N));
        if(t.isAlive())
            t.interrupt();
        lobby.sendPref();
        clearScreen();
        waitingRoom();
    }

    public int askMap()throws IOException{
        String temp;
        int num=0;
        do {System.out.println(MAP_OPT);
            System.out.println(MAP_Q);
            temp = sc.readLine();
            if(temp.isEmpty())
                temp="2";
            try{
                num = Integer.parseInt(temp);
            }
            catch (NumberFormatException e)
            {num=0;
            }
            if(num<1 || num>4){
                System.err.println(MAP_N);
            }
        }while(num<1 || num>4);
        lobby.setMapPref(num);
        System.out.println(MAP_Y+num);
        return num;
    }

    /**
     *
     * @param question
     * @param yes
     * @param no
     * @return
     * @throws IOException
     */

    public boolean ynAsk(String question, String yes, String no)throws IOException{
        String temp;
        do {
            System.out.println(question);
            temp = sc.readLine();
            temp=temp.toLowerCase();
            if (temp.equals("s")||temp.equals("y")||temp.equals("1")) {
                System.out.println(yes);
                return true;
            } else if (temp.equals("n")||temp.equals("2")||temp.isEmpty()) {
                System.out.println(no);
                return false;
            }else{
                System.err.println(GENERIC_N);
            }
        }while(true);

    }

    /**
     * method used for general game settings
     */

    public void gameSetup() throws IOException {

        lobby= new VirtualLobby(c,p);
        t = new Thread(() -> {
            try {
                while(true) {
                    lobby.pingRmi();
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException ignored) {
            }
        });

        if(c.isRmi()) {
            t.start();
        }

        System.out.println(LOBBY_HEAD);

        String temp;
        //TERMINATOR
        lobby.setTerminatorPref(ynAsk(TERMINATOR_Q,TERMINATOR_Y,TERMINATOR_N));

        //FINAL FRENZY
        lobby.setFinalFrenzyPref(ynAsk(FRENZY_Q,FRENZY_Y,FRENZY_N));

        //MAX KILL
        int num=0;
        do {
            System.out.println(SKULL_Q);
            temp = sc.readLine();
            if(temp.isEmpty())
                temp="8";
            try{
            num = Integer.parseInt(temp);
            }
            catch (NumberFormatException e)
            {num=0;
            }
            if(num<5 || num>8){
                System.err.println(SKULL_N);
            }
        }while(num<5 || num>8);
        lobby.setKillPref(num);
        System.out.println(SKULL_Y+num);

        //MAP
        lobby.setMapPref(askMap());
        System.out.println(LINE_SEP);

    }

    public void waitingRoom()throws IOException {
        clearScreen();
        System.out.println(WAITINGROOM_HEAD);
        Boolean flag=true;
        while (!lobby.isGameStarted()){
                if (flag&&lobby.hasGameTimerStarted()) {
                    System.out.println(COUNTDOWN_START);
                    flag=false;
                }
                try {
                    lobby.waitUpdate();
                }catch(ConnectException x){
                    System.err.println(CONNECTION_ERR);
                }
            for(VirtualPlayer e : lobby.getNewPlayersList() ){
                if (!e.isPrinted()) {
                    System.out.println(e.getUsername() + " con " + e.getCharacter() + ": è"+GREEN_BOLD+" Pronto!"+RESET);
                    e.setPrinted(true);
                }
            }

        }
        gameStart();

    }

    public void gameStart(){
        new CliGame(c,p).gameStart();
    }


}
