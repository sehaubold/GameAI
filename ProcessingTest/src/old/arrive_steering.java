package old;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

/**
 * 
 */

/**
 * @author Sam
 *
 */
public class arrive_steering extends PApplet{

    public static final int FRAME_WIDTH = 1000;
    public static final int FRAME_HEIGHT = 1000;
    public static final int MAX_VELOCITY = 10;
    public static final float MAX_ROT = PI / 4;
    public static final int RADIUS_DECEL = 80;
    public static final float RADIUS_DECEL_ROT = PI / 3;
    public static final int RADIUS_SAT = 10;
    public static final float RADIUS_SAT_ROT = PI / 24;
    public static final int EFFECT_RANGE = 3;
    public static final float EFFECT_RANGE_ROT = (float) 3;
    private static final int MAX_ACCELERATION = 10;
    public static int algType;
    public static PVector direction;
    public static PVector target;
    public static KinVar character;
    public static final int STEP = 5;
    private ArrayList<PShape> crumbs = new ArrayList<PShape>();
    public static float accumulator = 0;
    public static boolean crum = false;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Arrive1, or Arrive2?");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        if (input.contains("1")) {
            algType = 1;
        } else {
            algType = 2;
        }
        System.out.println("Show Breadcrumbs and Radii?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            crum = true;
        } 
        in.close();
        PApplet.main("old.arrive_steering");
    }

    public void settings(){
        size(FRAME_WIDTH, FRAME_HEIGHT);
        
    }

    public void setup(){
        fill(420,50,240);
        character = new KinVar();
        character.position = new PVector(width/2, height/2);
        character.velocity = new PVector(0, 0);
        character.acceleration = new PVector(0, 0);
        target = new PVector(width/2 + 1, height/2 + 1);
        direction = new PVector(0, 0);
        character.orientation = new PVector(1,  0);
        character.angRot = 0;
        character.angle = 0;
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
        if (algType == 1) {
            arriveOne();
        } else {
            arriveTwo();
        }

        
    }

    private void arriveTwo() {

        arriveSimple(MAX_ACCELERATION / 2, MAX_VELOCITY * 2, RADIUS_DECEL * 4, RADIUS_SAT, EFFECT_RANGE * 2);
        SteeringAdjust();
        kinematicAdjust();
        if (crum) {
            pushMatrix();
            fill(220,50,0);
            ellipse(target.x, target.y, RADIUS_DECEL * 4, RADIUS_DECEL * 4);
            fill(0,0,0);
            ellipse(target.x, target.y, RADIUS_SAT, RADIUS_SAT);
            fill(420,50,240);
            popMatrix();
            
        }
        
        pushMatrix();
        
        translate(character.position.x, character.position.y);
        rotate(character.orientation.heading());
        
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        popMatrix();
        

    }

    private void arriveOne() {

        arriveSimple(MAX_ACCELERATION, MAX_VELOCITY, RADIUS_DECEL, RADIUS_SAT, EFFECT_RANGE);
        SteeringAdjust();
        kinematicAdjust();
        if (crum) {
            pushMatrix();
            fill(220,50,0);
            ellipse(target.x, target.y, RADIUS_DECEL, RADIUS_DECEL);
            fill(0,0,0);
            ellipse(target.x, target.y, RADIUS_SAT, RADIUS_SAT);
            fill(420,50,240);
            popMatrix();
        }
        
        pushMatrix();
        
        translate(character.position.x, character.position.y);
        rotate(character.orientation.heading());
        
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        popMatrix();
    }

    private void SteeringAdjust() {
        float rot = character.velocity.heading() - character.orientation.heading();
        rot = mapToRange(rot);
        float rotSize = Math.abs(rot);
        float goalRot = 0;
        if (rotSize < RADIUS_SAT_ROT) {
            goalRot = 0;
        } else if (rotSize > RADIUS_DECEL_ROT) {
            goalRot = MAX_ROT;
        } else {
            goalRot = MAX_ROT * rotSize/RADIUS_DECEL_ROT;
        }
        if (rot < 0)
            goalRot *= -1;
        character.angRot = goalRot - character.angRot;
        character.angRot /= EFFECT_RANGE_ROT;
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

    private void kinematicAdjust() {
        character.velocity.add(character.acceleration);
        character.position.add(character.velocity);
        character.angle += character.angRot;
        character.orientation.set((float) Math.cos(character.angle), (float) Math.sin(character.angle));
        
        
    }

    private void arriveSimple(int maxAcceleration, int maxVelocity, int radiusDecel, int radiusSat, float timetoEffect) {
        direction.set(target.x - character.position.x, target.y - character.position.y);
        float distance = sqrt(direction.x * direction.x + direction.y * direction.y);
        float goalspeed = 0;
        if (distance < radiusSat) {
            goalspeed = 0;
        } else if (distance > radiusDecel) {
            goalspeed = maxVelocity;
        } else {
            goalspeed = maxVelocity * (distance/ (radiusDecel - radiusSat));
        }
        PVector goalVelocity = direction;
        goalVelocity.set(goalVelocity.x/goalVelocity.mag(), goalVelocity.y/goalVelocity.mag());
        goalVelocity.set(goalVelocity.x *= goalspeed, goalVelocity.y *= goalspeed);
        character.acceleration.set(goalVelocity.x - character.velocity.x, goalVelocity.y - character.velocity.y);
        character.acceleration.set(character.acceleration.x / timetoEffect, character.acceleration.y / timetoEffect);
    }

    public void mousePressed() {
        targetSet(mouseX, mouseY);
//        velSet();
        
    }

    

    /**
     * sets the target parameters
     * @param mouseX
     * @param mouseY
     */
    private void targetSet(int mouseX, int mouseY) {
        target = new PVector(mouseX, mouseY);
        
    }




    private class KinVar {

        public PVector orientation;
        public float angRot;
        public float angle;
        public PVector position;
        public PVector velocity;
        public PVector acceleration;
    }

}
