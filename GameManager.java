/*
By: Jennifer Vicentes
Purpose: This class manages the game state, including player management, game state transitions, and game actions.
It handles user authentication, game initialization, and the main game loop. 
It provides methods for adding/removing users, starting the game, managing turns, and performing game actions like drawing cards and playing cards.
It also handles the loading and saving of game state to files.

All the comments I wrote were put for me to keep track while developing, they are not AI generated.
*/ 

import java.nio.file.*;
import java.util.*;
import java.io.*;

public class GameManager {
    private final Path gameDir; // Path to the game directory
    private final AuthManager auth; // Authentication manager for user validation
    private Deck deck; // The deck of cards used in the game
    private final List<Player> players = new ArrayList<>(); // List of players in the game
    private int currentIdx = 0; // Index of the current player
    private boolean hasDrawn = false; // Flag to track if the current player has drawn a card
    private final Path turnFile; // File to store the current turn information
    private final Path stateFile; // File to store the game state

    // Constructor to initialize the GameManager with the game directory
    public GameManager(String name) throws IOException {
        gameDir = Paths.get(name);
        if (!Files.isDirectory(gameDir))
            throw new IllegalArgumentException("Game does not exist");
        auth = new AuthManager(gameDir);
        turnFile = gameDir.resolve("turn.txt");
        stateFile = gameDir.resolve("state.txt");
    }

    // Enum to represent the state of the game
    private enum State { NOT_STARTED, IN_PROGRESS, FINISHED }

    // Writes the current game state to the state file
    private void writeState(State s) throws IOException {
        Files.write(stateFile, Collections.singletonList(s.name()));
    }

    // Reads the current game state from the state file
    private State readState() throws IOException {
        if (!Files.exists(stateFile)) return State.NOT_STARTED;
        String txt = Files.readAllLines(stateFile).get(0).trim();
        return State.valueOf(txt);
    }

    // Ensures that user management actions can only be performed when the game is not in progress
    private void ensureCanManageUsers() throws IOException {
        State s = readState();
        if (s == State.IN_PROGRESS)
            throw new IllegalStateException("You cannot modify users while the game is in progress");
    }

    // Initializes a new game directory and sets up the admin user
    public static void init(String game) throws IOException {
        Path d = Paths.get(game);
        if (Files.exists(d)) throw new IllegalArgumentException("Game already exists");
        Files.createDirectory(d);
        Files.createFile(d.resolve("users.txt"));
        Files.write(d.resolve("state.txt"),
            Collections.singletonList(State.NOT_STARTED.name()));
        AuthManager a = new AuthManager(d);
        a.initAdmin(System.console());
    }

    // Adds a new user to the game
    public void addUser(String u) throws IOException {
        auth.requireAdmin(System.console());
        ensureCanManageUsers();
        auth.addUser(u, System.console());
    }

    // Removes a user from the game
    public void removeUser(String u) throws IOException {
        auth.requireAdmin(System.console());
        ensureCanManageUsers();
        auth.removeUser(u);
        Files.deleteIfExists(gameDir.resolve(u + ".txt"));
    }

    // Starts the game by initializing the deck, dealing cards, and setting the initial state
    public void start() throws IOException {
        auth.requireAdmin(System.console());
        writeState(State.IN_PROGRESS);
        loadPlayers();
        if (players.size() < 2) throw new IllegalStateException("Required at least 2 players");
        deck = new Deck();
        for (Player p: players)
            p.dealInitial(deck.deal(5));
        deck.start();
        deck.savePiles(gameDir);
        currentIdx = 0;
        hasDrawn   = false;
        saveTurn();
    }

    // Loads the list of players from the user data
    private void loadPlayers() throws IOException {
        players.clear();
        for (String u: auth.getUsers().keySet())
            if (!u.equals("admin"))
                players.add(new Player(u, gameDir));
    }

    // Saves the current turn information to the turn file
    private void saveTurn() throws IOException {
        String line = currentIdx + "," + hasDrawn;
        Files.write(turnFile, Collections.singletonList(line));
    }

    // Loads the current turn information from the turn file
    private void loadTurn() throws IOException {
        if (Files.exists(turnFile)) {
            String line = Files.readAllLines(turnFile).get(0);
            String[] parts = line.split(",", 2);
            currentIdx = Integer.parseInt(parts[0]);
            if (parts.length == 2) {
                hasDrawn = Boolean.parseBoolean(parts[1]);
            } else {
                hasDrawn = false;
                saveTurn();
            }
        } else {
            currentIdx = 0;
            hasDrawn   = false;
        }
    }

    // Loads the game state, including the deck, players, and turn information
    private void loadState() throws IOException {
        deck = Deck.load(gameDir);
        loadPlayers();
        loadTurn();
    }

    // Displays the turn order for the specified user
    public void order(String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();  // loads players AND currentIdx from turn.txt
    
        System.out.println("Turn order (next first):");
        for (int offset = 0; offset < players.size(); offset++) {
            String name = players.get((currentIdx + offset) % players.size()).getName();
            System.out.println("  " + name);
        }
    }    

    // Allows a user to play a card
    public void play(String cardStr, String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();
        Player p = players.get(currentIdx);
        if (!p.getName().equals(user))
            throw new SecurityException("Not your turn");
        Card toPlay = Card.fromString(cardStr);
        if (!p.getHand().contains(toPlay))
            throw new IllegalArgumentException("You don't have that card");
        if (!toPlay.matches(deck.topDiscard()))
            throw new IllegalArgumentException("You can't play that card");
        p.play(toPlay, deck);
        deck.savePiles(gameDir);

        if (p.hasWon()) {
            System.out.println("¡" + user + " won!");
            writeState(State.FINISHED);
            return;
        }

        int next = (currentIdx + 1) % players.size();
        currentIdx = next;
        hasDrawn   = false;
        saveTurn();
    }

    // Displays the cards held by a target user and the top of the discard pile
    public void cards(String target, String who, String as) throws IOException {
        auth.requireUser(as, System.console());
        loadState();
        if (!as.equals("admin") && !as.equals(who))
            throw new SecurityException("Without permission");
        Player p = players.stream()
                .filter(x->x.getName().equals(target))
                .findFirst().orElseThrow(()->new IllegalArgumentException("User does not exist"));
        System.out.println("Hand of " + target + ": " + p.getHand());
        System.out.println("Top Discard: " + deck.topDiscard());
        saveTurn();
    }

    // Allows a user to draw a card
    public void draw(String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();
        Player p = players.get(currentIdx);
        if (!p.getName().equals(user)) throw new SecurityException("Not your turn");

        if (hasDrawn) {
            throw new IllegalStateException("You have already drawn this turn");
        }
        Card c = p.draw(deck);
        deck.savePiles(gameDir);
        hasDrawn = true;
        saveTurn();   
        System.out.println("Drawn card : " + c);
        System.out.println("Hand: " + p.getHand());
        System.out.println("Top of Discard: " + deck.topDiscard());
    }

    // Allows a user to pass their turn
    public void pass(String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();
        Player p = players.get(currentIdx);
        if (!p.getName().equals(user)) throw new SecurityException("Not your turn");
        if (!hasDrawn) throw new IllegalStateException("You must draw before passing");
  
        System.out.println("Top of Discard after passing: " + deck.topDiscard());
        int next = (currentIdx + 1) % players.size();
        currentIdx = next;
        hasDrawn   = false;
        saveTurn();
        System.out.println("Next up: " + players.get(currentIdx).getName()); 
    }
}
