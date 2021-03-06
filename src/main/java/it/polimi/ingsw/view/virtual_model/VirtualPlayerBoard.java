package it.polimi.ingsw.view.virtual_model;

import java.util.ArrayList;
/**
 *saves all personal info
 */
public class VirtualPlayerBoard {
    private ArrayList<String> damage=new ArrayList<>();
    private ArrayList<String> marks= new ArrayList<>();
    private int skulls;
    private int ammoRed = 3;
    private int ammoBlue = 3;
    private int ammoYellow = 3;
    private boolean flipped;

    public ArrayList<String> getDamage() {
        return damage;
    }

    public void setDamage(ArrayList<String> damage) {
        this.damage = damage;
    }

    public ArrayList<String> getMarks() {
        return marks;
    }

    public void setMarks(ArrayList<String> marks) {
        this.marks = marks;
    }

    public int getSkulls() {
        return skulls;
    }

    public void setSkulls(int skulls) {
        this.skulls = skulls;
    }

    public int getAmmoRed() {
        return ammoRed;
    }

    public void setAmmoRed(int ammoRed) {
        this.ammoRed = ammoRed;
    }

    public int getAmmoBlue() {
        return ammoBlue;
    }

    public void setAmmoBlue(int ammoBlue) {
        this.ammoBlue = ammoBlue;
    }

    public int getAmmoYellow() {
        return ammoYellow;
    }

    public void setAmmoYellow(int ammoYellow) {
        this.ammoYellow = ammoYellow;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }
}
