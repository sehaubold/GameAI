package algorithms;

import java.util.ArrayList;

import executors.path_following;
import objects.Character;
import objects.Graph;
import objects.Obstacle;
import objects.Graph.Node;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * @author Sam
 *
 */
public class AlgorithmSet extends PApplet {


    public static void arriveSimple(PVector direction, PVector target, Character character, int maxAcceleration, int maxVelocity, int radiusDecel, int radiusSat, float timetoEffect) {
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
//    public static void seek(PVector direction, PVector target, Character character, int maxVelocity) {
//        direction.set(target.x - character.position.x, target.y - character.position.y);
//        float goalspeed = maxVelocity;
//        
//        PVector goalVelocity = direction;
//        goalVelocity.set(goalVelocity.x/goalVelocity.mag(), goalVelocity.y/goalVelocity.mag());
//        goalVelocity.set(goalVelocity.x *= goalspeed, goalVelocity.y *= goalspeed);
//        character.velocity.set(goalVelocity.x, goalVelocity.y);
//        character.acceleration.set(0, 0);
//    }
    

    public static void SteeringAdjust(Character character, float radiusSatRot, float radiusDecelRot, float effectRangeRot, float maxRot) {
        float rot = character.velocity.heading() - character.orientation.heading();
        rot = mapToRange(rot);
        float rotSize = Math.abs(rot);
        float goalRot = 0;
        if (rotSize < radiusSatRot) {
            goalRot = 0;
        } else if (rotSize > radiusDecelRot) {
            goalRot = maxRot;
        } else {
            goalRot = maxRot * rotSize/radiusDecelRot;
        }
        if (rot < 0)
            goalRot *= -1;
        character.angRot = goalRot - character.angRot;
        character.angRot /= effectRangeRot;
    }

    public static void kinematicAdjust(Character character) {
        character.velocity.add(character.acceleration);
        character.position.add(character.velocity);
        character.angle += character.angRot;
        character.orientation.set((float) Math.cos(character.angle), (float) Math.sin(character.angle));
        
        
    }

    public static float mapToRange(float angle) {
        angle = angle % TWO_PI;
        if (Math.abs(angle) < PI) {
            return angle;
        } else if (angle > PI) {
            return angle - TWO_PI;
        } else {
            return angle + TWO_PI;
        }
        
    }


    /**
     * returns the index of the vertex closest to the location
     * @param currentx
     * @param currenty
     * @return index of close vertex
     */
    public static int vertexClosest(double currentx, double currenty, int TILE_SIZE, int FRAME_WIDTH, Graph playable_area) {
        int loc_x = TILE_SIZE / 2;
        int loc_y = TILE_SIZE / 2;
        //generate Nodes
        for (int j = 0; j < FRAME_WIDTH/TILE_SIZE; j++) {
            for (int i = 0; i < FRAME_WIDTH/TILE_SIZE; i++) {
                //do things that interact with a tile here
                if (abs((float) (currentx - loc_x)) <= TILE_SIZE/2 && abs((float) (currenty - loc_y)) <= TILE_SIZE/2) {
                    if (playable_area.getNode("" + loc_x + "" + loc_y) != null)
                        return playable_area.getNode("" + loc_x + "" + loc_y).index;
                }
                loc_x += TILE_SIZE;
            }
            loc_x = TILE_SIZE / 2;
            loc_y += TILE_SIZE;
        }
        loc_x = (int) currentx;
        loc_y = (int) currenty;
        PVector current = new PVector(loc_x, loc_y);
        float minDist = MAX_FLOAT;
        Node closest = null;
        //check in case walls are spammed
        for (Node e : playable_area.nodes) {
            PVector test = new PVector((float) e.x, (float) e.y);
            if (e == null || current.dist(test) < minDist) {
                closest = e;
                minDist = current.dist(test);
            }
            //find node with lowest distance
        }
        if (closest != null)
            return closest.index;
        return 0;
    }

    /**
     * true if unobstructed, false if otherwise
     * @param direction
     * @param distance
     * @return
     */
    public static PVector rayCast(PVector direction, int distance, int width, int height, ArrayList<Obstacle> obstructions, Character character) {
        PVector xyCheck = new PVector(character.position.x, character.position.y);
        direction = direction.normalize();
        for (int i = 0; i < distance; i++) {
            
            for (Obstacle obstacle : obstructions) {
                if (obstacle.containsPoint((int) xyCheck.x, (int) xyCheck.y))
                    return xyCheck;
            }
            if (xyCheck.x > width && xyCheck.x < 0) {
                return xyCheck;
            }
            if (xyCheck.y > height && xyCheck.y < 0) {
                return xyCheck;
            }
            xyCheck.x += direction.x;
            xyCheck.y += direction.y;
        }
        return null;
    }
    /**
     * true if unobstructed, false if otherwise
     * @param direction
     * @param distance
     * @return
     */
    public static PVector rayCast(PVector direction, int distance, int width, int height, ArrayList<Obstacle> obstructions, Node loc) {
        PVector xyCheck = new PVector((float) loc.x, (float) loc.y);
        direction = direction.normalize();
        for (int i = 0; i < distance; i++) {
            
            for (Obstacle obstacle : obstructions) {
                if (obstacle.containsPoint((int) xyCheck.x, (int) xyCheck.y))
                    return xyCheck;
            }
            if (xyCheck.x > width && xyCheck.x < 0) {
                return xyCheck;
            }
            if (xyCheck.y > height && xyCheck.y < 0) {
                return xyCheck;
            }
            xyCheck.x += direction.x;
            xyCheck.y += direction.y;
        }
        return null;
    }
    
    public static void avoidObstacle(ArrayList<Obstacle> rooms, Character character, Graph playable_area, int width, int height) {
        int dist = (int) character.velocity.mag() * 3; //3 time steps distance
        PVector result = rayCast(character.velocity, dist, width, height, rooms, character);
        if (result == null) {
            return;
        } else {
            adjustVel(character, result, playable_area, width, height);
        }
        
    }


    private static void adjustVel(Character character, PVector result, Graph graph, int width, int height) {
        // TODO check in case skipping is an issue
//        int point1x, point2x, point3x, point4x = (int) result.x;
//        int point1y, point2y, point3y, point4y = (int) result.y;
        Node close = graph.nodes.get(vertexClosest(result.x, result.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, graph));
        int x = (int) result.x;
        int y = (int) result.y;
        if (close.x < x) {
            character.velocity.x += -path_following.MAX_VELOCITY;
        } else if (close.x > x) {
            character.velocity.x += path_following.MAX_VELOCITY;
        }
        if (close.y > y) {
            character.velocity.y += path_following.MAX_VELOCITY;
        }  else if (close.y > y) {
            character.velocity.y += -path_following.MAX_VELOCITY;
        } 
        character.velocity = character.velocity.normalize();
        character.velocity.mult(path_following.MAX_VELOCITY);
        
    }



 
}
