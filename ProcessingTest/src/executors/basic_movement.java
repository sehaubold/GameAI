package executors;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PShape;

public class basic_movement extends PApplet{
    public static float centerx = 0;
    public static float centery = 0;
    public static float accumulator = 0;
    public static final int FRAME_WIDTH = 1000;
    public static final int FRAME_HEIGHT = 1000;
    public static final int SHAPEX_OFFSET = 15;
    public static final int SHAPEY_OFFSET = 15;
    public static final int STEP = 5;
    private KineticVar shape;
    private ArrayList<PShape> crumbs;
    
    public static void main(String[] args) {
        PApplet.main("executors.basic_movement");
    }

    public void settings(){
        size(FRAME_WIDTH, FRAME_HEIGHT);
        centerx = SHAPEX_OFFSET;
        centery = height - SHAPEY_OFFSET;
    }

    public void setup(){
        fill(420,50,240);
        background(100);
        crumbs = new ArrayList<PShape>();
        shape = new KineticVar(centerx, centery);
    }

    public void draw(){
        if (accumulator != 0 && shape.x == centerx && shape.y == centery) {
            noLoop();
            return;
        }
        background(100);
        accumulator += STEP;
        
        pushMatrix();
        if (shape.y + SHAPEY_OFFSET >= height && shape.x + SHAPEX_OFFSET <= width) {
            shape.x += STEP;
        } else if (shape.x + 2*SHAPEX_OFFSET >= width && shape.y - SHAPEY_OFFSET >= 0) {
            translate(shape.x, shape.y);
            rotate(-HALF_PI);
            translate(-shape.x, -shape.y);
            shape.y -= STEP;
        } else if (shape.y - SHAPEY_OFFSET <= 0 && shape.x - SHAPEX_OFFSET >= 0) {
            translate(shape.x, shape.y);
            rotate(PI);
            translate(-shape.x, -shape.y);
            shape.x -= STEP;
        } else if (shape.x - 2*SHAPEX_OFFSET <= 0 && shape.y + SHAPEY_OFFSET <= height) {
            translate(shape.x, shape.y);
            rotate(HALF_PI);
            translate(-shape.x, -shape.y);
            shape.y += STEP;
        } 
        if (accumulator % 16 == 0) {
            crumbs.add(createShape(ELLIPSE, shape.x, shape.y, 15, 15));
        }
        shape.print();
        popMatrix();
        for (int i = 0; i < crumbs.size(); i++) {
            shape(crumbs.get(i));
        }
    }

    private class KineticVar {
        public float x;
        public float y;
        
        /**
         * @param x
         * @param y
         * @param theta
         */
        public KineticVar(float x, float y) {
            super();
            this.x = x;
            this.y = y;
        }

        public void print() {
            triangle(x, SHAPEX_OFFSET + y, x + 2*SHAPEX_OFFSET, y, x, y - SHAPEX_OFFSET);
            ellipse(x, y, 30,30);
        }
    }
}