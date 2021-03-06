package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.cards.weapon.Weapon;

import java.util.*;

import static it.polimi.ingsw.connection.ConnMessage.INFO_SEP;
import static it.polimi.ingsw.connection.ConnMessage.INNER_SEP;

public class

/**
 * An armory containing max 3 weapons the player can choose from
 */

Armory {

    /**
     * List of weapons in the armory
     */

    private ArrayList<Weapon> weaponList;

    /**
     * Defines the maximum capacity
     */
    private static final int MAX_CAPACITY = 3;

    /**
     * Checks if the armory is at maximum capacity of 3
     * @return true if as maximum capacity, false otherwise
     *
     * @author Yuting Cai
     */

    public boolean isFull(){

        return this.weaponList.size() == MAX_CAPACITY;
    }

    public Armory(ArrayList<Weapon> weaponList) {
        this.weaponList = weaponList;
    }

    public ArrayList<Weapon> getWeaponList() {
        return weaponList;
    }

    public String toString(){
        String s="";
        for(Weapon w: weaponList)
            s=s+w.getName()+INNER_SEP+w.getChamber().toString()+INNER_SEP+w.getReloadCost().toString().replaceFirst(w.getChamber().toString(),"")+INFO_SEP;
        return s;
    }
}
