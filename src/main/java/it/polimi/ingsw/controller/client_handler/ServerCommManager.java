package it.polimi.ingsw.controller.client_handler;

import it.polimi.ingsw.View;
import it.polimi.ingsw.model.Figure;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.model.board.map.Square;
import it.polimi.ingsw.model.cards.power_up.PowerUp;
import it.polimi.ingsw.model.cards.weapon.Effect;
import it.polimi.ingsw.model.cards.weapon.Weapon;
import it.polimi.ingsw.ViewClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static it.polimi.ingsw.Color.*;
import static it.polimi.ingsw.connection.ConnMessage.*;
import static it.polimi.ingsw.connection.server.ServerMessage.*;

        /**
         * when servers need to communicate with clients uses this class
         * it mainly converts a list of arguments into a long string of info separated by ; or :
         * @author Gregorio Barzasi
        */

public class ServerCommManager  extends Thread implements View {

    private int countdown;
    private SocketClientHandler socketClient;
    private Player owner;
    private RmiClientHandler rmiHandler;
    private ViewClient rmiClient;
    private boolean rmi;
    private boolean inUse =false;
    private String rplTh;
    private String updateBuffer;


    public ServerCommManager(SocketClientHandler socketClient,int countdown){
        this.countdown=countdown;
        this.socketClient = socketClient;
        this.rmi=false;
        this.owner=socketClient.getOwner();
    }
    public ServerCommManager(RmiClientHandler cl, int countdown){
        this.countdown=countdown;
        this.rmiHandler=cl;
        this.rmiClient=rmiHandler.getViewClient();
        this.rmi=true;
        this.owner=rmiHandler.getOwner();
    }

