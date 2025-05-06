
import java.util.*;

public class CrazyEights {
    public static void main(String[] args) {
        try {
            Map<String,String> flags = parseArgs(args);
            if (flags.containsKey("init")) {
                GameManager.init(flags.get("game"));
                return;
            }
            GameManager gm = new GameManager(flags.get("game"));
            if (flags.containsKey("add-user")) {
                gm.addUser(flags.get("add-user"));
            } else if (flags.containsKey("remove-user")) {
                gm.removeUser(flags.get("remove-user"));
            } else if (flags.containsKey("start")) {
                gm.start();
            } else if (flags.containsKey("order")) {
                gm.order(flags.get("user"));
            } else if (flags.containsKey("play")) {
                gm.play(flags.get("play"), flags.get("user"));
            } else if (flags.containsKey("cards")) {
                String[] cs = flags.get("cards").split(":");
                gm.cards(cs[0], cs[0], flags.get("user"));
            } else if (flags.containsKey("draw")) {
                gm.draw(flags.get("user"));
            } else if (flags.containsKey("pass")) {
                gm.pass(flags.get("user"));
            } else {
                throw new IllegalArgumentException("Invalid command");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Map<String,String> parseArgs(String[] args) {
        Map<String,String> m = new HashMap<>();
        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "--init": m.put("init",""); break;
                case "--add-user": m.put("add-user", args[++i]); break;
                case "--remove-user": m.put("remove-user", args[++i]); break;
                case "--start": m.put("start",""); break;
                case "--order": m.put("order",""); break;
                case "--play": m.put("play", args[++i]); break;
                case "--cards": m.put("cards", args[++i]); break;
                case "--draw": m.put("draw",""); break;
                case "--pass": m.put("pass",""); break;
                case "--user": m.put("user", args[++i]); break;
                case "--game": m.put("game", args[++i]); break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + args[i]);
            }
        }
        if (!m.containsKey("game")) throw new IllegalArgumentException("Missing --game <name>");
        return m;
    }
}
