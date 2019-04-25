///**
// * 
// */
//package old;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Random;
//import java.util.Scanner;
//
//import current.Graph.Edge;
//import current.Graph.Node;
//
//
///**
// * @author Sam
// *
// */
//public class GraphGen {
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        Scanner in = new Scanner(System.in);
//        System.out.println("Number of Vertices?");
//        int numVert = Integer.parseInt(in.nextLine().trim());
//        System.out.println("Number of Edges Out and In? One value as these need to be equal");
//        int numEdges = Integer.parseInt(in.nextLine().trim());
//        in.close();
//        Graph graph = generate(numVert, numEdges);
//        GraphWrite("src\\current\\graphTwoNodes.txt", stringNodes(graph));
//        GraphWrite("src\\current\\graphTwoEdges.txt", edgeNodes(graph));
//
//    }
//    private static Graph generate(int numVert, int numEdges) {
//        Graph ret = new Graph();
//        for (int i = 0; i < numVert; i++) {
//            String id = "";
//            if (i < 10) {
//                id += "00" + i;
//                ret.nodes.add(randomNode(id, ret, i));
//            } else if (i < 100) {
//                id += "0" + i;
//                ret.nodes.add(randomNode(id, ret, i));
//            } else {
//                id += i;
//                ret.nodes.add(randomNode(id, ret, i));
//            }
//        }
//        ret.nodes = genRandEdge(ret, numEdges);
//        return ret;
//    }
//    private static String edgeNodes(Graph ret) {
//        ArrayList<Node> nodes = ret.nodes;
//        String toRet = "";
//        for (Node node : nodes) {
//            for (Edge edge : node.outEdges)
//                toRet += node.identifier + " " + edge.child.identifier + " " + edge.weight +  "\n";
//        }
//        return toRet;
//    }
//    private static String stringNodes(Graph ret) {
//        ArrayList<Node> nodes = ret.nodes;
//        String toRet = "";
//        for (Node node : nodes) {
//            toRet += node.identifier + " " + node.latitude + " " + node.longitude + "\n";
//        }
//        return toRet;
//    }
//    private static ArrayList<Node> genRandEdge(Graph ret, int numEdges) {
//        ArrayList<Node> nodes = ret.nodes;
//        for (int i = 0; i < nodes.size(); i++) {
//            if (nodes.get(i).outEdges.size() < numEdges) {
//                Random rand = new Random();
//                int index1 = rand.nextInt(nodes.size());
//                int index2 = rand.nextInt(nodes.size());
//                
//                while (index1 == index2 || nodes.get(index1).inEdges.size() >= numEdges || nodes.get(index2).inEdges.size() >= numEdges ) {
//                    if (index1 == index2) {
//                        index2 = rand.nextInt(nodes.size());
//                    }
//                    if (nodes.get(index1).inEdges.size() >= numEdges) {
//                        index1 = rand.nextInt(nodes.size());
//                    }
//                    if (nodes.get(index2).inEdges.size() >= numEdges) {
//                        index2 = rand.nextInt(nodes.size());
//                    }
//                    
//                }
//                
//                ret.addEdge(nodes.get(i), nodes.get(index1), rand.nextDouble() * 100);
//                ret.addEdge(nodes.get(i), nodes.get(index2), rand.nextDouble() * 100);
//            }
//        }
//        return ret.nodes;
//    }
//    private static Node randomNode(String id, Graph ret, int index) {
//        Node rando = ret.new Node(id, 0, 0);
//        rando.index = index;
//        Random coorGen = new Random();
//        rando.latitude = coorGen.nextDouble();
//        rando.longitude = coorGen.nextDouble();
//        rando.outEdges = new ArrayList<Edge>();
//        rando.inEdges = new ArrayList<Edge>();
//        rando.g_val = 0;
//        rando.h_val = 0;
//        rando.fweight = 0;
//        return rando;
//    }
//    /**
//     * 
//     */
//    public static boolean GraphWrite(String filename, String out) {
//        try {
//             PrintStream output = new PrintStream(new File(filename.trim()));
//             output.println(out.trim());
//             output.close();
//        } catch (IOException e) {
//            System.out.println(filename + "could not be read");
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//}
