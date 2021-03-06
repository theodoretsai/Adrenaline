package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.map.Map;
import it.polimi.ingsw.model.board.map.Room;
import it.polimi.ingsw.model.board.map.Square;
import it.polimi.ingsw.model.cards.power_up.PowerUp;
import it.polimi.ingsw.controller.client_handler.ClientHandler;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Main controller class, implemented with satate pattern.
 *
 * @author Yuting Cai
 */

public class Controller {

    /**
     * All the characters from the game
     */
    private static String[] ALL_CHARACTERS = {"red","blue","yellow","green","grey"};

    /**
     * frenzy points
     */
    private static final int[] frenzyPointsVec;

    static {
        frenzyPointsVec = new int[]{2, 1, 1, 1, 0};
    }




    private static final int DEATH_DAMAGE = 11;
    private static final int OVER_KILL_DAMAGE = 12;


    //State pattern states

    ControllerState asBot;
    ControllerState choosingMove;
    ControllerState choosingPowerUpToUse;
    ControllerState choosingWeapon;
    ControllerState discardingPowerUp;
    ControllerState frenzySpecialAction;
    ControllerState moving;
    ControllerState picking;
    ControllerState pickingWeapon;
    ControllerState reloading;
    ControllerState shooting;
    ControllerState spawning;
    ControllerState teleporting;
    ControllerState usingNewton;
    ControllerState endGame;


    /**
     * Current controller state
     */

    ControllerState currentState;


    /**
     * True if the game has frenzy mode, false otherwise
     */
    private boolean hasFrenzy;

    /**
     * True if the game has bot, false otherwise
     */
    private boolean hasBot;


    /**
     * Main model of the controller, sends update based on this
     */
    private GameModel model;


    /**
     * View interface to communicate with user
     */
    private View view;

    /**
     * Notifier
     */
    private UpdateBuilder marshal;

    /**
     * Keeps track of the actions to generate in case of frenzy
     */
    private boolean oneAction = false;

    /**
     * Indicates test mode is on
     */
    private boolean testMode = false;


    /**
     * Starts the game reading parameters from a lobby
     * @param lobby Lobby to start from
     */

    public Controller(Lobby lobby) {

        lobby.setController(this);

        testMode=lobby.isTestMode();
        this.marshal=new UpdateBuilder(this);

        ArrayList<Player> playerList = new ArrayList<>();

        for(ClientHandler s: lobby.getJoinedPlayers()){
            playerList.add(s.getOwner());
        }

        this.view=playerList.get(0).getView();
        String map = intToMap(lobby.getMapPref());



        this.model = new GameModel(playerList,map, this);

        this.hasFrenzy = lobby.hasFinalFrenzy();
        this.hasBot = lobby.hasTerminatorPref();


        if(this.hasBot){

            String botColor = firstAvailableColor(playerList);
            this.model.setBot(new Terminator(botColor,this.model,testMode));

        }

        this.getModel().getBoard().getTrack().setSkullMax(lobby.getKillPref());


        this.asBot = new AsBot(this);
        this.choosingMove = new ChoosingMove(this);
        this.choosingPowerUpToUse = new ChoosingPowerUp(this);
        this.discardingPowerUp = new DiscardingPowerUp(this);
        this.choosingWeapon = new ChoosingWeapon(this);
        this.moving = new Moving(this);
        this.picking = new Picking(this);
        this.pickingWeapon = new PickingWeapon(this);
        this.reloading = new Reloading(this);
        this.shooting = new Shooting(this);
        this.teleporting = new Teleporting(this);
        this.usingNewton = new UsingNewton(this);
        this.shooting = new Shooting(this);
        this.spawning = new Spawning(this);
        this.frenzySpecialAction = new FrenzyShooting(this);
        this.endGame = new EndGame(this);

    }

    /**
     * Only for testing prurposes
     * @param playerList the list of players to start from
     */
    public Controller(ArrayList<Player> playerList){

        this.marshal=new UpdateBuilder(this);

        String map = intToMap(3);



        this.model = new GameModel(playerList,map, this);

        this.hasFrenzy = true;
        this.hasBot = true;

        //noinspection ConstantConditions
        String botColor = firstAvailableColor(playerList);
        this.model.setBot(new Terminator(botColor,model));


        this.getModel().getBoard().getTrack().setSkullMax(8);


        this.asBot = new AsBot(this);
        this.choosingMove = new ChoosingMove(this);
        this.choosingPowerUpToUse = new ChoosingPowerUp(this);
        this.discardingPowerUp = new DiscardingPowerUp(this);
        this.choosingWeapon = new ChoosingWeapon(this);
        this.moving = new Moving(this);
        this.picking = new Picking(this);
        this.pickingWeapon = new PickingWeapon(this);
        this.reloading = new Reloading(this);
        this.shooting = new Shooting(this);
        this.teleporting = new Teleporting(this);
        this.usingNewton = new UsingNewton(this);
        this.shooting = new Shooting(this);
        this.spawning = new Spawning(this);
        this.frenzySpecialAction = new FrenzyShooting(this);
        this.endGame = new EndGame(this);

    }

