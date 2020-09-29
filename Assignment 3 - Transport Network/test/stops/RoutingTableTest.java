package stops;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class RoutingTableTest {

    private Stop stopA;
    private Stop stopB;
    private Stop stopC;
    private Stop stopD;
    private Stop stopE;
    private Stop stopF;
    private Stop stopG;
    private Stop stopH;

    private List<Stop> stopList;
    private HashMap<Stop, Integer> stopHashMap;

    @Before
    public void setUp() throws Exception {
        stopA = new Stop("A", 0, 0);
        stopB = new Stop("B", 1, 1);
        stopC = new Stop("C", 1, -1/2);
        stopD = new Stop("D", 0, -1);
        stopE = new Stop("E", -1, 1);
        stopF = new Stop("F", -2, -2);
        stopG = new Stop("G", 1, 2);
        stopH = new Stop("H", 10, 10);

        stopList = new LinkedList<>();
        stopHashMap = new LinkedHashMap<>();

    }

    private int costCalculator(Stop stop1, Stop stop2) {
        return Math.abs(stop1.getX() - stop2.getX()) + Math.abs(stop1.getY() - stop2.getY());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addNeighbour() {

        // Base case, adding a stop that is not already a neighbour / checking correct intermediate stop
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);

        stopB.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopB);

        stopList.add(stopA);
        stopList.add(stopB);
        stopList.add(stopC);

        Assert.assertEquals(stopList, stopA.getRoutingTable().traverseNetwork());
        Assert.assertEquals(costCalculator(stopA, stopB), stopA.getRoutingTable().costTo(stopB));
        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopB));


        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopC));
        Assert.assertEquals(costCalculator(stopA, stopB) + costCalculator(stopB, stopC),
                stopA.getRoutingTable().costTo(stopC));


        // expected case, where an existing stop is overridden
        stopA.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopA);

        Assert.assertEquals(stopC, stopA.getRoutingTable().nextStop(stopC));
        Assert.assertTrue(stopA.getRoutingTable().traverseNetwork().containsAll(stopList));
        Assert.assertEquals(costCalculator(stopA, stopC), stopA.getRoutingTable().costTo(stopC));

        // Edge case - Null stop - expecting no change
        stopA.addNeighbouringStop(null);
        Assert.assertTrue(stopA.getRoutingTable().traverseNetwork().containsAll(stopList));

    }

    @Test
    public void addOrUpdateEntry() {
        stopList.add(stopA); stopList.add(stopB); stopList.add(stopC);
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);

        stopA.addNeighbouringStop(stopD);
        stopD.addNeighbouringStop(stopA);

        stopA.addNeighbouringStop(stopH);
        stopH.addNeighbouringStop(stopA);

        // Base case - no pre existing entry for the destination in the routing table
        Assert.assertTrue(stopA.getRoutingTable().addOrUpdateEntry(stopC, costCalculator(stopA, stopB), stopB));
        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopC));
        Assert.assertEquals(costCalculator(stopA, stopB), stopA.getRoutingTable().costTo(stopC));

        // standard case - cheaper cost then pre-existing route
        Assert.assertTrue(stopA.getRoutingTable().addOrUpdateEntry(stopC, costCalculator(stopA, stopD), stopD));
        Assert.assertEquals(stopD, stopA.getRoutingTable().nextStop(stopC));
        Assert.assertEquals(costCalculator(stopA, stopD), stopA.getRoutingTable().costTo(stopC));

        // standard case - more expensive the pre-existing route
        Assert.assertFalse(stopA.getRoutingTable().addOrUpdateEntry(stopC, costCalculator(stopA, stopH), stopH));
        Assert.assertNotEquals(stopH, stopA.getRoutingTable().nextStop(stopC));
        Assert.assertNotEquals(costCalculator(stopA, stopH), stopA.getRoutingTable().costTo(stopC));

        // edge case - is equally expensive as current route
    }

    @Test
    public void costTo() {

        // standard case - testing the cost to itself
        Assert.assertEquals(0, stopA.getRoutingTable().costTo(stopA));

        // standard case - stop on the routing table that is not itself
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);
        Assert.assertEquals(costCalculator(stopA, stopB), stopA.getRoutingTable().costTo(stopB));

        // edge case - stop not on the routing table
        Assert.assertEquals(Integer.MAX_VALUE, stopA.getRoutingTable().costTo(stopC));

        // edge case - null stop
        Assert.assertEquals(Integer.MAX_VALUE, stopA.getRoutingTable().costTo(null));
    }

    @Test
    public void getCosts() {
        // edge case - only the initial stop
        stopHashMap.put(stopA, 0);
        Assert.assertEquals(stopHashMap, stopA.getRoutingTable().getCosts());

        // Base case - direct to one stop
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);
        stopHashMap.put(stopB, costCalculator(stopA, stopB));
        Assert.assertEquals(stopHashMap, stopA.getRoutingTable().getCosts());

        // Standard case - direct to two stops
        stopA.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopA);
        stopHashMap.put(stopC, costCalculator(stopA, stopC));
        Assert.assertEquals(stopHashMap, stopA.getRoutingTable().getCosts());

        // Standard case - direct to multiple stops
        stopA.addNeighbouringStop(stopD);
        stopD.addNeighbouringStop(stopA);
        stopHashMap.put(stopD, costCalculator(stopA, stopD));
        Assert.assertEquals(stopHashMap, stopA.getRoutingTable().getCosts());

        // edge case - large cost
        Stop stopK = new Stop("k", 10000, 10000);
        stopA.addNeighbouringStop(stopK);
        stopK.addNeighbouringStop(stopA);
        stopHashMap.put(stopK, costCalculator(stopA, stopK));
        Assert.assertEquals(stopHashMap, stopA.getRoutingTable().getCosts());
    }

    @Test
    public void getStop() {

        // base case - standard stop
        Assert.assertEquals(stopA, stopA.getRoutingTable().getStop());

    }

    @Test
    public void nextStop() {

        // standard case - no intermediate stop i.e. neigbouring stop
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);
        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopB));

        // edge case - initial stop
        Assert.assertEquals(stopA, stopA.getRoutingTable().nextStop(stopA));

        // standard case - one intermediate stop
        stopB.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopB);
        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopC));

        // extended case - multiple intermediate stops
        stopC.addNeighbouringStop(stopD);
        stopD.addNeighbouringStop(stopC);
        Assert.assertEquals(stopB, stopA.getRoutingTable().nextStop(stopD));

        // edge case - destination stop is null
        Assert.assertNull(stopA.getRoutingTable().nextStop(null));

        // edge case - stop is not in the routing table
        Assert.assertNull(stopA.getRoutingTable().nextStop(stopH));
    }

    @Test
    public void synchronise() {

        // edge case - no changes are made
        stopA.getRoutingTable().synchronise();
        stopList.add(stopA);
        Assert.assertEquals(stopList, stopA.getRoutingTable().traverseNetwork());

        // Standard case - possibly one iteration
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);
        stopList.add(stopB);
        Assert.assertTrue(stopList.containsAll(stopA.getRoutingTable().traverseNetwork()));

        // Standard case - several neighbours to a stop
        stopB.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopB);

        stopB.addNeighbouringStop(stopD);
        stopD.addNeighbouringStop(stopB);
        stopList.add(stopC); stopList.add(stopD);
        Assert.assertTrue(stopList.containsAll(stopA.getRoutingTable().traverseNetwork()));
    }

    @Test
    public void transferEntries() {

        // edge case - null 'other' stop
        try {
            stopA.getRoutingTable().transferEntries(null);
            Assert.fail();
        } catch(NullPointerException e) {
        }

        // Base case - no difference in the routing tables
        Assert.assertFalse(stopA.getRoutingTable().transferEntries(stopA));

        // Standard case - routing table has entries which the other stop does not (single case)
        Assert.assertTrue(stopA.getRoutingTable().transferEntries(stopB));

        // Standard case - routing table has entries which the other stop does not (multiple case)
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);

        stopA.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopA);
        Assert.assertEquals( 3, stopC.getRoutingTable().getCosts().size());

        // standard case - routing table has cheaper entries than 'other' stop
        stopB.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopB);

        Assert.assertEquals(costCalculator(stopB, stopC), stopC.getRoutingTable().costTo(stopB));

        // standard case - routing table has more expensive entry than 'other' stop
        stopA.addNeighbouringStop(stopE);
        stopE.addNeighbouringStop(stopA);

        stopE.addNeighbouringStop(stopF);
        stopF.addNeighbouringStop(stopE);

        stopF.addNeighbouringStop(stopG);
        stopG.addNeighbouringStop(stopF);

        int initialCost = stopA.getRoutingTable().costTo(stopG);
        stopA.addNeighbouringStop(stopG);
        stopG.addNeighbouringStop(stopA);

        int afterCost = stopA.getRoutingTable().costTo(stopG);
        Assert.assertTrue(initialCost > afterCost);

        // standard case - routing table has same cost entry
        stopB.addNeighbouringStop(stopH);
        stopH.addNeighbouringStop(stopB);

        stopH.addNeighbouringStop(stopC);
        stopC.addNeighbouringStop(stopB);
        Assert.assertEquals(costCalculator(stopB, stopH) + costCalculator(stopB, stopC), stopC.getRoutingTable().costTo(stopH));
    }

    @Test
    public void traverseNetwork() {
        // base case, no others stops reachable
        stopList.add(stopA);
        Assert.assertEquals(stopList, stopA.getRoutingTable().traverseNetwork());

        // edge case, null stop
        stopA.addNeighbouringStop(null);
        Assert.assertEquals(stopList, stopA.getRoutingTable().traverseNetwork());

        // standard case, where there is one reachable stop
        stopA.addNeighbouringStop(stopB);
        stopB.addNeighbouringStop(stopA);
        stopList.add(stopB);
        Assert.assertTrue(stopList.containsAll(stopA.getRoutingTable().traverseNetwork()));

        // standard case, where there are two reachable stops
        stopA.addNeighbouringStop(stopE);
        stopE.addNeighbouringStop(stopA);
        stopList.add(stopE);
        Assert.assertTrue(stopList.containsAll(stopA.getRoutingTable().traverseNetwork()));

        // standard case, where there are multiple reachable stops
        stopE.addNeighbouringStop(stopF);
        stopF.addNeighbouringStop(stopE);
        stopList.add(stopF);
        Assert.assertTrue(stopList.containsAll(stopA.getRoutingTable().traverseNetwork()));

    }
}