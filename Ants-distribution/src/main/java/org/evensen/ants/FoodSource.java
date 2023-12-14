package org.evensen.ants;

public class FoodSource {
    private final Position position;
    private static final int radius = 10;
    private int foodAmount = 1000;

    public FoodSource(Position position){
        this.position = position;
    }


    public Position getPosition(){
        return this.position;
    }

    public static int getRadius(){
        return radius;
    }

    public boolean takeFood() {
        if (this.foodAmount > 1){
            this.foodAmount--;
            return true;
        } else if (this.foodAmount == 1){
            this.foodAmount--;
            return false;
        }
        return false;
    }
}
