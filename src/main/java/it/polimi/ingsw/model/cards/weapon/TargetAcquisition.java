package it.polimi.ingsw.model.cards.weapon;

import it.polimi.ingsw.model.Figure;
import it.polimi.ingsw.model.cards.weapon.aiming.AimingFilter;

import java.util.ArrayList;
import java.util.Set;

/**
 *Used by {@link Effect} class to aim the effect to a specific target.
 * Works by "filtering" step by step the available target.
 * @author Gregorio Barzasi
 */

public class TargetAcquisition implements SubEffect {

    private ArrayList<AimingFilter> aimRoutine;
    private int indexTemp;
    private Set<Figure> targetTemp;

    public TargetAcquisition( ArrayList<AimingFilter> aimRoutine){
        this.aimRoutine=aimRoutine;
        this.indexTemp=0;
    }

    public ArrayList<AimingFilter> getAimRoutine() {
        return aimRoutine;
    }
    /**
     * step by step reduces the target set to only the hittable player
     * @param p the set of target that have to filtered
     */
    public Set<Figure> applyEffect(Weapon w, Set<Figure> p){
        if(p==null)
            return null;
        for(;indexTemp<aimRoutine.size();indexTemp++){
            AimingFilter a = aimRoutine.get(indexTemp);
            p = a.filter(w, p);
            //if is empty you have no target available
            if(p==null)
                return null;
            if (p.isEmpty())
                return p;
        }
       return p;
    }


    public void resetSubEffect() {
        indexTemp=0;
    }

}