package stops;

/**
 * Represents an entry in a routing table.
 *
 * Each entry contains information for how to route passengers to a specific destination. This information includes the
 * best intermediate stop to route passengers to in order to get them to that destination, as well as the cost (in
 * Manhattan distance) as required to reach the destination when going via that intermediate stop.
 */
public class RoutingEntry {

    // initializing the member variables representing the next stop to reach the destination and the cost to destination
    private Stop nextStop;
    private int cost;

    /**
     * Creates a new default RoutingEntry object. The next stop should be stores as null, and the cost should be stored
     * as Integer.MAX_VALUE.
     *
     * These default values are used to represent a destination in a routing table to which there is currently no known
     * path.
     */
    public RoutingEntry() {

        this.nextStop = null;
        this.cost = Integer.MAX_VALUE;
    }

    /**
     * Creates a new RoutingEntry object with the given next stop and Cost for the given destination.
     *
     * If the given stop is null, or if the given cost is negative, then the default values should be used for BOTH next
     * stop and cost instead (that is, null for next stop and Integer.MAX_VALUE for cost).
     *
     * @param next The next intermediate stop to go to in order to reach the destination.
     * @param cost The cost to get to the destination.
     */
    public RoutingEntry(Stop next, int cost) {

        // Checking if the next stop is null, or the cost is negative.
        if (next == null || cost < 0) {
            this.nextStop = null;
            this.cost = Integer.MAX_VALUE;
        }

        this.nextStop = next;
        this.cost = cost;
    }

    /**
     * Returns the cost to get to the destination associated with this entry in the routing table.
     *
     * @return The cost associated with this entry.
     */
    public int getCost() {

        return this.cost;
    }

    /**
     * Returns the next stop to be visited in order to reach the destination, or null if there is not known route to
     * reach the destination associated with this entry in the routing table.
     *
     * @return the next stop, or null if there is no known route to the destination.
     */
    public Stop getNext() {

        return this.nextStop;
    }
}
