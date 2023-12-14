package org.evensen.ants;

public class MyDispersalPolicy implements DispersalPolicy {
    private final float k = 0.5f;
    private final float f = 0.95f;

    @Override
    // returns foodPheromone as first value of the array, foragingPheromone as the second value
    public float[] getDispersedValue(final AntWorld w, final Position p) {
        float[] result = new float[2];
        float sumFoodPhero = 0;
        float sumForagePhero = 0;
        float foodPhero = w.getFoodStrength(p);
        float foragePhero = w.getForagingStrength(p);
        int x = (int) p.getX();
        int y = (int) p.getY();

        // loop through immediate neighbours
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // don't evaluate own position & check for bounds
                if (!(i == x && j == y) && new Position (i, j).isInBounds(w.getWidth(), w.getHeight())) {
                    Position currentPos = new Position(i, j);
                    sumFoodPhero += w.getFoodStrength(currentPos);
                    sumForagePhero += w.getForagingStrength(currentPos);
                // if not in bounds (& not own position) -> we're looking at an edge position
                // so add own value
                } else if (!(i == x && j == y)) {
                    sumFoodPhero += foodPhero;
                    sumForagePhero += foragePhero;
                }
            }
        }
        // do the K and F calculations
        sumFoodPhero = ((1 - this.k) * sumFoodPhero) / 8 + (this.k * foodPhero);
        sumForagePhero = ((1 - this.k) * sumForagePhero) / 8 + (this.k * foragePhero);

        result[0] = sumFoodPhero * this.f;
        result[1] = sumForagePhero * this.f;
        return result;
    }
}
