package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.Ammo;
import it.polimi.ingsw.model.cards.power_up.PowerUp;
import it.polimi.ingsw.controller.Controller;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that actions are correctly generated
 *
 */
class ActionBuilderTest {

    /**
     * Builds several actions for multiple players and asserts that the actions are correct
     */
    @Test
    void build() {

        Player trump = new Player("Trump","President");
        Player pence  = new Player ("Pence","Vice");
        Player sean = new Player("Sean","PressS");

        ArrayList<Player> players = new ArrayList<>();

        players.add(trump);
        players.add(pence);
        players.add(sean);

        trump.setDead(false);
        pence.setDead(false);
        sean.setDead(false);

        Controller whiteHouse = new Controller(players);
        ArrayList<Action> trumpActions;

        trumpActions = ActionBuilder.build(trump,true);

        assertEquals(3,trumpActions.size());
        trump.getPowerupList().add(new PowerUp(new Ammo(1,0,0),"Teetrasporto"));

        trumpActions = ActionBuilder.build(trump,true);

        assertEquals(4,trumpActions.size());

        whiteHouse.decreaseMoveLeft();
        whiteHouse.decreaseMoveLeft();

        trumpActions = ActionBuilder.build(trump,true);

        assertEquals(2,trumpActions.size());

        whiteHouse.getModel().setMovesLeft(0);

        trumpActions = ActionBuilder.build(trump,true);

        assertEquals(2,trumpActions.size());

        whiteHouse.setHasBot(true);
        whiteHouse.getModel().setTurn(1);
        whiteHouse.getModel().setHasBotAction(true);

        trumpActions = ActionBuilder.build(trump,true);

        assertEquals(3,trumpActions.size());
    }
}