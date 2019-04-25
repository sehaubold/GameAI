package executors;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

/**
 * @author Sam
 *
 */
public class wander_steering extends PApplet{

    public static final int FRAME_WIDTH = 1000;
    public static final int FRAME_HEIGHT = 1000;
    public static final int MAX_VELOCITY = 10;
    public static final int MAX_ACCEL = 5;
    public static float MAX_ROT = PI / 4;
    public static final int RADIUS_DECEL = 80;
    public static final float RADIUS_DECEL_ROT = PI / 3;
    public static final int RADIUS_SAT = 10;
    public static final float RADIUS_SAT_ROT = PI / 24;
    public static final int EFFECT_RANGE = 3;
    public static final float EFFECT_RANGE_ROT = (float) 3;
    public static KinVar character;
    public static int algType;
    public static final int STEP = 5;
    private ArrayList<PShape> crumbs;
    public static float accumulator = 0;
    private static boolean crum = false;
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Wander1, or Wander2?");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        if (input.contains("1")) {
            algType = 1;
            MAX_ROT *= (1.0/15.0);
        } else {
            algType = 2;
        }

        System.out.println("Show Breadcrumbs?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            crum  = true;
        } 
        in.close();
        PApplet.main("wander_steering");
    }

    public void settings(){
        size(FRAME_WIDTH, FRAME_HEIGHT);
        
    }

    public void setup(){
        fill(420,50,240);
//        frameRate(10);
        crumbs = new ArrayList<PShape>();
        character = new KinVar();
        character.position = new PVector(width/2, height/2);
        character.velocity = new PVector(1, 0);
        character.acceleration = new PVector(0, 0);
        character.orientation = new PVector(1,  0);
        character.angRot = 0;
        character.angle = 0;
        character.velocity.setMag(MAX_VELOCITY);
    }

    public void draw(){
        background(100);
        accumulator += STEP;
        if (crum) {
            if (accumulator % 3 == 0) {
                crumbs.add(createShape(ELLIPSE, character.position.x, character.position.y, 15, 15));
            }
            for (int i = 0; i < crumbs.size(); i++) {
                shape(crumbs.get(i));
            }
        }
        if (algType == 1)
            wanderOne();
        else
            wanderTwo();
        
    }
    
    private void wanderTwo() {
        float change = random(-MAX_ROT, MAX_ROT);
        character.angRot = change / 6;
        character.angle += character.angRot;
        
        if (character.position.x < 0) 
            character.position.x = width;
        if (character.position.y < 0) 
            character.position.y = height;
        if (character.position.x > width) 
            character.position.x = 0;
        if (character.position.y > height) 
            character.position.y = 0;
        

        character.velocity.set(MAX_VELOCITY * cos(character.angle), MAX_VELOCITY * sin(character.angle));
        character.position.add(character.velocity);
        
        pushMatrix();
        translate(character.position.x, character.position.y);
        rotate(character.angle);
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        popMatrix();
    }

    private void wanderOne() {

        wanderAdjust();
        kinematicAdjust();
        pushMatrix();
        
        translate(character.position.x, character.position.y);
        rotate(character.orientation.heading());
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        
        popMatrix();
    }
    private float randomBinomial() {
        return random(0, 1) - random(0, 1);
    }
    private void wanderAdjust() {
        if (character.position.x < RADIUS_DECEL) {
            
            if (character.acceleration.x == 0)
                character.acceleration.x = MAX_ACCEL;
            else if (character.acceleration.x != 0)
                character.acceleration.x += MAX_ACCEL;
            
        } else if (character.position.x + RADIUS_DECEL > width) {
            

            if (character.acceleration.x == 0)
                character.acceleration.x = -MAX_ACCEL;
            else if (character.acceleration.x != 0)
                character.acceleration.x += -MAX_ACCEL;
            
        } else {
            character.acceleration.x = 0;            
        }
        if (character.position.y < RADIUS_DECEL) {

            if (character.acceleration.y == 0)
                character.acceleration.y = MAX_ACCEL;
            else if (character.acceleration.y != 0)
                character.acceleration.y += MAX_ACCEL;
            
        } else if (character.position.y + RADIUS_DECEL > height) {

            if (character.acceleration.y == 0)
                character.acceleration.y = -MAX_ACCEL;
            else if (character.acceleration.y != 0)
                character.acceleration.y += -MAX_ACCEL;
            
        } else {
            character.acceleration.y = 0;            
        }
        
        character.angRot = MAX_ROT + randomBinomial();
        character.angRot = mapToRange(character.angRot);
        character.angle += character.angRot;
        character.velocity.set((float) (MAX_VELOCITY * Math.cos(character.angle)), (float) (MAX_VELOCITY  * Math.sin(character.angle)));
        
    }
    

    private void kinematicAdjust() {
        character.velocity.add(character.acceleration);
        character.position.add(character.velocity);
        character.angle = character.velocity.heading();
        character.orientation.set((float) Math.cos(character.angle), (float) Math.sin(character.angle));
        
    }

    private float mapToRange(float angle) {
        angle = angle % TWO_PI;
        if (Math.abs(angle) < PI) {
            return angle;
        } else if (angle > PI) {
            return angle - TWO_PI;
        } else {
            return angle + TWO_PI;
        }
        
    }
    
    private class KinVar {

        public float angle;
        public PVector orientation;
        public float angRot;
        public PVector position;
        public PVector velocity;
        public PVector acceleration;
    }

}
