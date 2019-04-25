/**
 * 
 */
package current;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import current.Graph.Node;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

/**
 * @author Sam
 *
 */
public class path_following  extends PApplet {

    public static final int FRAME_WIDTH = 1000;
    public static final int FRAME_HEIGHT = 1000;
    public static final int STEP = 5;
    public static final String STATE_FILE = "src\\current\\stateinformation.txt";
    public static final int TILE_SIZE = 20;
    public static final int HIST_NUM = 50;
    public static final int VOID_STD = 100;
    public static final int CHAR_SIZE = 10;
    public static final int MAX_VELOCITY = 10;
    public static final float MAX_ROT = PI / 4;
    public static final int RADIUS_DECEL = 25;
    public static final float RADIUS_DECEL_ROT = PI / 3;
    public static final int RADIUS_SAT = 10;
    public static final float RADIUS_SAT_ROT = PI / 24;
    public static final int EFFECT_RANGE = 3;
    public static final float EFFECT_RANGE_ROT = (float) 3;
    public static final int DIGIT_REDUCE = 100;
    public static final int MAX_ACCELERATION = 5;
    private static final double EDGE_WEIGHT = TILE_SIZE;
    public static PVector direction;
    public static Character character;
    public static double timestamp;
    public static ArrayList<PShape> crumbs = new ArrayList<PShape>();
    private ArrayList<PShape> tiles = new ArrayList<PShape>();
    private BehaviorTree tree;
    private static boolean danceDone = false;
    private static boolean rewriteData;
    public static ArrayList<Obstacle> rooms;
    private static Character monster;
    private static Graph testGraph;
    private static DecisionTree characterTree;
    private static LearningTree monsterLearn;
    private static boolean manhattan_dist = false;
    private static boolean printDetails = true;
    public static float accumulator = 0;
    public static boolean crum = true;
    public static StringBuilder stateOutput;
    public static Algorithm lastRet;
    public static boolean learntree;
    private static boolean pause;
    private static boolean bounds;
    /**
     * collects state information before generating environment
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Manhattan, or Straight Line?");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        if (input.contains("M") || input.contains("m")) {
            manhattan_dist = false;
        } else {
            manhattan_dist = true;
        }
        System.out.println("Show Breadcrumbs and Radii?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            crum = true;
        } else {
            crum = false;
        }
        System.out.println("Show Room Bounds?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            bounds = true;
        } else {
            bounds = false;
        }
        System.out.println("Output data to console?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            printDetails = true;
        } else {
            printDetails = false;
        }
        System.out.println("Click Behavior: Pause or Manual");
        input = in.nextLine();
        if (input.contains("p") || input.contains("P")) {
            pause = true;
        } else {
            pause = false;
        }
        rewriteData = false;
        System.out.println("Use Learned Tree?");
        input = in.nextLine();
        if (input.contains("y") || input.contains("Y")) {
            learntree = true;
        } else {
            System.out.println("Rewrite Learned Tree Data?");
            input = in.nextLine();
            if (input.contains("y") || input.contains("Y")) {
                rewriteData = true;
            } else {
                rewriteData = false;
            }
            learntree = false;
        }
        in.close();
        PApplet.main("current.path_following");

    }

    public void settings(){
        size(FRAME_WIDTH, FRAME_HEIGHT);
        
    }

    public void setup(){
        fill(420,50,240);
        character = new Character();
        character.position = new PVector(TILE_SIZE/2, TILE_SIZE/2);
        character.velocity = new PVector(0, 0);
        character.acceleration = new PVector(0, 0);
        character.target = new PVector(TILE_SIZE/2 + 1, TILE_SIZE/2 + 1);
        character.goal = new PVector(TILE_SIZE/2 + 1, TILE_SIZE/2 + 1);
        direction = new PVector(0, 0);
        character.orientation = new PVector(1,  0);
        character.angRot = 0;
        character.angle = 0;
        character.alive = true;
        character.inSight = false;
        monster = new Character();
        monster.position = new PVector(3 * (FRAME_WIDTH/4) + TILE_SIZE/2, FRAME_HEIGHT/2 - TILE_SIZE/2);
//        monster.position = new PVector(650, 330);
        monster.velocity = new PVector(0, 0);
        monster.acceleration = new PVector(0, 0);
        monster.orientation = new PVector(1,  0);
        monster.angRot = 0;
        monster.angle = 0;
        monster.inSight = false;
        monster.path = new Stack<Node>();
        monster.target = new PVector(monster.position.x, monster.position.y);
        monster.goal = new PVector(monster.position.x, monster.position.y);
        monster.alive = true;
        rooms = createRooms();
        characterTree = DecisionTree.autogenerate(character);
        character.graph = gen_graph();
        monster.graph = gen_graph();

        tiles = createTiles();
        character.graph.findTarget = true;
        monster.graph.findTarget = true;
        if (printDetails) {
            character.graph.printDistances = false;
            character.graph.printPath = true;
            monster.graph.printDistances = false;
            monster.graph.printPath = true;
        } else {
            character.graph.printDistances = false;
            character.graph.printPath = false;
            monster.graph.printDistances = false;
            monster.graph.printPath = false;
        }
        if (!manhattan_dist ) {
            character.graph.manDist = true;
            monster.graph.manDist = true;
        }

        character.path = goalSet(character, (int) character.target.x, (int) character.target.y);
        monster.path = goalSet(monster, (int) monster.target.x, (int) monster.target.y);
        testGraph = gen_graph();
        testGraph.manDist = true;
        testGraph.printDistances = false;
        testGraph.printPath = false;
        monster.start = monster.graph.getStart().index;
        character.start = character.graph.getStart().index;
        character.run = true;
        monster.run = true;
        tree = BehaviorTree.autogenerate(monster);
        if (learntree) {
            monsterLearn = new LearningTree();            
        }
        if (rewriteData) {
            monsterLearn = new LearningTree("ignore");
            monsterLearn.DataFile(STATE_FILE, "State Information:\nAlive    |    In Sight    |    PathEmpty |    Action\n");
        }
        stateOutput = new StringBuilder("");
//      TODO remove
//        System.out.println("pause");
//        character.run = false;
//        monster.run = false;
    }

    /* 
     * draws environment with each iteration
     * (non-Javadoc)
     * @see processing.core.PApplet#draw()
     */
    public void draw(){
        stateOutput = new StringBuilder("");
        background(100);
        drawTiles();
        drawRoom();
        trail();
        if (character.run) {
            Algorithm behavior = characterTree.getAction(character);
//            System.out.println(behavior);
            if (character.alive) {
//                System.out.println(getRoomNum(monster.position.x, monster.position.y));
                characterUpdate(character);
                
                //character behavior
                if (behavior == Algorithm.WANDER) {
                    wander(character);
                    lastRet = behavior;
                } else if (behavior == Algorithm.FINDPATH) {
                    findPath(character);
                } else if (behavior == Algorithm.RUN) {
                    run(character);
                    lastRet = behavior;
                } else if (behavior == Algorithm.HIDE) {
                    hide(character);
                    lastRet = behavior;
                } else if (behavior == Algorithm.CHANGE1) {
                    changeRoom(1, character);
                    lastRet = behavior;
                } else if (behavior == Algorithm.CHANGE2) {
                    changeRoom(2, character);
                    lastRet = behavior;
                } else if (behavior == Algorithm.CHANGE3) {
                    changeRoom(3, character);
                    lastRet = behavior;
                }

                if (character.position.dist(monster.position) <= RADIUS_SAT * 2) {
                    character.alive = false;
                    character.run = false;
                    monster.alive = false;
                    monster.inSight = false;
                }
            } else if (behavior == Algorithm.DEAD && monster.path.isEmpty()) {
                resetChar();
                character.alive = true;
            }
        } else {
            monster.alive = false;
            character.target = new PVector(TILE_SIZE/2, TILE_SIZE/2);
            AlgorithmSet.arriveSimple(direction, character.target, character, MAX_ACCELERATION, MAX_VELOCITY, RADIUS_DECEL, RADIUS_SAT, EFFECT_RANGE);
            AlgorithmSet.SteeringAdjust(character,  RADIUS_SAT_ROT, RADIUS_DECEL_ROT, EFFECT_RANGE_ROT, MAX_ROT);
            AlgorithmSet.kinematicAdjust(character);
        }
        drawCharacter(character, 1);
        if (monster.run) {
            if (learntree) {
                Algorithm returned = monsterLearn.getAction(monster.alive, monster.inSight, monster.path.isEmpty());
                if (returned == Algorithm.FINDPATH) {
                    findPath(monster);
                } else if (returned == Algorithm.WANDER) {
                    wander(monster);
                } else if (returned == Algorithm.CHANGE1) {
                    changeRoom(1, monster);
                } else if (returned == Algorithm.CHANGE2) {
                    changeRoom(2, monster);
                } else if (returned == Algorithm.CHANGE3) {
                    changeRoom(3, monster);
                } else if (returned == Algorithm.SEARCH) {
                    search(monster);
                } else if (returned == Algorithm.CHASE) {
                    chase(monster);
                } else if (returned == Algorithm.DANCE) {
                    dance(monster);
                }
            } else {
                if (tree.getBehavior(monster)) {
                    //do nothing for now, may be tweaked
                }
            }
        }
        //draw monster
        drawMonster(monster, 2);
        if (rewriteData)
            storeInformation();
    }

