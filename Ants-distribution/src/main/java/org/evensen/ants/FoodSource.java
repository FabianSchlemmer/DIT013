package org.evensen.ants;

import static java.lang.Math.max;

public class FoodSource {
    private final Position pos;
    private int amount;
    public final static int RADIUS = 3;
    public final static int AMOUNT = 100;

    public FoodSource(Position p) {
        this.pos = p;
        this.amount = (int) max (Math.random() * AMOUNT, 1);
    }

    public boolean containsFood() {
        return 0 < this.amount;
    }

    public void takeFood() {
        assert this.containsFood();
        --this.amount;
    }

    public Position getPos() {
        return this.pos;
    }
}
