package algorithms;
///**
// * 
// */
//package necessarytxt;
//
//import java.util.ArrayList;
//import java.util.Random;
//import java.util.Scanner;
//import java.util.Stack;
//
//import executors.Graph.Edge;
//import executors.Graph.Node;
//
///**
// * @author Sam
// *
// */
//public class DjikstraAlg {
//
//    private static boolean recordData;
//    private static boolean printDetails;
//    private static boolean findTarget;
//    private static String id = "";
//    private static String id2 = "";
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        boolean largGraph = false;
//        boolean autoSelect = false;
//        System.out.println("Use Large Graph?");
//        Scanner in = new Scanner(System.in);
//        String input = in.nextLine();
//        if (input.contains("Y") || input.contains("y")) {
//            largGraph = true;
//        } else {
//            largGraph = false;
//        }
//        System.out.println("Find Target?");
//        input = in.nextLine();
//        if (input.contains("y") || input.contains("Y")) {
//            findTarget = true;
//            System.out.println("Auto Select Target?");
//            input = in.nextLine();
//            if (input.contains("y") || input.contains("Y")) {
//                autoSelect = true;
//            }  else {
//                autoSelect = false;
//                System.out.println("Select Start:");
//                id2 = in.nextLine();
//                System.out.println("Select Target:");
//                id = in.nextLine();
//            }
//        }  else {
//            findTarget = false;
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
//            worker = GraphReader.GraphRead("src\\executors\\graphTwoEdges.txt", "src\\executors\\graphTwoNodes.txt");
//        } else {
//            worker = GraphReader.GraphRead("src\\executors\\graph1.txt", "src\\executors\\NodeNames.txt");
//        }
//        if (autoSelect) {
//            Random targetSelect = new Random();
//            worker.setTarget(targetSelect.nextInt(worker.nodes.size()));
//            worker.setStart(targetSelect.nextInt(worker.nodes.size()));
//        } else if (findTarget) {
//            worker.setTarget(worker.getNode(id).index);
//            worker.setStart(worker.getNode(id2).index);
//        }
//        if (printDetails) {
//            worker.printDistances = true;
//            worker.printPath = true;
//        } else if (findTarget){
//            worker.printDistances = false;
//            worker.printPath = false;
//        } else {
//            worker.printDistances = false;
//            worker.printPath = false;
//            worker.findTarget = false;
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
//        djikstras(worker);
//        String store = "Free Memory: " + Runtime.getRuntime().freeMemory();
//        long endTime = System.nanoTime();
//        long runtime = endTime - startTime;
//        store += "\nRun time: " + runtime;
//        
//        if (recordData) {
//            String details = "Distance: " + worker.nodes.get(worker.getTarget().index).fweight + "\n";
//            for (Node eNode : worker.nodes) {
//                details += eNode.identifier + ": " + eNode.g_val + "\n";
//            }
//            if (findTarget) {
//                details += "Path: " + worker.getTarget().printPath() + "\n";
//            }
//            details += store;
//            GraphReader.GraphWrite("src\\executors\\output.txt", out);
//            GraphReader.DataWrite("src\\executors\\DijkstraOutput.txt", details);
//        }
//        if (printDetails) {
//            System.out.println(store);
//        }
//    }
//
//
//
//    /**
//     * @param worker
//     * @return
//     */
//    public static Stack<Node> djikstras(Graph worker) {
//        worker.resetParents();
//        if (worker.resetHueristic()) {
//            ArrayList<Node> nodes = worker.nodes;
//            int tarIndex = worker.getTarget().index;
//            int starIndex = worker.getStart().index;
//            boolean closedSet[] = new boolean[nodes.size()];
//            
//            for (int i = 0; i < closedSet.length; i++) {
//                nodes.get(i).g_val = Double.MAX_VALUE;
//                closedSet[i] = false;
//            }
//            nodes.get(starIndex).g_val = 0;
//
//            ArrayList<Integer> openSet = new ArrayList<Integer>();
//            openSet.add(starIndex);
//            
//            while (!openSet.isEmpty()) {
//                if (openSet.size() <= 0)
//                    break;
//                int work = minDistance(openSet, nodes);
//                
//                closedSet[work] = true;
//                addChildren(work, nodes, openSet, closedSet);
//                
//                for (int j = 0; j < nodes.size(); j++) {
//                    if (containsEdge(work, j, nodes) && lessPath(work, j, nodes)) {
//                        Node check = nodes.get(j);
//                        Node update = nodes.get(work);
//                        double weight = getEdgeWeight(nodes, work, j);
//                        check.g_val = update.g_val + weight;
//                        check.pathParent = nodes.get(work);
//                    }
//                }
//                if (worker.findTarget  && closedSet[tarIndex])
//                    break;
//            }
//            if (worker.printDistances) {
//                System.out.println("Start: " + nodes.get(starIndex).identifier);
//                System.out.println("Distance: ");
//                for (Node eNode : nodes) {
//                    System.out.println(eNode.identifier + ": " + eNode.g_val);
//                }
//            }
//            if (worker.findTarget) {
//                if (worker.printPath) {
//                    System.out.println("Start: " + nodes.get(starIndex).identifier + " Target: " + nodes.get(tarIndex).identifier);
//                    System.out.println("Distance: " + nodes.get(tarIndex).g_val);
//                    System.out.println("Path: " + nodes.get(tarIndex).printPath());
//                }
//                Stack<Node> path = new Stack<Node>(); 
//                Node executors = nodes.get(tarIndex);
//                while (executors.pathParent != null) {
//                    path.push(executors);
//                    executors = executors.pathParent;
//                }
////                worker.resetParents();
//                return path;
//            } else
//                return null;
//        } else {
//            throw new IllegalArgumentException("\nNull target or start node\n");
//        }
//    }
//    /**
//     * @param work
//     * @param nodes
//     * @param openSet
//     * @param closedSet 
//     */
//    private static void addChildren(int work, ArrayList<Node> nodes, ArrayList<Integer> openSet, boolean[] closedSet) {
//        Node worker = nodes.get(work);
//        ArrayList<Edge> edges = worker.outEdges;
//        for (int i = 0; i < edges .size(); i++) {
//            if (!closedSet[edges.get(i).child.index])
//                openSet.add(edges.get(i).child.index);
//        }
//        
//    }
//
//
//    /**
//     * @param work
//     * @param j
//     * @param graph
//     * @return
//     */
//    private static boolean lessPath(int work, int j, ArrayList<Node> graph) {
//        Node check = graph.get(j);
//        Node update = graph.get(work);
//        double weight = getEdgeWeight(graph, work, j);
//        if (update.g_val + weight < check.g_val) {
//            return true;
//        }
//        return false;
//    }
//
//
//    /**
//     * @param work
//     * @param j
//     * @param graph
//     * @return
//     */
//    private static boolean containsEdge(int work, int j, ArrayList<Node> graph) {
//        ArrayList<Edge> edges = graph.get(work).outEdges;
//        for (int i = 0; i < edges.size(); i++) {
//            if (edges.get(i).child.index == j) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//    /**
//     * @param graph
//     * @param pindex
//     * @param cindex
//     * @return
//     */
//    public static double getEdgeWeight(ArrayList<Node> nodes, int pindex, int cindex) {
//        if (pindex == cindex)
//            return 0;
//        Node parent = nodes.get(pindex);
//        Node child = nodes.get(cindex);
//        Edge edge = null;
//        for (int i = 0; i < parent.outEdges.size(); i++) {
//            edge = parent.getEdge(i);
////            System.out.println(child.identifier + " " + edge.child.identifier);
//            if (child.identifier.equals(edge.child.identifier)) {
//                break;
//            } else {
//                edge = null;
//            }
//        }
//        if (edge == null) {
//            return Double.MAX_VALUE;
//        }
//        return edge.weight;
//    }
//    /**
//     * @param dist
//     * @param sptSet
//     * @param graph
//     * @return
//     */
//    private static int minDistance(ArrayList<Integer> openSet, ArrayList<Node> graph) {
//        double least = Double.MAX_VALUE;
//        int index = -1;
//        for (int i = 0; i < openSet.size(); i++) {
//            if (graph.get(openSet.get(i)).g_val < least) {
//                index = openSet.get(i);
//                least = graph.get(openSet.get(i)).g_val;
//            }
//        }
//        openSet.remove(Integer.valueOf(index));
//        return index; 
//    }
//}
