/**
 * 
 */
package trees;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

import current.path_following;
import enumerations.Algorithm;
import enumerations.Attribute;

/**
 * @author Sam
 *
 */
public class LearningTree {
    private static final int ACTION_NUM = 8;
    private static final int IRRELEVANT = 3;
    public final String TREE_FILE = path_following.STATE_FILE;
    
    public DecisionNode root;
    
    
    /**
     * 
     */
    public LearningTree() {
        super();
        root = new DecisionNode();
        ArrayList<Attribute> attributeset = new ArrayList<Attribute>();
        attributeset.add(Attribute.ALIVE);
        attributeset.add(Attribute.INSIGHT);
        attributeset.add(Attribute.PATHEMPTY);
        ArrayList<DataSet> examples = parseStateFile();
        makeTree(examples, attributeset, root);
    }
    /**
     * 
     */
    public LearningTree(String designation) {
        super();
        root = new DecisionNode();
    }
    public Algorithm getAction(boolean live, boolean sight, boolean path) {
        DataSet state = new DataSet(live, sight, path, null);
        return root.traverse(state);
    }
    public ArrayList<DataSet> parseStateFile() {
        ArrayList<DataSet> sets = new ArrayList<DataSet>();
        Scanner fileIn;
        try {
            fileIn = new Scanner(new File(TREE_FILE));
            //discard irrelevant lines
            for (int i = 0; i < IRRELEVANT; i++) {
                fileIn.nextLine();
            }
            while (fileIn.hasNextLine()) {
                String dataSet = fileIn.nextLine();
                StringTokenizer linescan = new StringTokenizer(dataSet, " ");
                boolean live = Boolean.parseBoolean(linescan.nextToken().trim());
                boolean sight = Boolean.parseBoolean(linescan.nextToken().trim());
                boolean path = Boolean.parseBoolean(linescan.nextToken().trim());
                Algorithm action = parseAction(linescan.nextToken().trim()); 
                
                DataSet newSet = new DataSet(live, sight, path, action);
                sets.add(newSet);
            }
        } catch (IOException e) {
            System.out.println("Names could not be read");
            e.printStackTrace();
        }
        return sets;
    }
    private Algorithm parseAction(String actionString) {
        if (actionString.contains("followpath")) {
            return Algorithm.FINDPATH;
        } else if (actionString.contains("wander")) {
            return Algorithm.WANDER;
        } else if (actionString.contains("change")) {
            if (actionString.contains("1")) {
                return Algorithm.CHANGE1;
            } else if (actionString.contains("2")) {
                return Algorithm.CHANGE2;
            } else if (actionString.contains("3")) {
                return Algorithm.CHANGE3;
            }
        } else if (actionString.contains("search")) {
            return Algorithm.SEARCH;
        } else if (actionString.contains("chase")) {
            return Algorithm.CHASE;
        } else if (actionString.contains("reset")) {
            return Algorithm.DANCE;
        }
        throw new IllegalArgumentException("Improper action string: " + actionString);
    }
    /**
     * 
     */
    public boolean DataFile(String filename, String out) {
        try {
             PrintStream output = new PrintStream(new File(filename.trim()));
             output.println(out);
             output.close();
        } catch (IOException e) {
            System.out.println(filename + "could not be read");
            e.printStackTrace();
        }
        return true;
    }
        /**
         * 
         */
        public boolean DataWrite(String filename, String out) {
    //        parseState(out);
            BufferedWriter adder = null;
            try {
                adder = new BufferedWriter(new FileWriter(filename, true));
                adder.write(out);
                adder.newLine();
                adder.flush();
                adder.close();
    //            System.out.println(out);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
    
            return true;
        }
        
        
    public void makeTree(ArrayList<DataSet> examples, ArrayList<Attribute> attributes, DecisionNode decisionNode) {
        //calculate init entropy
        double initialEntropy = entropy(examples);
        //if zero entropy, no division needed. these are uniform actions, so attach child as such
        if (initialEntropy <= 0) {
            attachChild(examples, decisionNode);
            return;
        }
        
        //num of examples
        int exampleNum = examples.size();
        
        //hold best found split
        double bestInfoGain = 0;
        Attribute bestSplitAttribute = null;
        ArrayList<ArrayList<DataSet>> bestSets = null;
        
        //check each attribute
        for (Attribute attribute : attributes) {
            //perform split
            ArrayList<ArrayList<DataSet>> sets = splitByAttribute(examples, attribute);
            
            //find entropy and information gain
            double overallEntropy = entropyOfSets(sets, exampleNum);
//            System.out.println(overallEntropy);
            double infoGain = initialEntropy - overallEntropy;
            //update best information gain
            if (infoGain > bestInfoGain) {
                bestInfoGain = infoGain;
                bestSplitAttribute = attribute;
                bestSets = sets;
            }
        }
        //select best attribute
        decisionNode.testAttribute = bestSplitAttribute;
        if (bestSplitAttribute == null) {
            attatchActions(decisionNode, examples);
            return;
        }
        //remove used attribute from list of attributes to pass down tree
        ArrayList<Attribute> newAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : attributes) {
            newAttributes.add(attribute);
        }
        newAttributes.remove(bestSplitAttribute);
        
        //create and fill children
        for (ArrayList<DataSet> set : bestSets) {
            if (set.size() > 0) {
                //find attribute value in set
                int attributeValue = set.get(0).getValue(bestSplitAttribute);
                
                //create child
                DecisionNode child = new DecisionNode();
                
                //add to tree
                decisionNode.addChild(attributeValue, child);
                
                //recursive call
                makeTree(set, newAttributes, child);
            }
        }
    }
    private void attachChild(ArrayList<DataSet> examples, DecisionNode decisionNode) {
//        DecisionNode child = new DecisionNode();
//        System.out.println(decisionNode.testAttribute + " " + examples.get(0).action);
//        child.action = examples.get(0).action;
//        child.testAttribute = Attribute.ACTION;
//        decisionNode.children.add(child);
        decisionNode.testAttribute = Attribute.ACTION;
        decisionNode.action = examples.get(0).action;
    }
    private void attatchActions(DecisionNode decisionNode, ArrayList<DataSet> examples) {
//        printSet(examples);
        decisionNode.testAttribute = Attribute.SELECT;
        ArrayList<DecisionNode> newChildren = new ArrayList<DecisionNode>();
        for (int i = 0; i < ACTION_NUM; i++) {
            DecisionNode add = new DecisionNode();
            add.testAttribute = Attribute.ACTION;
            newChildren.add(add);
        }
        newChildren.get(0).action = Algorithm.FINDPATH;
        newChildren.get(1).action = Algorithm.WANDER;
        newChildren.get(2).action = Algorithm.SEARCH;
        newChildren.get(3).action = Algorithm.CHANGE1;
        newChildren.get(4).action = Algorithm.CHANGE2;
        newChildren.get(5).action = Algorithm.CHANGE3;
        newChildren.get(6).action = Algorithm.CHASE;
        newChildren.get(7).action = Algorithm.DANCE;
        decisionNode.children = newChildren;
        int totalActions = examples.size();
        decisionNode.probabilities = new double[ACTION_NUM];

        //tally up actions
        int[] actionTallies = new int[ACTION_NUM];
        
        //iterate over examples
        for (DataSet example : examples) {
            //increment proper tally
            actionTallies[getActionIndex(example.action)]++;
        }
        

        for (int i = 0; i < actionTallies.length; i++) {
            double proportion = ((double) actionTallies[i]) / ((double) totalActions);
            decisionNode.probabilities[i] = proportion; 
        }
        
    }
    /**
     * @param sets
     * @param exampleNum
     * @return
     */
    private double entropyOfSets(ArrayList<ArrayList<DataSet>> sets, int exampleNum) {
        
        //init entropy
        double entropy = 0;
        //get entropy contribution from each set
        for (ArrayList<DataSet> set : sets) {
            //calculate proportion of data
            double proportion = ((double) set.size()) / ((double) exampleNum);
            //calc entropy
            if (proportion > 0)
                entropy -= proportion * entropy(set);
        }
        return entropy;
    }
    /**
     * @param examples
     * @param attribute
     * @return
     */
    private ArrayList<ArrayList<DataSet>> splitByAttribute(ArrayList<DataSet> examples, Attribute attribute) {
        ArrayList<ArrayList<DataSet>> sets = new ArrayList<ArrayList<DataSet>>();
        sets.add(new ArrayList<DataSet>());
        sets.add(new ArrayList<DataSet>());
        //creating set of lists based on attribute value (true or false)
        for (DataSet example : examples) {
            sets.get(example.getValue(attribute)).add(example);
        }
        //return sets
        return sets;
    }
    /**
     * @param examples
     * @return
     */
    private double entropy(ArrayList<DataSet> examples) {
        //get examplenum
        int examplenum = examples.size();
        
        //if only one, entropy is zero
        if (examplenum == 0 || examplenum == 1) return 0;
        
        //otherwise tally up actions
        int[] actionTallies = new int[ACTION_NUM];
        
        //iterate over examples
        for (DataSet example : examples) {
            //increment proper tally
            actionTallies[getActionIndex(example.action)]++;
        }
        //find action number in set
        int actionCount = getActionNumber(actionTallies);
        
        //if actions equal zero or 1, there is zero entropy
        if (actionCount == 0 || actionCount == 1) return 0;
        
        //init entropy
        double entropy = 0;
        
        //add in contribution of each action
        for (double actionTally : actionTallies) {
            if (actionTally > 0) {
                double proportion = ((double) actionTally) / ((double) actionCount);
                entropy -= proportion * log2(proportion);
            }
        }
        return entropy;
    }
    /**
     * base conversion for log function
     * @param proportion
     * @return
     */
    private double log2(double proportion) {
        return Math.log(proportion) / Math.log(2);
    }
    private int getActionNumber(int[] actionTallies) {
        int total = 0;
        for (int i : actionTallies) {
            if (i > 0) total += i;
        }
        return total;
    }
    private int getActionIndex(Algorithm action) {
        int index = 0;
        if (action == Algorithm.FINDPATH) {
            index = 0;
        } else if (action == Algorithm.WANDER) {
            index = 1;
        } else if (action == Algorithm.SEARCH) {
            index = 2;
        } else if (action == Algorithm.CHANGE1) {
            index = 3;
        } else if (action == Algorithm.CHANGE2) {
            index = 4;
        } else if (action == Algorithm.CHANGE3) {
            index = 5;
        } else if (action == Algorithm.CHASE) {
            index = 6;
        } else if (action == Algorithm.DANCE) {
            index = 7;
        }
        
        
        return index;
    }
    /**
     * @author Sam
     *
     */
    public class DataSet {
        public boolean alive;
        public boolean inSight;
        public boolean pathEmpty;
        public Algorithm action;
        
