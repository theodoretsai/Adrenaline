package it.polimi.ingsw.model;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.model.board.map.Map;
import it.polimi.ingsw.model.board.map.Room;
import it.polimi.ingsw.model.board.map.Square;

import java.util.*;


/**
 * A figure of any kind in the game
 *
 * @author Yuting Cai
 * Unless method is otherwise stated
 */

public abstract class Figure {


    /**
     * Chosen character
     */

    private String character;

    /**
     * The score of the player
     */

    private int points;

    /**
     * The Player's current position
     */

    private Square position;

    /**
     * Previous position of the player, needed in some weapon's effects
     */

    private Square oldPosition;

    /**
     * The player's board
     */

    private PlayerBoard personalBoard;

    /**
     * Main model reference
     */

    private GameModel model;

    /**
     * List of available actions
     */

    private ArrayList<Action> actions;

    /**
     * true is the player is dead, false otherwise
     */

    private boolean isDead;

    /**
     * returns the list of squares within the range
     * @param range the range within the player can move
     * @return the list of possible destinations
     */

    public ArrayList<Square> canGo(int range) {

        int row;
        int column;

        ArrayList<Square> destinations = new ArrayList<>();

        for (row = 0; row < Map.HEIGHT; row++) {
            for (column = 0; column < Map.WIDTH; column++) {
                if (this.distanceTo(this.model.getBoard().getMap().getSquareMatrix()[row][column]) <= range) {

                    destinations.add(this.model.getBoard().getMap().getSquareMatrix()[row][column]);

                }
            }
        }

        return destinations;
    }

    /**
     * default constructor
     * @param character the chosen character
     */

    public Figure(String character){

        this.character = character;
        this.points = 0;
        this.position = null;
        this.oldPosition = null;
        this.personalBoard = new PlayerBoard(this);
        this.actions = new ArrayList<>();
        this.isDead = true;


    }


    /**
     * For testing only
     */
    public Figure(String character, Square position) {
        this.character = character;
        this.position = position;
    }

    /**
     * Default constructor
     */
    public Figure(String character, GameModel model) {
        this.character = character;
        this.model = model;
        this.character = character;
        this.points = 0;
        this.position = null;
        this.oldPosition = null;
        this.personalBoard = new PlayerBoard(this);
        this.actions = new ArrayList<>();
        this.isDead = true;

    }

    /**
     * Default constructor
     */
    public Figure() {
        this.points = 0;
        this.position = null;
        this.oldPosition = null;
        this.personalBoard = new PlayerBoard(this);
        this.actions = new ArrayList<>();

    }

    public Square getPosition(){
        return position;
    }


    /**
     * Verifies whether a player can see another one
     *
     * @param p the player you want to know whether the current player can see or not
     * @return true if the current player can see the other one; false otherwise
     */

    public boolean canSee(Figure p) {
        if(this.getPosition()==null||p.getPosition()==null)
            return false;
        if (this.getPosition().getRoom() == p.getPosition().getRoom()) {
            return true;

        } else if (this.getPosition().getNorth() != null && getPosition().getNorth().getRoom().equals(p.getPosition().getRoom())) {
            return true;

        } else if (this.getPosition().getEast() != null && getPosition().getEast().getRoom().equals(p.getPosition().getRoom())) {
            return true;

        } else if (this.getPosition().getSouth() != null && getPosition().getSouth().getRoom().equals(p.getPosition().getRoom())){
            return true;

        } else
            return this.getPosition().getWest() != null && getPosition().getWest().getRoom().equals(p.getPosition().getRoom());
        //intellij simplification return false if last condition is not verified
    }

    /**
     * Verifies whether a player can see a square
     * @param s the {@link Square} you want to know whether the current player can see or not
     * @return true is the current player can see the square; false otherwise
     */

    public boolean canSeeSquare(Square s){

        if (position.getRoom() == s.getRoom()) {
            return true;

        } else if (position.getNorth() != null && position.getNorth().getRoom().equals(s.getRoom())) {
            return true;

        } else if (position.getEast() != null && position.getEast().getRoom().equals(s.getRoom())) {
            return true;

        } else if (position.getWest() != null && position.getWest().getRoom().equals(s.getRoom())) {
            return true;

        } else return position.getSouth() != null && position.getSouth().getRoom().equals(s.getRoom());
        //intellij simplification return false if last condition is not verified
    }

    /**
     * Verifies whether a player can see a room
     * @param r the {@link Room} you want to know whether the current player can see or not
     * @return true is the current player can see the room; false otherwise
     */

    public boolean canSeeRoom(Room r){

        if (getPosition().getRoom() == r) {
            return true;

        } else if (getPosition().getNorth() != null && getPosition().getNorth().getRoom().equals(r)) {
            return true;

        } else if (getPosition().getEast() != null && getPosition().getEast().getRoom().equals(r)) {
            return true;

        } else if (getPosition().getWest() != null && getPosition().getWest().getRoom().equals(r)) {
            return true;

        } else return getPosition().getSouth() != null && getPosition().getSouth().getRoom().equals(r);
        //intellij simplification return false if last condition is not verified

    }


    /**
     * @return a list of all players visible.
     * @author Gregorio Barzasi
     */

    public Set<Figure> allCanSee(){
        Set<Figure> visible = new HashSet<>();

        for(Figure p : this.allFigures()){
            if(this.canSee(p))
                visible.add(p);
        }
        return visible;
    }


    /**
     * @return a list of all players.
     * @author Gregorio Barzasi
     */
    public Set<Figure> allFigures(){
        Set<Figure> f = new HashSet<>(model.getPlayerList());
        if(model.getBot()!=null)
            f.add(model.getBot());
        return f;
    }

    /**
     * Make damage and set marks
     * @author Gregorio Barzasi
     */

