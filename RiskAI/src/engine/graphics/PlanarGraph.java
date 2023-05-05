package engine.graphics;

import engine.GameEngine;
import engine.Province;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlanarGraph {

    public final GameEngine engine;

    public int[][] pixelMap;

    public IntVector[] provincePositions;

    public ArrayList<ArrayList<Integer>> continentDefiningPointList;

    public static final int X_BOUND = 600;
    public static final int Y_BOUND = 600;

    private final Random randomiser;

    public PlanarGraph(GameEngine engine, long seed) {
        this.engine = engine;
        generateProvinces();
        randomiser = new Random(seed);
    }

    private static IntVector[] generateDefiningPoints() {
        IntVector[] toReturn = new IntVector[GameEngine.PROVINCE_COUNT];

        for (int i = 0; i < toReturn.length; i++) {
            while (true) {
                IntVector randomVector = IntVector.randomVector(X_BOUND, Y_BOUND);
                boolean found = false;
                for (int j = 0; j < i; j++) {
                    if (IntVector.squaredDistance(toReturn[j], randomVector) < 2500) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    toReturn[i] = randomVector;
                    break;
                }
            }
        }
        return toReturn;
    }

    private static ArrayList<HashSet<Integer>> assignNeighbours(int[][] map) {

        ArrayList<HashSet<Integer>> toReturn = new ArrayList<HashSet<Integer>>();

        fillArrayList(toReturn, GameEngine.PROVINCE_COUNT, HashSet::new);

        for (int x = 0; x < X_BOUND; x++) {
            for (int y = 0; y < Y_BOUND; y++) {
                collectNeighbour(toReturn, map, x, y, x + 1, y);
                collectNeighbour(toReturn, map, x, y, x, y + 1);
            }
        }

        return toReturn;
    }

    private void generateProvinces() {
        IntVector[] definingPoints = generateDefiningPoints();
        provincePositions = definingPoints;

        int[][] map = assignPoints(definingPoints);
        System.out.println("Generated map");
        ArrayList<HashSet<Integer>> neighbours = assignNeighbours(map);
        System.out.println("Assigned neighbours");
        int[] continents = assignContinents(neighbours);
        System.out.println("Assigned continents");
        for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {
            engine.provinces[i] = new Province(i, continents[i]);
        }
        for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {
            for (int province : neighbours.get(i)) {
                engine.provinces[i].neighbours.add(engine.provinces[province]);
            }
        }

        pixelMap = map;
    }

    private int[] assignContinents(ArrayList<HashSet<Integer>> neighbours) {

        List<List<Integer>> neighbourList = neighbours.stream().map(set -> set.stream().toList()).toList();

        int[] toReturn = new int[GameEngine.PROVINCE_COUNT];
        ArrayList<ArrayList<Integer>> continentProvinces = new ArrayList<ArrayList<Integer>>();
        fillArrayList(continentProvinces, GameEngine.CONTINENT_COUNT, ArrayList::new);
        List<Integer> availablePoints = IntStream.range(0, GameEngine.PROVINCE_COUNT).boxed().collect(Collectors.toList());

        Arrays.fill(toReturn, -1);

        for (int i = 0; i < GameEngine.CONTINENT_COUNT; i++) {

            int toChoose = (int) (Math.random() * availablePoints.size());
            int continentDefiningPoint = availablePoints.remove(toChoose);
            toReturn[continentDefiningPoint] = i;
            continentProvinces.get(i).add(continentDefiningPoint);

            System.out.println("Added: "+toChoose+" As continent defining point for continent: "+i);

        }

        continentDefiningPointList = continentProvinces;

        int chosenContinents = GameEngine.CONTINENT_COUNT;
        while (chosenContinents < GameEngine.PROVINCE_COUNT) {

            for (int i = 0; i < GameEngine.CONTINENT_COUNT; i++) {
                int provinceInContientLocalIndex = (int) (Math.random() * continentProvinces.get(i).size());
                int provinceInContinent = continentProvinces.get(i).get(provinceInContientLocalIndex);

                List<Integer> neighboursToProvince = neighbourList.get(provinceInContinent);
                int randomNeighbourLocalIndex = (int) (Math.random() * neighboursToProvince.size());
                int randomNeighbour = neighboursToProvince.get(randomNeighbourLocalIndex);

                if (toReturn[randomNeighbour] == -1) { // -1 means it has no province assigned
                    toReturn[randomNeighbour] = i;
                    continentProvinces.get(i).add(randomNeighbour);
                    chosenContinents++;



                }

            }

        }

        return toReturn;

    }

    private static <T> void fillArrayList(ArrayList<T> arrayList, int count, Supplier<T> supplier) {
        for (int i = 0; i < count; i++) {
            arrayList.add(supplier.get());
        }
    }

    private static void collectNeighbour(ArrayList<HashSet<Integer>> neighbours, int[][] map, int x0, int y0, int x1, int y1) {
        if (x1 >= X_BOUND || y1 >= Y_BOUND) return;

        int province0 = map[x0][y0];
        int province1 = map[x1][y1];
        if (province0 != province1) {
            neighbours.get(province0).add(province1);
            neighbours.get(province1).add(province0);
        }
    }

    private static int[][] assignPoints(IntVector[] definingPoints) {
        int[][] toReturn = new int[X_BOUND][Y_BOUND];

        for (int x = 0; x < X_BOUND; x++) {
            for (int y = 0; y < Y_BOUND; y++) {
                toReturn[x][y] = getClosestPoint(x, y, definingPoints);
            }
        }
        return toReturn;
    }

    private static int getClosestPoint(int x, int y, IntVector[] points) {

        int minDistnace = Integer.MAX_VALUE;
        int minIndex = 0;

        IntVector local = new IntVector(x, y);

        for (int i = 0; i < points.length; i++) {

            int localDistance = IntVector.squaredDistance(local, points[i]);

            if (localDistance < minDistnace) {
                minDistnace = localDistance;
                minIndex = i;
            }

        }
        return minIndex;
    }

}
