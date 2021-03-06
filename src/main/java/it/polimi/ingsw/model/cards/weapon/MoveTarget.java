package it.polimi.ingsw.model.cards.weapon;

import it.polimi.ingsw.model.Figure;
import it.polimi.ingsw.model.board.map.Square;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Effect that moves player around ( also the weapon owner, like and extra move);
 * you can define the final position for the target and also if the move could append in any direction or not;
 * @author Gregorio Barzasi
 */

public class MoveTarget implements SubEffect {

    /**
     * the max step allowed in move action
     */
    private int maxSteps;
    /**
     * the constraint on final position
     */
    private String finalPos;
    /**
     * indicates that move has to be done only on one cardinal direction
     */
    private boolean directional;
    /**
     * temporary saves the chosen square
     */
    private Square squareTemp;
    /**
     * temporary saves the hittable targets
     */
    private Set<Figure> targetTemp;

    public MoveTarget(int maxSteps, String finalPos, boolean directional) {
        this.maxSteps=maxSteps;
        this.finalPos=finalPos;
        this.directional=directional;
        this.targetTemp=new HashSet<>();
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    /**
     * moves your target to a certain position verifying that the final position is allowed.
     */
    public Set<Figure> applyEffect(Weapon w, Set<Figure> p){
        if(p==null)
            return null;
        if(p.isEmpty())
            return p;

        switch(finalPos){
            case "last":
                squareTemp = w.getLastHit().getPosition();
                break;
            case "selected":
                squareTemp = w.getSelected();
                break;
            case "me":
                squareTemp = w.getOwner().getPosition();
                break;
            case "far":
                ArrayList<Square> pos=new ArrayList<>();
                for(Figure f: w.getDamaged())
                    pos.add(f.getPosition());
                squareTemp = getFarthest(w.getOwner(),pos);
                break;

        }

        if(squareTemp==null&&w.getMoveTemp()==null) {
                targetTemp.addAll(p);
                w.setMoveTemp(this);
                return null;
        }else if(w.getMoveTemp()!=null){
            p.clear();
            p.addAll(targetTemp);
            w.setMoveTemp(null);
        }

        for (Figure target : p) {
            target.setOldPosition(target.getPosition());
            target.setPosition(squareTemp);
            w.setLastMoved(target);
        }

        if(finalPos.equals("visible")&&!w.getOwner().canSee(w.getLastMoved())) {
            w.getLastMoved().setPosition(w.getLastMoved().getOldPosition());
            targetTemp.addAll(p);
            w.setMoveTemp(this);
            return null;
        }
        return p;
    }

    @Override
    public void resetSubEffect() {
        targetTemp.clear();
        squareTemp=null;
    }

    /**
     * @return the farthest square from a target
     */
    public Square getFarthest(Figure p, ArrayList<Square> all) {
        int max=0;
        Square far=null;
        for(Square s : all)
            if(p.distanceTo(s)>max) {
                max = p.distanceTo(s);
                far = s;
            }
        return far;

    }

    public boolean isDirectional() {
        return directional;
    }

    public void setSquareTemp(Square squareTemp) {
        this.squareTemp = squareTemp;
    }

    public Set<Figure> getTargetTemp() {
        return targetTemp;
    }

}
