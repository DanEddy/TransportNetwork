package stops;
import java.util.*;

/**
 * The class should map destination stops to ROutingEntry objects.
 *
 * The table is able to redirect passengers from their current stop to the next intermediate stop which they should go
 * to, in order to reach their final destination.
 */
public class RoutingTable {

    // Defining the initial stop
    private Stop initialStop;

    // Defining a HashMap to represent the routingTable
    private Map<Stop, RoutingEntry> routingTable = new LinkedHashMap<>();

    /**
     * Creates a new RoutingTable for the given stop.
     * The routing table should be created with an entry for its initial stop (i.e. a mapping from the stop to a
     * RoutingEntry.RoutingEntry() for that stop with a cost of 0).
     *
     * @param initialStop The stop for which this table will handle routing.
     */
    public RoutingTable(Stop initialStop) {

        this.initialStop = initialStop;
        routingTable.put(initialStop, new RoutingEntry(initialStop, 0));
    }

    /**
     * A Private helper method used to determine the manhatten distance (and subsequently Cost) when travelling between
     * two stops.
     * @param stop1 The initial Stop.
     * @param stop2 The Destination Stop.
     * @return The Manhattan Distance (Travel Cost) between two Stop1 and Stop 2.
     */
    private int costCalculator(Stop stop1, Stop stop2) {
        return Math.abs(stop1.getX() - stop2.getX()) + Math.abs(stop1.getY() - stop2.getY());
    }

    /**
     * Adds the given stop as a neighbour of the stop stored in this table.
     *
     * A neighbouring stop should be added as a destination in this table, with the cost to reach that destination
     * simply being the manhattan distance between this table's stop and the given neighbour stop.
     *
     * If the given neighbour already exists in the table, it should be updated (as defined in
     * addOrUpdateEntry(Stop, int, Stop)).
     *
     * The 'intermediate'/'next' stop between this table's stop and the new neighbour stop should simply be the
     * neighbour stop itself.
     *
     * Once the new neighbour has been added as an entry, this table should be synchronised with the rest of the network
     * using the synchronise() method.
     *
     * @param neighbour The stop to be added as a neighbour.
     */
    public void addNeighbour(Stop neighbour) {

        // Checking if the neighbouring stop already exists in the routingTable
        if (this.routingTable.containsKey(neighbour)) {

            // updating neighbour's entry.
            addOrUpdateEntry(neighbour, costCalculator(neighbour, neighbour), neighbour);
        }

        // Adding the neighbouring stop to the routing table
        routingTable.put(neighbour, new RoutingEntry(neighbour, costCalculator(this.initialStop, neighbour)));

        // Synchronising the routingTable
        synchronise();
    }

    /**
     * If there is currently no entry for the destination in the table, a new entry for the given destination should be
     * added, with a RoutingEntry for the given cost and next (intermediate) stop.
     *
     * If there is already an entry for the given destination, and the newCost is lower than the current cost associated
     * with the destination, then the entry should be updated to have the given newCost and next (intermediate) stop.
     *
     * If there is already an entry for the given destination, but the newCost is greater than or equal to the current
     * cost associated with the destination, then the entry should remain unchanged.
     *
     * @param destination The destination stop to add/update the entry.
     * @param newCost The new cost to associate with the new/updated entry
     * @param intermediate The new intermediate/next stop to associate with the new/updated entry.
     * @return True if a new entry was added, or an existing one was updated, or false if the table remained unchanged.
     */
    public boolean addOrUpdateEntry(Stop destination, int newCost, Stop intermediate) {

        // Checking if the destination is not already in the routingTable
        if (!this.routingTable.containsKey(destination)) {
            routingTable.put(destination, new RoutingEntry(intermediate, newCost));
            return true;
        }

        // Checking if the newCost is cheaper than the current cost.
        if (newCost < costTo(destination)) {
            this.routingTable.replace(destination, new RoutingEntry(intermediate, newCost));
            return true;
        }

        // Assuming that newCost is not null, than the only remaining option is that newCost >= current cost, thus will
        // remain unchanged - returning false
        return false;
    }

    /**
     * Returns the cost associated with getting to the given stop.
     *
     * @param stop The stop to get the cost.
     * @return The cost to the given stop, or Integer.MAX_VALUE if the stop is not currently in this routing table.
     */
    public int costTo(Stop stop) {

        // If the stop is not in the table, returning max_value
        if (!routingTable.containsKey(stop)) {
            return Integer.MAX_VALUE;
        }

        // Returning the cost to get to the stop, as calculated by the getCosts() method.
        return routingTable.get(stop).getCost();

    }

    /**
     * Maps each destination stop in this table to the cost associated with getting to that destination.
     *
     * @return A mapping from destination stops to the costs associated with getting to those stops.
     */
    public java.util.Map<Stop, Integer> getCosts() {

        Map<Stop, Integer> routeCosts = new LinkedHashMap<>();

        for (Stop destination : this.routingTable.keySet()) {
            routeCosts.put(destination, routingTable.get(destination).getCost());
        }

        return routeCosts;
    }

    /**
     * Return the stop for which this table will handle routing.
     *
     * @return the stop for which this table will handle routing.
     */
    public Stop getStop() {

        return this.initialStop;
    }