    //if you start from 0 you use < mate.  :)

    public void inflictDamage(int num, Figure target){
        Token t = new Token(this);
        for(int i=0; i<num; i++) {
            target.getPersonalBoard().addDamage(t);
        }
    }
    public void inflictMark(int num, Figure target){
        Token t = new Token(this);
        for(int i=0; i<num; i++) {
            target.getPersonalBoard().addMark(t);
        }
    }


    /**
     * Calculates the distance between a player and a square
     * @param s the {@link Square} you wanna know the distance to
     * @return the distance to that {@Link Square}, -1 if unreachable:
     * meaning that the square is a blank since all maps are completely connected
     *
     * calculated using Breadth-first search
     *
     * @author Yuting Cai
     */

    public int distanceTo(Square s) {

        int distance = 0;
        int size;
        int i;
        LinkedList<Square> next = new LinkedList<>();
        HashSet<Square> visited = new HashSet<>();
        Square sq=this.getPosition();
        if (sq==null)
            return -1;
        next.add(sq);

        while(!next.isEmpty()) {


            size = next.size();

            for (i=0 ;i < size; i++){
                Square node = next.getFirst();
                next.pop();


                if (node == s) {
                    return distance;
                }

                if (node.getNorth() != null && !visited.contains(node.getNorth())) {
                    next.add(node.getNorth());
                }
                if (node.getEast() != null && !visited.contains(node.getEast())) {
                    next.add(node.getEast());
                }
                if (node.getSouth() != null && !visited.contains(node.getSouth())) {
                    next.add(node.getSouth());
                }
                if (node.getWest() != null && !visited.contains(node.getWest())) {
                    next.add(node.getWest());
                }

                visited.add(node);
            }

            distance++;
        }

        return -1;

    }

    /**
     * RIP
     * Manages player death, should be invoked and the end of the turn in which the player is killed
     * distributes points to the players who contributed to this player's death
     * shifts this players's point vector
     * resets this player's damage array
     */

    public void die(){

        HashMap<Figure,Integer> contributors = new HashMap<>();
        int tmp;
        int i;

        //give point to the player who inflicted first blood
        if (!this.model.isFrenzy() && !this.getPersonalBoard().getDamage().isEmpty()) {
            this.getPersonalBoard().getDamage().get(0).getOwner().addPoints(1);
        }

        //maps each player with their contribution to the list
        for (Token t : this.getPersonalBoard().getDamage()){

            if(!contributors.containsKey(t.getOwner())) {
                contributors.put(t.getOwner(),1);
            }else{
                tmp = contributors.get(t.getOwner());
                contributors.replace(t.getOwner(),tmp+1);
            }
        }

        //loads all players who have contributed to the kill into an Arraylist
        ArrayList<Figure> murderers = new ArrayList<>(contributors.keySet());



        //initialising a new arraylist of ordered contributors
        ArrayList<Figure> ordered = new ArrayList<>();
        boolean added;
        //inserts in order into a new list


        //sorted insertion into ordered
        while(!murderers.isEmpty()){


            added = false;

            for(i=0;i<ordered.size();i++) {

                if (contributors.get(ordered.get(i)) == contributors.get(murderers.get(0))) {
                    ordered.add(i, murderers.get(0));
                    murderers.remove(0);
                    added = true;
                    break;
                }
                if (!added && contributors.get(ordered.get(i)) > contributors.get(murderers.get(0))) {
                    ordered.add(i, murderers.get(0));
                    murderers.remove(0);
                    added = true;
                    break;
                }
            }

            if(!added) {
                ordered.add(murderers.get(0));
                murderers.remove(0);
            }

        }


        Collections.reverse(ordered);

        int k;

        //in case of same damage dealt by multiple players the one who hit firs get sorted in front
        for(i=0;i<ordered.size();i++) {
            for (k = i; k < ordered.size(); k++) {
                if (contributors.get(ordered.get(k)).equals(contributors.get(ordered.get(i))) && damagePriority(ordered.get(k)) < damagePriority(ordered.get(i))) {

                    Figure temp = ordered.get(i);
                    ordered.set(i,ordered.get(k));
                    ordered.set(k,temp);

                }
            }
        }

        for( i=0 ; i<ordered.size() ; i++) {

            ordered.get(i).addPoints(this.getPersonalBoard().getPointVec()[i]);

        }

        this.getPersonalBoard().resetDamage();
        this.getPersonalBoard().addSkull();
        this.isDead = true;

    }

    /**
     * private method used in die() for determining scoring
     * priority inc ase 2 player deal the same damage
     * @param f1 Figure to check if this has priority over
     * @return true if this has priority, false otherwise
     */

    private int damagePriority(Figure f1){

        int i;
        for(i=0;i<this.getPersonalBoard().getDamage().size();i++){
            if(this.getPersonalBoard().getDamage().get(i).getOwner().equals(f1)){
                return i;
            }
        }

        return -1;
    }


    public int getPoints() {
        return points;
    }

    /**
     * adds a desired amount of points to the player
     * @param addedPoints desired amount of points
     */

    public void addPoints(int addedPoints){
        this.points += addedPoints;
    }

    /*just a bunch of setters and getters*/



    public Square getOldPosition() {
        return oldPosition;
    }

    public void setOldPosition(Square oldPosition) {
        this.oldPosition = oldPosition;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public void setPosition(Square position) {
        this.position = position;
    }

    public PlayerBoard getPersonalBoard() {
        return personalBoard;
    }

    public void setPersonalBoard(PlayerBoard personalBoard) {
        this.personalBoard = personalBoard;
    }

    public GameModel getModel() {
        return model;
    }

    public void setModel(GameModel model) {
        this.model = model;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }
}