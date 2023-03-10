package net.unethicalite.plugins.abyss;

import net.runelite.api.Item;
import net.runelite.api.ItemID;

public enum TPMethod {
    RING_OF_DUELING("Ring of dueling"),
    CONSTRUCTION_CAPET("Construct. cape(t)", ItemID.CONSTRUCT_CAPET);

    private final String name;
    private int getID;

    TPMethod(String name, int getID){
        this.name = name;
        this.getID = getID;
    }

    TPMethod(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getID(){
        return getID;
    }
}
