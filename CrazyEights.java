
/*
By: Jennifer Vicentes
Purpose: This class is the main entry point for the Crazy Eights game.
It handles command-line arguments for initializing the game, adding/removing users, starting the game, and performing game actions. 

All the comments I wrote were put for me to keep track while developing, they are not AI generated. 
*/
import java.util.*;

public class CrazyEights {
    public static void main(String[] args) {
        try {
            // Parse command-line arguments into a map of flags and their values
            Map<String, String> flags = parseArgs(args);

            // Initialize the game directory if the "--init" flag is provided
            if (flags.containsKey("init")) {
                GameManager.init(flags.get("game"));
                return;
            }

            // Create a GameManager instance for the specified game directory
            GameManager gm = new GameManager(flags.get("game"));

            // Handle different command-line flags and execute corresponding actions
            if (flags.containsKey("add-user")) {
                gm.addUser(flags.get("add-user")); // Add a new user
            } else if (flags.containsKey("remove-user")) {
                gm.removeUser(flags.get("remove-user")); // Remove an existing user
            } else if (flags.containsKey("start")) {
                gm.start(); // Start the game
            } else if (flags.containsKey("order")) {
                gm.order(flags.get("user")); // Display the turn order for a user
            } else if (flags.containsKey("play")) {
                gm.play(flags.get("play"), flags.get("user")); // Play a card
            } else if (flags.containsKey("cards")) {
                // Shows the hands held by userx trying to authenticate as usery. It will also show the top of the discard pile.
                String[] cs = flags.get("cards").split(":");
                gm.cards(cs[0], cs[0], flags.get("user"));
            } else if (flags.containsKey("draw")) {
                gm.draw(flags.get("user")); // Draw a card
            } else if (flags.containsKey("pass")) {
                gm.pass(flags.get("user")); // Pass the turn
            } else {
                // Throw an exception if an invalid command is provided
                throw new IllegalArgumentException("Invalid command");
            }
        } catch (Exception e) {
            // Print the error message and exit with a non-zero status code
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    // Parses command-line arguments into a map of flags and their values
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--init": 
                    m.put("init", ""); // Initialize the game
                    break;
                case "--add-user": 
                    m.put("add-user", args[++i]); // Add a new user
                    break;
                case "--remove-user": 
                    m.put("remove-user", args[++i]); // Remove an existing user
                    break;
                case "--start": 
                    m.put("start", ""); // Start the game
                    break;
                case "--order": 
                    m.put("order", ""); // Display the turn order
                    break;
                case "--play": 
                    m.put("play", args[++i]); // Play a card
                    break;
                case "--cards": 
                    m.put("cards", args[++i]); // Show cards held by a user
                    break;
                case "--draw": 
                    m.put("draw", ""); // Draw a card
                    break;
                case "--pass": 
                    m.put("pass", ""); // Pass the turn
                    break;
                case "--user": 
                    m.put("user", args[++i]); // Specify the user
                    break;
                case "--game": 
                    m.put("game", args[++i]); // Specify the game directory
                    break;
                default:
                    // Throw an exception for unknown flags
                    throw new IllegalArgumentException("Unknown flag: " + args[i]);
            }
        }

        // Ensure the "--game" flag is provided
        if (!m.containsKey("game")) throw new IllegalArgumentException("Missing --game <name>");
        return m;
    }
}