    /**
     * Used to determine the first available figure to use as Bot
     * @param playerList the list of all players
     * @return the first available figure
     */
    private static String firstAvailableColor(ArrayList<Player> playerList){

        //marks the character names that have been taken already
        //the characters are stored in a pre defined array
        int i;
        for(i = 0 ; i < ALL_CHARACTERS.length ; i++){
            for (Player p : playerList) {
                if (p.getCharacter().equals(ALL_CHARACTERS[i])){
                    ALL_CHARACTERS[i] = "taken";
                }
            }
        }

        //returns the first unmarked character
        for(i = 0 ; i < ALL_CHARACTERS.length ; i++){
            if(!ALL_CHARACTERS[i].equals("taken")){
                return ALL_CHARACTERS[i];
            }
        }

        return "red";
    }

    /**
     * Goes back to choosing the move
     */
    public void goBack(){
        this.currentState = this.choosingMove;
        this.currentState.executeState();
    }

    /**
     * decreases the remaining actions by one
     */
    public void decreaseMoveLeft(){
        this.setMovesLeft(this.model.getMovesLeft() -1 );
    }

    /**
     * ends a turn
     * adds tokens to the killshot track
     * iterates the current player to the next
     * on the player list
     */

    void endTurn() {

        ArrayList<Figure> deathCheckList = new ArrayList<>(this.getModel().getPlayerList());

        if(this.hasBot) {
            deathCheckList.add(this.getModel().getBot());
        }

        for (Figure f : deathCheckList) {

            if (f.getPersonalBoard().getDamage().size() >= DEATH_DAMAGE) {

                ArrayList<Token> addToTrack = new ArrayList<>();
                addToTrack.add(f.getPersonalBoard().getDamage().get(DEATH_DAMAGE - 1));
                if(f.getPersonalBoard().getDamage().size() >= OVER_KILL_DAMAGE) {
                    addToTrack.add(f.getPersonalBoard().getDamage().get(OVER_KILL_DAMAGE - 1));
                }
                this.getBoard().getTrack().getKillsTrack().add(addToTrack);

                f.die();
                f.setPosition(null);

            }

        }


        if(this.hasBot && this.getModel().getBot().isDead() && this.model.getTurn()!=0){
            this.getModel().getBot().setPosition(this.returnSpawns().get((int) (Math.random() * returnSpawns().size())));
            this.getModel().getBot().setDead(false);
        }

        this.model.getBoard().refillSquares();

        this.checkEndStatus();
        this.iteratePlayer();

        if(this.getModel().isFrenzy() && this.model.getPlayerList().indexOf(getCurrentPlayer()) == 0){
            this.oneAction = true;
        }

        this.resetTurn();

    }

    /**
     * Private method for checking if a the game should end used in end turn
     */

    private void checkEndStatus(){

        if(!checkLeftPlayer())
            endGame();

        if(this.getBoard().getTrack().getKillsTrack().size() >= this.getBoard().getTrack().getSkullMax()){

            if(this.hasFrenzy && !this.model.isFrenzy() && this.model.getFrenzyState()==0){

                this.startFrenzy();
                this.model.getBoard().refillSquares();

                this.model.setFrenzyState(1);

            }else if(this.hasFrenzy){
                if (this.model.getFrenzyState() > this.model.getPlayerList().size()) {
                    /* ****************GAME_ENDS******************* */
                    this.endGame();
                    /* ****************GAME_ENDS******************* */
                }

            }else{
                //noinspection ConstantConditions
                if (!this.hasFrenzy) {
                    /* ****************GAME_ENDS******************* */
                    this.endGame();
                    /* ****************GAME_ENDS******************* */
                }
            }

            System.out.println("\nFrenzyState: "+this.model.getFrenzyState());
            System.out.println("playerlist size ：" +this.model.getPlayerList().size());
            this.model.setFrenzyState(this.model.getFrenzyState() + 1);
        }
    }

    /**
     * Iterates the current player to the next one
     */

