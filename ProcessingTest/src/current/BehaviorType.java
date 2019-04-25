/**
 * 
 */
package current;

/**
 * @author Sam
 *
 */
public enum BehaviorType {
    //node type
    SEQUENCE,
    SELECTOR,
    DECORATOR,
    RANDOM_SELECT,
    RANDOM_SEQ,
    PARALLEL,
    LEAF,
    //content typing
    BOOLEAN,
    PARAMETER,
    EQUAL,
    LESS,
    GREATER, 
    //bool type
    CONDITION, UPDATE, ACTION
}