        public DataSet(boolean live, boolean sight, boolean path, Algorithm action2) {
            this.alive = live;
            this.inSight = sight;
            this.pathEmpty = path;
            this.action = action2;
        }

        public int getValue(Attribute bestSplitAttribute) {
            if (bestSplitAttribute == Attribute.ALIVE) {
                if (alive) {
                    return 1;
                }
                return 0;
            }
            if (bestSplitAttribute == Attribute.INSIGHT) {
                if (inSight) {
                    return 1;
                }
                return 0;
            }
            if (bestSplitAttribute == Attribute.PATHEMPTY) {
                if (pathEmpty) {
                    return 1;
                }
                return 0;
            }
            return 0;
        }
    }
    /**
     * @author Sam
     *
     */
    public class DecisionNode {
        public Algorithm action;
        public ArrayList<DecisionNode> children;
        public Attribute testAttribute;
        public double[] probabilities;
        
        /**
         * 
         */
        public DecisionNode() {
            super();
            children = new ArrayList<DecisionNode>();
            testAttribute = null;
        }

        public void addChild(int attributeValue, DecisionNode child) {
            //TODO Check if data is getting messed up
            if (attributeValue >= children.size()) {
                while (attributeValue >= children.size()) {
                    children.add(child);
                }
            }
            children.set(attributeValue, child);
        }
        public Algorithm traverse(DataSet state) {
            if (testAttribute == Attribute.ACTION) {
                return action;
            } else if (testAttribute == Attribute.SELECT) {
                return children.get(selectIndex()).traverse(state);
            }
            if (children.size() == 1) {
                return children.get(0).traverse(state);
            }
            return children.get(getValue(testAttribute, state)).traverse(state);
        }
        private int selectIndex() {
            Random random = new Random();
            double nextRand = random.nextDouble();
            double threshold1 = 0;
            double threshold2 = probabilities[0];
            for (int i = 0; i < probabilities.length; i++) {
                if (nextRand > threshold1 && nextRand <= threshold2)
                    return i;
                threshold1 += probabilities[i];
                threshold2 += probabilities[i + 1];
            }
            return 1;
        }

        public int getValue(Attribute bestSplitAttribute, DataSet character) {
            if (bestSplitAttribute == Attribute.ALIVE && character.alive) {
                return 1;
            }
            if (bestSplitAttribute == Attribute.INSIGHT && character.inSight) {
                return 1;
            }
            if (bestSplitAttribute == Attribute.PATHEMPTY && character.pathEmpty) {
                return 1;
            }
            return 0;
        }
    }
}
