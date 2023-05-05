package engine;

import java.util.HashSet;

public class Province {

    public final HashSet<Province> neighbours;
    public final int id;
    public final int continentId;

    public Player owner;
    public int troopCount;

    public Province(int id, int continentId) {
        this.neighbours = new HashSet<Province>();
        this.id = id;
        this.continentId = continentId;
    }


    @Override
    public String toString() {
        return "[" + id + "]: " + neighbours.stream().map(neighbours -> neighbours.id).toList() + "    continent=" + continentId;
    }
}