    void iteratePlayer(){


        if (this.model.getPlayerList().indexOf(this.model.getCurrentPlayer()) != this.model.getPlayerList().size() - 1) {
            this.model.setCurrentPlayer(this.model.getPlayerList().get(this.model.getPlayerList().indexOf(model.getCurrentPlayer()) + 1));
        } else {
            this.model.setCurrentPlayer(this.model.getPlayerList().get(0));
        }

        if (!checkLeftPlayer())
            endGame();


    }

    /**
     * Restes the parameters of the turn
     * Resets the current state to choosing move
     * and shifts the view listened by the server
     */

    private void resetTurn(){

        this.model.setMovesLeft(this.getMoves());
        this.model.setHasBotAction(true);
        this.model.setTurn(this.model.getTurn() + 1);
        this.view = this.getCurrentPlayer().getView();
        this.update();
        this.goBack();

    }

    /**
     * returns the available actions at the start of the turn
     * @return the number of available actions
     */

     private int getMoves(){

        if(this.isOneAction()){
            return 1;
        }else{
            return 2;
        }
     }

    /**
     * ends the game by displaying the leaderboard
     */

    private void endGame(){

        this.getModel().getBoard().getTrack().scorePoints();
        this.update();

        this.setCurrentState(endGame);
        this.currentState.executeState();

        if(this.hasFrenzy){
            for(Player p: this.model.getPlayerList()){
                p.die();
            }
        }

    }

    /**
     * Starts frenzy mode
     */

    private void startFrenzy(){
        this.model.setFrenzy(true);

        for(Player p : this.model.getPlayerList()) {
            if (p.getPersonalBoard().canFlip()) {
                p.getPersonalBoard().setPointVec(frenzyPointsVec);
                p.getPersonalBoard().setFlipped(true);
                p.getPersonalBoard().setDeathNum(0);
            }
        }


    }


    /**
     * Inquires all the player that were hit by an effect and have a tagback grenade if they want to use it
     * @param temp the list of players to inquire
     * @param hitter the player who inflited damage
     */
    void askTagbacks(Set<Figure> temp, Figure hitter){
        //do not remove otherwise it will modify hitTargetSet directly!!
        Set<Figure> targets=new HashSet<>(temp);
        //---------------------------------------

        if(this.hasBot()) {
            targets.remove(this.getModel().getBot());
        }

        ArrayList<Player> finalTargets = new ArrayList<>();

        for(Figure p : targets){
            finalTargets.add((Player)p);
        }

        Player tmp = this.getCurrentPlayer();

        Iterator<Player> removeNoVenoms = finalTargets.iterator();

        while(removeNoVenoms.hasNext()){
            Player p = removeNoVenoms.next();
            ArrayList<PowerUp> options = new ArrayList<>(p.getPowerupList());

            filterPUs(options,PowerUp.TAGBACK_GRENADE);
            if(options.isEmpty()){
                removeNoVenoms.remove();
            }
        }

        for(Player p : finalTargets){

            if(!p.isInactive()){

                this.getModel().setCurrentPlayer(p);
                this.setView(this.getCurrentPlayer().getView());

                Boolean useTagback = this.getView().showBoolean(ControllerMessages.ASKING_VENOM);

                if(useTagback!=null&&useTagback) {

                    ArrayList<PowerUp> options = new ArrayList<>(p.getPowerupList());

                    Controller.filterPUs(options, PowerUp.TAGBACK_GRENADE);

                    PowerUp choice = this.getView().showPowerUp(options);


                    if (choice != null) {
                        p.inflictMark(1, hitter);
                        p.removePowerUp(choice);
                    }
                }
            }
        }

        this.getModel().setCurrentPlayer(tmp);
        this.setView(tmp.getView());
        this.update();

    }

    /**
     * Determines the squares a player can reach within a given range
     *
     * @param p the Player inquiring on
     * @param range the range
     * @return the list of possible squares
     */

    ArrayList<Square> canGo(Figure p, int range){

        int row;
        int column;

        ArrayList<Square> options = new ArrayList<>();

        for(row = 0; row < Map.HEIGHT; row++){
            for(column = 0; column < Map.WIDTH; column++){

                if(p.distanceTo(this.getModel().getBoard().getMap().getSquareMatrix()[row][column])
                        <= range && ! this.getBoard().getMap().getSquareMatrix()[row][column].getRoom().getColor().equals(Room.VOID)){

                    options.add(this.getBoard().getMap().getSquareMatrix()[row][column]);

                }
            }
        }

        return options;

    }
    /**
     * Determines the squares a player can reach within a given range removing ones outside cardinal directions
     *
     * @param p the Player inquiring on
     * @param range the range
     * @return the list of possible squares in cardinal direction
     */
    ArrayList<Square> canGo(Figure p, int range,boolean directional){
        ArrayList<Square> all = canGo(p,range);
        if(!directional)
            return all;
        ArrayList<Square> temp = new ArrayList<>();
        for(Square s:all){
            if(p.getPosition().getPosition().getColumn()==s.getPosition().getColumn()||p.getPosition().getPosition().getRow()==s.getPosition().getRow())
                temp.add(s);
        }
        return temp;
    }

