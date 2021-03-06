package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.board.map.SpawnSquare;
import it.polimi.ingsw.model.board.map.Square;
import it.polimi.ingsw.model.cards.weapon.Weapon;

import java.util.ArrayList;

/**
 * controller state of picking a weapon
 */


public class PickingWeapon implements ControllerState {

    private static final int max = 3;

    /**
     * the location the player is picking from
     */
    private Square location;

    /**
     * the controller reference
     */
    private Controller controller;

    /**
     * Default constructor
     * @param controller the reference controller
     */
    PickingWeapon(Controller controller) {
        this.controller = controller;
    }

    /**
     * Makes the player select a weapon fro the armory to pick, if the player already
     * has three weapons calls discardWeapon() method to manage discarding
     */

    @Override
    public void executeState(){


        ArrayList<Weapon> options = new ArrayList<>(((SpawnSquare) this.location).getArmory().getWeaponList());

        this.controller.checkInactivity();
        Weapon choice = this.controller.getView().showWeapon(options);

        if (choice == null&&(controller.getView().isDisconnected()||controller.getView().isInactive())) {
            controller.endTurn();
        } else if(choice == null){

            this.controller.goBack();

        }else {

            if (this.controller.getCurrentPlayer().getPersonalBoard().getAmmoInventory().covers(choice.getReloadCost().unchambered(choice.getChamber()))){
                this.controller.getCurrentPlayer().getPersonalBoard().getAmmoInventory().subtract(choice.getReloadCost().unchambered(choice.getChamber()));
                if (this.controller.getCurrentPlayer().getWeaponsList().size() < max) {

                    this.controller.getCurrentPlayer().getWeaponsList().add(choice);
                    this.controller.decreaseMoveLeft();
                    ((SpawnSquare) this.location).getArmory().getWeaponList().remove(choice);

                    choice.setOwner(this.controller.getCurrentPlayer());

                    this.controller.getCurrentPlayer().setPosition(this.location);

                    this.controller.update();
                    this.controller.goBack();

                } else {

                    this.discardWeapon(choice);

                }
            }else{
                this.controller.getView().displayMessage(ControllerMessages.CANNOT_PICK_WEAPON);
                this.controller.goBack();
            }
        }
    }

    /**
     * Makes the player choose one weapon between four to dicard, then
     * puts the rest into the player's weapon list
     * @param arg the weapons to choose from
     */

    private void discardWeapon(Weapon arg){

        ArrayList<Weapon> options = new ArrayList<>(this.controller.getCurrentPlayer().getWeaponsList());

        Weapon choice = this.controller.getView().showWeapon(options);

        if(choice != null) {
            options.remove(choice);
            options.add(arg);

            this.controller.getCurrentPlayer().getWeaponsList().clear();
            this.controller.getCurrentPlayer().getWeaponsList().addAll(options);

            ((SpawnSquare) this.location).getArmory().getWeaponList().remove(arg);
            ((SpawnSquare) this.location).getArmory().getWeaponList().add(choice);
            choice.setLoaded(true);
            arg.setOwner(this.controller.getCurrentPlayer());
            this.controller.decreaseMoveLeft();
            this.controller.getCurrentPlayer().setPosition(this.location);
        }

        this.controller.getCurrentPlayer().setPosition(this.location);
        this.controller.decreaseMoveLeft();
        this.controller.update();
        this.controller.goBack();

    }

    void setLocation(Square location) {
        this.location = location;
    }
}
