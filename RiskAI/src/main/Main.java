package main;

import java.util.ArrayList;

import engine.graphics.GraphicsEngine;
import engine.graphics.PlanarGraph;
import org.jblas.FloatMatrix;

import engine.GameEngine;

public class Main {

	// How FloatMatrix stores data can potentially mess up basically every function we have written that depends on it :)
	// SUS SEED: 1683284450060
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		long seed = System.currentTimeMillis();



		GameEngine engine = new GameEngine();

		PlanarGraph graphMaker = new PlanarGraph(engine,seed);

		engine.printProvinces();

		GraphicsEngine graphicsEngine = new GraphicsEngine(graphMaker);

		for (int i = 0; i < GameEngine.CONTINENT_COUNT; i++) {
			System.out.println(i + " " + graphMaker.continentDefiningPointList.get(i).get(0));
		}

		graphicsEngine.setVisible(true);
		System.out.println("SEED: "+seed);
	}
	/*
	 * 
	 * float[][] matrixData = {{1,2},{3,4}}; float[][] vectorData = {{1,0}};
	 * 
	 * FloatMatrix m = new FloatMatrix(matrixData); FloatMatrix v = new
	 * FloatMatrix(vectorData);
	 * 
	 * FloatMatrix res = new FloatMatrix();
	 * 
	 * v.mmuli(m, res);
	 * 
	 * System.out.println(res.toString());
	 * 
	 */
}

