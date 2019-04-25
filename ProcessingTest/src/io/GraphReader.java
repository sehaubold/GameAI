package io;
///**
// * 
// */
//package necessarytxt;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.util.Scanner;
//import java.util.StringTokenizer;
//
//import executors.Graph.Node;
//
///**
// * @author Sam
// *
// */
//public class GraphReader {
//    public static final double REDUCER = 1.0;
//    /**
//     * @param filenameT 
//     * 
//     */
//    public static Graph GraphRead(String filename, String filenameT) {
//        Scanner fileIn;
//        Graph ret = new Graph();
//        int count = 0;
//        try {
//            fileIn = new Scanner(new File(filenameT));
//            while (fileIn.hasNextLine()) {
//                String line = fileIn.nextLine();
//                StringTokenizer linescan = new StringTokenizer(line, " ");
//                String node = linescan.nextToken().trim();
//                double lat = Double.parseDouble(linescan.nextToken().trim());
//                double lon = Double.parseDouble(linescan.nextToken().trim());
//                Node add = ret.new Node(node, 0, 0, lat, lon, count);
//                ret.nodes.add(add);
//                count++;
//            }
//        } catch (IOException e) {
//            System.out.println("Names could not be read");
//            e.printStackTrace();
//        }
//        
//        try {
//            fileIn = new Scanner(new File(filename));
//            while (fileIn.hasNextLine()) {
//                String line = fileIn.nextLine();
//                StringTokenizer linescan = new StringTokenizer(line, " ");
//                String parentNode = linescan.nextToken().trim();
//                String childNode = linescan.nextToken().trim();
//                double weight = Double.parseDouble(linescan.nextToken().trim()) / REDUCER;
//                Node parent = null;
//                Node child = null;
//                for (int i = 0; i < ret.nodes.size() && !parentNode.equals("") && !childNode.equals(""); i++) {
//                    if (parentNode.equals(ret.nodes.get(i).identifier)) {
//                        parent = ret.nodes.get(i);
//                    }
//                    if (childNode.equals(ret.nodes.get(i).identifier)) {
//                        child = ret.nodes.get(i);
//                    } 
//                }
//                if (parent != null && child != null)
//                    ret.addEdge(parent, child, weight);
//            }
//        } catch (IOException e) {
//            System.out.println(filename + "could not be read");
//            e.printStackTrace();
//        }
//        ret.setTarget(ret.nodes.size() - 1);
//        ret.setStart(0);
//        return ret;
//    }
//    /**
//     * 
//     */
//    public static boolean GraphWrite(String filename, String out) {
//        try {
//             PrintStream output = new PrintStream(new File(filename.trim()));
//             output.println(out);
//             output.close();
//        } catch (IOException e) {
//            System.out.println(filename + "could not be read");
//            e.printStackTrace();
//        }
//        return true;
//    }
//    /**
//     * 
//     */
//    public static boolean DataWrite(String filename, String out) {
//        BufferedWriter adder = null;
//        try {
//            adder = new BufferedWriter(new FileWriter(filename, true));
//            adder.write(out);
//            adder.newLine();
//            adder.newLine();
//            adder.flush();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        } finally {     
//           if (adder != null) try {
//               adder.close();
//           } catch (IOException ioe2) {
//              // ignore it
//           }
//        } 
//
//        return true;
//    }
//
//}
