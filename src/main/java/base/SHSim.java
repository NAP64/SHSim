package base;
import java.util.*;

public class SHSim
{
    //interfaces are vision for attacker
    public interface SHSimObject
    {
        public String name();
        public String type();
        public String subType();
    }
    
    //A Obstacle is a locakable physical path (doors, windows, etc) that connects two spaces.
    public interface Obstacle extends SHSimObject
    {
        public boolean isLocked();
        public Space [] connects();
    }

    public interface Space extends SHSimObject
    {
        public ArrayList<Obstacle> paths();
        public ArrayList<Space> connects();
        public ArrayList<SHSimObject> contains();//devices
        public ArrayList<Input> monitoredBy();
        public ArrayList<SHSimObject> accessable();//connections, indirectly to devices
    }

    public interface MainBoard extends SHSimObject
    {
        public ArrayList<Pair<SHSimObject, Connection>> controls();
        public ArrayList<Triplet<SHSimObject, Connection, String>> protectedControls();
        public ArrayList<String> volatileInfo();
        public ArrayList<String> staticInfo();
        public ArrayList<Connection> connects();
    }

    public interface Output extends SHSimObject
    {}

    public interface Input extends SHSimObject
    {
        public ArrayList<Pair<SHSimObject, Connection>> controls();
    }

    public interface SimpleDevice extends MainBoard, Output, Input{}

    public interface Connection extends SHSimObject
    {
        public ArrayList<MainBoard> connects();
        public ArrayList<Connection> supports();
        public MainBoard MasterNode();
    }

    public interface AbstractConnection extends Connection
    {
        public ArrayList<Connection> depends();
    }

    //simple object implementations
    //user can define more complex ones 
    public class ObstacleObject implements Obstacle
    {
        String name;
        String type;
        String subtype;
        boolean locked;
        Space [] connects;

        //Simple controled by
        public ObstacleObject(String n, String t, String st, Space a, Space b)
        {
            locked = true;
            name = n;
            type = t;
            subtype = st;
            connects = new Space[2];
            connects[0] = a;
            connects[1] = b;
        }

        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        
        public boolean isLocked()
        {   return locked;}
        public Space [] connects()
        {   return connects;}

        //Simple mechanism, more complex ones can be done instead
        public void setLock(boolean b)
        {   locked = b;}
    }

    public class SpaceObject implements Space
    {
        String name;
        String type;
        String subtype;
        ArrayList<Obstacle> paths;
        ArrayList<Space> connects;
        ArrayList<SHSimObject> contains;
        ArrayList<Input> monitoredBy;
        ArrayList<SHSimObject> accessable;

        public SpaceObject (String n, String t, String st)
        {
            name = n;
            type = t;
            subtype = st;
            paths = new ArrayList<Obstacle>();
            connects = new ArrayList<Space>();
            contains = new ArrayList<SHSimObject>();
            monitoredBy = new ArrayList<Input>();
            accessable = new ArrayList<SHSimObject>();
        }
        public void addObstacle(Obstacle o)
        {   paths.add(o);}
        public void addConnectedSpace(Space s)
        {   connects.add(s);}
        public void addContained(SHSimObject o)
        {   contains.add(o);}
        public void addMonitor(Input i)
        {   monitoredBy.add(i);}
        public void addAccessable(SHSimObject o)
        {   accessable.add(o);}

        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<Obstacle> paths()
        {   return paths;}
        public ArrayList<Space> connects()
        {   return connects;}
        public ArrayList<SHSimObject> contains()
        {   return contains;}
        public ArrayList<Input> monitoredBy()
        {   return monitoredBy;}
        public ArrayList<SHSimObject> accessable()
        {   return accessable;}
    }

    public class MainBoardObject implements MainBoard
    {
        String name;
        String type;
        String subtype;
        ArrayList<Pair<SHSimObject, Connection>> controls;
        ArrayList<Triplet<SHSimObject, Connection, String>> protectedControls;
        ArrayList<String> volatileInfo;
        ArrayList<String> staticInfo;
        ArrayList<Connection> connects;

        public MainBoardObject (String n, String t, String st)
        {
            name = n;
            type = t;
            subtype = st;
            controls = new ArrayList<Pair<SHSimObject, Connection>>();
            protectedControls = new ArrayList<Triplet<SHSimObject, Connection, String>>();
            connects = new ArrayList<Connection>();
            volatileInfo = new ArrayList<String>();
            staticInfo = new ArrayList<String>();
        }

        public void addControl(SHSimObject o, Connection c)
        {   controls.add(new Pair<SHSimObject, Connection>(o, c));}
        public void addProtectedControl(SHSimObject o, Connection c, String s)
        {   protectedControls.add(new Triplet<SHSimObject, Connection, String>(o, c, s));}
        public void addVolatile(String v)
        {   volatileInfo.add(v);}
        public void addStatic(String s)
        {   staticInfo.add(s);}
        public void addConnects(Connection c)
        {   connects.add(c);}

        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<Pair<SHSimObject, Connection>> controls()
        {   return controls;}
        public ArrayList<Triplet<SHSimObject, Connection, String>> protectedControls()
        {   return protectedControls;}
        public ArrayList<String> volatileInfo()
        {   return volatileInfo;}
        public ArrayList<String> staticInfo()
        {   return staticInfo;}
        public ArrayList<Connection> connects()
        {   return connects;}
    }

    public class OutputObject implements Output
    {
        String name;
        String type;
        String subtype;
        public OutputObject (String n, String t, String st)
        {
            name = n;
            type = t;
            subtype = st;
        }
        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
    }