    /**
     * this code will generate a random point within the selected room
     * and create a path to the closest playable point in that room
     * @param num
     * @param kinematic
     */
    public static boolean changeRoom(int num, Character kinematic) {
        int thresholdx1 = 0;
        int thresholdx2 = 0;
        int thresholdy1 = 0;
        int thresholdy2 = 0;
        if (num == 1) {
            thresholdx2 = FRAME_WIDTH/2;
            thresholdy2 = 750;
        } else if (num == 2) {
            thresholdx1 = FRAME_WIDTH/2;
            thresholdx2 = FRAME_WIDTH;
            thresholdy2 = 750;
        } else if (num == 3) {
            thresholdx2 = FRAME_WIDTH;
            thresholdy1 = 750;
            thresholdy2 = FRAME_HEIGHT;
        }
        Random random = new Random();
        int newX = random.nextInt(thresholdx2 - thresholdx1);
        int newY = random.nextInt(thresholdy2 - thresholdy1);
        newX += thresholdx1;
        newY += thresholdy1;
        kinematic.path = goalSet(kinematic, newX, newY);
        return true;
        
    }

    private void storeInformation() {
        stateOutput.append(monster.alive);
        stateOutput.append(" ");
        stateOutput.append(monster.inSight);
        stateOutput.append(" ");
        stateOutput.append(tree.emptyPath);
        stateOutput.append(" ");
        stateOutput.append(tree.lastAction);
        stateOutput.append(" ");
        
        monsterLearn.DataWrite(STATE_FILE, stateOutput.toString());
        
    }

