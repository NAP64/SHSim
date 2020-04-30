import java.util.*;

import base.SHSim.*;

public class Attacker
{
    static HashMap<String, Integer> obstacleBreachTime;
    static HashMap<String, Integer> accessTime;
    //static HashMap<String, Integer> connectionBreachTime;
    static HashMap<String, Integer> deviceBreachTime;
    static int moveCost;
    static int maxCost;
    static HashMap<SHSimObject, Integer> resultTime;
    static HashMap<SHSimObject, Integer> accessable;
    static HashMap<SHSimObject, String> resultTimep;
    static HashMap<SHSimObject, String> accessablep;
    static LinkedList<SHSimObject> toRun;
    public static void simAttack(AbstractConnection internet, Space... spaces)
    {
        init();
        resultTime = new HashMap<SHSimObject, Integer>();//breached
        accessable = new HashMap<SHSimObject, Integer>();//control over
        resultTimep = new HashMap<SHSimObject, String>();
        accessablep = new HashMap<SHSimObject, String>();
        toRun = new LinkedList<SHSimObject>();
        for (Space s : spaces)
        {
            resultTime.put(s, 0);
            resultTimep.put(s, "");
            toRun.add(s);
        }
        accessable.put(internet, 0);
        accessablep.put(internet, "");
        toRun.add(internet);
        SHSimObject obj = toRun.poll();
        int count = 0;
        while (obj != null)
        {
            //System.out.println(obj.name());
            if (obj instanceof Space)
                runSpace((Space)obj);
            else if (obj instanceof Connection)
                runConnection((Connection)obj);
            else
                runDevice(obj);
            count++;
            obj = toRun.poll();
        }
        //System.out.println("\n" + count);
        System.out.println("\nBreached: ");
        for (SHSimObject o : resultTime.keySet())
            System.out.println(o.name() + ":" + resultTime.get(o) + ":" + resultTimep.get(o));
        System.out.println("\nJoined connections: ");
        for (SHSimObject o : accessable.keySet())
            if (o instanceof Connection)
                System.out.println(o.name() + ":" + accessable.get(o) + ":" + accessablep.get(o));
        System.out.println("\nControllable: ");
        for (SHSimObject o : accessable.keySet())
            if (!(o instanceof Connection))
                System.out.println(o.name() + ":" + accessable.get(o) + ":" + accessablep.get(o));
    }

    // runXxx calculates and update breachtime for next level.
    //Access to space = breach, unless space monitored
    private static void runSpace(Space obj)//require at least breached
    {
        for (Input in : obj.monitoredBy())
            if (!resultTime.containsKey(in))//breached == can disable, accessable == can send input
                return;//monitored, cannot continue
        int cur = resultTime.get(obj);
        String path = resultTimep.get(obj) + obj.name() + ", ";
        for (Space s : obj.connects())
            testAndAdd(resultTime, resultTimep, s, cur, path);
        for (Obstacle o : obj.paths())
            if (resultTime.containsKey(o))
            {
                Space[] s = o.connects();
                for (int i = 0; i < 2; i++)
                    if (s[i] != null && s[i] != obj)
                        testAndAdd(resultTime, resultTimep, s[i], resultTime.get(o) + moveCost, resultTimep.get(o) + o.name() + ", ");
            }
            else if (!o.isLocked() || existBreachTime(obstacleBreachTime, o))
            {
                int off = o.isLocked() ? getBreachTime(obstacleBreachTime, o) : moveCost;
                Space[] s = o.connects();
                for (int i = 0; i < 2; i++)
                    if (s[i] != null && s[i] != obj)
                        testAndAdd(resultTime, resultTimep, s[i], cur + off, path + o.name() + ", ");
            }
        for (SHSimObject so : obj.contains())
            if (existBreachTime(deviceBreachTime, so))
                testAndAdd(resultTime, resultTimep, so, cur + getBreachTime(deviceBreachTime, so), path);
            else
                testAndAdd(resultTime, resultTimep, so, cur + moveCost, path);
        specialConnectionBreach(obj, cur, path);
        for (SHSimObject so : obj.accessable())
            if (so instanceof Connection)
            {
                if (existBreachTime(accessTime, so))
                    testAndAdd(accessable, accessablep, so, cur + getBreachTime(accessTime, so), path);
            }
            else 
                specialDeviceBreach(so, cur, path);
        specialDeviceBreach(obj, cur, path);
    }

    // currently support low to high breaching only
    //not connected/not visible -> connected(can access) -> breached(full control, usually requires taking the master node)
    private static void runConnection(Connection obj)//require at least accessble
    {
        int cur = -1;
        int curmin = -1;
        boolean breach;//not used currently. 
        String path = "";
        String mpath = "";
        if (resultTime.containsKey(obj))
        {
            cur = resultTime.get(obj);
            curmin = cur;
            breach = true;
            path = resultTimep.get(obj) + obj.name() + ", ";
            mpath = resultTimep.get(obj) + obj.name() + ", ";
        }
        if (accessable.containsKey(obj))
        {
            if (cur == -1)
            {
                cur = accessable.get(obj);
                breach = false;
                path = accessablep.get(obj) + obj.name() + ", ";
                curmin = cur;
                mpath = path;
            }
            else
            {
                curmin = accessable.get(obj);
                mpath = accessablep.get(obj) + obj.name() + ", ";
            }
            if (cur < curmin)
            {
                curmin = cur;
                mpath=path;
            }
        }
        else if (cur == -1)
            return;
        specialDeviceBreach(obj, cur, path);
        specialConnectionBreach(obj, cur, path);

        //No matter breached or access-only, try grant access to next level connections if allowed
        for (Connection c : obj.supports())
            if (existBreachTime(accessTime, c))
                testAndAdd(accessable, accessablep, c, curmin + getBreachTime(accessTime, c), mpath);

    }
    
