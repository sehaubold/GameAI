package trees;

import java.util.Random;

import enumerations.Algorithm;
import enumerations.DecisionType;
import objects.Character;

/**
 * decision tree to govern character behavior
 * @author Sam
 *
 */
public class DecisionTree {
    public DecNode root;

    /**
     * creates basic root node and inits tree
     */
    public DecisionTree() {
        root = new DecNode(DecisionType.BOOLEAN, DecisionType.GREATER, "", 0);
    }
    /**
     * traverses tree for action value
     * @param character
     * @return
     */
    public Algorithm getAction(Character character) {
        return root.getAction(character);
    }
    
    /**
     * autogenerates tree for path_following character
     * @return
     */
    public static DecisionTree autogenerate() {
        //generate leafs
        DecisionTree tree = new DecisionTree();
        DecNode wander = tree.new DecNode(DecisionType.LEAF, Algorithm.WANDER);
        DecNode change1 = tree.new DecNode(DecisionType.LEAF, Algorithm.CHANGE1);
        DecNode change2 = tree.new DecNode(DecisionType.LEAF, Algorithm.CHANGE2);
        DecNode change3 = tree.new DecNode(DecisionType.LEAF, Algorithm.CHANGE3);
        DecNode hide = tree.new DecNode(DecisionType.LEAF, Algorithm.HIDE);
        DecNode run = tree.new DecNode(DecisionType.LEAF, Algorithm.RUN);
        DecNode pathfind = tree.new DecNode(DecisionType.LEAF, Algorithm.FINDPATH);
//        DecNode arrive = tree.new DecNode(DecisionType.LEAF, Algorithm.ARRIVE);
        DecNode dead = tree.new DecNode(DecisionType.LEAF, Algorithm.DEAD);
        
        
        //branching nodes
        DecNode flee = tree.new DecNode(DecisionType.BRANCHINGLEAF, 2);
        flee.children[0] = hide;
        flee.children[1] = run;
        DecNode changeRooms = tree.new DecNode(DecisionType.BRANCHINGLEAF, 3);
        changeRooms.children[0] = change1;
        changeRooms.children[1] = change2;
        changeRooms.children[2] = change3;
        DecNode free = tree.new DecNode(DecisionType.BRANCHINGLEAF, 2);
        free.children[0] = wander;
        free.children[1] = changeRooms;
        
        //start construction of first tier
        DecNode sighted = tree.new DecNode(DecisionType.BOOLEAN, null, "insight", 0);
        sighted.children[0] = free;
        sighted.children[1] = flee;
        DecNode pathCheck = tree.new DecNode(DecisionType.BOOLEAN, null, "pathempty", 0);
        pathCheck.children[0] = pathfind;
        pathCheck.children[1] = sighted;
        DecNode root = tree.new DecNode(DecisionType.BOOLEAN, null, "alive", 0);
        root.children[0] = dead;
        root.children[1] = pathCheck;
        tree.root = root;
        return tree;
        
    }

    /**
     * contains methods that govern traversal
     * @author Sam
     *
     */
    private class DecNode {
        public DecisionType type;
        public DecisionType quantifier;
        public String field;
        public double parameter;
        public DecNode[] children;
        public Algorithm leafAction;
        /**
         * parent node constructor
         * @param type
         * @param quantifier
         * @param character
         * @param field
         * @param parameter
         */
        public DecNode(DecisionType type, DecisionType quantifier, String field, double parameter) {
            super();
            this.type = type;
            this.quantifier = quantifier;
            this.field = field;
            this.parameter = parameter;
            this.children = new DecNode[2];
        }
        /**
         * action node constructor
         * @param type
         * @param action
         */
        public DecNode(DecisionType type, Algorithm action) {
            super();
            this.type = type;
            this.leafAction = action;
        }

        /**
         * constructor for a non binary node
         * @param type
         * @param num
         */
        public DecNode(DecisionType type, int num) {
            super();
            this.type = type;
            this.children = new DecNode[num];
        }
        /**
         * traverses children to get action value
         * @param character
         * @return Algorithm Type
         */
        public Algorithm getAction(Character character) {
            if (type == DecisionType.BOOLEAN) {
                boolean bool = checkBool(character);
                if (bool) {
                    return children[1].getAction(character);
                } else {
                    return children[0].getAction(character);
                }
            } else if (type == DecisionType.PARAMETER) {
                boolean check = false;
                if (quantifier == DecisionType.GREATER) {
                    check = getGreater(character);
                } else if (quantifier == DecisionType.EQUAL) {
                    check = getEqual(character);
                } else if (quantifier == DecisionType.LESS) {
                    check = getLess(character);
                }

                if (check) {
                    return children[1].getAction(character);
                } else {
                    return children[0].getAction(character);
                }
            } else if (type == DecisionType.BRANCHINGLEAF) {
                Random random = new Random();
                return children[random.nextInt(children.length)].getAction(character);
            } else {
                return leafAction;
            }
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
        /**
         * returns boolean value based on field
         * @param character
         * @return
         */
        private boolean checkBool(Character character) {
           if (field.equalsIgnoreCase("insight")) {
               return character.inSight;
           }
           if (field.equalsIgnoreCase("alive")) {
               return character.alive;
           }
           if (field.equalsIgnoreCase("pathempty")) {
               return character.path.isEmpty();
           }
           throw new IllegalArgumentException("Improper Boolean check call in Decision Tree");
        }
            
        
        
    }
}
