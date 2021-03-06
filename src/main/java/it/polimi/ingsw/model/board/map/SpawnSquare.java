package it.polimi.ingsw.model.board.map;

import it.polimi.ingsw.model.board.Armory;

/**
 * A Square that includes a respawn spot, also contains an armory where
 * players can pick a weapon
 * @author Yuting Cai
 */

public class SpawnSquare extends Square {


    /**
     * Armory containing three weapons max
     */
    private Armory armory;


    SpawnSquare(Cell position, Room room) {
        super(position, room);
    }

    public Armory getArmory() {
        return armory;
    }

    public void setArmory(Armory armory) {
        this.armory = armory;
    }

    @Override
    public boolean isSpawn(){
        return true;
    }
}
