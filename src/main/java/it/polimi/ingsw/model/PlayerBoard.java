package it.polimi.ingsw.model;

import it.polimi.ingsw.model.cards.Ammo;
import java.util.*;

/**
 * Playerboard, contains information about ammo, received damage and marks, and points drop upon death
 * @author Yuting Cai
 */

public class PlayerBoard {

    public static final int[] points;

    static {
        points = new int[]{8, 6, 4, 2, 1, 1, 0};
    }

    private static final int INIT_AMMO_RED = 1;
    private static final int INIT_AMMO_BLU = 1;
    private static final int INIT_AMMO_YELLOW = 1;

    /**
     * The board's owner
     */
    private Figure owner;

    /**
     * The damage which a player has received from other players
     */
    private ArrayList<Token> damage;

    /**
     * Array of points released at death in order of contribution
     */
    private int[] pointVec;

    /**
     * the number which the player has died
     */
    private int deathNum=0;

    /**
     * True if the board has been flipped, false otherwise
     */
    private boolean flipped;

    /**
     * The amount of marks the player has received
     */
    private ArrayList<Token> mark;

    /**
     * The amount of ammunition available to the player
     */
    private Ammo ammoInventory;
    private static final int MAX_AMMO = 3;

    private static final int MAX_HEALTH = 11;

    /**
     * Initiates the board with default values
     * @param owner the board's player
     */

    public PlayerBoard(Figure owner) {

        this.owner = owner;
        this.damage = new ArrayList<>();
        this.pointVec = new int[7];
        this.mark = new ArrayList<>();
        this.ammoInventory = new Ammo(INIT_AMMO_RED,INIT_AMMO_BLU,INIT_AMMO_YELLOW);

        this.pointVec[0]=points[0];
        this.pointVec[1]=points[1];
        this.pointVec[2]=points[2];
        this.pointVec[3]=points[3];
        this.pointVec[4]=points[4];
        this.pointVec[5]=points[5];
        this.pointVec[6]=points[6];

        this.flipped=false;

    }

    /**
     * Adds a damage token to the player's board
     * @param t the token to apply
     */

    public void damage(Token t) {

        if(this.damage.size() < MAX_HEALTH){

            this.damage.add(t);
        } else if (this.damage.size() == MAX_HEALTH) {

            this.damage.add(t);
            t.getOwner().getPersonalBoard().addMark(new Token(this.owner));
        }

    }

    /**
     * Adds a damage token to the player's board,
     * also if a player has a marks of the same owner applies the marks
     * @param t the token to apply
     */

    public void addDamage(Token t){

        while(mark.contains(t)) {

            mark.remove(t);
            damage(t);
        }

        damage(t);

    }

    /**
     * Resets the damage to zero
     */

    public void resetDamage() {
        this.damage = new ArrayList<>();
    }

    /**
     * Adds a mark token to the player's board
     * @param t the token to apply
     */

    public void addMark(Token t){

        this.mark.add(t);

    }

    /**
     * Removes a mark token to the player's board
     * @param t the token to apply
     */

    public void removeMark(Token t) {
        this.mark.remove(t);
    }

    /**
     * Adds a Skull to the Figure, reducing points dropped upon death
     */

    public void addSkull() {

        int i;

        for(i=0;i<this.pointVec.length-1;i++) {

            this.pointVec[i] = this.pointVec[i + 1] ;

        }
        deathNum++;

    }

    /**
     * Adds an amount of ammo to the player's ammunition inventory
     * @param a the number of ammunition to add
     */

    public void addAmmo(Ammo a) {

        owner.getPersonalBoard().getAmmoInventory().setRed(a.getRed() + owner.getPersonalBoard().getAmmoInventory().getRed());
        owner.getPersonalBoard().getAmmoInventory().setBlue(a.getBlue() + owner.getPersonalBoard().getAmmoInventory().getBlue());
        owner.getPersonalBoard().getAmmoInventory().setYellow(a.getYellow() + owner.getPersonalBoard().getAmmoInventory().getYellow());

        if(owner.getPersonalBoard().getAmmoInventory().getRed() > MAX_AMMO){
            owner.getPersonalBoard().getAmmoInventory().setRed(MAX_AMMO);
        }

        if(owner.getPersonalBoard().getAmmoInventory().getBlue() > MAX_AMMO){
            owner.getPersonalBoard().getAmmoInventory().setBlue(MAX_AMMO);
        }

        if(owner.getPersonalBoard().getAmmoInventory().getYellow() > MAX_AMMO){
            owner.getPersonalBoard().getAmmoInventory().setYellow(MAX_AMMO);
        }
    }

    /**
     * Removes an amount of ammo from the player's ammunition inventory
     * @param a the number of ammunition to remove
     */

    public boolean removeAmmo(Ammo a) {
        int red = owner.getPersonalBoard().getAmmoInventory().getRed();
        int blue = owner.getPersonalBoard().getAmmoInventory().getBlue();
        int yellow = owner.getPersonalBoard().getAmmoInventory().getYellow();

        if(red-a.getRed()<0)
            return false;
        if(blue-a.getBlue()<0)
            return false;
        if(yellow-a.getYellow()<0)
            return false;
        owner.getPersonalBoard().getAmmoInventory().setRed(red-a.getRed());
        owner.getPersonalBoard().getAmmoInventory().setBlue(blue-a.getBlue());
        owner.getPersonalBoard().getAmmoInventory().setYellow(yellow-a.getYellow());
        return true;
    }

    /**
     * Checks if the player must flip their board when starting frenzy mode
     * @return true if the board should be flipped, false otherwise
     */

    public boolean canFlip(){
        return this.getDamage().isEmpty();
    }


    ///Getters and Setters///

    public ArrayList<Token> getDamage() {
        return damage;
    }

    public void setDamage(ArrayList<Token> damage) {
        this.damage = damage;
    }

    public int[] getPointVec() {
        return pointVec;
    }

    public void setPointVec(int[] pointVec) {
        this.pointVec = pointVec;
    }

    public ArrayList<Token> getMark() {
        return mark;
    }

    public void setMark(ArrayList<Token> marks) {
        this.mark = marks;
    }

    public Ammo getAmmoInventory() {
        return ammoInventory;
    }

    public void setAmmoInventory(Ammo ammoInventory) {
        this.ammoInventory = ammoInventory;
    }

    public Figure getOwner() {
        return owner;
    }

    public int getDeathNum() {
        return deathNum;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public void setDeathNum(int deathNum) {
        this.deathNum = deathNum;
    }
}