    /**
     * Returns the next intermediate stop which passengers should be routed to in order to reach the given destination.
     * If the given stop is null or not in the table, then return null.
     *
     * @param destination The destination which the passengers are being routed.
     * @return The best stop to route the passengers to in order to reach the given destination.
     */
    public Stop nextStop(Stop destination) {

        // Checking if destination is null, or not in the routingTable
        if (destination == null || !routingTable.containsKey(destination)) {
            return null;
        }

        // Returning the next/intermediate stop as defined in the routeEntry
        return this.routingTable.get(destination).getNext();
    }

    /**
     * Synchronises this routing table with the other tables in the network.
     *
     * In each iteration, every stop in the network which is reachable by this table's stop (as returned by
     * traverseNetwork()) must be considered. For each stop x in the network, each of its neighbours must be visited,
     * and the entries from x must be transferred to each neighbour (using the transferEntries(Stop) method).
     *
     * If any of these transfers transfers results in a change to the table that the entries are being transferred, than
     * the entire process must be repeated again. These iterations should continue happening until no changes occur to
     * any of the tables in the network.
     *
     * This process is designed to handle changes which need to be propagated throughout the entire network, which could
     * take more than one iteration.
     */
    public void synchronise() {

        boolean networkChanged;

        do {
            networkChanged = false;

            // Iterating over all reachable stops
            for (Stop stop : this.traverseNetwork()) {

                // Iterating over each reachable stops neighbours
                for (Stop neighbour : stop.getNeighbours()) {
                    networkChanged = stop.getRoutingTable().transferEntries(neighbour);
                }
            }
        } while (networkChanged);
    }

    /**
     * Updates the entries in the routing table of the given other stop, with the entries from this routing table.
     *
     * If this routing table has entries which the other stop's table doesn't, then the entries should be added to the
     * other table (as defined in addOrUpdateEntry(Stop, int, Stop)) with the cost being updated to include the
     * distance.
     *
     * If this routing table has entries which the other stop's table does have, and the new cost would be lower than
     * that associated with its existing entry, then the other table's entry should be updated (as defined in
     * addOrUpdateEntry(Stop, int, Stop)).
     *
     * If this routing table has entries which the other stop's table does have, but the new cost would be greater than
     * or equal to that associated with its existing entry, then the other table's entry should remain unchanged.
     *
     * @param other The stop whose routing table this table's entries should be transferred.
     * @return True if any new entries were added to the other stop's table, or if any of its existing entries were
     * updated, or false if the other stop's table remains unchanged.
     * @require this.getStop().getNeighbours().contains(other) == true
     */
    public boolean transferEntries(Stop other) {

        // Cost to get from this stop to other stop
        int costToOther = initialStop.getRoutingTable().costTo(other);

        // Defining lists of all destinations of this stop and other stop
        List<Stop> otherDestinations = new ArrayList<>(other.getRoutingTable().routingTable.keySet());
        List<Stop> thisDestinations = new ArrayList<>(this.routingTable.keySet());

        // checking for additional destinations in this routing table
        if (!otherDestinations.containsAll(thisDestinations)) {

            // Iterating over all destinations in this routing table
            for (Stop destination : thisDestinations) {

                // updating other tables entries.
                other.getRoutingTable().addOrUpdateEntry(destination, costTo(destination) + costToOther, initialStop);
            }

            // Returning true, as changes have been made
            return true;
        }

        // Iterating over all destinations in this routing table
        for (Stop destination : thisDestinations) {

            // if destination is in both routingTables, checking if new route is 'cheaper'
            if (costTo(destination) + costToOther < other.getRoutingTable().costTo(destination)) {

                // Updating the other's routeTable to pass through this stop on the way to destination
                other.getRoutingTable().addOrUpdateEntry(destination, costTo(destination) + costToOther, initialStop);

                // Returning true, as changes have been made.
                return true;
            }
        }

        // No changes made - new route 'more expensive'
        return false;
    }

    /**
     * Performs a traversal of all the stops in the network, and returns a list of every stop which is reachable from the
     * stop stored in this table.
     *  1. Firstly create an empty list of Stops and an empty Stack of Stops. Push the RoutingTable's Stop on to the
     *      stack.
     *  2. While the stack is not empty,
     *      2.1 pop the top Stop (current) from the stack.
     *      2.2 for each of that stop's neighbours,
     *          2.2.1 if they are not in the list, add them to the stack.
     *      2.3 Then add the current Stop to the list.
     *  3. Return the list of seen stops.
     *
     * @return All of the stops in the network which are reachable by the stop stored in this table.
     */
    public List<Stop> traverseNetwork() {

        // Step 1 - creating an empty list of stops and stack of stops. pushing the current stop onto the stack.
        List<Stop> seenStops = new ArrayList<>();
        Stack<Stop> stopStack = new Stack<>();
        stopStack.push(this.initialStop);

        // Step 2 - While the stack is not empty:
        while(!stopStack.isEmpty()) {

            // Step 2.1 - pop the top Stop from the stack
            Stop currentStop = stopStack.pop();

            // Step 2.2 - for each of the top stops neighbours
            for (Stop neighbour : currentStop.getNeighbours()) {

                // Step 2.2.1 - if they are not in the list, adding the stops to the stack
                if (!seenStops.contains(neighbour)) {
                    stopStack.push(neighbour);
                }
            }

            // Step 2.3 - Adding the current Stop to the list, and checking for duplicates.
            if (!seenStops.contains(currentStop)) {
                seenStops.add(currentStop);
            }
        }

        // Step 3 - Returning the list of seenStops
        return seenStops;
    }
}
