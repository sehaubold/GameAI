package old;

import java.util.ArrayList;

import current.AStarAlg;
import current.AlgorithmSet;
import current.Character;
import current.Graph;
import current.path_following;
import current.Graph.Node;

public class MinMaxTree {
    public MinMax root;
    public Character character;
    public Character monster;
    private int characterNode;
    private int monsterNode;
    
    
    
    /**
     * @param monster 
     * @param root
     */
    public MinMaxTree(Character param, Character monster, int TILE_SIZE, int FRAME_WIDTH) {
        super();
        this.character = param;
        characterNode = AlgorithmSet.vertexClosest(character.position.x, character.position.y, TILE_SIZE, FRAME_WIDTH, character.graph);
        this.monster = monster;
        monsterNode = AlgorithmSet.vertexClosest(monster.position.x, monster.position.y, TILE_SIZE, FRAME_WIDTH, monster.graph);
        this.root = new MinMax(1, character.graph.nodes.get(characterNode), 0);
    }
    public Node maximize() {
        Node worker = root.evaluate().node;
//        System.out.println("Returned: "+ worker.x + " " + worker.y);
//        System.out.println("root: "+ root.node.x + " " + root.node.y);
//        System.out.println("Character: "+ character.position.x + " " + character.position.y);
        return worker;
    }


    private class MinMax {
        private int MoM; //min or max
        public Node node;
        public double value;
        public ArrayList<MinMax> children;
        /**
         * @param moM
         * @param node
         * @param tier 
         */
        public MinMax(int moM, Node node, int tier) {
            super();
            MoM = moM;
            this.node = node;
            children = new ArrayList<MinMax>();
            System.out.println("Level: " + tier);
            if (tier <= 3) {

                System.out.println("Node: "+ node.x + " " + node.y + " Level: " + tier);
                int tileSize = path_following.TILE_SIZE;
                int width = path_following.FRAME_WIDTH;
                int temp_right_x = (int) (node.x + tileSize);
                int temp_down_y = (int) (node.y + tileSize);
                int temp_left_x = (int) (node.x - tileSize);
                int temp_up_y = (int) (node.y - tileSize);
                Node right = character.graph.nodes.get(AlgorithmSet.vertexClosest(temp_right_x, node.y, tileSize, width, character.graph));
                Node left = character.graph.nodes.get(AlgorithmSet.vertexClosest(temp_left_x, node.y, tileSize, width, character.graph));
                Node down = character.graph.nodes.get(AlgorithmSet.vertexClosest(node.x, temp_down_y, tileSize, width, character.graph));
                Node up = character.graph.nodes.get(AlgorithmSet.vertexClosest(node.x, temp_up_y, tileSize, width, character.graph));
                //if found, add them to the list
                if (right != null)
                    children.add(new MinMax(-moM, right, tier + 1));
                if (left != null)
                    children.add(new MinMax(-moM, left, tier + 1));
                if (up != null)
                    children.add(new MinMax(-moM, up, tier + 1));
                if (down != null)
                    children.add(new MinMax(-moM, down, tier + 1));
                
            }
            if (children.isEmpty()) {
                character.graph.setStart(characterNode);
                character.graph.setTarget(node.index);
                AStarAlg.aStar(character.graph);
                this.value = character.graph.getTarget().fweight;

                character.graph.setStart(monsterNode);
                character.graph.setTarget(node.index);
                AStarAlg.aStar(character.graph);
                this.value += character.graph.getTarget().fweight;
            }

        }
        
        public MinMax evaluate() {
            if (children.isEmpty()) {
                System.out.println("emptyChildren " + node.x + " " + node.y);
                return this;
            }
            
            MinMax ret = null;
            double compare;
            if (MoM == 1) {
                compare = 0;
                for (MinMax minMax : children) {
                    MinMax test = minMax.evaluate();
                    if (test.value > compare) {
                        compare = test.value;
                        ret = test;
                    }
                }
            } else if (MoM == -1) {
                compare = Double.MAX_VALUE;

                for (MinMax minMax : children) {
                    MinMax test = minMax.evaluate();
                    if (test.value < compare) {
                        compare = test.value;
                        ret = test;
                    }
                }
            } else {
                throw new IllegalArgumentException("Incorrect Value");
            }
            
            
            return ret;
        }
        
        
        
    }
}
