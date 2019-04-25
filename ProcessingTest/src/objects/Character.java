package objects;

import java.util.Stack;

import objects.Graph.Node;
import processing.core.PVector;

public class Character {

    public PVector orientation;
    public float angRot;
    public float angle;
    public PVector position;
    public PVector velocity;
    public PVector acceleration;
    public boolean inSight;
    public boolean alive;
    public Stack<Node> path;
    public int start;
    public PVector target;
    public PVector goal;
    public Graph graph;
    public boolean run;
}