    public class InputObject implements Input
    {
        String name;
        String type;
        String subtype;
        ArrayList<Pair<SHSimObject, Connection>> controls;
        public InputObject (String n, String t, String st)
        {
            name = n;
            type = t;
            subtype = st;
            controls = new ArrayList<Pair<SHSimObject, Connection>>();
        }
        public void addControl(SHSimObject o, Connection c)
        {   controls.add(new Pair<SHSimObject, Connection>(o, c));}
        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<Pair<SHSimObject, Connection>> controls()
        {   return controls;}
    }

    //such that can be viewed as an output/input device
    public class SimpleDeviceObject implements SimpleDevice
    {
        String name;
        String type;
        String subtype;
        ArrayList<Pair<SHSimObject, Connection>> controls;
        ArrayList<Triplet<SHSimObject, Connection, String>> protectedControls;
        ArrayList<String> volatileInfo;
        ArrayList<String> staticInfo;
        ArrayList<Connection> connects;

        public SimpleDeviceObject (String n, String t, String st)
        {
            name = n;
            type = t;
            subtype = st;
            controls = new ArrayList<Pair<SHSimObject, Connection>>();
            protectedControls = new ArrayList<Triplet<SHSimObject, Connection, String>>();
            connects = new ArrayList<Connection>();
            volatileInfo = new ArrayList<String>();
            staticInfo = new ArrayList<String>();
        }

        public void addControl(SHSimObject o, Connection c)
        {   controls.add(new Pair<SHSimObject, Connection>(o, c));}
        public void addProtectedControl(SHSimObject o, Connection c, String s)
        {   protectedControls.add(new Triplet<SHSimObject, Connection, String>(o, c, s));}
        public void addVolatile(String v)
        {   volatileInfo.add(v);}
        public void addStatic(String s)
        {   staticInfo.add(s);}
        public void addConnects(Connection c)
        {   connects.add(c);}

        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<Pair<SHSimObject, Connection>> controls()
        {   return controls;}
        public ArrayList<Triplet<SHSimObject, Connection, String>> protectedControls()
        {   return protectedControls;}
        public ArrayList<String> volatileInfo()
        {   return volatileInfo;}
        public ArrayList<String> staticInfo()
        {   return staticInfo;}
        public ArrayList<Connection> connects()
        {   return connects;}
    }

    public class PhysicalConnectionObject implements Connection
    {
        String name;
        String type;
        String subtype;
        ArrayList<MainBoard> connects;
        MainBoard master;
        ArrayList<Connection> supports;
        public PhysicalConnectionObject (String n, String t, String st, MainBoard o)
        {
            name = n;
            type = t;
            subtype = st;
            master = o;
            connects = new ArrayList<MainBoard>();
            supports = new ArrayList<Connection>();
        }
        public void addConnects(MainBoard c)
        {   connects.add(c);}
        public void setMaster(MainBoard m)
        {   master = m;}
        public void addSupports(Connection c)
        {   supports.add(c);}
        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<MainBoard> connects()
        {   return connects;}
        public ArrayList<Connection> supports()
        {   return supports;}
        public MainBoard MasterNode()
        {   return master;}
    }

    public class AbstractConnectionObject implements AbstractConnection
    {
        String name;
        String type;
        String subtype;
        ArrayList<MainBoard> connects;
        MainBoard master;
        ArrayList<Connection> depends;
        ArrayList<Connection> supports;
        public AbstractConnectionObject (String n, String t, String st, MainBoard o)
        {
            name = n;
            type = t;
            subtype = st;
            master = o;
            connects = new ArrayList<MainBoard>();
            depends = new ArrayList<Connection>();
            supports = new ArrayList<Connection>();
        }
        public void addConnects(MainBoard c)
        {   connects.add(c);}
        public void addDepends(Connection c)
        {   depends.add(c);}
        public void addSupports(Connection c)
        {   supports.add(c);}
        public void setMaster(MainBoard m)
        {   master = m;}
        public String name()
        {   return name;}
        public String type()
        {   return type;}
        public String subType()
        {   return subtype;}
        public ArrayList<MainBoard> connects()
        {   return connects;}
        public ArrayList<Connection> depends()
        {   return depends;}
        public ArrayList<Connection> supports()
        {   return supports;}
        public MainBoard MasterNode()
        {   return master;}
    }


    public class Pair<T1, T2>
    {
        T1 a;
        T2 b;

        public Pair(T1 a1, T2 b1)
        {
            a = a1;
            b = b1;
        }

        public T1 get1()
        {   return a;}

        public T2 get2()
        {   return b;}

        public int hashCode()
        {
            return a.hashCode() + b.hashCode();
        }

        public boolean equals(Object o)
        {
            if (o.getClass() == Pair.class)
                return a.equals(((Pair<Object, Object>)o).get1()) && b.equals(((Pair<Object, Object>)o).get2());
            return false;
        }
    }

    public class Triplet<T1, T2, T3>
    {
        T1 a;
        T2 b;
        T3 c;

        public Triplet(T1 a1, T2 b1, T3 c1)
        {
            a = a1;
            b = b1;
            c = c1;
        }

        public T1 get1()
        {   return a;}

        public T2 get2()
        {   return b;}
        
        public T3 get3()
        {   return c;}
        
        public int hashCode()
        {
            return a.hashCode() + b.hashCode() + c.hashCode();
        }

        public boolean equals(Object o)
        {
            if (o.getClass() == Triplet.class)
                return a.equals(((Triplet<Object, Object, Object>)o).get1()) 
                    && b.equals(((Triplet<Object, Object, Object>)o).get2())
                    && c.equals(((Triplet<Object, Object, Object>)o).get3());
            return false;
        }
    }

    public SHSim()
    {}
}