    public String askAndWait(String question,String args) throws IOException{
        String rpl;
        synchronized (socketClient) {
            socketClient.getOut().println(question);
            while (!socketClient.getIn().readLine().equals(AKN)) ;
            socketClient.getOut().println(args);
            do
                rpl = socketClient.getIn().readLine();
            while (rpl.isEmpty());
        }
            if(rpl.equals(NOTHING)) {
                return null;
            }
            return rpl;

    }
    /**
     * Convert into Strings, sends it to client and wait for a reply.
     * then convert the string into a PU pointer and returns it
     * @param args the Powerups to show
     * @return a pu element
     */
    @Override
    public PowerUp showPowerUp(ArrayList<PowerUp> args) {
        String s = "";
        String rpl = "";
        String[] temp;
        for (PowerUp p : args) {
            s = s + p.getName() + INNER_SEP + p.getAmmoOnDiscard().toString() + INFO_SEP;
        }

        /*-------------------------------------------*/
        final String sTh=s;
        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showPowerUp(parseString(sTh)));
                }
                    else {
                    setRplTh(askAndWait(SHOW_PU,sTh));
                }

                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });
        if(!handleInactivity(t)){
            return null;
            /*-------------------------------------------*/
        }

        rpl=rplTh;
        if(rpl==null) {
            return null;
        }
        temp = rpl.split(INNER_SEP);
        for (PowerUp p : args) {
            if (p.getName().equals(temp[0]) && p.getAmmoOnDiscard().toString().equals(temp[1])) {
                return p;
            }
        }
        return null;
    }
    /**
     * Convert args into Strings, sends it to client and wait for a reply.
     * then convert the string into a PU pointer and returns it
     * @param args the Powerups to show
     * @return a pu element
     */

    public Weapon showWeapon(ArrayList<Weapon> args){
        String s="";
        String rpl="";
        String[] temp;
        for(Weapon p : args){
            s=s+p.getName()+INNER_SEP+p.getChamber().toString()+INNER_SEP+p.getReloadCost().toString().replaceFirst(p.getChamber().toString(),"")+INFO_SEP;
        }

        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showWeapon(parseString(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_WEAPONS,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/

        if(rpl==null) {
            return null;
        }
        temp=rpl.split(INNER_SEP);
        for(Weapon p : args){
                if(p.getName().equals(temp[0]))
                    return p;
        }
        return null;
    }

    public Action showActions(ArrayList<Action> args) {
        String s="";
        String rpl="";
        String[] temp;
        for(Action a : args){
            s=s+a.getDescription()+INNER_SEP+a.getRange()+INFO_SEP;
        }
        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showActions(parseString(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_ACTIONS,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/
        if(rpl==null)
            return null;
        temp=rpl.split(INNER_SEP);
        for(Action p : args){
            if(p.getDescription().equals(temp[0]))
                return p;
        }
        return null;
    }

    public Square showPossibleMoves(ArrayList<Square> args, Boolean show) {
        String s=show.toString()+INFO_SEP;
        String rpl="";
        for(Square p : args){
            s=s+p.getPosition().getRow()+INNER_SEP+p.getPosition().getColumn()+INFO_SEP;
        }

        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showPossibleMoves(parseString(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_MOVES,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/
        if(rpl==null)
            return null;
        for(Square p : args){
            if((p.getPosition().getRow()+INNER_SEP+p.getPosition().getColumn()).equals(rpl))
                return p;
        }
        return null;
    }


    public Boolean showBoolean(String message){
        String rpl="";
        /*-------------------------------------------*/
        final String sTh=message;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(String.valueOf(rmiClient.showBoolean(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_BOOLEAN,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(isInactive()) {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }
        else if(!handleInactivity(t))
            return null;

        rpl=rplTh;
        /*-------------------------------------------*/
        if(rpl==null)
            return null;

        return Boolean.parseBoolean(rpl);
    }

    public void displayMessage(String message){
        try {
            setInUse(true);
            if (rmi) {
                rmiClient.displayMessage(message);
            } else
                askAndWait(SHOW_MESSAGE, message);
            setInUse(false);
            try {
                sleep(3000);
            } catch (InterruptedException e){}
        }catch(IOException e){
            handleDisconnection();
        }
    }

    public void displayLeaderboard(ArrayList<Figure> all){
        String s="";
        for(Figure f: all)
            s=s+f.getCharacter()+INFO_SEP;

        try {
            setInUse(true);
            if (rmi) {
                rmiClient.displayLeaderboard(parseString(s));
            } else
                askAndWait(DISPLAY_LEADERBOARD, s);
            setInUse(false);
        }catch(IOException e){
            handleDisconnection();
        }
    }
    public String chooseDirection(ArrayList<Figure> args){
        String s="";
        String rpl="";
        for(Figure f: args)
            s=s+f.getCharacter()+INFO_SEP;


        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                String x;
                if (rmi) {
                    x=rmiClient.chooseDirection(parseString(sTh));
                }
                else {
                    x=askAndWait(CHOOSE_DIRECTION,sTh);
                }
                if(x==null)
                    setRplTh(null);
                else
                    setRplTh(x.toLowerCase());
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/
        if(rpl==null||rpl.equals(NOTHING))
            return null;
        return rpl;
    }

    public Effect showEffects(Set<Effect> args){
        String s="";
        String rpl="";
        String[] temp;
        String cost;
        for(Effect e : args){
            cost=e.getCost().toString();
            if(cost.isEmpty())
                cost=NOTHING;
            s=s+e.getName()+INNER_SEP+cost+INFO_SEP;
        }
        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showEffects(parseString(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_EFFECTS,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/

        if(rpl==null)
            return null;
        temp=rpl.split(INNER_SEP);
        for(Effect p : args){
            if(p.getName().equals(temp[0]))
                return p;
        }
        return null;
    }

    public ArrayList<Figure> showTargetAdvanced(Set<Figure> args,int maxNum,boolean fromDiffSquare, String msg){
        String s="";
        String rpl="";
        String[] temp;
        HashMap<String,Figure> players = new HashMap<>();
        s=maxNum+INNER_SEP+fromDiffSquare+INNER_SEP+msg.replaceAll(INNER_SEP,"").replaceAll(INFO_SEP,"")+INFO_SEP;
        for(Figure a : args){
            s=s+a.getCharacter()+INFO_SEP;
            players.put(a.getCharacter(),a);
        }
        /*-------------------------------------------*/
        final String sTh=s;

        Thread t = new Thread(() -> {
            try{
                setInUse(true);
                if (rmi) {
                    setRplTh(rmiClient.showTargetAdvanced(parseString(sTh)));
                }
                else {
                    setRplTh(askAndWait(SHOW_TARGET_ADV,sTh));
                }
                setInUse(false);
            }catch(IOException e){
                handleDisconnection();
                setRplTh(null);
            }
        });

        if(!handleInactivity(t)) {
            return null;
        }
        rpl=rplTh;
        /*-------------------------------------------*/

        if(rpl==null)
            return null;

        ArrayList<Figure> target = new ArrayList<>();
        temp=rpl.split(INFO_SEP);
        if(temp.length>maxNum)
            return null;
        for(String a : temp){
            if(!players.containsKey(a))
                return null;
            target.add(players.get(a));
        }
        return target;
    }

    public void sendsUpdate(String s){
        updateBuffer = s;
        if(owner.isInactive()||owner.isDisconnected()||(rmi && rmiClient==null)) {
            return;
        }
        setInUse(true);
        try{
            if (rmi) {
                rmiClient.updateModel(s);
            }
            else {
                askAndWait(UPDATE, s);
            }
            setInUse(false);
        }catch(IOException e){
            handleDisconnection();
        }
    }


    public void handleDisconnection(){
        owner.setDisconnected(true);
        owner.setInactive(true);

        this.rmiHandler=null;
        this.rmiClient=null;
        this.rmi=false;
        System.err.println(owner.getUsername()+CLIENT_UNREACHABLE);

    }

    public boolean handleInactivity(Thread t){
        t.setPriority(THREAD_PRIORITY);
        t.start();
        int i=0;
        System.out.print("\n"+owner.getCharacter()+" Inactivity countdown: ");
        for(;i<countdown;i++){
            for(int j=0;j<100;j++) {
                if(isDisconnected())
                    break;
                if (!t.isAlive())
                    return true;
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
            }
            System.out.print(countdown-i+"s ");
            if(isDisconnected())
                break;
        }
        System.out.println("\n"+owner.getCharacter()+" is inactive!");
        t.interrupt();
        setInUse(false);
        setInactive(true);
        return false;
    }

    public void ping() throws IOException {
        if (!isRmi()) {
            synchronized (socketClient){
            socketClient.getOut().println(PING);
            while (!socketClient.getIn().readLine().equals(PONG)) ;
            }
        } else {
            rmiClient.isConnected();
        }
    }
    public void reactivatePlayer() {
        Boolean b;
        b = showBoolean(RETURN_IN_GAME);
        if(b!=null&&b) {
            owner.setInactive(false);
        }
        sendsUpdate(updateBuffer);
        displayMessage("PRONTO!");
    }

    @Override
    public void run(){

        try {
            //Repeat forever

            while(true) {
                //if communication is free do stuff
                if(!isInUse()) {
                    //if the player is connected but inactive check if the player wants to return to game
                    if (isInactive() && !isDisconnected())
                         reactivatePlayer();
                     else
                        ping();
                        //if the player is active and connected Ping the player
                }
            }
        }catch(IOException e){
            handleDisconnection();
            while(isDisconnected()) {
                try {
                    sleep(1000);
                } catch (InterruptedException d) {}
            }
            if(isRmi()) {
                while (rmiHandler.getViewClient() == null);
                System.out.println("view set");
                rmiClient=rmiHandler.getViewClient();
            }
            setInactive(false);
            sendsUpdate(updateBuffer);
            this.run();
        }
    }


    /*getters and setters*/
    public SocketClientHandler getSocketClient() {
        return socketClient;
    }

    public void setSocketClient(SocketClientHandler socketClient) {
        this.socketClient = socketClient;
    }

    public synchronized boolean isRmi() {
        return rmi;
    }

    public void setRmi(boolean rmi) {
        this.rmi = rmi;
    }

    public synchronized boolean isInUse() {
        return inUse;
    }

    public synchronized void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public ViewClient getRmiClient() {
        return rmiClient;
    }

    public void setRmiClient(ViewClient rmiClient) {
        this.rmiClient = rmiClient;
    }

    public boolean isDisconnected() {
        return owner.isDisconnected();
    }

    public void setDisconnected(boolean disconnected) {
        owner.setDisconnected(disconnected);
    }

    public boolean isInactive() {
        return owner.isInactive();
    }

    public void setInactive(boolean inactive) {
        owner.setInactive(inactive);
    }

    public void setRplTh(String rplTh) {
        this.rplTh = rplTh;
    }

    public ArrayList<String> parseString(String args){
        ArrayList<String> argList = new ArrayList<>();

        if(args.equals(NOTHING))
            return argList;

        String[] s=args.split(INFO_SEP);
        for(String x : s){
            argList.add(x);
        }
        return argList;
    }

    @Override
    public void reconnectPlayer(ClientHandler b) {
        if(b.isRmi()){
            this.rmiHandler=(RmiClientHandler)b;
            this.rmi=true;
            System.out.println(PURPLE+owner.getCharacter()+" reconnected"+RESET);
        }else{
            this.socketClient=(SocketClientHandler)b;
            this.rmi=false;
            System.out.println(CYAN+owner.getCharacter()+" reconnected"+RESET);
        }
        setDisconnected(false);
    }
}
