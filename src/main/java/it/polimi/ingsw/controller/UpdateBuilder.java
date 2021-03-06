package it.polimi.ingsw.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.polimi.ingsw.model.Figure;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Token;
import it.polimi.ingsw.model.board.Armory;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.map.Map;
import it.polimi.ingsw.model.board.map.NonSpawnSquare;
import it.polimi.ingsw.model.board.map.SpawnSquare;
import it.polimi.ingsw.model.board.map.Square;
import it.polimi.ingsw.model.cards.Ammo;
import it.polimi.ingsw.model.cards.AmmoLot;
import it.polimi.ingsw.model.cards.power_up.PowerUp;
import it.polimi.ingsw.model.cards.weapon.Weapon;

import java.util.ArrayList;

import static it.polimi.ingsw.connection.ConnMessage.*;

/**
 * This class build a json string with all model info.
 * @author Gregorio Barzasi
 */
public class UpdateBuilder {
    public static final String TERMINATOR_NAME = "THANOS";
    private Controller controller;

    private ObjectMapper mapper = new ObjectMapper();

    public UpdateBuilder(Controller conn){
        this.controller=conn;
    }

    /**
     * @return the created json node
     */
    public JsonNode create(){
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("players",allPlayersNode());
        rootNode.set("main_board",mainBoardNode());
        rootNode.put("turn",controller.getCurrentPlayer().getCharacter());

        return rootNode;
    }
    /**
     * @return the created json node with all players info
     */
    private ObjectNode allPlayersNode(){
        ArrayList<Player> players = controller.getModel().getPlayerList();
        ObjectNode playersNode = mapper.createObjectNode();

        for(Player p : players){
            playersNode.set(p.getCharacter(),playerNode(p));
        }
        if(controller.getModel().getBot()!=null)
            playersNode.set(controller.getModel().getBot().getCharacter(),botNode(controller.getModel().getBot()));

        return playersNode;
    }
    /**
     * @return the created json node with bot info if is present
     */
    private ObjectNode botNode(Figure p){
        ObjectNode playerNode = mapper.createObjectNode();
        //username,points and position ( "row:column")
        playerNode.put("username",TERMINATOR_NAME);
        playerNode.put("points",p.getPoints());

        if(p.getPosition()!=null)
            playerNode.put("pos",p.getPosition().getPosition().getRow()+":"+p.getPosition().getPosition().getColumn());
        else
            playerNode.put("pos",NOTHING+INNER_SEP+NOTHING);

        //add Weapon
        ObjectNode weaponNode = mapper.createObjectNode();
        weaponNode.put("HASTA",true);
        weaponNode.put("LA",true);
        weaponNode.put("VISTA",true);
        playerNode.set("weapons",weaponNode);
        ObjectNode puNode = mapper.createObjectNode();
        playerNode.set("powerups",puNode);

        //add board section
        playerNode.set("board",boardNode(p));

        return playerNode;
    }

    /**
     * @return the created json node with a player info such as state and points
     */
    private ObjectNode playerNode(Player p){
        ObjectNode playerNode = mapper.createObjectNode();
        //username,points and position ( "row:column")
        playerNode.put("inactive",p.isInactive());
        playerNode.put("disconnected",p.isDisconnected());
        playerNode.put("username",p.getUsername());
        playerNode.put("points",p.getPoints());

        if(p.getPosition()!=null)
             playerNode.put("pos",p.getPosition().getPosition().getRow()+":"+p.getPosition().getPosition().getColumn());
        else
            playerNode.put("pos",NOTHING+INNER_SEP+NOTHING);

            //add Weapon
        ObjectNode weaponNode = mapper.createObjectNode();
        for(Weapon w:p.getWeaponsList())
            weaponNode.put(w.getName(),w.isLoaded());
        playerNode.set("weapons",weaponNode);

        //add pu
        ObjectNode puNode = mapper.createObjectNode();
        int i=0;
        for(PowerUp pu :p.getPowerupList()) {
            puNode.put(Integer.toString(i),pu.getName()+INNER_SEP+pu.getAmmoOnDiscard().toString());
            i++;
        }

        playerNode.set("powerups",puNode);

        //add board section
        playerNode.set("board",boardNode(p));

        return playerNode;
    }

    /**
     * @return the created json node with player board info given a player
     * @param p the player who need to be read
     */
    private ObjectNode boardNode(Figure p){

            ObjectNode boardNode = mapper.createObjectNode();

            //damage and marks array
            ArrayNode damage = mapper.createArrayNode();
            for(Token t:p.getPersonalBoard().getDamage()){
                damage.add(t.toString());
            }
            ArrayNode marks = mapper.createArrayNode();
            for(Token t:p.getPersonalBoard().getMark()){
                marks.add(t.toString());
            }
            boardNode.putPOJO("damage", damage);
            boardNode.putPOJO("marks", marks);
             boardNode.put("flipped",p.getPersonalBoard().isFlipped());

            //add skulls
            boardNode.put("skulls", p.getPersonalBoard().getDeathNum());

            //create ammoNode
            ObjectNode ammoNode = mapper.createObjectNode();
            Ammo a = p.getPersonalBoard().getAmmoInventory();
            ammoNode.put("red", a.getRed());
            ammoNode.put("blue", a.getBlue());
            ammoNode.put("yellow",a.getYellow());
            boardNode.set("ammo",ammoNode);

            return boardNode;

        }

    /**
     * @return build the info from main board
     */
        private ObjectNode mainBoardNode(){
         ObjectNode mainBoardNode = mapper.createObjectNode();
         Board board=controller.getModel().getBoard();
         Map map = board.getMap();
         //skull
         mainBoardNode.put("skull",board.getTrack().getSkullMax());

         //frenzy
         mainBoardNode.put("frenzy",controller.getModel().isFrenzy());

         //map
         mainBoardNode.put("map",map.getName());

         //killshottrack
         mainBoardNode.put("killshot_track",killshottrack());
         //cells
         ObjectNode cellsNode = mapper.createObjectNode();
         cellsNode.set("cells_pu",createCellNode(map,false));
         cellsNode.set("cells_armory",createCellNode(map,true));
         mainBoardNode.set("cells",cellsNode);

         return mainBoardNode;
        }

    /**
     * @return the string with info relative to killshottrack
     */
    private String killshottrack(){
        Board board=controller.getModel().getBoard();
        ArrayList<ArrayList<Token>> killshot = board.getTrack().getKillsTrack();
        StringBuilder s= new StringBuilder();
        for(ArrayList<Token> aT: killshot) {
            for (Token t : aT)
                s.append(t.getOwner().getCharacter()).append(INNER_SEP);
            s.append(INFO_SEP);
        }
        return s.toString();
    }
    /**
     * @return the created json node map cells
     */
        private ObjectNode createCellNode(Map map, boolean isArmory){
        ObjectNode node = mapper.createObjectNode();
        Square[][] sq = map.getSquareMatrix();
        for(int r=0;r<3;r++)
            for(int c=0;c<4;c++){
                Square s = sq[r][c];
                String coord = r+":"+c;
                if (s.isSpawn() && isArmory){
                     Armory armory=((SpawnSquare)s).getArmory();
                     node.put(coord,armory.toString());
                }
                if (!s.isSpawn() && !isArmory){
                    AmmoLot ammoLot =((NonSpawnSquare)s).getDrop();
                    if(ammoLot!=null)
                        node.put(coord,ammoLot.toString());
                    else
                        node.put(coord,"empty");
                }
            }
                return node;
    }

    }
