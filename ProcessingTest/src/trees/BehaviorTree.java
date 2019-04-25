package trees;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import current.Character;
import current.Graph;
import current.path_following;
import current.Graph.Node;
import enumerations.BehaviorType;
import processing.core.PShape;
import processing.core.PVector;


/**
 * @author Sam
 *
 */
public class BehaviorTree {
    public String lastAction = "";
    public Node root;
    public PVector nextLoc;
    public Stack<PVector> dance;
    public boolean danceDone;
    public boolean emptyPath;

    /**
     * 
     */
    public BehaviorTree() {
        root = new Node(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "isalive", null, 0);
        nextLoc = new PVector();
    }
    public boolean getBehavior(Character character) {
        return root.executeBehavior(character);
    }
    
    public static BehaviorTree autogenerate(Character character) {
        BehaviorTree tree = new BehaviorTree();
        // generate leafs
        Node isdead = tree.new Node(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "isdead", null, 0);
        Node followpath = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "followpath", null, 0);
        Node dance = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "dance", null, 0);
        Node insight = tree.new Node(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "insight", null, 0);
        Node chase = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "chase", null, 0);
        Node wander = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "wander", null, 0);
        Node search = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "search", null, 0);
        Node reset = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "reset", null, 0);
        Node roomChange1 = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms1", null, 0);
        Node roomChange2 = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms2", null, 0);
        Node roomChange3 = tree.new Node(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms3", null, 0);
        
        // generate composites
        Node deadCharacterSeq = tree.new Node(BehaviorType.SEQUENCE, "danceSeq");
        Node chaseSeq = tree.new Node(BehaviorType.SEQUENCE, "chaseSeq");
        Node randSearch = tree.new Node(BehaviorType.RANDOM_SELECT, "randomSelect");
        Node randWander = tree.new Node(BehaviorType.RANDOM_SELECT, "randomSelectWander");
        Node randRoom = tree.new Node(BehaviorType.RANDOM_SELECT, "randomRoom");
        Node selectorRoot = tree.new Node(BehaviorType.SELECTOR, "selectRoot");
        //random room
        randRoom.children.add(roomChange1);
        randRoom.children.add(roomChange2);
        randRoom.children.add(roomChange3);
        
        //random wander
        randWander.children.add(wander);
        randWander.children.add(randRoom);
        
        // random search selector
        randSearch.children.add(randWander);
        randSearch.children.add(search);
        
        // chase sequence
        chaseSeq.children.add(insight);
        chaseSeq.children.add(chase);
        
        //dance sequence
        deadCharacterSeq.children.add(isdead);
        deadCharacterSeq.children.add(dance);
        deadCharacterSeq.children.add(reset);
        
        //root and children assigned
        tree.root = selectorRoot;
        selectorRoot.children.add(followpath);
        selectorRoot.children.add(deadCharacterSeq);
        selectorRoot.children.add(chaseSeq);
        selectorRoot.children.add(randSearch);
        
        return tree;
        
    }

    private class Node {
        public BehaviorType type;
        public BehaviorType actiontype;
        public BehaviorType bool;
        public String field;
        public ArrayList<Node> children;
        public BehaviorType quantifier;
        public double parameter;
        
        /**
         * bool Node constructor
         * @param type Check composite type. this will typically be a leaf for this constructor
         * @param actiontype Condition, update, or bool
         * @param bool for boolean only. field or parameter based
         * @param field what field is this describing?
         * @param quantifier greater, less, or equal sign discrete variables
         * @param parameter param to compare field to
         */
        public Node(BehaviorType type, BehaviorType actiontype, BehaviorType action, String field,
                BehaviorType quantifier, double parameter) {
            super();
            this.type = type;
            this.actiontype = actiontype;
            this.bool = action;
            this.field = field;
            this.quantifier = quantifier;
            this.parameter = parameter;
            this.children = new ArrayList<Node>();
        }
        /**
         * composite Node constructor
         * @param type Check composite type. this will typically be a leaf for this constructor
         */
        public Node(BehaviorType type, String name) {
            super();
            this.type = type;
            this.actiontype = null;
            this.bool = null;
            this.field = name;
            this.quantifier = null;
            this.parameter = 0;
            this.children = new ArrayList<Node>();
        }
        
        public boolean executeBehavior(Character character) {
            if (type == BehaviorType.SELECTOR) {
                for (Node e : children) {
                    if (e.executeBehavior(character)) {
                        return true;
                    }
                }
                return false;
            } else if (type == BehaviorType.SEQUENCE) {
                for (Node e : children) {
                    if (!e.executeBehavior(character)) {
                        return false;
                    }
                }
                return true;
            } else if (type == BehaviorType.RANDOM_SELECT) {
                Random random = new Random();
                int select = random.nextInt(children.size());
                for (int i = select; i < children.size(); i++) {
                    if (children.get(i).executeBehavior(character)) {
                        return true;
                    }
                }
                for (int i = 0; i < select; i++) {
                    if (children.get(i).executeBehavior(character)) {
                        return true;
                    }
                }
                return false;
                
            }  else if (type == BehaviorType.RANDOM_SEQ) {
                Random random = new Random();
                int select = random.nextInt(children.size());
                for (int i = select; i < children.size(); i++) {
                    if (!children.get(i).executeBehavior(character)) {
                        return false;
                    }
                }
                for (int i = 0; i < select; i++) {
                    if (!children.get(i).executeBehavior(character)) {
                        return false;
                    }
                }
                return true;
            } else if (type == BehaviorType.DECORATOR) {
                return children.get(0).executeBehavior(character);
            } else if (type == BehaviorType.LEAF) {
                return resolveLeaf(character);
            } else {
                return false;
            }
        }
        private boolean resolveLeaf(Character character) {
            if (actiontype == BehaviorType.CONDITION) {
                return conditionCheck(character);
            } else if (actiontype == BehaviorType.UPDATE) {
                return updateState(character);
            } else if (actiontype == BehaviorType.ACTION) {
                return performAction(character);
            }
            return false;
        }

        private boolean performAction(Character character) {
//            if (field.equals("charge")) {
//                return charge(character);
//            } else if (field.equals("raycast")) {
////                System.out.println("raycast");
//                return raycast(character);
//            }  else 
            recordBeh();
            if (field.equals("followpath")) {
//                return followPath(character);
                return path_following.findPath(character);
            } else if (field.equals("chase")) {
//                return chase(character);
                return path_following.chase(character);
            } else if (field.equals("wander")) {
//                return wander(character);
                return path_following.wander(character);
            } else if (field.equals("changerooms1")) {
//                return changeRoom(character, 1);
                return path_following.changeRoom(1, character);
            } else if (field.equals("changerooms2")) {
//                return changeRoom(character, 2);
                return path_following.changeRoom(2, character);
            } else if (field.equals("changerooms3")) {
//                return changeRoom(character, 3);
                return path_following.changeRoom(3, character);
            } else if (field.equals("search")) {
//                return search(character);
                return path_following.search(character);
            } else if (field.equals("dance")) {
              return dance(character);
              
          } else if (field.equals("reset")) {
            return reset(character);
        }
            return false;
        }

        private boolean updateState(Character character) {
            if (field.equals("isalive")) {
                character.alive = true;
                return true;
            } else if (field.equals("isdead")) {
                character.alive = false;
                return true;
            }  else if (field.equals("insight")) {
                character.inSight = !character.inSight;
                return true;
            } else if (field.equals("reset")) {
                character.position = new PVector(path_following.TILE_SIZE/2, path_following.FRAME_HEIGHT - path_following.TILE_SIZE/2);
                character.velocity = new PVector(0, 0);
                character.acceleration = new PVector(0, 0);
                character.orientation = new PVector(1,  0);
                character.angRot = 0;
                character.angle = 0;
                character.inSight = false;
                character.path = new Stack<Graph.Node>();
                character.target = new PVector(character.position.x + 1, character.position.y + 1);
                character.goal = new PVector(character.position.x + 1, character.position.y + 1);
                character.alive = true;
                path_following.crumbs = new ArrayList<PShape>();
                return true;
            }
            return false;
        }
        private boolean conditionCheck(Character character) {
                    if (bool == BehaviorType.BOOLEAN) {
                        if (field.equals("isalive")) {
                            //TODO alter to be more applicable to the monster
                            return character.alive;
                        } else if (field.equals("isdead")) {
        //                    System.out.println(!character.alive);
                            return !character.alive;
                        } else if (field.equals("insight")) {
                            return character.inSight;
                        } else if (field.equals("outsight")) {
                            return !character.inSight;
                        } else if (field.equals("pathempty")) {
                            return character.path.isEmpty();
                        }
                    } else if (bool == BehaviorType.PARAMETER) {
                        boolean check = false;
                        if (quantifier == BehaviorType.GREATER) {
                            check = getGreater(character);
                        } else if (quantifier == BehaviorType.EQUAL) {
                            check = getEqual(character);
                        } else if (quantifier == BehaviorType.LESS) {
                            check = getLess(character);
                        }
                        return check;
                    }
                    return false;
                }
        private void recordBeh() {
                    lastAction = field;
                    if (!field.equals("followpath"))
                        emptyPath = true;
                    else
                        emptyPath = false;
        //            path_following.stateOutput.append("Monster Behavior: ");
        //            path_following.stateOutput.append(field);
        //            path_following.stateOutput.append("\n");
        //            path_following.stateOutput.append("Last Monster Action: ");
        //            path_following.stateOutput.append(lastAction);
        //            path_following.stateOutput.append("\n");
                    
                }
        private boolean dance(Character character) {
            if (danceDone) {
                danceDone = false;
                character.alive = true;  
                path_following.character.run = true;
                character.run = true;
                return false;
            }
            Graph graph = new Graph();
            int toAdd = 200;
            character.path = new Stack<Graph.Node>();
            Graph.Node loc1 = graph.new Node("", 0, 0, character.position.x - toAdd/2, character.position.y - toAdd/2, 0);
            Graph.Node loc2 = graph.new Node("", 0, 0, character.position.x + toAdd/2, character.position.y - toAdd/2, 0);
            Graph.Node loc3 = graph.new Node("", 0, 0, character.position.x - toAdd/2, character.position.y + toAdd/2, 0);
            Graph.Node loc4 = graph.new Node("", 0, 0, character.position.x - toAdd/2, character.position.y + toAdd/2, 0);
            Graph.Node loc5 = graph.new Node("", 0, 0, character.position.x + toAdd/2, character.position.y + toAdd/2, 0);
            Graph.Node loc6 = graph.new Node("", 0, 0, character.position.x, character.position.y, 0);
            character.path.push(loc6);
            character.path.push(loc5);
            character.path.push(loc4);
            character.path.push(loc3);
            character.path.push(loc2);
            character.target = new PVector((float) loc1.x, (float)  loc1.y);
            character.goal = new PVector((float) loc6.x, (float) loc6.y);
            danceDone = true;
            
            
            
            return true;
        }
        private boolean reset(Character character) {
//            monster.position = new PVector(FRAME_WIDTH/2, FRAME_HEIGHT/2);
            character.angRot = 0;
            character.angle = 0;
            character.inSight = false; 
            character.goal = new PVector((float) character.graph.nodes.get(character.start).x , (float) character.graph.nodes.get(character.start).y);
            Stack<Graph.Node> storage = new Stack<Graph.Node>();
            int num = character.path.size();
            for (int i = 0; i < num; i++) {
                storage.push(character.path.pop());
            }
            character.path.push(character.graph.nodes.get(character.start));
            for (int i = 0; i < num; i++) {
                character.path.push(storage.pop());
            }
            return true;
        }
//        private boolean wander(Character parameter) {
//            float dist = path_following.TILE_SIZE * 2;
//            float pointsx[] = new float[] {parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x};
//            float pointsy[] = new float[] {parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y};
//            pointsx[0] += dist;
//            pointsx[1] -= dist;
//            pointsy[2] += dist;
//            pointsy[3] -= dist;
//            pointsx[4] += dist;
//            pointsy[4] += dist;
//            pointsx[5] -= dist;
//            pointsy[5] -= dist;
//            pointsx[6] += dist;
//            pointsy[6] -= dist;
//            pointsx[7] -= dist;
//            pointsy[7] += dist;
//            
//            for (int j = 0; j < pointsx.length; j++) {
//                if (pointsx[j] <= 0) pointsx[j] = parameter.position.x;
//                if (pointsx[j] >= path_following.FRAME_WIDTH) pointsx[j] = parameter.position.x;
//                if (pointsy[j] <= 0) pointsy[j] = parameter.position.y;
//                if (pointsy[j] >= path_following.FRAME_HEIGHT) pointsy[j] = parameter.position.y;
//            }
//            Random selector = new Random();
//            int selected = selector.nextInt(pointsx.length);
//            parameter.graph.setTarget(AlgorithmSet.vertexClosest(pointsx[selected], pointsy[selected], path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
//            parameter.goal = new PVector(pointsx[selected], pointsy[selected]);
//            parameter.graph.setStart(AlgorithmSet.vertexClosest(parameter.position.x, parameter.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
//            parameter.path = AStarAlg.aStar(parameter.graph);
//        
//            return true;
//        }
//        private boolean changeRoom(Character character, int num) {
//                    int thresholdx1 = 0;
//                    int thresholdx2 = 0;
//                    int thresholdy1 = 0;
//                    int thresholdy2 = 0;
//                    Random random = new Random();
//                    if (num == 1) {
//                        thresholdx2 = path_following.FRAME_WIDTH/2;
//                        thresholdy2 = 750;
//                    } else if (num == 2) {
//                        thresholdx1 = path_following.FRAME_WIDTH/2;
//                        thresholdx2 = path_following.FRAME_WIDTH;
//                        thresholdy2 = 750;
//                    } else if (num == 3) {
//                        thresholdx2 = path_following.FRAME_WIDTH;
//                        thresholdy1 = 750;
//                        thresholdy2 = path_following.FRAME_HEIGHT;
//                    }
//                    int newX = random.nextInt(thresholdx2 - thresholdx1);
//                    int newY = random.nextInt(thresholdy2 - thresholdy1);
//                    newX += thresholdx1;
//                    newY += thresholdy1;
//        //            int newX = thresholdx2 - thresholdx1;
//        //            newX /= 2;
//        //            newX += thresholdx1;
//        //            int newY = thresholdy2 - thresholdy1;
//        //            newY /= 2;
//        //            newY += thresholdy1;
//        
//                    character.graph.setTarget(AlgorithmSet.vertexClosest(newX, newY, path_following.TILE_SIZE, path_following.FRAME_WIDTH, character.graph));
//                    character.goal = new PVector(newX, newY);
//                    character.graph.setStart(AlgorithmSet.vertexClosest(character.position.x, character.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, character.graph));
//                    character.path = AStarAlg.aStar(character.graph);
//                    return true;
//                }
//        @SuppressWarnings("unused")
//        private boolean raycast(Character character) {
//            PVector cast = AlgorithmSet.rayCast(character.velocity, (int) (character.velocity.mag() * 3), 
//                    path_following.FRAME_WIDTH, path_following.FRAME_HEIGHT, path_following.rooms, character);
//            return cast == null;        
//        }
//
//        private boolean search(Character parameter) {
////            System.out.println("search");
//                PVector search = new PVector();
//                if (searchLocations.isEmpty()) {
////                    System.out.println("searchisempty");
//                    float dist = path_following.TILE_SIZE * 2;
//                    float pointsx[] = new float[] {parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x, parameter.position.x};
//                    float pointsy[] = new float[] {parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y, parameter.position.y};
//                    pointsx[0] += dist;
//                    pointsx[1] -= dist;
//                    pointsy[2] += dist;
//                    pointsy[3] -= dist;
//                    pointsx[4] += dist;
//                    pointsy[4] += dist;
//                    pointsx[5] -= dist;
//                    pointsy[5] -= dist;
//                    pointsx[6] += dist;
//                    pointsy[6] -= dist;
//                    pointsx[7] -= dist;
//                    pointsy[7] += dist;
//                    if (step >= pointsx.length) step = 0;
//                    for (int j = step; j < pointsx.length; j++) {
//                        if (pointsx[j] < 0) pointsx[j] = parameter.position.x;
//                        if (pointsx[j] > path_following.FRAME_WIDTH) pointsx[j] = parameter.position.x;
//                        if (pointsy[j] < 0) pointsy[j] = parameter.position.y;
//                        if (pointsy[j] > path_following.FRAME_HEIGHT) pointsy[j] = parameter.position.y;
//                        if (pointsx[j] != parameter.position.x && pointsy[j] != parameter.position.y)
//                            searchLocations.push(new PVector(pointsx[j], pointsy[j]));
//                    }
//                    for (int j = 0; j < step; j++) {
//                        if (pointsx[j] < 0) pointsx[j] = parameter.position.x;
//                        if (pointsx[j] > path_following.FRAME_WIDTH) pointsx[j] = parameter.position.x;
//                        if (pointsy[j] < 0) pointsy[j] = parameter.position.y;
//                        if (pointsy[j] > path_following.FRAME_HEIGHT) pointsy[j] = parameter.position.y;
//                        if (pointsx[j] != parameter.position.x && pointsy[j] != parameter.position.y)
//                            searchLocations.push(new PVector(pointsx[j], pointsy[j]));
//                    }
//                }
//                search = searchLocations.pop();
//                parameter.graph.setTarget(AlgorithmSet.vertexClosest(search.x, search.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
//                parameter.goal = new PVector((float) parameter.graph.getTarget().x, (float) parameter.graph.getTarget().y);
//                parameter.graph.setStart(AlgorithmSet.vertexClosest(parameter.position.x, parameter.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, parameter.graph));
//                parameter.path = AStarAlg.aStar(parameter.graph);
////                System.out.println(parameter.path.size());
//
//                return true;
//            }
//
//        @SuppressWarnings("unused")
//        private boolean charge(Character character) {
////            character.velocity.mult(2);
//            return false;
//        }
//
//        private boolean followPath(Character character) {
//            if (!character.path.isEmpty() && character.position.dist(character.target) < path_following.RADIUS_DECEL) {
//                Graph.Node nextTarget = character.path.pop();
//                character.target.x = (float) nextTarget.x;
//                character.target.y = (float) nextTarget.y;
//            } else if (character.path.isEmpty() && character.position.dist(character.target) <= path_following.RADIUS_SAT) {
//                return false;
//            }
//            AlgorithmSet.arriveSimple(new PVector(), character.target, character, path_following.MAX_ACCELERATION, path_following.MAX_VELOCITY, path_following.RADIUS_DECEL, path_following.RADIUS_SAT, path_following.EFFECT_RANGE);
//            AlgorithmSet.SteeringAdjust(character,  path_following.RADIUS_SAT_ROT, path_following.RADIUS_DECEL_ROT, path_following.EFFECT_RANGE_ROT, path_following.MAX_ROT);
//            AlgorithmSet.kinematicAdjust(character);
//
//            return true;
//        }
//
//        private boolean chase(Character character) {
//            searchLocations.removeAllElements();
//            Character target = path_following.character;
//            character.goal = new PVector(target.position.x + target.velocity.x, target.position.y + target.velocity.y);
//            character.graph.setTarget(AlgorithmSet.vertexClosest(character.goal.x, character.goal.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, character.graph));
//            character.graph.setStart(AlgorithmSet.vertexClosest(character.position.x, character.position.y, path_following.TILE_SIZE, path_following.FRAME_WIDTH, character.graph));
//            character.path = AStarAlg.aStar(character.graph);
//
//            return true;
//        }

        private boolean getLess(Character character) {
            if (field.equalsIgnoreCase("orientation")) {
                return (character.orientation.heading() < parameter);
            } else if (field.equalsIgnoreCase("velocityx")) {
                return (character.velocity.x < parameter);
            } else if (field.equalsIgnoreCase("positionx")) {
                return (character.position.x < parameter);
            } else if (field.equalsIgnoreCase("accelerationx")) {
                return (character.acceleration.x < parameter);
            } else if (field.equalsIgnoreCase("velocityy")) {
                return (character.velocity.y < parameter);
            } else if (field.equalsIgnoreCase("positiony")) {
                return (character.position.y < parameter);
            } else if (field.equalsIgnoreCase("accelerationy")) {
                return (character.acceleration.y < parameter);
            }
            return false;
        }
        private boolean getEqual(Character character) {
            if (field.equalsIgnoreCase("orientation")) {
                return (character.orientation.heading() == parameter);
            } else if (field.equalsIgnoreCase("velocityx")) {
                return (character.velocity.x == parameter);
            } else if (field.equalsIgnoreCase("positionx")) {
                return (character.position.x == parameter);
            } else if (field.equalsIgnoreCase("accelerationx")) {
                return (character.acceleration.x == parameter);
            } else if (field.equalsIgnoreCase("velocityy")) {
                return (character.velocity.y == parameter);
            } else if (field.equalsIgnoreCase("positiony")) {
                return (character.position.y == parameter);
            } else if (field.equalsIgnoreCase("accelerationy")) {
                return (character.acceleration.y == parameter);
            }
            return false;
        }
        private boolean getGreater(Character character) {
            if (field.equalsIgnoreCase("orientation")) {
                return (character.orientation.heading() > parameter);
            } else if (field.equalsIgnoreCase("velocityx")) {
                return (character.velocity.x > parameter);
            } else if (field.equalsIgnoreCase("positionx")) {
                return (character.position.x > parameter);
            } else if (field.equalsIgnoreCase("accelerationx")) {
                return (character.acceleration.x > parameter);
            } else if (field.equalsIgnoreCase("velocityy")) {
                return (character.velocity.y > parameter);
            } else if (field.equalsIgnoreCase("positiony")) {
                return (character.position.y > parameter);
            } else if (field.equalsIgnoreCase("accelerationy")) {
                return (character.acceleration.y > parameter);
            }
            return false;
        }
        
    }
}
