package trees;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import enumerations.BehaviorType;
import executors.path_following;
import objects.Character;
import objects.Graph;
import processing.core.PShape;
import processing.core.PVector;


/**
 * generates actions and behavior for a character
 * based on structure of nodes
 * @author Sam
 *
 */
public class BehaviorTree {
    public String lastAction = "";
    public BehaviorNode root;
    public PVector nextLoc;
    public Stack<PVector> dance;
    public boolean danceDone;
    public boolean emptyPath;

    /**
     * 
     */
    public BehaviorTree() {
        root = new BehaviorNode(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "isalive", null, 0);
        nextLoc = new PVector();
    }
    public boolean getBehavior(Character character) {
        return root.executeBehavior(character);
    }
    
    /**
     * autocreates desired monster behavior for path_following
     * @param character is actually the monster
     * @return behavior tree for use
     */
    public static BehaviorTree autogenerate(Character character) {
        //containing tree
        BehaviorTree tree = new BehaviorTree();
        // generate leafs
        BehaviorNode isdead = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "isdead", null, 0);
        BehaviorNode followpath = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "followpath", null, 0);
        BehaviorNode dance = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "dance", null, 0);
        BehaviorNode insight = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.CONDITION, BehaviorType.BOOLEAN, "insight", null, 0);
        BehaviorNode chase = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "chase", null, 0);
        BehaviorNode wander = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "wander", null, 0);
        BehaviorNode search = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "search", null, 0);
        BehaviorNode roomChange1 = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms1", null, 0);
        BehaviorNode roomChange2 = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms2", null, 0);
        BehaviorNode roomChange3 = tree.new BehaviorNode(BehaviorType.LEAF, BehaviorType.ACTION, BehaviorType.ACTION, "changerooms3", null, 0);
        
        // generate composites
        BehaviorNode danceSeq = tree.new BehaviorNode(BehaviorType.SEQUENCE, "danceSeq");
        BehaviorNode chaseSeq = tree.new BehaviorNode(BehaviorType.SEQUENCE, "chaseSeq");
        BehaviorNode randSearch = tree.new BehaviorNode(BehaviorType.RANDOM_SELECT, "randomSelect");
        BehaviorNode randWander = tree.new BehaviorNode(BehaviorType.RANDOM_SELECT, "randomSelectWander");
        BehaviorNode randRoom = tree.new BehaviorNode(BehaviorType.RANDOM_SELECT, "randomRoom");
        BehaviorNode selectorRoot = tree.new BehaviorNode(BehaviorType.SELECTOR, "selectRoot");
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
        danceSeq.children.add(isdead);
        danceSeq.children.add(dance);
        
        //root and children assigned
        tree.root = selectorRoot;
        selectorRoot.children.add(followpath);
        selectorRoot.children.add(danceSeq);
        selectorRoot.children.add(chaseSeq);
        selectorRoot.children.add(randSearch);
        
        return tree;
        
    }

    /**
     * contains behavior types and traversal methods
     * @author Sam
     *
     */
    private class BehaviorNode {
        public BehaviorType type;
        public BehaviorType actiontype;
        public BehaviorType bool;
        public String field;
        public ArrayList<BehaviorNode> children;
        public BehaviorType quantifier;
        public double parameter;
        
        /**
         * BOOLEAN BehaviorNode constructor
         * @param type Check composite type. this will typically be a leaf for this constructor
         * @param actiontype Condition, update, or bool
         * @param bool for boolean only. field or parameter based
         * @param field what field is this describing?
         * @param quantifier greater, less, or equal sign discrete variables
         * @param parameter param to compare field to
         */
        public BehaviorNode(BehaviorType type, BehaviorType actiontype, BehaviorType action, String field,
                BehaviorType quantifier, double parameter) {
            super();
            this.type = type;
            this.actiontype = actiontype;
            this.bool = action;
            this.field = field;
            this.quantifier = quantifier;
            this.parameter = parameter;
            this.children = new ArrayList<BehaviorNode>();
        }
        /**
         * composite BehaviorNode constructor also works for ACTION nodes
         * @param type Check composite type. this will typically be a leaf for this constructor
         */
        public BehaviorNode(BehaviorType type, String name) {
            super();
            this.type = type;
            this.actiontype = null;
            this.bool = null;
            this.field = name;
            this.quantifier = null;
            this.parameter = 0;
            this.children = new ArrayList<BehaviorNode>();
        }
        
        /**
         * proceeds through node children based on enumeration for node type
         * @param character
         * @return true if children met behavior, false if otherwise
         */
        public boolean executeBehavior(Character character) {
            if (type == BehaviorType.SELECTOR) {
                // returns true if a child returns true
                for (BehaviorNode e : children) {
                    if (e.executeBehavior(character)) {
                        return true;
                    }
                }
                return false;
            } else if (type == BehaviorType.SEQUENCE) {
                // returns true only if all children return true
                for (BehaviorNode e : children) {
                    if (!e.executeBehavior(character)) {
                        return false;
                    }
                }
                return true;
            } else if (type == BehaviorType.RANDOM_SELECT) {
                // selects a random point in children to start at, then
                // executes from given child to end of array, then picks
                // up from zero to reach the rest of the children.
                // behaves like selector node
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
                // selects a random point in children to start at, then
                // executes from given child to end of array, then picks
                // up from zero to reach the rest of the children.
                // behaves like sequence node
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
                // meant to perform as a wrapper TODO IMPLEMENT MODIFICATION BEHAVIOR
                // only has one child at any time
                return children.get(0).executeBehavior(character);
            } else if (type == BehaviorType.LEAF) {
                //leave should just execute action, condition check, or update
                return resolveLeaf(character);
            } else {
                throw new IllegalArgumentException("Improper Composite Type");
            }
        }
        /**
         * decide path based on enumeration
         * @param character
         * @return
         */
        private boolean resolveLeaf(Character character) {
            if (actiontype == BehaviorType.CONDITION) {
                return conditionCheck(character);
            } else if (actiontype == BehaviorType.UPDATE) {
                return updateState(character);
            } else if (actiontype == BehaviorType.ACTION) {
                return performAction(character);
            }
            throw new IllegalArgumentException("Improper enumeration for leaf");
        }

        private boolean performAction(Character character) {
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
              return path_following.dance(character);
              
          }
            return false;
        }

        /**
         * updates a state within the character based on the nodes field
         * @param character
         * @return boolean based on success
         */
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
        /**
         * checks a condition based on fields in the character
         * @param character
         * @return boolean evauation of a field
         */
        private boolean conditionCheck(Character character) {
                    if (bool == BehaviorType.BOOLEAN) {
                        if (field.equals("isalive")) {
                            return character.alive;
                        } else if (field.equals("isdead")) {
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
        /**
         * store behavior in tree for easy recording external to logical progression
         * of the PApplet
         */
        private void recordBeh() {
                    lastAction = field;
                    if (!field.equals("followpath"))
                        emptyPath = true;
                    else
                        emptyPath = false;
                    
        }

        /**
         * returns a < comparison for a given field
         * @param character to pull fields from
         * @return boolean based on lesser evaluation
         */
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
        /**
         * returns a == comparison for a given field
         * @param character to pull fields from
         * @return boolean based on equality evaluation
         */
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
        /**
         * returns a > comparison for a given field
         * @param character to pull fields from
         * @return boolean based on greater evaluation
         */
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
