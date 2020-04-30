import base.*;
import base.SHSim.*;

public class Sim
{
    static SHSim base;
    public static void main(String[] args)
    {
        base = new SHSim();
        //Spaces
        SpaceObject outside = base.new SpaceObject("outside", "outside", "");
        SpaceObject lr = base.new SpaceObject("living room", "room", "");
        ObstacleObject entrance = base.new ObstacleObject("entrance", "door", "", outside, lr);
        outside.addObstacle(entrance);
        lr.addObstacle(entrance);
        ObstacleObject pd = base.new ObstacleObject("patio door", "door", "", outside, lr);
        outside.addObstacle(pd);
        lr.addObstacle(pd);
        ObstacleObject pw = base.new ObstacleObject("patio window", "window", "large", outside, lr);
        outside.addObstacle(pw);
        lr.addObstacle(pw);
        SpaceObject b1 = base.new SpaceObject("bedroom 1", "room", "");
        SpaceObject b2 = base.new SpaceObject("bedroom 2", "room", "");
        ObstacleObject d1 = base.new ObstacleObject("bedroom 1 door", "door", "", lr, b1);
        lr.addObstacle(d1);
        b1.addObstacle(d1);
        ObstacleObject w1 = base.new ObstacleObject("bedroom 1 window", "window", "median", outside, b1);
        outside.addObstacle(w1);
        b1.addObstacle(w1);
        ObstacleObject d2 = base.new ObstacleObject("bedroom 2 door", "door", "", lr, b2);
        lr.addObstacle(d2);
        b2.addObstacle(d2);
        ObstacleObject w2 = base.new ObstacleObject("bedroom 2 window", "window", "median", outside, b2);
        outside.addObstacle(w2);
        b2.addObstacle(w2);
        d1.setLock(false);
        d2.setLock(false);

        //devices
        //nest router
        MainBoardObject ghlr = addGoogleHomeObject(lr, new SpaceObject [] {outside, lr}, new SpaceObject [] {outside, lr, b2}, "Nest Wifi", "living room");
        //nest iq camera indoor
        MainBoardObject gh1 = addGoogleHomeObject(b1, new SpaceObject [] {outside, b1}, new SpaceObject [] {outside, b1, b2}, "Nest Cam IQ Indoor", "bedroom 1");
        InputObject cam = base.new InputObject("Nest Cam IQ Indoor camera", "camera", "visible light");
        cam.addControl(gh1, null);
        for (Obstacle o : b1.paths())
            if (o.name().equals("Nest Cam IQ Indoor bedroom 1 enclosure"))
                for (int i = 0; i < 2; i++)
                    if (o.connects()[i].name().equals("Nest Cam IQ Indoor bedroom 1"))
                        ((SpaceObject)o.connects()[i]).addContained(cam);
        b1.addMonitor(cam);

        MainBoardObject gh2 = addGoogleHomeObject(b2, new SpaceObject [] {outside, b2}, new SpaceObject [] {outside, b2, b1, lr}, "Google Home", "bedroom 2");
        //SimpleDeviceObject router = base.new SimpleDeviceObject("WiFi router", "IEEE 802.11ac router", "model");
        lr.addContained(ghlr);
        SimpleDeviceObject doorlock = base.new SimpleDeviceObject("Nest X Yale Lock", "Nest X Yale Lock", "");
        lr.addContained(doorlock);
        outside.addAccessable(doorlock);
        doorlock.addControl(entrance, null);
        SimpleDeviceObject nestc = base.new SimpleDeviceObject("Nest Connect", "Nest hub", "");
        lr.addContained(nestc);
        PhysicalConnectionObject wifi = base.new PhysicalConnectionObject("wifi", "IEEE 802.11ac", "WPA2 + AES", ghlr);
        lr.addAccessable((SHSim.SHSimObject)wifi);
        outside.addAccessable((SHSim.SHSimObject)wifi);
        PhysicalConnectionObject weave_2_4g = base.new PhysicalConnectionObject("Weave2.4G", "IEEE 802.15.4", "Weave", nestc);
        lr.addAccessable((SHSim.SHSimObject)weave_2_4g);
        outside.addAccessable((SHSim.SHSimObject)weave_2_4g);
        addConnectsBetween(wifi, ghlr);
        addConnectsBetween(wifi, gh1);
        addConnectsBetween(wifi, gh2);
        addConnectsBetween(wifi, nestc);
        addConnectsBetween(weave_2_4g, nestc);
        addConnectsBetween(weave_2_4g, doorlock);
        MainBoardObject server = base.new MainBoardObject("internet", "server", "external");
        AbstractConnectionObject internet = base.new AbstractConnectionObject("internet", "internet", "", server);
        addConnectsBetween(internet, ghlr);
        addConnectsBetween(internet, gh1);
        addConnectsBetween(internet, gh2);
        addConnectsBetween(internet, nestc);
        addConnectsBetween(internet, server);
        //addConnectsBetween(internet, router);
        addSupportDepend(wifi, internet);
        AbstractConnectionObject nestService = base.new AbstractConnectionObject("Nest Service", "Nest Service", "", server);
        addConnectsBetween(nestService, ghlr);
        addConnectsBetween(nestService, gh1);
        addConnectsBetween(nestService, gh2);
        addConnectsBetween(nestService, nestc);
        addConnectsBetween(nestService, server);
        addConnectsBetween(nestService, doorlock);
        addSupportDepend(internet, nestService);
        addSupportDepend(weave_2_4g, nestService);
        ghlr.addControl(doorlock, nestService);
        AbstractConnectionObject nestAware = base.new AbstractConnectionObject("Nest Aware", "Nest Aware", "", server);
        addConnectsBetween(nestAware, gh1);
        addSupportDepend(internet, nestAware);

        Attacker.simAttack(internet, outside);
        System.out.println("Done");
    }