    /**
     * this method updates whether the character and monster can see each other
     * @param char2
     */
    private void characterUpdate(Character char2) {
        if (!char2.alive || !char2.run)
            return;
        PVector direction = PVector.sub(monster.position, char2.position);
        PVector sightline = AlgorithmSet.rayCast(direction, (int) char2.position.dist(monster.position), FRAME_WIDTH, FRAME_HEIGHT, rooms, char2);
        if (sightline == null) {
            if (!char2.inSight)
                char2.path.removeAllElements();
            if (!monster.inSight)
                monster.path.removeAllElements();
            char2.inSight = true;
            monster.inSight = true;
        } else {
            char2.inSight = false;
            monster.inSight = false;
            
        }
        
    }

    /* 
     * this method stores the mouse click location and generates a path for the
     * character to follow to that point, allowing for manual state manipulation
     * (non-Javadoc)
     * @see processing.core.PApplet#mousePressed()
     */
    public void mousePressed() {
        if (!pause) {
            int truLoc[] = new int[2];
            truLoc[0] = mouseX;
            truLoc[1] = mouseY;
            PVector adjusted = adjustLoc(character, truLoc[0], truLoc[1]);
            truLoc[0] = (int) adjusted.x;
            truLoc[1] = (int) adjusted.y;
            Node e = character.graph.nodes.get(AlgorithmSet.vertexClosest(character.position.x, character.position.y, TILE_SIZE, FRAME_WIDTH, character.graph));
            if (checkPlayability((int) e.x, (int) e.y)) 
                    character.path = goalSet(character,truLoc[0], truLoc[1]);
        } else {
            monster.run = !monster.run;
            if (monster.run) {
                loop();
            } else {
                noLoop();
            }
        }
    }

