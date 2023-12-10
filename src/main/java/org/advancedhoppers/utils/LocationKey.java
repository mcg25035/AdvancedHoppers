package org.advancedhoppers.utils;


import java.util.List;
import java.util.Objects;

public class LocationKey {
    private int x;
    private int y;
    private int z;

    public LocationKey(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this){
            return true;
        }
        if (!(obj instanceof LocationKey)){
            return false;
        }
        LocationKey locationKey = (LocationKey) obj;
        return locationKey.x == this.x && locationKey.y == this.y && locationKey.z == this.z;
    }

}