    /**
     * Determines the squares a player can reach within a given range removing ones outside cardinal directions
     *
     * @param p the Player inquiring on
     * @return the list of possible squares in cardinal direction
     */
    ArrayList<Square> visibleSquare(Figure p){
        ArrayList<Square> options =new ArrayList<>();
        int row;
        int column;

        Square[][] matrix= this.getModel().getBoard().getMap().getSquareMatrix();

        for(row = 0; row < Map.HEIGHT; row++){
            for(column = 0; column < Map.WIDTH; column++){

                if(p.canSeeSquare(matrix[row][column])&& ! matrix[row][column].getRoom().getColor().equals(Room.VOID)){
                    options.add(matrix[row][column]);
                }
            }
        }

        return options;
    }

    /**
     * Determines the squares a player can reach within a given range removing ones outside cardinal directions
     *
     * @return the list of possible squares in cardinal direction
     */
    ArrayList<Figure> playersInRange(Square s, int min,int max){
        ArrayList<Figure> options =new ArrayList<>();
        int dist;
        Set<Figure> allFig = new HashSet<>(model.getPlayerList());
        if(model.getBot()!=null)
            allFig.add(model.getBot());

        for(Figure p:allFig){
            dist = p.distanceTo(s);
            if(dist<=max&&dist>=min)
                options.add(p);
        }
        return options;
    }


    /**
     * Updates the view of each player
     */

    public void update(){
        String s = marshal.create().toString();
        for(Player p: model.getPlayerList()) {
            if (p.getView() == null) {
                continue;
            }
            p.getView().sendsUpdate(s);
        }
    }

    /**
     * Converts the map preference from integer to the
     * name of the map
     * @param i the preference expressed in int
     * @return the corresponding name
     */
    private String intToMap(int i){

        String map = "large";

        switch(i){

            case 4:
                map = "small";
                break;
            case 3:
                map = "medium2";
                break;
            case 2:
                map = "medium1";
                break;
            case 1:
                map = "large";
                break;

        }

        return map;
    }

    /**
     * Static method that removes all Power Ups NOT of the given type
     * from an ArrayList of Power Ups
     * @param puList ArrayList to remove from
     * @param toKeep All Power Ups to remove excluding this one
     */
    static void filterPUs(ArrayList<PowerUp> puList , String toKeep){

        puList.removeIf(p -> !p.getName().equals(toKeep));

    }

    /**
     * checks if a player has left
     * @return
     */
    private boolean checkLeftPlayer() {
        for(Player p: model.getPlayerList())
            if(!p.isDisconnected()&&!p.isInactive())
                return true;
        return false;
    }

    /**
     * checks if the current player is inactive
     */
    void checkInactivity(){
        if(this.getView().isInactive() || this.getView().isDisconnected()){
            this.endTurn();
        }
    }

    /**
     * Return the list of all Spawn squares on the map
     * @return the list of spawn squares
     */
    ArrayList<Square> returnSpawns(){

        ArrayList<Square> spawns = new ArrayList<>();
        int row;
        int column;

        for(row = 0; row < Map.HEIGHT; row++){
            for(column = 0; column < Map.WIDTH; column ++){
                if(this.getBoard().getMap().getSquareMatrix()[row][column].isSpawn())
                    spawns.add(this.getBoard().getMap().getSquareMatrix()[row][column]);
            }
        }

        return spawns;

    }


    ///////////////////////
    //Getters and Setters//
    ///////////////////////


    public Board getBoard() {
        return this.getModel().getBoard();
    }

    ControllerState getPickingWeapon() {
        return pickingWeapon;
    }

    ControllerState getShooting() {
        return shooting;
    }

    public ControllerState getSpawning() {
        return spawning;
    }

    public ControllerState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ControllerState currentState) {
        this.currentState = currentState;
    }

    public GameModel getModel() {
        return model;
    }

    public View getView() {
        return view;
    }

    private void setMovesLeft(int moves) {
        this.model.setMovesLeft(moves);
    }

    public Player getCurrentPlayer() {
        return this.model.getCurrentPlayer();
    }

    public boolean hasBot() {
        return hasBot;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setHasBot(boolean hasBot) {
        this.hasBot = hasBot;
    }

    public boolean isOneAction() {
        return oneAction;
    }

    public ControllerState getChoosingMove() {
        return choosingMove;
    }
}