    /**
     * finds a point in a random direction, then navigates to the closest point in that direction
     * distance is currently two tiles away
     * @param parameter
     */
    public static boolean wander(Character parameter) {
        float dist = path_following.TILE_SIZE * 2;
        float pointsx[] = new float[] {parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x};
        float pointsy[] = new float[] {parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y};
        pointsx[0] += dist;
        pointsx[1] -= dist;
        pointsy[2] += dist;
        pointsy[3] -= dist;
        pointsx[4] += dist;
        pointsy[4] += dist;
        pointsx[5] -= dist;
        pointsy[5] -= dist;
        pointsx[6] += dist;
        pointsy[6] -= dist;
        pointsx[7] -= dist;
        pointsy[7] += dist;
        
        for (int j = 0; j < pointsx.length; j++) {
            if (pointsx[j] <= 0) pointsx[j] = parameter.position.x;
            if (pointsx[j] >= path_following.FRAME_WIDTH) pointsx[j] = parameter.position.x;
            if (pointsy[j] <= 0) pointsy[j] = parameter.position.y;
            if (pointsy[j] >= path_following.FRAME_HEIGHT) pointsy[j] = parameter.position.y;
        }
        Random selector = new Random();
        int selected = selector.nextInt(pointsx.length);
        parameter.graph.setTarget(AlgorithmSet.vertexClosest(pointsx[selected], pointsy[selected], path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
        parameter.goal = new PVector(pointsx[selected], pointsy[selected]);
        parameter.graph.setStart(AlgorithmSet.vertexClosest(parameter.position.x, parameter.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
        parameter.path = AStarAlg.aStar(parameter.graph);
        return true;
    }
    public static boolean findPath(Character parameter) {
        if (parameter.position.dist(parameter.target) < RADIUS_DECEL && !(parameter.target.x == parameter.goal.x && parameter.target.y == parameter.goal.y) && !parameter.path.isEmpty()) {
            Node nextTarget = parameter.path.pop();
            parameter.target.x = (float) nextTarget.x;
            parameter.target.y = (float) nextTarget.y;
        } else if (parameter.path.isEmpty() && parameter.position.dist(parameter.target) <= path_following.RADIUS_SAT) {
            return false;
        }
        AlgorithmSet.arriveSimple(direction, parameter.target, parameter, MAX_ACCELERATION, MAX_VELOCITY, RADIUS_DECEL, RADIUS_SAT, EFFECT_RANGE);
        AlgorithmSet.SteeringAdjust(parameter,  RADIUS_SAT_ROT, RADIUS_DECEL_ROT, EFFECT_RANGE_ROT, MAX_ROT);
        AlgorithmSet.kinematicAdjust(parameter);
        return true;

    }
    public static boolean chase(Character param) {
        Character target = path_following.character;
        param.goal = new PVector(target.position.x + target.velocity.x, target.position.y + target.velocity.y);
        param.graph.setTarget(AlgorithmSet.vertexClosest(param.goal.x, param.goal.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, param.graph));
        param.graph.setStart(AlgorithmSet.vertexClosest(param.position.x, param.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, param.graph));
        param.path = AStarAlg.aStar(param.graph);
        return true;

    }
    public static void dance(Character param) {
        if (danceDone) {
            Node start = param.graph.nodes.get(param.start);
            PVector check = new PVector((float) start.x, (float) start.y);
            if (!param.path.isEmpty() || !(param.position.dist(check) <= RADIUS_DECEL)) {
                param.target = param.goal;
                AlgorithmSet.arriveSimple(direction, param.target, param, MAX_ACCELERATION, MAX_VELOCITY, RADIUS_DECEL, RADIUS_SAT, EFFECT_RANGE);
                AlgorithmSet.SteeringAdjust(param,  RADIUS_SAT_ROT, RADIUS_DECEL_ROT, EFFECT_RANGE_ROT, MAX_ROT);
                AlgorithmSet.kinematicAdjust(param);
                return;
            }
            danceDone = false;
            param.alive = true;  
            path_following.character.run = true;
            param.run = true;
            return;
        }
        Graph graph = new Graph();
        int toAdd = 200;
        param.path = new Stack<Graph.Node>();
        Graph.Node loc1 = graph.new Node("", 0, 0, param.position.x - toAdd/2, param.position.y - toAdd/2, 0);
        Graph.Node loc2 = graph.new Node("", 0, 0, param.position.x + toAdd/2, param.position.y - toAdd/2, 0);
        Graph.Node loc3 = graph.new Node("", 0, 0, param.position.x - toAdd/2, param.position.y + toAdd/2, 0);
        Graph.Node loc4 = graph.new Node("", 0, 0, param.position.x - toAdd/2, param.position.y + toAdd/2, 0);
        Graph.Node loc5 = graph.new Node("", 0, 0, param.position.x + toAdd/2, param.position.y + toAdd/2, 0);
        Graph.Node loc6 = graph.new Node("", 0, 0, param.position.x, param.position.y, 0);
        param.path.push(loc6);
        param.path.push(loc5);
        param.path.push(loc4);
        param.path.push(loc3);
        param.path.push(loc2);
        param.target = new PVector((float) loc1.x, (float)  loc1.y);
        danceDone = true;
        
        param.angRot = 0;
        param.angle = 0;
        param.inSight = false; 
        Stack<Graph.Node> storage = new Stack<Graph.Node>();
        int num = param.path.size();
        for (int i = 0; i < num; i++) {
            storage.push(param.path.pop());
        }
        Node reset = param.graph.nodes.get(param.start);
        param.path.push(reset);
        for (int i = 0; i < num; i++) {
            param.path.push(storage.pop());
        }
        param.goal = new PVector((float) reset.x, (float) reset.y);
    }
    public static boolean search(Character parameter) {
    PVector search = new PVector();
    Stack<PVector> searchLocations = new Stack<PVector>();

    float dist = path_following.TILE_SIZE * 2;
    float pointsx[] = new float[] {parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x};
    float pointsy[] = new float[] {parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y};
    pointsx[0] += dist;
    pointsx[1] -= dist;
    pointsy[2] += dist;
    pointsy[3] -= dist;
    for (int j = 0; j < pointsx.length; j++) {
            searchLocations.push(new PVector(pointsx[j], pointsy[j]));
    }
    search = searchLocations.pop();
    parameter.path = goalSet(parameter, (int) search.x, (int) search.y);
    
    while (!searchLocations.isEmpty()) {
        search = searchLocations.pop();
        Stack<Node> temp = goalSet(parameter, (int) search.x, (int) search.y);
        ArrayList<Node> invert = new ArrayList<Node>();
        while (!temp.isEmpty())
            invert.add(temp.pop());
        for (int k = invert.size() - 1; k > 0; k--)
            parameter.path.push(invert.get(k));
        
        for (int k = 0; k < invert.size(); k++)
            parameter.path.push(invert.get(k));

    }
        return true;

    }


    /**
     * sends out rays in 7 directions, looking for a location that can't be seen by the monster's predicted
     * future location, then navigates to the first found
     * future additions may include farthest point, random point, etc. TODO
     * @param character2
     */
    private void hide(Character character2) {
            ArrayList<PVector> rays = new ArrayList<PVector>();
            PVector predictedFuture = new PVector(monster.position.x + monster.velocity.x, monster.position.y + monster.velocity.y);
            PVector fleeDir = new PVector(character2.position.x - predictedFuture.x, character2.position.y - predictedFuture.y);
            fleeDir = fleeDir.normalize();
            fleeDir.mult(TILE_SIZE);
            rays.add(fleeDir);
            PVector fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(PI/3);
            rays.add(fleeRay);
            fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(-PI/3);
            rays.add(fleeRay);
            fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(PI/6);
            rays.add(fleeRay);
            fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(-PI/6);
            rays.add(fleeRay);
            fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(PI/2);
            rays.add(fleeRay);
            fleeRay = new PVector(fleeDir.x, fleeDir.y);
            fleeRay = fleeRay.rotate(-PI/2);
            rays.add(fleeRay);
            Node current = character2.graph.nodes.get(AlgorithmSet.vertexClosest(character2.position.x, character2.position.y, TILE_SIZE, FRAME_WIDTH, character2.graph));
            int target = -1;
            character2.graph.setStart(current.index);
            for (int i = 0; i < rays.size(); i++) {
                int test = AlgorithmSet.vertexClosest(character2.position.x + i * rays.get(i).x, character2.position.y + i * rays.get(i).y, TILE_SIZE, FRAME_WIDTH, character2.graph);
                Node testNode = character2.graph.nodes.get(test);
                fleeDir = new PVector((float) (monster.position.x - testNode.x), (float) (monster.position.y - testNode.y));
                fleeDir = fleeDir.normalize();
                PVector loc = new PVector((float) testNode.x, (float) testNode.y);
                PVector ret = AlgorithmSet.rayCast(fleeDir, (int) PVector.dist(predictedFuture, loc), width, height, rooms, testNode);
                if (ret != null) {
                    target = testNode.index;
                    break;
                }
            }
            
            if (target == -1) {
                return;
            }
            character2.graph.setStart(current.index);
            character2.graph.setTarget(target);
            character2.path = AStarAlg.aStar(character2.graph);
        }

    /**
     * algorithm allows for a character to check for locations at 3 angles away from the monster
     * then selects the farthest to navigate to
     * @param player
     */
    private void run(Character player) {
            PVector predictedFuture = new PVector(monster.position.x + monster.velocity.x, monster.position.y + monster.velocity.y);
            PVector fleeDir = new PVector(player.position.x - predictedFuture.x, player.position.y - predictedFuture.y);
            fleeDir = fleeDir.normalize();
            fleeDir.mult(TILE_SIZE);
            PVector fleeRay1 = new PVector(fleeDir.x, fleeDir.y);
            fleeRay1 = fleeRay1.rotate(PI/12);
            PVector fleeRay2 = new PVector(fleeDir.x, fleeDir.y);
            fleeRay2 = fleeRay2.rotate(-PI/12);
            float max = 0;
            Node current = player.graph.nodes.get(AlgorithmSet.vertexClosest(player.position.x, player.position.y, TILE_SIZE, FRAME_WIDTH, player.graph));
            int target = -1;
            player.graph.setStart(current.index);
            for (int i = 0; i < 15; i++) {
                int opp = AlgorithmSet.vertexClosest(player.position.x + i * fleeDir.x, player.position.y + i * fleeDir.y, TILE_SIZE, FRAME_WIDTH, player.graph);
                int right = AlgorithmSet.vertexClosest(player.position.x + i * fleeRay1.x, player.position.y + i * fleeRay1.y, TILE_SIZE, FRAME_WIDTH, player.graph);
                int left = AlgorithmSet.vertexClosest(player.position.x + i * fleeRay2.x, player.position.y + i * fleeRay2.y, TILE_SIZE, FRAME_WIDTH, player.graph);
    
                player.graph.setTarget(opp);
                AStarAlg.aStar(player.graph);
                if (target == -1 || player.graph.getTarget().fweight > max) {
                    target = opp;
                    max = (float) player.graph.getTarget().fweight;
                }
    
                player.graph.setTarget(right);
                AStarAlg.aStar(player.graph);
                if (player.graph.getTarget().fweight > max) {
                    target = right;
                    max = (float) player.graph.getTarget().fweight;
                }
                
                player.graph.setTarget(left);
                AStarAlg.aStar(player.graph);
                if (player.graph.getTarget().fweight > max) {
                    target = left;
                    max = (float) player.graph.getTarget().fweight;
                }
                
    //            pushMatrix();
    //            fill(255,50,70);
    //            ellipse(character.position.x + i * fleeDir.x, character.position.y + i * fleeDir.y, RADIUS_DECEL/2, RADIUS_DECEL/2);
    //            ellipse(character.position.x + i * fleeRay1.x, character.position.y + i * fleeRay1.y, RADIUS_DECEL/2, RADIUS_DECEL/2);
    //            ellipse(character.position.x + i * fleeRay2.x, character.position.y + i * fleeRay2.y, RADIUS_DECEL/2, RADIUS_DECEL/2);
    //            fill(420,50,240);
    //            popMatrix();
            }
    //        noLoop();
    
            player.graph.setStart(current.index);
            player.graph.setTarget(target);
            player.path = AStarAlg.aStar(player.graph);
            
            
        }

    /**
     * location given returns true if playable, false if not.
     * @param loc_x
     * @param loc_y
     * @return
     */
    private static boolean checkPlayability(int loc_x, int loc_y) {
        if (loc_x < 0 || loc_y < 0 || loc_x > FRAME_WIDTH || loc_y > FRAME_WIDTH)
            return false;
        for (int j = 0; j < rooms.size(); j++) {
            if (rooms.get(j).containsPoint(loc_x, loc_y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * adjusts location to match a valid location
     */
    private static PVector adjustLoc(Character parameter, int locx1, int locy1) {
        if (!checkPlayability(locx1, locy1)) {
            //LEFT
            int point1x = locx1;
            int point1y = locy1;
            //RIGHT
            int point2x = locx1;
            int point2y = locy1;
            //UP
            int point3x = locx1;
            int point3y = locy1;
            //DOWN
            int point4x = locx1;
            int point4y = locy1;
            //RIGHTUP
            int point5x = locx1;
            int point5y = locy1;
            //RIGHTDOWN
            int point6x = locx1;
            int point6y = locy1;
            //LEFTUP
            int point7x = locx1;
            int point7y = locy1;
            //LEFTDOWN
            int point8x = locx1;
            int point8y = locy1;
            while (!checkPlayability(locx1, locy1)) {
                point1x--;
                point2x++;
                point3y--;
                point4y++;
                point5x++;
                point5y--;
                point6x++;
                point6y++;
                point7x--;
                point7y--;
                point8x--;
                point8y++;
                if (checkPlayability(point1x, point1y)) {
                    locx1 = point1x - CHAR_SIZE;
                    locy1 = point1y;
                    
                } else if (checkPlayability(point2x, point2y)) {
                    locx1 = point2x + CHAR_SIZE;
                    locy1 = point2y;
                }  else if (checkPlayability(point3x, point3y)) {
                    locx1 = point3x;
                    locy1 = point3y - CHAR_SIZE;
                }  else if (checkPlayability(point4x, point4y)) {
                    locx1 = point4x;
                    locy1 = point4y + CHAR_SIZE;
                }  else if (checkPlayability(point5x, point5y)) {
                    locx1 = point5x + CHAR_SIZE;
                    locy1 = point5y - CHAR_SIZE;
                }  else if (checkPlayability(point6x, point6y)) {
                    locx1 = point6x + CHAR_SIZE;
                    locy1 = point6y + CHAR_SIZE;
                }  else if (checkPlayability(point7x, point7y)) {
                    locx1 = point7x - CHAR_SIZE;
                    locy1 = point7y - CHAR_SIZE;
                }  else if (checkPlayability(point8x, point8y)) {
                    locx1 = point8x - CHAR_SIZE;
                    locy1 = point8y + CHAR_SIZE;
                } 
            }
            return new PVector(locx1, locy1);
        } else if (!(parameter.target.x == parameter.goal.x && parameter.target.y == parameter.goal.y)) {
            int templock1 = locx1;
            int templock2 = locy1;
          //LEFT
            int point1x = locx1;
            int point1y = locy1;
            //RIGHT
            int point2x = locx1;
            int point2y = locy1;
            //UP
            int point3x = locx1;
            int point3y = locy1;
            //DOWN
            int point4x = locx1;
            int point4y = locy1;
            //RIGHTUP
            int point5x = locx1;
            int point5y = locy1;
            //RIGHTDOWN
            int point6x = locx1;
            int point6y = locy1;
            //LEFTUP
            int point7x = locx1;
            int point7y = locy1; //PRANKD
            //LEFTDOWN
            int point8x = locx1;
            int point8y = locy1;
            int count = 0;
            while (checkPlayability(locx1, locy1) && count <= CHAR_SIZE/2) {
                locx1 = templock1;
                locy1 = templock2;
                point1x--;
                point2x++;
                point3y--;
                point4y++;
                point5x++;
                point5y--;
                point6x++;
                point6y++;
                point7x--;
                point7y--;
                point8x--;
                point8y++;
                if (!checkPlayability(point1x, point1y)) {
                    locx1 += count;
                    locy1 = point1y;
                    
                } else if (!checkPlayability(point2x, point2y)) {
                    locx1 -= count;
                    locy1 = point2y;
                }  else if (!checkPlayability(point3x, point3y)) {
                    locx1 = point3x;
                    locy1 += count;
                }  else if (!checkPlayability(point4x, point4y)) {
                    locx1 = point4x;
                    locy1 -= count;
                }  else if (!checkPlayability(point5x, point5y)) {
                    locx1 -= count;
                    locy1 += count;
                }  else if (!checkPlayability(point6x, point6y)) {
                    locx1 -= count;
                    locy1 -= count;
                }  else if (!checkPlayability(point7x, point7y)) {
                    locx1 += count;
                    locy1 += count;
                }  else if (!checkPlayability(point8x, point8y)) {
                    locx1 += count;
                    locy1 -= count;
                } 
                count++;
            }
            return new PVector(locx1, locy1);
        }
        return new PVector(locx1, locy1);
    
        
    }

    /**
     * sets the path parameters based on location given
     * @param xin
     * @param yin
     */
    private static Stack<Node> goalSet(Character param, int xin, int yin) {
        if (checkPlayability(xin, yin)) {
            param.goal = new PVector(xin, yin);
            param.graph.setTarget(AlgorithmSet.vertexClosest(xin, yin, TILE_SIZE, FRAME_WIDTH, param.graph));
            param.graph.setStart(AlgorithmSet.vertexClosest(param.position.x, param.position.y, TILE_SIZE, FRAME_WIDTH, param.graph));
            return AStarAlg.aStar(param.graph);
        } else {
            PVector adjusted = adjustLoc(param, xin, yin);
            param.goal = new PVector(adjusted.x, adjusted.y);
            param.graph.setTarget(AlgorithmSet.vertexClosest(xin, yin, TILE_SIZE, FRAME_WIDTH, param.graph));
            param.graph.setStart(AlgorithmSet.vertexClosest(param.position.x, param.position.y, TILE_SIZE, FRAME_WIDTH, param.graph));
            return AStarAlg.aStar(param.graph);
        }
        
    }

    /**
     * returns the room the location is in
     * @param num1
     * @param num2
     * @return
     */
    public int getRoomNum(float num1, float num2) {
        int x = (int) num1;
        int y = (int) num2;
        if (x >= 0 && x < FRAME_WIDTH/2 && y >= 0 && y < 750)
            return 1;
        else if (x >= FRAME_WIDTH/2 && x <= FRAME_WIDTH && y >= 0 && y < 750)
            return 2;
        else
            return 3;
    }

    /**
     * draws monster to scale i
     * @param mon
     * @param i
     */
    private void drawMonster(Character mon, int i) {
        pushMatrix();
    
        fill(0,220,50);
        translate(mon.position.x, mon.position.y);
        rotate(mon.orientation.heading());
//        triangle(0 , 0, CHAR_SIZE * 1 * i, CHAR_SIZE * i, CHAR_SIZE * 1 * i, -CHAR_SIZE * i );
        triangle(0, CHAR_SIZE/2 * i, CHAR_SIZE * i, 0, 0, - CHAR_SIZE/2 * i);
        ellipse(0, 0, CHAR_SIZE * i, CHAR_SIZE * i);
    
        fill(420,50,240);
        popMatrix();
        
    }

    /**
     * draws character to scale i
     * @param character2
     * @param i
     */
    private void drawCharacter(Character character2, int i) {
        pushMatrix();
        
        translate(character.position.x, character.position.y);
        rotate(character.orientation.heading());
        
        triangle(0, CHAR_SIZE/2 * i, CHAR_SIZE * i, 0, 0, - CHAR_SIZE/2 * i);
        ellipse(0, 0, CHAR_SIZE * i, CHAR_SIZE * i);
        popMatrix();
    }

    /**
     * prints character trails
     */
    private void trail() {
        accumulator += STEP;
        if (crum) {
            if (accumulator % 3 == 0 && character.velocity.mag() > 0) {
                fill(420,50,240);
                crumbs.add(createShape(ELLIPSE, character.position.x, character.position.y, 15, 15));
                fill(0,50,255);
                crumbs.add(createShape(ELLIPSE, monster.position.x, monster.position.y, 15, 15));
                fill(420,50,240);
            }
            for (int i = crumbs.size() - HIST_NUM; i < crumbs.size(); i++) {
                if (i >= 0)
                    shape(crumbs.get(i));
            }
            printHistory();
            if (bounds)
                printBoundaries();
        }        
    }

    /**
     * prints room boundries
     */
    private void printBoundaries() {
        PVector botRoomStart = new PVector(0, 750);
        PVector rightRoomStart = new PVector(FRAME_WIDTH/2, 0);
        PVector end = new PVector(FRAME_WIDTH, 750);
        shape(createShape(RECT, botRoomStart.x, botRoomStart.y - TILE_SIZE/2, end.x, TILE_SIZE));
        shape(createShape(RECT, rightRoomStart.x - TILE_SIZE, rightRoomStart.y, TILE_SIZE, end.y ));
        
    }

    /**
     * resets the character simulation state
     */
    private void resetChar() {
        character.position = new PVector(TILE_SIZE/2, TILE_SIZE/2);
        character.velocity = new PVector(0, 0);
        character.acceleration = new PVector(0, 0);
        character.target = new PVector(TILE_SIZE/2 + 1, TILE_SIZE/2 + 1);
        character.goal = new PVector(TILE_SIZE/2 + 1, TILE_SIZE/2 + 1);
        character.orientation = new PVector(1,  0);
        character.angRot = 0;
        character.angle = 0;
        character.inSight = false;    
        character.graph.setStart(0);  
        character.graph.setTarget(0);
        character.path = new Stack<Graph.Node>();
        crumbs = new ArrayList<PShape>();
    }

    /**
     * prints a set amount of last locations the characters were in
     */
    private void printHistory() {
        
        pushMatrix();
        fill(220,50,0);
        ellipse(character.target.x, character.target.y, RADIUS_DECEL, RADIUS_DECEL);
        fill(0,220,0);
        ellipse(character.goal.x, character.goal.y, RADIUS_DECEL/2, RADIUS_DECEL/2);
        fill(0,220,0);
        ellipse((int) character.graph.getTarget().x, (int) character.graph.getTarget().y, RADIUS_DECEL, RADIUS_DECEL);
        fill(0,0,0);
        ellipse(character.target.x, character.target.y, RADIUS_SAT, RADIUS_SAT);
        fill(420,50,240);
        popMatrix();
    
        pushMatrix();
        fill(220,50,0);
        ellipse(monster.target.x, monster.target.y, RADIUS_DECEL, RADIUS_DECEL);
        fill(255,100,0);
        ellipse((int) monster.goal.x, (int) monster.goal.y, RADIUS_DECEL*2, RADIUS_DECEL*2);
        fill(0,0,0);
        ellipse(monster.target.x, monster.target.y, RADIUS_SAT, RADIUS_SAT);
        fill(420,50,240);
        popMatrix();
    }

    /**
     * generates a set of void areas within the environment for obstructions
     * @return
     */
    private ArrayList<Obstacle> createRooms() {
        ArrayList<Obstacle> room = new ArrayList<Obstacle>();
        int leftCorn_x = 700;
        int leftCorn_y = 0;
        //create void 1
        fill(0);
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        leftCorn_x = 700;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        // create void 2 -> L shape
        leftCorn_x = 000;
        leftCorn_y = 700;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100 - TILE_SIZE;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100 + TILE_SIZE;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y -= 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y -= 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //void 3
        leftCorn_x = 900;
        leftCorn_y = 500;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //void 4
        leftCorn_x = 300;
        leftCorn_y = 200;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x += 100;
        leftCorn_y = 200;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_x = 400;
        leftCorn_y = 100;
        room.add(new Obstacle(createShape(RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD), RECT, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacles now
        //obstacle 1
        fill(0, 0, 200);
        leftCorn_x = 100;
        leftCorn_y = 400;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 2
        leftCorn_x = 500;
        leftCorn_y = 500;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        leftCorn_y += 100;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 3
        leftCorn_x = 800;
        leftCorn_y = 300;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 3
        leftCorn_x = 100;
        leftCorn_y = 900;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 4
        leftCorn_x = 500;
        leftCorn_y = 900;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 5
        leftCorn_x = 900;
        leftCorn_y = 900 - TILE_SIZE;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 6
        leftCorn_x = 300;
        leftCorn_y = 800;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        //obstacle 7
        leftCorn_x = 700;
        leftCorn_y = 800;
        room.add(new Obstacle(createShape(ELLIPSE, leftCorn_x + VOID_STD/2, leftCorn_y + VOID_STD/2, VOID_STD, VOID_STD), ELLIPSE, leftCorn_x, leftCorn_y, VOID_STD, VOID_STD, false));
        
        return room;
    }

    /**
     * Draw each void area
     */
    private void drawRoom() {
        for(int i = 0; i < rooms.size(); i++) {
            shape(rooms.get(i).obstacle);
        }
        fill(420,50,240);
        
    }

    /**
     * find the nodes adjacent to the given node
     * @param parent
     * @param loc_x
     * @param loc_y
     * @param graph
     * @return
     */
    private ArrayList<Node> getAdjTiles(Node parent, int loc_x, int loc_y, Graph graph) {
        ArrayList<Node> children = new ArrayList<Node>();
        int temp_right_x = loc_x + TILE_SIZE;
        int temp_down_y = loc_y + TILE_SIZE;
        int temp_left_x = loc_x - TILE_SIZE;
        int temp_up_y = loc_y - TILE_SIZE;
        Node right = graph.getNode("" + temp_right_x + "" + loc_y);
        Node left = graph.getNode("" + temp_left_x + "" + loc_y);
        Node down = graph.getNode("" + loc_x + "" + temp_down_y);
        Node up = graph.getNode("" + loc_x + "" + temp_up_y);
        //if found, add them to the list
        if (right != null)
            children.add(right);
        if (left != null)
            children.add(left);
        if (up != null)
            children.add(up);
        if (down != null)
            children.add(down);
        
        return children;
    }

    /**
         * generate the graph
         * @return
         */
        private Graph gen_graph() {
            Graph ret = new Graph();
            int loc_x = TILE_SIZE / 2;
            int loc_y = TILE_SIZE / 2;
            Node toAdd;
            int count = 0;
            //generate Nodes
            for (int j = 0; j < FRAME_WIDTH/TILE_SIZE; j++) {
                for (int i = 0; i < FRAME_WIDTH/TILE_SIZE; i++) {
                    if (checkPlayability(loc_x, loc_y)) {
                        toAdd = ret.new Node("" + loc_x + "" + loc_y, 0, 0, loc_x, loc_y, count);
                        ret.nodes.add(toAdd);
                        count++;
                    }
                    loc_x += TILE_SIZE;
                }
                loc_x = TILE_SIZE / 2;
                loc_y += TILE_SIZE;
            }
            
            //generate Edges
        
            loc_x = TILE_SIZE / 2;
            loc_y = TILE_SIZE / 2;
            for (int j = 0; j < FRAME_WIDTH/TILE_SIZE; j++) {
                for (int i = 0; i < FRAME_WIDTH/TILE_SIZE; i++) {
                    Node parent = ret.getNode("" + loc_x + "" + loc_y);
                    if (parent != null) {
                        ArrayList<Node> children = getAdjTiles(parent, loc_x, loc_y, ret);
                        for (int childNum = 0; childNum < children.size(); childNum++) {
                            if (children.get(childNum) != null)
                                ret.addEdge(parent, children.get(childNum), EDGE_WEIGHT);
                        }                
    //                    System.out.println(parent.toString());
                    }
        
                    loc_x += TILE_SIZE;
                }
                loc_x = TILE_SIZE / 2;
                loc_y += TILE_SIZE;
            }
            ret.setStart(0);
            ret.setTarget(0);
            return ret;
        }

    /**
     * creates tiles from the graph representation
     * @return
     */
    private ArrayList<PShape> createTiles() {
        ArrayList<PShape> tiles = new ArrayList<>();
    
          for (Node e: character.graph.nodes) {
              fill(203);
              int x = (int) e.x - TILE_SIZE/2;
              int y = (int) e.y - TILE_SIZE/2;
              tiles.add(createShape(RECT, x, y, TILE_SIZE, TILE_SIZE));
          }
        return tiles;
    }

    /**
     * gives a room number for a location and throws if it is an invalid room
     * @param x
     * @param y
     * @return
     */
    public int getRoomNum(int x, int y) {
        if (x >= 0 && x < FRAME_WIDTH/2 && y >= 0 && y < 750)
            return 1;
        else if (x >= FRAME_WIDTH/2 && x <= FRAME_WIDTH && y >= 0 && y < 750)
            return 2;
        else if (x >= 0 && x <= FRAME_WIDTH && y >= 750 && y < FRAME_HEIGHT)
            return 3;
        throw new IllegalArgumentException("improper room");
    }

    /**
     * draw the tiles from the graph
     */
    private void drawTiles() {
        for(int i = 0; i < tiles.size(); i++) {
            shape(tiles.get(i));
        }
        fill(420,50,240);
        
    }
   
}
