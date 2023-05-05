package engine.graphics;

public class IntVector {
    public int x;
    public int y;

    public IntVector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static int squaredDistance(IntVector p0, IntVector p1) {
        int dx = p1.x - p0.x;
        int dy = p1.y - p0.y;
        return dx * dx + dy * dy;
    }

    public static float distance(IntVector p0, IntVector p1) {
        return (float) Math.sqrt(squaredDistance(p0, p1));
    }

    public static IntVector randomVector(int xBound,int yBound) {

        int xPos = (int)(Math.random()*xBound);
        int yPos = (int)(Math.random()*yBound);

        return new IntVector(xPos,yPos);

    }

}
