package org.evensen.ants;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MyAntWorld implements AntWorld {
    private final int width;
    private final int height;
    private FoodSource[] foodSources;
    private final boolean[][] food;
    private final Position home;
    private final float[][] foragingPheromones;
    private final float[][] foodPheromones;

    /**
     * @param width
     * @param height
     * @param foodSources
     */
    public MyAntWorld(final int width, final int height, int foodSources) {
        this.width = width;
        this.height = height;
        this.food = new boolean[width][height];
        this.foodSources = new FoodSource[foodSources];
        for (int i = 0; i < foodSources; i++) {
            placeFood(i);
        }
        this.home = new Position(width, height/2);
        this.foragingPheromones = new float[width][height];
        this.foodPheromones = new float[width][height];
    }
    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isObstacle(final Position p) {
        return !p.isInBounds(this.width, this.height);
    }

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        if (p.isInBounds(this.width, this.height)) this.foragingPheromones[(int) p.getX()][(int) p.getY()] += amount;

    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        if (p.isInBounds(this.width, this.height)) this.foodPheromones[(int) p.getX()][(int) p.getY()] += amount;
    }

    @Override
    public void dropFood(final Position p) {

    }

    @Override
    public void pickUpFood(final Position p) {
        if (containsFood(p)) {
            for (int i = 0; i < this.foodSources.length; i++) {
                final FoodSource foodSource = this.foodSources[i];
                final Position pos = foodSource.getPos();
                // find the foodsource whose radius we are in, then take from it
                if (p.isWithinRadius(pos, FoodSource.RADIUS)) {
                    foodSource.takeFood();
                    // replace the foodsource if it's empty
                    if (!foodSource.containsFood()) {
                        removeFood(i);
                        placeFood(i);
                    }
                    return;
                }
            }
        }
    }

    // requires an index into the foodSources array to remove the food
    private void removeFood(final int index) {
        final FoodSource oldFood = this.foodSources[index];
        this.foodSources[index] = null;
        final Position oldPos = oldFood.getPos();
        updateCellsInRadius(oldPos);
    }

    // requires an index into the foodSources array to place the food
    private void placeFood(final int index) {
        final int x = (int) (Math.random() * (this.width - 1));
        final int y = (int) (Math.random() * (this.height - 1));
        final Position p = new Position (x, y);
        final FoodSource newFood = new FoodSource(p);
        this.foodSources[index] = newFood;
        // todo: should have separate functionality that does not loop through all foodsources and checks their radius
        // todo: to update the matrix, as this check is not required for >placing< food (updateCellsInRadius does this)
        updateCellsInRadius(p);
    }

    private void updateCellsInRadius(Position p) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        // restrict the cells to update by foodsource radius and world dimensions
        final int min_x = min (x + FoodSource.RADIUS, this.width - 1);
        final int min_y = min (y + FoodSource.RADIUS, this.height - 1);
        for (int i = max (x - FoodSource.RADIUS, 0); i <= min_x; i++) {
            for (int j = max (y - FoodSource.RADIUS, 0); j <= min_y; j++) {
                final Position matrixIndex = new Position(i, j);
                // update the cell based on the existence of foodsources within radius
                this.food[i][j] = isFoodOnCell(matrixIndex);
            }
        }
    }

    // used in update function. mostly for making sure overlapping foodsources don't suffer from one of them running out
    private boolean isFoodOnCell(Position cell) {
        for (final FoodSource foodSource : this.foodSources) {
            if (foodSource != null) {
                if (cell.isWithinRadius(foodSource.getPos(), foodSource.RADIUS)) {
                    return true;
                }
            }
        } return false;
    }

    @Override
    public float getDeadAntCount(final Position p) {
        return 0;
    }

    @Override
    public float getForagingStrength(final Position p) {
        if (p.isInBounds(this.width, this.height)) return this.foragingPheromones[(int) p.getX()][(int) p.getY()];
        return 0.0f;
    }

    @Override
    public float getFoodStrength(final Position p) {
        if (p.isInBounds(this.width, this.height)) return this.foodPheromones[(int) p.getX()][(int) p.getY()];
        return 0.0f;
    }

    @Override
    public boolean containsFood(final Position p) {
        if (!p.isInBounds(this.width, this.height)) {
            return false;
        }
        return this.food[(int) p.getX()][(int) p.getY()];
    }

    @Override
    public long getFoodCount() {
        return 0;
    }

    @Override
    public boolean isHome(final Position p) {
        return p.isWithinRadius(this.home, 20);
    }

    @Override
    public void dispersePheromones() {

    }

    @Override
    public void setObstacle(final Position p, final boolean add) {

    }

    @Override
    public void hitObstacle(final Position p, final float strength) {

    }
}
