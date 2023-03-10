package net.unethicalite.plugins.abyss;
import net.runelite.api.ItemID;

public enum Runes
{
    NATURE_RUNE("Nature", 9547),
    COSMIC_RUNE("Cosmic", 8523);

    private final String name;
    private final int regionID;

    Runes(String name, int regionID)
    {
        this.name = name;
        this.regionID = regionID;
    }

    public String getName()
    {
        return name;
    }

    public int getRegionID(){
        return regionID;
    }
}