    private static void runDevice(SHSimObject obj)
    {
        int cur = -1;
        int curmin = -1;
        boolean breach;//not used currently. 
        String path = "";
        String mpath = "";
        if (resultTime.containsKey(obj))
        {
            cur = resultTime.get(obj);
            curmin = cur;
            breach = true;
            path = resultTimep.get(obj) + obj.name() + ", ";
            mpath = resultTimep.get(obj) + obj.name() + ", ";
        }
        if (accessable.containsKey(obj))
        {
            if (cur == -1)
            {
                cur = accessable.get(obj);
                breach = false;
                path = accessablep.get(obj) + obj.name() + ", ";
                curmin = cur;
                mpath = path;
            }
            else
            {
                curmin = accessable.get(obj);
                mpath = accessablep.get(obj) + obj.name() + ", ";
            }
            if (cur < curmin)
            {
                curmin = cur;
                mpath=path;
            }
        }
        else if (cur == -1)
            return;
        specialDeviceBreach(obj, cur, path);
        specialConnectionBreach(obj, cur, path);
        //Assume controls() offer full control over anything but mainboard for input, everything for mainboard
        //ignores connection for now. Should require existence of connection, currently unsupported
        if (obj instanceof MainBoard)
        {
            for (Pair<SHSimObject, Connection> p : ((MainBoard)obj).controls())
                if (p.get1() instanceof Obstacle)
                {
                    ((ObstacleObject)(p.get1())).setLock(false);
                    resultTime.put(p.get1(), curmin);
                    resultTimep.put(p.get1(), mpath);
                    toRun.add(((Obstacle)(p.get1())).connects()[0]);
                    toRun.add(((Obstacle)(p.get1())).connects()[1]);
                }
                else
                    testAndAdd(accessable, accessablep, p.get1(), curmin + moveCost, mpath);
            for (Connection co : ((MainBoard)obj).connects())
                if (co.MasterNode() == obj)
                {
                    testAndAdd(accessable, accessablep, co, curmin + moveCost, mpath);
                    testAndAdd(resultTime, resultTimep, co, curmin + moveCost, mpath);
                }

        }
        else if (obj instanceof Input)
            for (Pair<SHSimObject, Connection> p : ((Input)obj).controls())
                if (p.get1() instanceof Obstacle)
                {
                    ((ObstacleObject)(p.get1())).setLock(false);
                    resultTime.put(p.get1(), curmin);
                    resultTimep.put(p.get1(), mpath);
                    toRun.add(((Obstacle)(p.get1())).connects()[0]);
                    toRun.add(((Obstacle)(p.get1())).connects()[1]);
                }


    }
    
    //will try breach/control device directly or indirectly via input or connection
    //return if normal breach procedure continues
    //space->accessable, input/mb->control/protectedControl, connection->connected, no direct
    private static void specialDeviceBreach(SHSimObject d, int time, String path) {
        //1.laser
        if (d instanceof Input && d.type().equals("microphone") && d.subType().equals("MEMS"))
            for (Pair<SHSimObject, Connection> p : ((Input)d).controls())
                if (p.get2() == null && existBreachTime(accessTime, p.get1()))
                    testAndAdd(accessable, accessablep, p.get1(), time + getBreachTime(accessTime, p.get1()), 
                        path + d.name() + ", ");
    }

    //No clearly weak connection in current setup...
    //connection->support, device(mb)->connected, space->accessable
    private static void specialConnectionBreach(SHSimObject co, int time, String path) {
        return;
    }

    public static void init()
    {
        moveCost = 5;
        maxCost = 24*60;
        obstacleBreachTime = new HashMap<String, Integer>();
        accessTime = new HashMap<String, Integer>();
        //connectionBreachTime = new HashMap<String, Integer>();
        deviceBreachTime = new HashMap<String, Integer>();
        //obstacleBreachTime.put("window", 40);
        //obstacleBreachTime.put("door", 60);
        obstacleBreachTime.put("enclosure", 60);
        accessTime.put("IEEE 802.11acWPA2 + AES", 20*60);//dictionary
        deviceBreachTime.put("Nest X Yale Lock", 30);//uninstall from inside
        accessTime.put("hubvoice control", 30);//laser
    }

    private static void testAndAdd(HashMap<SHSimObject, Integer> map, HashMap<SHSimObject, String> map2,
            SHSimObject obj, int time, String path)
    {
        //System.out.println(obj.name() + ":"+path);
        if (!map.containsKey(obj) || map.get(obj) > time)
        {
            map.put(obj, time);
            map2.put(obj, path);
            toRun.add(obj);
        }
    }

    /*
    static void testAndAdd(HashMap<SHSimObject, Integer> map, SHSimObject obj, int time)
    {
        if (!map.containsKey(obj) || map.get(obj) > time)
        {
            map.put(obj, time);
            toRun.add(obj);
        }
    }
    */

    static boolean existBreachTime(HashMap<String, Integer> map, SHSimObject obj)
    {
        if (!map.containsKey(obj.type() + obj.subType()))
            return map.containsKey(obj.type());
        return true;
    }

    static int getBreachTime(HashMap<String, Integer> map, SHSimObject obj)
    {
        if (!map.containsKey(obj.type() + obj.subType()))
            if (map.containsKey(obj.type()))
                return map.get(obj.type());
            else return -1;
        return map.get(obj.type() + obj.subType());
    }
}