package it.polimi.ingsw.view.virtual_model;

import java.util.ArrayList;
/**
 *used from gui to temporary save some info
 */
public class VirtualGame {

    private String targetSquare = "";
    private String targetPlayer = "";
    private ArrayList<String> targetPlayers  = new ArrayList<>();

    private String weapon = "";
    private String powerup = "";
    private String decision = "";

    private String effect = "";
    private String direction = "";

    private ArrayList<String> hideSquare = new ArrayList<>();

    private ArrayList<String> actions = new ArrayList<>();
    private String action = "";


    public VirtualGame() {
    }

    public synchronized String getDecision() {
        return decision;
    }

    public synchronized void setDecision(String decision) {
        this.decision = decision;
    }

    public synchronized ArrayList<String> getTargetPlayers() {
        return targetPlayers;
    }

    public synchronized void setTargetPlayers(ArrayList<String> targetPlayers) {
        this.targetPlayers = targetPlayers;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public synchronized String getDirection() {
        return direction;
    }

    public synchronized void setDirection(String direction) {
        this.direction = direction;
    }

    public synchronized ArrayList<String> getActions() {
        return actions;
    }

    public synchronized void setActions(ArrayList<String> actions) {
        this.actions = actions;
    }

    public synchronized String getTargetSquare() {
        return targetSquare;
    }

    public synchronized void setTargetSquare(String targetSquare) {
        this.targetSquare = targetSquare;
    }

    public synchronized void setTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public synchronized String getWeapon() {
        return weapon;
    }

    public synchronized void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public synchronized String getPowerup() {
        return powerup;
    }

    public synchronized void setPowerup(String powerup) {
        this.powerup = powerup;
    }

    public synchronized String getEffect() {
        return effect;
    }

    public synchronized void setEffect(String effect) {
        this.effect = effect;
    }

    public synchronized ArrayList<String> getHideSquare() {
        return hideSquare;
    }

}
