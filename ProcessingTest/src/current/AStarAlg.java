package current;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import current.Graph.Edge;
import current.Graph.Node;


/**
 * @author Sam
 *
 */
public class AStarAlg {



//    private static boolean printDetails;
//    private static boolean recordData;
//    private static boolean manhattan;
//
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        boolean largGraph = false;
//        boolean autoSelect = false;
//        String id = "";
//        String id2 = "";
//        System.out.println("Use Large Graph?");
//        Scanner in = new Scanner(System.in);
//        String input = in.nextLine();
//        if (input.contains("Y") || input.contains("y")) {
//            largGraph = true;
//        } else {
//            largGraph = false;
//        }
//        System.out.println("Manhattan, or Straight Line?");
//        input = in.nextLine();
//        if (input.contains("M") || input.contains("m")) {
//            manhattan = true;
//        } else {
//            manhattan = false;
//        }
//        System.out.println("Auto Select Start and Target?");
//        input = in.nextLine();
//        if (input.contains("y") || input.contains("Y")) {
//            autoSelect = true;
//        } else {
//            autoSelect = false;
//            System.out.println("Select Start:");
//            id2 = in.nextLine();
//            System.out.println("Select Target:");
//            id = in.nextLine();
//        }
//        System.out.println("Output data to console?");
//        input = in.nextLine();
//        if (input.contains("y") || input.contains("Y")) {
//            printDetails = true;
//        } else {
//            printDetails = false;
//        }
//        System.out.println("Record Data");
//        input = in.nextLine();
//        if (input.contains("y") || input.contains("Y")) {
//            recordData = true;
//        } else {
//            recordData = false;
//        }
//        in.close();
//        long startTime = System.nanoTime();
//        Graph worker;
//        if (largGraph) {
//            worker = GraphReader.GraphRead("src\\current\\graphTwoEdges.txt", "src\\current\\graphTwoNodes.txt");
//        } else {
//            worker = GraphReader.GraphRead("src\\current\\graph1.txt", "src\\current\\NodeNames.txt");
//        }
//        if (autoSelect) {
//            Random targetSelect = new Random();
//            worker.setTarget(targetSelect.nextInt(worker.nodes.size()));
//            worker.setStart(targetSelect.nextInt(worker.nodes.size()));
//        } else {
//            worker.setTarget(worker.getNode(id).index);
//            worker.setStart(worker.getNode(id2).index);
//        }
//        if (manhattan) {
//            worker.manDist = true;
//        }
//        if (printDetails) {
//            worker.printDistances = true;
//            worker.printPath = true;
//        } else {
//            worker.printDistances = false;
//            worker.printPath = false;
//        }
//        worker.COORDINATE_CONVERSION_FACTOR = 100.0;
//        String out = "";
//        if (worker != null) {
//            for (int i = 0; i < worker.nodes.size(); i++) {
//                out += worker.nodes.get(i).toString() + "\n";
//            }
//            out += "Start: " + worker.getStart().identifier + " Index:" + worker.getStart().index + "\n";
//            out += "Target: " + worker.getTarget().identifier + " Index:" + worker.getTarget().index + "\n";
//        }
//        aStar(worker);
//        String store = "Free Memory: " + Runtime.getRuntime().freeMemory();
//        long endTime = System.nanoTime();
//        long runtime = endTime - startTime;
//        store += "\nRun time: " + runtime;
//        
//        if (recordData) {
//            String details = "Distance: " + worker.nodes.get(worker.getTarget().index).fweight + "\n";
//            details += "Path: " + worker.getTarget().printPath() + "\n";
//            details += "Predicted: " + worker.getStart().h_val + " Actual: " + worker.getTarget().g_val + "\n";
//            details += store;
//            GraphReader.GraphWrite("src\\current\\output.txt", out);
//            GraphReader.DataWrite("src\\current\\AStarOutput.txt", details);
//        }
//        if (printDetails) {
//            System.out.println(store);
//        }
//    }


    /**
     * @param worker
     * @return
     */
    public static Stack<Node> aStar(Graph worker) {
        worker.resetParents();
        if (worker.resetHueristic()) {
            Node target = worker.getTarget();
            Node begin = worker.getStart();
            begin.g_val = 0;
            boolean found = false;

            PriorityQueue<Node> queue = new PriorityQueue<Node>( new Comparator<Node>(){
                 public int compare(Node i, Node j) {
                     if (i.fweight < j.fweight) {
                         return -1;
                     } else if(i.fweight > j.fweight) {
                         return 1;
                     } else {
                         return 0;
                     }
                 }
             });
            //originally was an arraylist, but the set is faster. Would also freeze up with arraylist.
            ArrayList<Node> explored = new ArrayList<Node>();
            queue.add(begin);
            while((!found) && (!queue.isEmpty())) {

                    Node current = queue.poll();
                    if (!explored.contains(current)) {
                    explored.add(current);
                    if (!current.identifier.equals(begin.identifier) && current.pathParent == null)
                        System.out.println("Null parent: " + current.identifier);
                    ArrayList<Node> adjNodes = generateAdj(current, queue, explored);
                    if(current.identifier.equals(target.identifier)){
                            found = true;
                            //connect the goal tile to the path for rewind
                            target.fweight = current.fweight;
                            target.pathParent = current.pathParent;
                    }
                    
                    for(Node e : adjNodes) {
                            Node nextNode = e;
                            double temp_g = current.g_val + traversal(current, e);
                            double temp_f = temp_g + nextNode.h_val;

                            if (!explored.contains(nextNode) && (!queue.contains(nextNode)) || (temp_f < nextNode.fweight)) {

                                    nextNode.pathParent = current;
                                    nextNode.g_val = temp_g;
                                    nextNode.fweight = temp_f;

                                    if (queue.contains(nextNode)) {
                                            queue.remove(nextNode);
                                    }

                                    queue.add(nextNode);

                            }

                    }
                    }
            }
            if (worker.printPath ) {
                System.out.println("Distance: " + worker.nodes.get(target.index).fweight);
                System.out.println("Path: " + target.printPath());
                System.out.println("Predicted: " + begin.h_val + " Actual: " + target.g_val);
            }
            Stack<Node> path = new Stack<Node>(); 
            Node current = target;
            while (current.pathParent != null) {
                path.push(current);
                current = current.pathParent;
            }
//            worker.resetParents();
            return path;
        } else {
            throw new IllegalArgumentException("\nNull target or start node\n");
        }
    }


    private static ArrayList<Node> generateAdj(Node current, PriorityQueue<Node> queue, ArrayList<Node> explored) {
        ArrayList<Node> ret = new ArrayList<Node>();
        for (Edge edge : current.outEdges) {
            if (!queue.contains(edge.child) || !explored.contains(edge.child))
                ret.add(edge.child);
        }
        return ret;
    }


    private static double traversal(Node e, Node e2) {
        for(Edge edge : e.outEdges)
            if (edge.parent.identifier.equals(e.identifier) && edge.child.identifier.equals(e2.identifier)) {
                return edge.weight;
            }
        throw new IllegalArgumentException("Non-null parent in node" + e.identifier);
    }

}
