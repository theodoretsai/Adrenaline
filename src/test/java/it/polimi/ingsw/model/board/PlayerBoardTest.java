package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.PlayerBoard;
import it.polimi.ingsw.model.Token;
import it.polimi.ingsw.model.cards.Ammo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerBoardTest {


    /**
     * Test verifies that Mercader correctly deals damage to Trotsky
     * First verifies that Trotsky cannot receive damage after being overkilled
     *
     * Also verifies that Mercader has received Trotsky's vendetta mark
     */

    @Test
    void damage() {


        Player trotsky = new Player("Trotsky","victim");


        Player mercader = new Player("Mercader","assassin");


        assertTrue(trotsky.getPersonalBoard().getDamage().isEmpty());

        Token t = new Token(mercader);


        int i;
        for(i = 0 ; i < 4 ; i++){

            trotsky.getPersonalBoard().damage(t);

        }

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),4);

        for(i=4 ; i<11 ; i++){

            trotsky.getPersonalBoard().damage(t);

        }

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),11);

        trotsky.getPersonalBoard().damage(t);

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),12);

        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);


        assertEquals(trotsky.getPersonalBoard().getDamage().size(),12);

        assertFalse(mercader.getPersonalBoard().getMark().isEmpty());
        assertSame(mercader.getPersonalBoard().getMark().get(0).getOwner(), trotsky);


    }

    /**
     * Test that damage is reset correctly
     */

    @Test
    void resetDamage() {

        Player trotsky = new Player("Trotsky","victim");


        Player mercader = new Player("Mercader","assassin");

        assertTrue(trotsky.getPersonalBoard().getDamage().isEmpty());

        Token t = new Token(mercader);


        int i;
        for(i = 0 ; i < 4 ; i++){

            trotsky.getPersonalBoard().damage(t);

        }

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),4);

        for(i=4 ; i<11 ; i++){

            trotsky.getPersonalBoard().damage(t);

        }

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),11);

        trotsky.getPersonalBoard().damage(t);

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),12);

        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);
        trotsky.getPersonalBoard().damage(t);


        assertEquals(trotsky.getPersonalBoard().getDamage().size(),12);

        trotsky.getPersonalBoard().resetDamage();

        assertTrue(trotsky.getPersonalBoard().getDamage().isEmpty());

    }

    /**
     * Tests that Marks are added correctly
     */

    @Test
    void addMark() {

        Player trotsky = new Player("Trotsky","victim");


        Player mercader = new Player("Mercader","victim");



        Token t = new Token(mercader);
        Token v = new Token(trotsky);
        trotsky.getPersonalBoard().addMark(t);

        assertFalse(trotsky.getPersonalBoard().getMark().isEmpty());
        assertSame(trotsky.getPersonalBoard().getMark().get(0), t);


        t.getOwner().getPersonalBoard().addMark(v);
        assertFalse(mercader.getPersonalBoard().getMark().isEmpty());
    }

    /**
     * Test verifies that marks are removed correctly
     */

    @Test
    void removeMark() {

        Player trotsky = new Player("Trotsky","victim");


        Player mercader = new Player("Mercader","victim");

        Token t = new Token(mercader);
        Token v = new Token(trotsky);
        trotsky.getPersonalBoard().addMark(t);

        assertFalse(trotsky.getPersonalBoard().getMark().isEmpty());
        assertSame(trotsky.getPersonalBoard().getMark().get(0), t);


        t.getOwner().getPersonalBoard().addMark(v);
        assertFalse(mercader.getPersonalBoard().getMark().isEmpty());

        trotsky.getPersonalBoard().removeMark(t);
        mercader.getPersonalBoard().removeMark(v);

        assertTrue(mercader.getPersonalBoard().getMark().isEmpty());
        assertTrue(trotsky.getPersonalBoard().getMark().isEmpty());
    }

    /**
     * Verifies that the damage is added correctly considering marks
     */

    @Test
    void addDamage() {


        Player trotsky = new Player("Trotsky","victim");


        Player mercader = new Player("Mercader","assassin");

        assertTrue(trotsky.getPersonalBoard().getDamage().isEmpty());

        Token t = new Token(mercader);

        int i;
        for(i=0;i<6;i++) {

            trotsky.getPersonalBoard().addMark(t);

        }

        assertEquals(6,trotsky.getPersonalBoard().getMark().size());

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),0);

        trotsky.getPersonalBoard().addDamage(t);

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),7);
        assertEquals(trotsky.getPersonalBoard().getMark().size(),0);

        for(i=6;i<20;i++) {

            trotsky.getPersonalBoard().addMark(t);

        }

        trotsky.getPersonalBoard().addDamage(t);

        assertEquals(12,trotsky.getPersonalBoard().getDamage().size());
        assertTrue(trotsky.getPersonalBoard().getMark().isEmpty());


    }

    /**
     * Verifies the correct interaction of marks from 2 different players
     */

    @Test
    void addDamage2Players() {


        Player trotsky = new Player("Trotsky","victim");

        Player mercader = new Player("Mercader","assassin");


        Player stalin = new Player("Stalin","contractor");


        assertTrue(trotsky.getPersonalBoard().getDamage().isEmpty());

        Token t = new Token(mercader);
        Token c = new Token(stalin);

        int i;


        //mercader gives 6 tokens to trotsky

        for(i=0;i<6;i++) {

            trotsky.getPersonalBoard().addMark(t);

        }

        //stalin gives 3 tokens to trotsky

        for(i=0;i<3;i++) {

            trotsky.getPersonalBoard().addMark(c);

        }

        //trotsky should have 6+3 marks

        assertEquals(9,trotsky.getPersonalBoard().getMark().size());

        //nobody has dealt any damage yet

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),0);

        //stalin deals 1 damage

        trotsky.getPersonalBoard().addDamage(c);

        //only mercader's 6 marks should be left

        assertEquals(trotsky.getPersonalBoard().getMark().size(),6);

        //mercarder deals 1 damage

        trotsky.getPersonalBoard().addDamage(t);

        //total damage dealt should now be 11 and all marks should have been applied

        assertEquals(trotsky.getPersonalBoard().getDamage().size(),11);
        assertEquals(trotsky.getPersonalBoard().getMark().size(),0);

        //mercader applies 20 marks to trotsky because why not

        for(i=6;i<20;i++) {

            trotsky.getPersonalBoard().addMark(t);

        }

        //mercarder applies an ice pick to trotsky's head

        trotsky.getPersonalBoard().addDamage(t);

        //trotsky recieves the ice pick to the head but fights back giving mercarder a mark, but dies in the hospital

        assertEquals(12,trotsky.getPersonalBoard().getDamage().size());
        assertEquals(1,mercader.getPersonalBoard().getMark().size());

        //all trotsky's marks should be cleared, mercarder wasted his 20 marks

        assertTrue(trotsky.getPersonalBoard().getMark().isEmpty());


    }

    /**
     * Asserts that when skulls are added the player's point vector is correctly shifted
     */

    @Test
    void addSkull() {

        Player joer = new Player("Mormont","Lord Commander");

        int i;
        for(i=0;i<7;i++) {
            assertEquals(joer.getPersonalBoard().getPointVec()[i], PlayerBoard.points[i]);
        }

        joer.getPersonalBoard().addSkull();

        for(i=0;i<6;i++) {
            assertEquals(joer.getPersonalBoard().getPointVec()[i], PlayerBoard.points[i+1]);
        }

        joer.getPersonalBoard().addSkull();

        for(i=0;i<5;i++) {
            assertEquals(joer.getPersonalBoard().getPointVec()[i], PlayerBoard.points[i+2]);
        }

        joer.getPersonalBoard().addSkull();
        joer.getPersonalBoard().addSkull();
        joer.getPersonalBoard().addSkull();
        joer.getPersonalBoard().addSkull();
        joer.getPersonalBoard().addSkull();
        joer.getPersonalBoard().addSkull();

        for(i=0;i<joer.getPersonalBoard().getPointVec().length;i++) {
            assertEquals(joer.getPersonalBoard().getPointVec()[i],0);
        }

    }

    /**
     * tests that ammo is correctly added to the player's inventory
     */
    @Test
    void addAndRemoveAmmo() {

        Ammo a = new Ammo(1,0,0);
        Ammo b = new Ammo(2,1,0);
        Player pip = new Player("pip","watchman");

        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getYellow());
        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getBlue());
        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getRed());

        pip.getPersonalBoard().removeAmmo(b);
        pip.getPersonalBoard().addAmmo(a);

        assertEquals(2,pip.getPersonalBoard().getAmmoInventory().getRed());
        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getBlue());
        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getYellow());

        pip.getPersonalBoard().addAmmo(b);
        pip.getPersonalBoard().addAmmo(b);
        pip.getPersonalBoard().addAmmo(b);
        pip.getPersonalBoard().addAmmo(b);

        assertEquals(3,pip.getPersonalBoard().getAmmoInventory().getRed());
        assertEquals(3,pip.getPersonalBoard().getAmmoInventory().getBlue());
        assertEquals(1,pip.getPersonalBoard().getAmmoInventory().getYellow());


    }

}