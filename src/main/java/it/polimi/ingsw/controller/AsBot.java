package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Figure;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.board.map.Map;
import it.polimi.ingsw.model.board.map.Square;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * controller state corresponding to the phase of using a bot
 */

public class AsBot implements ControllerState{

    /**
     * the controller reference
     */

    private Controller controller;

    /**
     * Default constructor
     * @param controller the reference controller
     */

    AsBot(Controller controller) {
        this.controller = controller;

    }

    /**
     * Makes the player choose a move and then a target to shoot
     */

    @Override
    public void executeState() {

        //Checks if it's the first turn before spawning the bot
        if(this.controller.getModel().getBot().isDead() && this.controller.getModel().getTurn() != 0){
            this.spawnBot();
        }

        this.moveBot();

        //adds visible players to the target list
        ArrayList<Figure> targets = new ArrayList<>();

        for(Player p : this.controller.getModel().getPlayerList()){

            if(this.controller.getModel().getBot().canSee(p) && p!= this.controller.getModel().getCurrentPlayer()){
                targets.add(p);
            }
        }

        if(!targets.isEmpty()) {
            Set<Figure> temp=new HashSet<>(targets);
            //shoots the target

            this.controller.checkInactivity();
            ArrayList<Figure> choice = this.controller.getView().showTargetAdvanced(temp,1,false,ControllerMessages.ASK_TARGET);

            if(choice == null){
                this.botHasMoved();
                this.controller.update();
                this.controller.goBack();
            }else {
                this.controller.getModel().getBot().shoot((Player) choice.get(0));
                this.botHasMoved();
                this.controller.update();

                //asks the target if he wants to use a tagback grenade
                Set<Figure> hitTarget = new HashSet<>(choice);
                this.controller.askTagbacks(hitTarget,this.controller.getModel().getBot());

                this.controller.goBack();

            }

        }else {

            this.botHasMoved();
            this.controller.update();
            this.controller.goBack();
        }

    }

    /**
     * manages spawning of the bot, makes the player choose ont of the three spawning spots
     * and proceeds to spawn the bot on that spot
     */

    private void spawnBot(){

        Square spawnPoint = this.controller.getView().showPossibleMoves(this.controller.returnSpawns(), true);

        if(spawnPoint != null) {
            this.controller.getModel().getBot().setPosition(spawnPoint);
            this.controller.getModel().getBot().setDead(false);
            this.controller.update();
        }else if(this.controller.getView().isInactive() || this.controller.getView().isDisconnected()){
            this.botHasMoved();
            this.controller.getModel().getBot().setPosition(this.controller.returnSpawns().get((int) (Math.random() * this.controller.returnSpawns().size())));
            this.controller.update();
            this.controller.endTurn();
        }else{
            this.controller.goBack();
        }

    }

    /**
     * Takes a square adjacent to the bot's position and moves the bot to that square,
     * if the inserted square is not adjacent asks for input again
     */

    private void moveBot(){

        ArrayList<Square> canGo = botCanGo();

        //makes the player select a destination
        Square botDestination = this.controller.getView().showPossibleMoves(canGo, false);

        this.controller.checkInactivity();
        if(botDestination != null) {

            this.controller.getModel().getBot().setPosition(botDestination);
            this.controller.update();
        }else{
            this.controller.goBack();
        }


    }

    /**
     * Pure method that returns the list of squares a bot can go to
     * @return the possible destinations of the bot
     */

    private ArrayList<Square> botCanGo(){

        //adds adjacent squares to the bots possible destinations
        ArrayList<Square> canGo = new ArrayList<>();

        int row;
        int column;

        for(row = 0; row < Map.HEIGHT; row++){
            for(column = 0; column < Map.WIDTH; column++){

                if(this.controller.getBoard().getMap().getSquareMatrix()[row][column].isAdjacent(this.controller.getModel().getBot().getPosition())){
                    canGo.add(this.controller.getBoard().getMap().getSquareMatrix()[row][column]);
                }
            }
        }
        canGo.add(this.controller.getModel().getBot().getPosition());

        canGo.add(this.controller.getModel().getBot().getPosition());

        return canGo;

    }

    private void botHasMoved(){
        this.controller.getModel().setHasBotAction(false);
    }

}
