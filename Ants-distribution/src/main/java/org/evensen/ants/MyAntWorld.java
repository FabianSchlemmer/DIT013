package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAntWorld implements AntWorld {
    private final int width;
    private final int height;
    private float[][] foodPheromone;
    private float[][] foragingPheromone;
    private final boolean[][] foodMatrix;
    private final Position homePosition;
    private final List<FoodSource> foodSources;
    private final DispersalPolicy dispersalPolicy;

    public MyAntWorld(final int worldWidth, final int worldHeight, final int sources, final DispersalPolicy policy) {
        this.dispersalPolicy = policy;
        this.width = worldWidth;
        this.height = worldHeight;
        this.foodMatrix = new boolean[worldWidth][worldHeight];
        this.foragingPheromone = new float[worldWidth][worldHeight];
        this.foodPheromone = new float[worldWidth][worldHeight];
        this.homePosition = new Position(worldWidth, worldHeight / 2);
        this.foodSources = new ArrayList<>();
        for (int i = 0; i < sources; i++) {
            placeFoodSource();
        }
        for (FoodSource foodSource : this.foodSources){
            updateFoodMatrix(foodSource.getPosition(), true);
        }
    }

    private void updateFoodMatrix(Position p, boolean c){
        if (p.isInBounds(this.width, this.height)){
            int radius = FoodSource.getRadius();
            int x = (int) p.getX();
            int y = (int) p.getY();
            for (int a = Math.max(0, x - radius); a <= Math.min(this.width - 1, x + radius); a++) {
                for (int b = Math.max(0, y - radius); b <= Math.min(this.height - 1, y + radius); b++) {
                    Position ab = new Position(a,b);
                    if (ab.isWithinRadius(p, radius)) {
                        this.foodMatrix[a][b] = c;
                    }
                }
            }
        }
    }

    @Override
    public boolean containsFood(Position p){
        if (p.isInBounds(this.width, this.height)){
            if (this.foodMatrix[(int) p.getX()][(int) p.getY()]){
                FoodSource foodSource = findFoodSource(p);
                if (foodSource != null){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void pickUpFood(Position p) {
        FoodSource foodSource = findFoodSource(p);
        if (foodSource != null && !foodSource.takeFood()) {
            this.foodSources.remove(foodSource);
            updateFoodMatrix(foodSource.getPosition(), false);
            placeFoodSource();
        }
    }
    private FoodSource findFoodSource(Position p2) {
        int radius = FoodSource.getRadius();
        for (FoodSource foodSource : this.foodSources) {
            if (foodSource != null && p2.isWithinRadius(foodSource.getPosition(), radius)) {
                return foodSource;
            }
        }
        return null;
    }
    private void placeFoodSource() {
        Random random = new Random();
        int diff = FoodSource.getRadius();
        int x = random.nextInt(diff, this.width - diff);
        int y = random.nextInt(diff, this.height - diff);
        while (avoidTwoAtOnePoint(x, y, diff)){
            x = random.nextInt(diff, this.width - diff);
            y = random.nextInt(diff, this.height - diff);
        }
        Position foodPosition = new Position(x, y);
        FoodSource foodSource = new FoodSource(foodPosition);
        this.foodSources.add(foodSource);
        updateFoodMatrix(foodSource.getPosition(), true);
    }

    private boolean avoidTwoAtOnePoint(int x, int y, int diff){
        for (int xdiff = -diff; xdiff <= diff; xdiff++){
            for (int ydiff = -diff; ydiff <= diff; ydiff++){
                if (this.foodMatrix[x+xdiff][y+ydiff]){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getWidth(){
        return this.width;
    }
    @Override
    public int getHeight(){
        return this.height;
    }
    @Override
    public boolean isObstacle(Position p){
        return !p.isInBounds(this.width, this.height);
    }
    @Override
    public void dropForagingPheromone(Position p, float amount){
        if (p.isInBounds(this.width, this.height)){
            int x = (int) p.getX();
            int y = (int) p.getY();
            float oldAmount = this.foragingPheromone[x][y];
            this.foragingPheromone[x][y] = Math.min(1, oldAmount + amount);
        }
    }
    @Override
    public void dropFoodPheromone(Position p, float amount){
        if (p.isInBounds(this.width, this.height)){
            int x = (int) p.getX();
            int y = (int) p.getY();
            float oldAmount = this.foodPheromone[x][y];
            this.foodPheromone[x][y] = Math.min(1, oldAmount + amount);
        }
    }
    @Override
    public float getForagingStrength(Position p){
        if (p.isInBounds(this.width, this.height)){
            return this.foragingPheromone[(int) p.getX()][(int) p.getY()];
        }
        return 0;
    }
    @Override
    public float getFoodStrength(Position p){
        if (p.isInBounds(this.width, this.height)){
            return this.foodPheromone[(int) p.getX()][(int) p.getY()];
        }
        return 0;
    }
    @Override
    public void dropFood(Position p){

    }
    @Override
    public float getDeadAntCount(Position p){
        return 0;
    }

    @Override
    public long getFoodCount(){
        return 0;
    }
    @Override
    public boolean isHome(final Position p) {
        return p.isWithinRadius(this.homePosition, 20);
    }
    @Override
    public void dispersePheromones() {
        float[][] tempFoodPhero = new float[this.width][this.height];
        float[][] tempForagePhero = new float[this.width][this.height];

        for (FoodSource foodSource : this.foodSources) {
            dropFoodPheromone(foodSource.getPosition(), 1);
        }

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                if (!isObstacle(new Position(x, y))) {
                    // call dispersalPolicy on each cell
                    float[] pheroValues = this.dispersalPolicy.getDispersedValue(this, new Position(x, y));
                    tempFoodPhero[x][y] = pheroValues[0];
                    tempForagePhero[x][y] = pheroValues[1];
                }
            }
        }

        this.foodPheromone = tempFoodPhero;
        this.foragingPheromone = tempForagePhero;
    }

    public void selfContainedDisperse() {
        final float K = 0.5f;
        final float F = 0.95f;

        for (FoodSource foodSource : this.foodSources) {
            dropFoodPheromone(foodSource.getPosition(), 1);
        }
        float[][] tempFoodPhero = new float[this.width][this.height];
        float[][] tempForagePhero = new float[this.width][this.height];

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                float sumFoodPhero = 0;
                float sumForagePhero = 0;
                if (!isObstacle(new Position(x, y))) {
                    float foodPhero = this.foodPheromone[x][y];
                    float foragePhero = this.foragingPheromone[x][y];

                    // loop through immediate neighbours
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            // don't evaluate own position & check for bounds
                            if (!(i == x && j == y) && new Position (i, j).isInBounds(this.width, this.height)) {
                                sumFoodPhero += this.foodPheromone[i][j];
                                sumForagePhero += this.foragingPheromone[i][j];
                            // if not in bounds (& not own position) -> we're looking at an edge position
                            // so add own value
                            } else if (!(i == x && j == y)) {
                                sumFoodPhero += foodPhero;
                                sumForagePhero += foragePhero;
                            }
                        }
                    }
                    // do the K and F calculations
                    sumFoodPhero = ((1 - K) * sumFoodPhero) / 8 + (K * foodPhero);
                    sumForagePhero = ((1 - K) * sumForagePhero) / 8 + (K * foragePhero);
                }
                tempFoodPhero[x][y] = sumFoodPhero * F;
                tempForagePhero[x][y] = sumForagePhero * F;
            }
        }
        // overwrite old pheromone matrix
        this.foodPheromone = tempFoodPhero;
        this.foragingPheromone = tempForagePhero;
    }

    @Override
    public void setObstacle(final Position p, final boolean add) {
    }

    @Override
    public void hitObstacle(final Position p, final float strength) {

    }
}