/*
import java.util.HashSet;

final int CONTINENTS = 6;
final int PROVINCES = 40;

final int WIDTH = 1000;
final int HEIGHT = 1000;
final int COLOUR_VARIATION = 16;

// province_index -> province_position
final PVector[] definingPoints = new PVector[PROVINCES];

// (pixel_x, pixel_y) -> province_index
final int[][] map = new int[WIDTH][HEIGHT];

// province_index -> province_colour
final color[] colours = new color[PROVINCES];

// continent_index -> continent_colour
final color[] continentColours = new color[CONTINENTS];

// {border = (province_index_0, province_index_1)}
final HashSet<PVector> graph = new HashSet<PVector>();

// province_index -> [neighbours = province_index]
final ArrayList<ArrayList<Integer>> neighbours = new ArrayList<ArrayList<Integer>>();

// continent_index -> main_province = province_index
final int[] continentDefiningPoints = new int[CONTINENTS];

// continent_index -> continent_size
final int[] continentSize = new int[CONTINENTS];

// province_index -> continent_index
final int[] continent = new int[PROVINCES];

double euclideanDistance(PVector a, PVector b) {
  double dx = a.x - b.x;
  double dy = a.y - b.y;
  return dx * dx + dy * dy;
}

double manhattanDistance(PVector a, PVector b) {
  return abs(a.x - b.x) + abs(a.y - b.y);
}

void clearMapData() {
  graph.clear();
  neighbours.clear();
}

void generateProvincePoints() {
  for (int i = 0; i < PROVINCES; i++) {
    int x = floor(random(0, WIDTH));
    int y = floor(random(0, HEIGHT));

    definingPoints[i] = new PVector(x, y);
  }
}

void assignProvincesToPixels() {
  for (int x = 0; x < WIDTH; x++) {
    for (int y = 0; y < HEIGHT; y++) {
      int minimumPoint = 0;
      double minimumDistance = pow(10, 9);
      PVector currentPoint = new PVector(x, y);
      for (int i = 0; i < PROVINCES; i++) {
        double distance = euclideanDistance(currentPoint, definingPoints[i]);

        if (distance<minimumDistance) {
          minimumPoint = i;
          minimumDistance = distance;
        }
        map[x][y] = minimumPoint;
      }
    }
  }
}

void generateNeighbour(int x0, int y0, int x1, int y1) {
  if (x1 >= WIDTH || y1 >= HEIGHT) {
    return;
  }

  int node0 = map[x0][y0];
  int node1 = map[x1][y1];
  if (node0 != node1) {
    graph.add(new PVector(node0, node1));
    neighbours.get(node0).add(node1);
    neighbours.get(node1).add(node0);
  }
}

void generateNeighbours() {
  for (int i = 0; i < 40; i++) {
    neighbours.add(new ArrayList<Integer>());
  }

  for (int x=0; x < WIDTH; x++) {
    for (int y=0; y < HEIGHT; y++) {
      generateNeighbour(x, y, x+1, y);
      generateNeighbour(x, y, x, y+1);
    }
  }
}

boolean isContinentCollision(int randomCDPoint, int i) {
  // helper function for generateContinentPoints that checks whether the recently randomly generated condinent defining point has already been chosen

  for (int j = 0; j < i; j++) {
    if (randomCDPoint == continentDefiningPoints[j]) {
      return true;
    }
  }
  return false;
}

void generateContinentPoints() {
  for (int i = 0; i < CONTINENTS; i++) {
    int random = floor(random(0, 40));

    while (isContinentCollision(random, i)) {
      random = floor(random(0, 40));
    }

    continentDefiningPoints[i] = random;
  }
}

void assignContinentsToProvinces() {
  for (int i = 0; i < PROVINCES; i++) {
    int closestCDPoint = 0;
    double distanceToCDP = pow(10, 9);

    for (int j = 0; j < CONTINENTS; j++) {
      double distance = euclideanDistance(definingPoints[i], definingPoints[continentDefiningPoints[j]]);

      if (distance < distanceToCDP) {
        closestCDPoint = j;
        distanceToCDP = distance;
      }
    }

    continent[i] = closestCDPoint;
    continentSize[closestCDPoint]++;
  }
}

void populateGroup(HashSet<Integer> group, int node) {
  if (group.contains(node)) {
    return;
  }

  group.add(node);
  for (int neighbour : neighbours.get(node)) {
    if (continent[node] == continent[neighbour]) {
      populateGroup(group, neighbour);
    }
  }
}

boolean checkContinentDiscontinuity() {
  for (int i = 0; i < CONTINENTS; i++) {
    int mainNode = continentDefiningPoints[i];
    HashSet<Integer> group = new HashSet<Integer>();
    populateGroup(group, mainNode);

    if (continentSize[i] != group.size()) {
      println("Continent " + i + " is incorrect (0, " + green(continentColours[i]) + ", " + blue(continentColours[i]) + "). Group size: " + group.size() + ". Continent size: " + continentSize[i]);
      return true;
    }
  }
  return false;
}

void generateContinents() {
  generateContinentPoints();
  assignContinentsToProvinces();

  // Retry if there are 2 disjoint clusters of provinces with the same continent
  if (checkContinentDiscontinuity()) {
    generateContinents();
  }
}

void generateMap() {
  // Clears all the data structures to make it possible to generate multiple maps
  clearMapData();

  // Generates provinces by creating a list with 40 uniformly distributed points
  generateProvincePoints();

  // Assigns a province to each pixel by looping through the pixels and choosing what province is closest
  assignProvincesToPixels();

  // Creates two neighbour datastructures:
  //  - A list containing edges represented by a pair of province_indices
  //  - A list containing a list of all neighbours for each province
  generateNeighbours();

  // Generates continents by first choosing a random province to represent each point and then assigning each province the continent as its continent
  // Retries if there are 2 disjoint clusters of provinces with the same continent
  generateContinents();
}

void generateContinentColours() {
  // Generates continent colours without red component and with the following distribution of green and blue:

  // blue ^
  //      |
  // 223  | X
  //      |      X
  //      |
  //      |           X
  //      |
  //      |              X
  //      |
  //  32  |               X
  //      +-------------------->
  //        32           223  green

  for (int i = 0; i < CONTINENTS; i++) {
    // map(x, a, b, c, d) function maps x linearly from the interval [a, b] to [c, d]
    // e.g. x = a maps to c and x = (a + b)/2 maps to (c + d)/2

    int g = (int)map(cos((PI / 2) * i / (CONTINENTS - 1)), 0, 1, 32, 223);
    int b = (int)map(sin((PI / 2) * i / (CONTINENTS - 1)), 0, 1, 32, 223);
    continentColours[i] = color(0, g, b);
  }
}

void generateProvinceColours() {
  for (int i = 0; i < definingPoints.length; i++) {
    color continentColor = continentColours[continent[i]];
    colours[i] = color(0, random(green(continentColor) - COLOUR_VARIATION, green(continentColor) + COLOUR_VARIATION), random(blue(continentColor) - COLOUR_VARIATION, blue(continentColor) + COLOUR_VARIATION));
  }
}

void setup() {
  size(1000, 1000);

  generateMap();

  // Colours are completely independant from the rest of the map logic and can thus be generated afterwards
  generateContinentColours();
  generateProvinceColours();

  // Demonstrate matrix construction, multiplication and formatting


}

void draw() {
  background(0);
  noStroke();

  for (int x=0; x < WIDTH; x++) {
    for (int y=0; y < HEIGHT; y++) {
      color currentColour = colours[map[x][y]];
      fill(currentColour);
      rect(x, y, 1, 1);
    }
  }

  fill(0);

  stroke(255, 0, 0);
  for (PVector i : graph) {
    PVector p0 = definingPoints[(int)i.x];
    PVector p1 = definingPoints[(int)i.y];
    line(p0.x, p0.y, p1.x, p1.y);
  }

  fill(0, 255, 0);

  for (int i = 0; i < PROVINCES; i++) {
    circle(definingPoints[i].x, definingPoints[i].y, 20);
  }
}

*/