    public static void addConnectsBetween(PhysicalConnectionObject c, MainBoardObject m)
    {
        c.addConnects(m);
        m.addConnects(c);
    }

    public static void addConnectsBetween(AbstractConnectionObject c, MainBoardObject m)
    {
        c.addConnects(m);
        m.addConnects(c);
    }

    public static void addConnectsBetween(PhysicalConnectionObject c, SimpleDeviceObject m)
    {
        c.addConnects(m);
        m.addConnects(c);
    }

    public static void addConnectsBetween(AbstractConnectionObject c, SimpleDeviceObject m)
    {
        c.addConnects(m);
        m.addConnects(c);
    }

    public static void addSupportDepend(PhysicalConnectionObject supporter, AbstractConnectionObject dependent)
    {
        dependent.addDepends(supporter);
        supporter.addSupports(dependent);
    }
    public static void addSupportDepend(AbstractConnectionObject supporter, AbstractConnectionObject dependent)
    {
        dependent.addDepends(supporter);
        supporter.addSupports(dependent);
    }

    public static MainBoardObject addGoogleHomeObject(SpaceObject location, SpaceObject[] visible, SpaceObject[] audible, String name, String postfix)
    {
        SpaceObject gh = base.new SpaceObject(name + " " + postfix, name, "");
        ObstacleObject enc = base.new ObstacleObject(name + " " + postfix + " enclosure", "enclosure", "", location, gh);
        gh.addObstacle(enc);
        location.addObstacle(enc);
        MainBoardObject mb = base.new MainBoardObject(name + " " + postfix, "hub", "voice control");
        mb.addVolatile("account credential");
        mb.addStatic("account info");
        mb.addStatic("wifi password");
        OutputObject speaker = base.new OutputObject(name + " " + postfix + " speaker", "speaker", "moving-coil");
        OutputObject led = base.new OutputObject(name + " " + postfix + " led", "led", "");
        InputObject button = base.new InputObject(name + " " + postfix + " button", "button", "capacitance");
        InputObject microphone = base.new InputObject(name + " " + postfix + " microphone", "microphone", "MEMS");
        button.addControl(mb, null);
        microphone.addControl(mb, null);
        gh.addContained(mb);
        gh.addContained(speaker);
        gh.addContained(led);
        gh.addContained(button);
        gh.addContained(microphone);
        mb.addControl(speaker, null);
        mb.addControl(led, null);
        for (SpaceObject s : audible)
        {
            s.addAccessable(speaker);
            s.addAccessable(microphone);
        }
        for (SpaceObject s : visible)
        {
            s.addAccessable(led);
            s.addAccessable(microphone);
        }
        location.addAccessable(button);
        return mb;
    }
}