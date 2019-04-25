/**
 * 
 */
package current;

import java.util.ArrayList;

/**
 * @author Sam
 *
 */
public class Graph {

    public double COORDINATE_CONVERSION_FACTOR = 1.0;
    public ArrayList<Node> nodes;
    public int target;
    public int start;
    public boolean manDist = false;
    public boolean findTarget = true;
    public boolean printDistances = false;
    public boolean printPath = true;
    /**
     * 
     */
    public Graph() {
        nodes = new ArrayList<Node>();
        target = -1;
        start = -1;
    }
    public boolean addEdge(Node parentID, Node childID, double weight) {
        parentID.addEdge(childID, weight);
        return true;
    }
    public Node getNode(String id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).identifier.equalsIgnoreCase(id))
                return nodes.get(i);
        }
        return null;
    }
    public void resetParents() {
        Node current = nodes.get(target);
        while (current.pathParent != null) {
            Node next = current.pathParent;
            current.pathParent = null;
            current = next;
        }
        for (Node node : nodes) {
            node.pathParent = null;
            node.g_val = 0;
            node.fweight = 0;
            if (manDist) {
                node.h_val = manHueristic(node);
            } else {
                node.h_val = hueristic(node);
            }
            node.fweight = node.g_val + node.h_val;
            
        }
        
    }
    /**
     * @param parentID
     * @return
     */
    public double hueristic(Node parentID) {
        double lat = parentID.x;
        double lon = parentID.y;
        double latTar = nodes.get(target).x;
        double lonTar = nodes.get(target).y;
        double x = latTar - lat;
        double y = lonTar - lon;
        double distance = Math.sqrt(x*x + y*y);
        distance *= COORDINATE_CONVERSION_FACTOR;
        return distance;
    }
    /**
     * @param parentID
     * @return
     */
    public double manHueristic(Node parentID) {
        double lat = parentID.x;
        double lon = parentID.y;
        double latTar = nodes.get(target).x;
        double lonTar = nodes.get(target).y;
        double x = latTar - lat;
        double y = lonTar - lon;
        double distance = Math.abs(x) + Math.abs(y);
        return distance;
    }
    /**
     * @return the target
     */
    public Node getTarget() {
        return nodes.get(target);
    }
    /**
     * @return the start
     */
    public Node getStart() {
        return nodes.get(start);
    }
    /**
     * @return
     */
    public boolean resetHueristic() {
        
        if (start < 0 || target < 0) {
            return false;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (manDist)
                nodes.get(i).h_val = (manHueristic(nodes.get(i)));
            else
                nodes.get(i).h_val = (hueristic(nodes.get(i)));
        }
        return true;
    }
    /**
     * @param i
     */
    public void setStart(int i) {
        start = i;
        
    }
    /**
     * @param i
     */
    public void setTarget(int i) {
        target = i;
    }
    
    
/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String ret = "";
        for (Node element : nodes) {
            ret += element.toString() + "\n";
        }
                
        return ret;
    }


/**
 * @author Sam
 *
 */
public class Node {

    public String identifier;
    public ArrayList<Edge> outEdges;
    public ArrayList<Edge> inEdges;
    public Node pathParent;
    public double g_val;
    public double fweight;
    public double h_val; 
    public double x;
    public double y; 
    public int index; 
    /**
     * 
     */
    public Node(String parentID, double goal, double hueristic) {
        identifier = parentID;
        g_val = goal;
        h_val = hueristic;
        fweight = g_val + h_val;
        x = 0;
        y = 0;
        index = 0;
        outEdges =  new ArrayList<Edge>();
        inEdges =  new ArrayList<Edge>();
    }
    /**
     * @param count 
     * 
     */
    public Node(String parentID, double goal, double hueristic, double lat, double lon, int count) {
        identifier = parentID;
        g_val = goal;
        h_val = hueristic;
        fweight = g_val + h_val;
        index = count;
        x = lat;
        y = lon;
        outEdges =  new ArrayList<Edge>();
        inEdges =  new ArrayList<Edge>();
    }
    /**
     * @param index
     * @return
     */
    public Edge getEdge(int index) {
        return outEdges.get(index);
    }
    /**
     * @param newEdge
     * @return
     */
    public boolean addEdge(Edge newEdge) {
        return outEdges.add(newEdge);
    }
    public void addEdge(Node childID, double weight) {
        Edge edge = new Edge(this, childID, weight);
        outEdges.add(edge);
        childID.inEdges.add(edge);
        
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String ret = "Node [index= " + index + " identifier=" + identifier + ", g_val=" + g_val + ", h_val=" + h_val + ", x=" + x + ", y=" + y + "]\n Out: \n";
        for (int i = 0; i < outEdges.size(); i++) {
            Edge edge = outEdges.get(i);
            ret += edge.parent.identifier + " " + edge.child.identifier + " " + edge.weight + "\n";
        }
        ret += "\n In:\n";

        for (int i = 0; i < inEdges.size(); i++) {
            Edge edge = inEdges.get(i);
            ret += edge.parent.identifier + " " + edge.child.identifier + " " + edge.weight + "\n";
        }
        return ret;
    }
    public String printPath() {
        String ret = "";
        if (pathParent != null) {
            ret += pathParent.printPath() + " ";
        }
        ret += this.identifier;
        
        return ret;
    }

}
/**
 * @author Sam
 *
 */
public class Edge {
    public Node parent;
    public Node child;
    public double weight;

    /**
     * 
     */
    public Edge(Node tparent, Node tchild, double tweight) {
        parent = tparent;
        child = tchild;
        weight = tweight;
    }

}
}