package it.polimi.ingsw.view.command_line_view;

import it.polimi.ingsw.ViewClient;
import it.polimi.ingsw.connection.ConnectionTech;
import it.polimi.ingsw.connection.client.RmiClient;
import it.polimi.ingsw.connection.client.SClient;
import it.polimi.ingsw.connection.client.SClientCommManager;
import it.polimi.ingsw.view.virtual_model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static it.polimi.ingsw.view.command_line_view.CLiBoardStuff.*;
import static it.polimi.ingsw.Color.*;
import static it.polimi.ingsw.view.command_line_view.CliMessages.*;
import static it.polimi.ingsw.connection.ConnMessage.*;

/**
 * this class implements all methods needed in game to actually play the game. it receives request from server and
 * let the player reply
 *
 * @author Gregorio Barzasi
 */

public class CliGame implements ViewClient {

    public static final int THREAD_PRIORITY=3;

    private BufferedReader sc=new BufferedReader(new InputStreamReader(System.in));
    private CliBoard board;
    private UpdateParser parser;
    private ConnectionTech c;

    public CliGame(ConnectionTech c,VirtualPlayer p){
        this.board=new CliBoard(new VirtualModel(p));
        this.parser=new UpdateParser(board.getModel());
        this.c=c;
    }
    /**
     *Initilize the communication and starts the game session
     */
    public void gameStart(){
        if(!c.isRmi()) {
            ((SClient) c).setCommManager(new SClientCommManager(((SClient) c), this));
            ((SClient) c).getCommManager().setPriority(THREAD_PRIORITY);
            ((SClient) c).getCommManager().start();
        }else {
            try {
                ((RmiClient)c).getClientHandler().setView((ViewClient) UnicastRemoteObject.exportObject(this, 0));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        System.out.print("\nAttendi lo spawn della board\n");
    }
    /**
     *Offer a generic choice to the player between elemets of an array
     */
    public String genericChoice(ArrayList<String> args,String q, String error, boolean printArgs) {
        int i =1;
        String[] temp;
        int reply=0;
        do {
            i=1;
            System.out.print(RESET);
            if(printArgs) {
                for (String s : args) {
                    temp = s.split(INNER_SEP);
                    System.out.print("( "+i + "- " + temp[0]);
                    if (ALL_AMMO.contains(temp[1])) {
                        System.out.print("<");
                        board.colorizeCP(temp[1]);
                        System.out.print("> )  ");
                    } else
                        System.out.print(" )  ");
                    i++;
                }
            }
            System.out.print(RESET+q+"\n");
            reply=chooseFromArray(args,error);
        } while (reply==0);
        if(reply==-1)
            return NOTHING;
        return args.get(reply-1);
    }
    /**
     *print array element and verify that the reply is in the array
     */
    public int chooseFromArray(ArrayList<String>args, String err){
        String s ="";
        int reply=0;
        try {
            //clean the input
            while(sc.ready()) {
                sc.read();
            }
            s = sc.readLine();
            if(s.equals(BACK))
                return -1;
            try {
                reply = Integer.parseInt(s);
            }catch(NumberFormatException e){
                reply=0;
            }
            if (reply<=0 || args.size()<(reply)) {
                System.out.println(err);
                reply=0;
            }
        } catch (IOException e) {
        e.printStackTrace();
    }
        return reply;
    }
    /**
     * Shows the PowerUps and makes the user choose one, then returns it
     *
     * @param args the Powerups to show
     * @return the chosen one
     */
    public String showPowerUp(ArrayList<String> args) {
        return genericChoice(args,CHOOSE_PU_Q,CHOOSE_PU_ERR,true);
    }

    /**
     * Shows the Weapons and makes the user choose one, then returns it
     *
     * @param args the Weapons to show
     * @return the chosen one
     */
    public String showWeapon(ArrayList<String> args) {
        int i;
        String[] temp;
        int reply=0;
        do {
            i=1;
            System.out.print(RESET+"\n");
                for (String s : args) {
                    temp = s.split(INNER_SEP);
                    System.out.print("("+i + "- " + temp[0]+"[");
                    board.colorizeCP(temp[1]);
                    System.out.print("]");
                    if(temp.length>2)
                        board.colorizeCP(temp[2]);
                    System.out.print(")  ");
                    i++;
                }
            System.out.print(RESET+CHOOSE_WP_Q+"\n");
            reply=chooseFromArray(args,CHOOSE_WP_ERR);
        } while (reply==0);
        if(reply==-1)
            return NOTHING;
        return args.get(reply-1);
    }

    /**
     * Show the possible moves that a player can perform then makes the user executeState one,
     * then returns it
     *
     * @param args the moves to show
     * @return the chosen one
     */
    public String showActions(ArrayList<String> args) {
        int i =1;
        String[] temp;
        int reply=0;
        do {
            i=1;
            System.out.print(RESET);

                for (String s : args) {
                    temp = s.split(INNER_SEP);
                    if(ACTION_TRANSLATION.containsKey(temp[0].toLowerCase().replace(" ","_")))
                        temp[0]=ACTION_TRANSLATION.get(temp[0].toLowerCase().replace(" ","_"));
                    System.out.print("( "+i + "- " + temp[0]);
                    if (!temp[1].equals("0")&&!temp[1].equals("100")) {
                        System.out.print(": "+temp[1]);
                    }
                        System.out.print(" )  ");
                    i++;
                }

            System.out.print(RESET+CHOOSE_ACTION_Q+"\n");
            reply=chooseFromArray(args,CHOOSE_ACTION_ERR);
        } while (reply==0);
        if(reply==-1)
            return NOTHING;
        return args.get(reply-1);
    }

    /**
     * Shows the possible destinations a figure can reach and then returns the chosen value
     *
     * @param args the possible destinations
     * @return the chosen one
     */
    public String showPossibleMoves(ArrayList<String> args) {
        VirtualPlayer owner=board.getModel().getOwner();
        String reply="";
        String[] temp={"",""};
        String charTemp;
        HashMap<String,String> column=new HashMap<>();
        column.put("A","0");
        column.put("B","1");
        column.put("C","2");
        column.put("D","3");
        HashMap<String,String> row=new HashMap<>();
        row.put("1","0");
        row.put("2","1");
        row.put("3","2");
        row.put("4","3");

        do {
            if (args.get(0).equals("true")) {
                args.remove(0);
                printCells(args);
            }
            System.out.println(WHITE+CHOOSE_SQUARE_Q);
            try {
                reply = sc.readLine();
                if(reply.equals("<"))
                    return NOTHING;
                if(reply.equals(">")) {
                    reply="MINE";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            reply=reply.toUpperCase();
            charTemp=reply.replaceAll(INNER_SEP,"");
            if(charTemp.length()>1) {
                temp[0] = String.valueOf(charTemp.toCharArray()[0]);
                temp[1] = String.valueOf(charTemp.toCharArray()[1]);
            }
            if(reply.isEmpty()||(charTemp.toCharArray().length==2&&row.containsKey(temp[0])&&column.containsKey(temp[1])))
                reply=row.get(temp[0])+INNER_SEP+column.get(temp[1]);
            else if(reply=="MINE")
                reply = owner.getRow()+ INNER_SEP +owner.getColumn();
            else {
                System.out.println(CHOOSE_SQUARE_ERR);
                reply = "";
            }
        }while (reply.isEmpty()||!args.contains(reply));

        return reply;
    }

    /**
     * Shows the Cells
     *
     * @param args the Cells to show
     */
    public void printCells(ArrayList<String> args) {
        HashMap<String,String> orizontal=new HashMap<>();
        orizontal.put("0","A");
        orizontal.put("1","B");
        orizontal.put("2","C");
        orizontal.put("3","D");
        HashMap<String,String> vertical=new HashMap<>();
        vertical.put("0","1");
        vertical.put("1","2");
        vertical.put("2","3");
        vertical.put("3","4");

        String[] temp;
        for(String s: args){
            temp=s.split(INNER_SEP);
            System.out.print(vertical.get(temp[0])+INNER_SEP+orizontal.get(temp[1]));
            System.out.println();
        }

    }

    /**
     * Shows the valid targets a Figure can target, then returns the targets the user decides
     * to hit
     *
     * @param args the valid targets
     * @return the chosen targets
     */
    public String showTargets(ArrayList<String> args) {
        return genericChoice(args,CHOOSE_TARGET_Q,CHOOSE_TARGET_ERR,true);
    }

    /**
     * returns the chosen one direction
     *
     * @return the chosen one
     */
    public String chooseDirection(ArrayList<String> args) {
        clearScreen();
        board.draw();

        int i=0;
        String reply="";
        while(true){
            i=1;
            System.out.println(RESET+SHOW_DIRECTION_Q);
            for (String s : args) {
                System.out.print(" ");
                board.printPawn(s);
                System.out.print(" ");
                i++;
            }
            System.out.print(")");
            System.out.println();
            try {
                while(sc.ready()) {
                    sc.read();
                }
                reply = sc.readLine();
                if(reply.equals(BACK))
                    return NOTHING;

                reply=reply.toLowerCase();
                if (ALL_DIRECTIONS.contains(reply)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print(SHOW_DIRECTION_ERR);
        }

        return reply;
    }


    /**
     * Displays a message and makes the user make a boolean choice
     *
     * @param message the question's message
     * @return the user's choice
     */
    public boolean showBoolean(String message) {
        String s;
        System.out.println("\n"+message+"(s/n)");
        try {
            do
                s = sc.readLine();
            while (s.isEmpty());

            return (s.equals("s")||s.equals("S")||s.equals("y")||s.equals("Y"));

        }catch (IOException e){
            return false;
        }
    }

    /**
     * Displays a message to the user
     *
     * @param message the message
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays the leaderboard of the game to the user
     */
    public void displayLeaderboard(ArrayList<String> args) {



        HashMap<Integer,VirtualPlayer> leaderBoard = new HashMap<>();

        for(VirtualPlayer p : board.getModel().getAllPlayers())
            leaderBoard.put(args.indexOf(p.getCharacter()),p);
        System.out.println(ADRENALINA_HEAD);
        System.out.println();
        for(int i=0; i<args.size();i++) {
            VirtualPlayer k=leaderBoard.get(i);
            System.out.print(RESET+"├{"+GREEN +(i+1)+RESET + "}-" +PURPLE_BOLD_BRIGHT);
            System.out.print( k.getUsername() + " con " + k.getCharacter()+RESET+" (");
            board.printPawn(k.getCharacter());
            System.out.println(") " +k.getPoints());
        }
        Thread t = new Thread(() -> {
            System.out.println("\nPremi un tasto per chiudere");
            try {
                sc.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        });
        t.start();
        }

    /**
     *Shows the effect of weapon actually in use
     *
     */
    public String showEffects(ArrayList<String> args) {
        int i;
        String[] temp;
        int reply=0;
        do {
            i=1;
            System.out.println(RESET+SHOW_EFFECTS_Q);
            for (String s : args) {
                temp = s.split(INNER_SEP);
                System.out.print("("+i + "- " + temp[0]+")[");
                if(!temp[1].equals(NOTHING))
                     board.colorizeCP(temp[1]);
                System.out.print("]  ");
                i++;
            }
            System.out.println(RESET);
            reply=chooseFromArray(args,SHOW_EFFECTS_ERR);
        } while (reply==0);
        if(reply==-1)
            return NOTHING;
        return args.get(reply-1);
    }
    /**
     *calls the update
     *
     * @param message update info
     */
    public void updateModel(String message) {
        parser.updateModel(message);
        clearScreen();
        board.draw();
    }



    /**
     * Show the possible target that a player can hit then makes the user choose one,
     * then returns it
     *
     * @param args the target to show
     * @return the target choice
     */
    public String showTargetAdvanced(ArrayList<String> args) {
        clearScreen();
        board.draw();
        String[] temp;
        temp=args.get(0).split(INNER_SEP);
        args.remove(0);
        ArrayList<String> allTargets=new ArrayList<>();

        int maxNum=Integer.parseInt(temp[0]);
        boolean fromDiffSquare = Boolean.parseBoolean(temp[1]);
        String msg = temp[2];

        int i =1;
        int reply=0;
        while(allTargets.size()<maxNum){
            i=1;
            System.out.println(RESET+msg);
            System.out.println(RESET);
            for (String s : args) {
                System.out.print("("+i + "- ");
                board.printPawn(s);
                System.out.print(")  ");
                i++;
            }
            if(!allTargets.isEmpty()) {
                System.out.print("[ ");
                for (String s : allTargets) {
                    board.printPawn(s);
                    System.out.print(" ");
                }
                System.out.print("]  ");
            }
            System.out.println();
            String s;
            try {
                //clean the input
                while(sc.ready()) {
                    sc.read();
                }
                s = sc.readLine();
                if(s.equals(BACK))
                    return NOTHING;
                if(s.equals(NEXT)) {
                    break;
                }
                try {
                    reply = Integer.parseInt(s);
                }catch(NumberFormatException e){
                    reply=0;
                }

                if (reply<1 || args.size()<(reply)) {
                    System.out.println(SHOW_TARGET_ADV_ERR);
                }else if (!fromDiffSquare || verifyDiffSquare(allTargets,args.get(reply-1))){
                    allTargets.add(args.get(reply-1));
                    args.remove(args.get(reply-1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String def="";
        for(String k: allTargets)
            def=def+k+INFO_SEP;

        return def;
    }
    /**
     * Verify that players are from different square
     *
     */
    public boolean verifyDiffSquare(ArrayList<String> args,String test){
        VirtualPlayer p = board.getModel().findPlayer(test);
        VirtualPlayer v;
        for(String s: args) {
            v = board.getModel().findPlayer(s);
            if (p.getRow() == v.getRow() && p.getColumn() == v.getColumn()) {
                return false;
            }
        }
        return true;
    }

    public boolean isConnected(){
        return true;
    }
}
