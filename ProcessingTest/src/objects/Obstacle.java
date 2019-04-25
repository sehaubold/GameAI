package objects;

import processing.core.PApplet;
import processing.core.PShape;

/**
 * @author Sam
 *
 */
public class Obstacle extends PApplet {
    public PShape obstacle;
    public int shape;
    public int width;
    public int height;
    public int originx;
    public int originy;
    public boolean movable;
    
    /**
     * constructor for obstacle in environment, can be used on walls as well
     * @param shapetype
     * @param leftCorn_x
     * @param leftCorn_y
     * @param width
     * @param height
     */
    public Obstacle(PShape obs, int shapetype, int leftCorn_x, int leftCorn_y, int width, int height, boolean movable) {
        if (shapetype == ELLIPSE) {
            leftCorn_x += width/2;
            leftCorn_y  += height/2;
        }
        obstacle = obs;
        shape = shapetype;
        this.width = width;
        this.height = height;
        originx = leftCorn_x;
        originy = leftCorn_y;
        this.movable = movable;
    }
    
    /**
     * this will report whether a point is contained in the obstacle or not
     * @param x
     * @param y
     * @return boolean true if point is contained
     * 
     */
    public boolean containsPoint(int x, int y) {
        if (shape == RECT)
            return (x >= originx && x <= originx + width) && (y >= originy && y <= originy + height);
        else {
            //TODO MAKE PERCEPTION OF CIRCLE WORK BETTER
//            return (x >= originx - width/2 && x <= originx + width/2) && (y >= originy - height/2 && y <= originy + height/2);
            int newx = abs(x - originx);
            int newy = abs(y - originy);
            newx *= newx;
            newy *= newy;
            int distance = (int) sqrt(newx + newy);
            return (distance <= width/2);
        }
    }
    

}
