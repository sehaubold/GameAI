package executors;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * 
 */

/**
 * @author Sam
 *
 */
public class flocking_behavior extends PApplet {

    public static final int FRAME_WIDTH = 1000;
    public static final int FRAME_HEIGHT = 1000;
    public static final int NUM_CHAR = 50;
    public static final float MAX_FORCE = (float) 0.07;
    public static final float MAX_SPEED = 2;
    public static final float WEIGHT_COH = (float) 1.5;
    public static final float WEIGHT_SEP = (float) 2.0;
    public static final float SEP_CON = (float) 35.0;
    public static final float WEIGHT_ALIGN = (float) 1.0;
    public static final float INIT_ROT = (float) 1.5;
    public static final int MAX_VELOCITY = 10;
    public static final int MAX_ACCEL = 5;
    public static float MAX_ROT = PI / 60;
    private static int algType;
    public static final int RADIUS_DECEL = 80;
    public static final float RADIUS_DECEL_ROT = PI / 3;
    public static final int RADIUS_SAT = 10;
    public static final float RADIUS_SAT_ROT = PI / 24;
    public static final int EFFECT_RANGE = 3;
    public static final float EFFECT_RANGE_ROT = (float) 3;
    private static final float COH_CON = 70;
    private static final float ALIGN_CON = 70;
    private ArrayList<KinVar> characters;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("New character behavior: Flock or Wander?");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        if (input.contains("flock")) {
            algType = 1;
            MAX_ROT *= (1.0/15.0);
        } else {
            algType = 2;
        }
        in.close();
        PApplet.main("executors.flocking_behavior");
    }

    public void settings(){
        size(FRAME_WIDTH, FRAME_HEIGHT);
    }

    public void setup(){
        fill(420,50,240);
        characters = new ArrayList<KinVar>();
        for (int i = 0; i < NUM_CHAR; i++) {
            characters.add(new KinVar(width/2, height/2, INIT_ROT, random(TWO_PI), 1));
        }
        
    }

    public void draw(){
        background(100);
        for (KinVar character : characters) {
            if (character.behavior == 1)
                flockAlg(characters, character);
            else if (character.behavior == 2) {
                wanderAlg(character, characters);
            }
        }
        
    }
    
    private void flockAlg(ArrayList<KinVar> characters2, KinVar character) {
        PVector boids1 = separate(characters2, character); 
        PVector boids2 = align(characters2, character); 
        PVector boids3 = cohesion(characters2, character); 
        boids1.mult(WEIGHT_SEP);
        boids2.mult(WEIGHT_ALIGN);
        boids3.mult(WEIGHT_COH);
        
        character.acceleration.add(boids1);
        character.acceleration.add(boids2);
        character.acceleration.add(boids3);

        character.velocity.add(character.acceleration);
        character.velocity.limit(MAX_SPEED);
        character.position.add(character.velocity);
        character.acceleration.set(0, 0);

        if (character.position.x < 0) 
            character.position.x = width;
        if (character.position.y < 0) 
            character.position.y = height;
        if (character.position.x > width) 
            character.position.x = 0;
        if (character.position.y > height) 
            character.position.y = 0;
        drawMe(character);
    }

    public PVector separate (ArrayList<KinVar> characterlist, KinVar character) {
      PVector sepRet = new PVector(0, 0);
      int num = 0;
      for (KinVar target : characterlist) {
            float difference = PVector.dist(character.position, target.position);
            if ((difference > 0) && (difference < SEP_CON)) {
                  PVector change = PVector.sub(character.position, target.position);
                  num++; 
                  change.normalize();
                  change.div(difference);
                  sepRet.add(change);
            }
      }
      if (num > 0) {
        sepRet.div((float) num);
      }

      if (sepRet.mag() > 0) {
        sepRet.normalize();
        sepRet.mult(MAX_SPEED);
        sepRet.sub(character.velocity);
        sepRet.limit(MAX_FORCE);
      }
      return sepRet;
    }

    public PVector align (ArrayList<KinVar> characterlist, KinVar character) {
      PVector total = new PVector(0, 0);
      int num = 0;
      for (KinVar other : characterlist) {
        float difference = PVector.dist(character.position, other.position);
        if ((difference > 0) && (difference < ALIGN_CON)) {
          total.add(other.velocity);
          num++;
        }
      }
      if (num > 0) {
        total.div((float)num);
        total.normalize();
        total.mult(MAX_SPEED);
        PVector steer = PVector.sub(total, character.velocity);
        steer.limit(MAX_FORCE);
        return steer;
      } else {
        return new PVector(0, 0);
      }
    }

    public PVector cohesion (ArrayList<KinVar> characterlist, KinVar character) {
      PVector total = new PVector(0, 0); 
      int num = 0;
      for (KinVar other : characterlist) {
        float d = PVector.dist(character.position, other.position);
        if ((d > 0) && (d < COH_CON)) {
          total.add(other.position);
          num++;
        }
      }
      if (num > 0) {
        total.div(num);
        PVector desired = PVector.sub(total, character.position);
        desired.normalize();
        desired.mult(MAX_SPEED);
        PVector steer = PVector.sub(desired, character.velocity);
        steer.limit(MAX_FORCE);  
        return steer;
      } 
      else {
        return new PVector(0, 0);
      }
    }

    public void drawMe(KinVar character) {
        pushMatrix();
        translate(character.position.x, character.position.y);
        rotate(character.velocity.heading());
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        popMatrix();
    }
    

    public void mousePressed() {
        if (algType == 1) {
            characters.add(new KinVar(mouseX, mouseY,INIT_ROT, random(TWO_PI), 1));
        } else {
            characters.add(new KinVar(mouseX, mouseY,INIT_ROT, random(TWO_PI), 2));
        }
        
    }
    private void wanderAlg(KinVar character, ArrayList<KinVar> characters) {
        float change = random(-MAX_ROT, MAX_ROT);
        character.angRot = change;
        character.angle += character.angRot;
        
        if (character.position.x < 0) 
            character.position.x = width;
        if (character.position.y < 0) 
            character.position.y = height;
        if (character.position.x > width) 
            character.position.x = 0;
        if (character.position.y > height) 
            character.position.y = 0;
        

        character.velocity.set(MAX_VELOCITY / 2.0f * cos(character.angle), MAX_VELOCITY / 2.0f * sin(character.angle));
        character.position.add(character.velocity);
        
        pushMatrix();
        fill(0,200,0);
        translate(character.position.x, character.position.y);
        rotate(character.angle);
        triangle(0, 15, 30, 0, 0, - 15);
        ellipse(0, 0, 30, 30);
        fill(420,50,240);
        popMatrix();
    }

    public class KinVar {
        public int behavior;
        public float angRot;
        public float angle;
        public PVector position;
        public PVector velocity;
        public PVector acceleration;
        public PVector orientation;
        
        /**
         * 
         */
        public KinVar(int posX, int posY, float rot, float ang, int behavior) {
            super();
            angRot = (float) rot;
            this.behavior = behavior;
            orientation = new PVector(cos(ang), sin(ang));
            position = new PVector(posX, posY);
            acceleration = new PVector(0, 0);
            angle = ang;
            velocity = new PVector(cos(angle), sin(angle));
        }

        
    }

}
