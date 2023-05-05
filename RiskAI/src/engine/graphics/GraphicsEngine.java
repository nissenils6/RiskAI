package engine.graphics;

import engine.GameEngine;
import engine.Province;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphicsEngine extends JFrame {

    private final PlanarGraph planarGraph;

    private static final Color[] CONTINENT_COLOURS = new Color[GameEngine.CONTINENT_COUNT];

    private static final int PADDING = 50;

    static {
        initialiseColours();
    }

    public GraphicsEngine(PlanarGraph planarGraph) {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        setSize(PlanarGraph.X_BOUND + 2 * PADDING, PlanarGraph.Y_BOUND + 2 * PADDING);
        this.planarGraph = planarGraph;

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);//cierra aplicacion
            }
        });
    }

    public static Color generateContinentColour(int continent) {
        int r = (int) (Math.random() * 223) + 23;
        int g = (int) (Math.random() * 223) + 23;
        int b = (int) (Math.random() * 223) + 23;
        return new Color(r, g, b);
    }

    private static void initialiseColours() {
        for (int i = 0; i < CONTINENT_COLOURS.length; i++) {
            CONTINENT_COLOURS[i] = generateContinentColour(i);
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        renderProvinces(g2);
    }

    public static final int RADIUS = 10;
    public static final int CHAR_WIDTH = 7;

    private void renderProvinces(Graphics2D g2) {
        g2.setColor(Color.black);

        g2.setStroke(new BasicStroke(1.5f));

        for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {
            IntVector position = planarGraph.provincePositions[i];

            for (Province neighbourProvince : planarGraph.engine.provinces[i].neighbours) {
                IntVector neighbourPosition = planarGraph.provincePositions[neighbourProvince.id];

                g2.drawLine(position.x + PADDING, position.y + PADDING, neighbourPosition.x + PADDING, neighbourPosition.y + PADDING);
            }
        }

        for (int i = 0; i < GameEngine.PROVINCE_COUNT; i++) {

            IntVector position = planarGraph.provincePositions[i];

            g2.setColor(CONTINENT_COLOURS[planarGraph.engine.provinces[i].continentId]);

            for (int m = 0; m < planarGraph.continentDefiningPointList.size(); m++) {    // this list is fine
                System.out.println("Continent: " + m + " Contains: " + planarGraph.continentDefiningPointList.get(m));
            }

            int radius = RADIUS;
            if (planarGraph.continentDefiningPointList.get(planarGraph.engine.provinces[i].continentId).get(0) == i) {
                radius = RADIUS * 2;
            }

            g2.fillOval(position.x - radius + PADDING, position.y - radius + PADDING, 2 * radius, 2 * radius);

            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(3));

            int stringXOffset = ((int) (Math.log10(Math.max(1, i))) + 1) * CHAR_WIDTH / 2;

            g2.drawString("" + i, position.x - stringXOffset + PADDING, position.y + 5 + PADDING);
        }
    }

}
