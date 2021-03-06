package it.polimi.ingsw.controller;
import it.polimi.ingsw.model.cards.weapon.Weapon;

import java.util.ArrayList;

/**
 * controller state of choosing a weapon
 */

public class ChoosingWeapon implements ControllerState {

    /**
     * the controller reference
     */
    private Controller controller;

    /**
     * Default constructor
     * @param controller the reference controller
     */
    ChoosingWeapon(Controller controller) {
        this.controller = controller;
    }

    /**
     * Makes the player choose a weapon through the view and afterwards
     * sets the current state of controller to shooting
     */

    @Override
    public void executeState() {

        //load the current player's weapons into "options"

        ArrayList<Weapon> options = new ArrayList<>(this.controller.getCurrentPlayer().getWeaponsList());

        options.removeIf(w -> !w.isLoaded());

        this.controller.checkInactivity();
        //Displays the options to the view and the view returns the chosen weapon
        Weapon choice = this.controller.getView().showWeapon(options);

        if (choice == null&&(controller.getView().isDisconnected()||controller.getView().isInactive())) {
            controller.endTurn();
        } else if(choice == null){
            this.controller.goBack();
            this.controller.choosingMove.executeState();
        }else {
            //Sets the shooting state's weapon to the chosen one and changes the current state to shooting
            ((Shooting) this.controller.getShooting()).setShootingWith(choice);
            this.controller.setCurrentState(this.controller.shooting);
            this.controller.currentState.executeState();
        }
    }


}
