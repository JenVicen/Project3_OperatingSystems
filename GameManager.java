import java.nio.file.*;
import java.util.*;
import java.io.*;

public class GameManager {
    private final Path gameDir;
    private final AuthManager auth;
    private Deck deck;
    private final List<Player> players = new ArrayList<>();
    private int currentIdx = 0;
    private boolean hasDrawn = false;
    private final Path turnFile;
    private final Path stateFile;


    public GameManager(String name) throws IOException {
        gameDir = Paths.get(name);
        if (!Files.isDirectory(gameDir))
            throw new IllegalArgumentException("Game does not exist");
        auth = new AuthManager(gameDir);
        turnFile = gameDir.resolve("turn.txt");
        stateFile = gameDir.resolve("state.txt");
    }

    private enum State { NOT_STARTED, IN_PROGRESS, FINISHED }

    private void writeState(State s) throws IOException {
        Files.write(stateFile, Collections.singletonList(s.name()));
    }

    private State readState() throws IOException {
        if (!Files.exists(stateFile)) return State.NOT_STARTED;
        String txt = Files.readAllLines(stateFile).get(0).trim();
        return State.valueOf(txt);
    }

    private void ensureCanManageUsers() throws IOException {
        State s = readState();
        if (s == State.IN_PROGRESS)
            throw new IllegalStateException("You cannot modify users while the game is in progress");
    }

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

    public void addUser(String u) throws IOException {
        auth.requireAdmin(System.console());
        ensureCanManageUsers();
        auth.addUser(u, System.console());
    }
    public void removeUser(String u) throws IOException {
        auth.requireAdmin(System.console());
        ensureCanManageUsers();
        auth.removeUser(u);
        Files.deleteIfExists(gameDir.resolve(u + ".txt"));
    }

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

    private void loadPlayers() throws IOException {
        players.clear();
        for (String u: auth.getUsers().keySet())
            if (!u.equals("admin"))
                players.add(new Player(u, gameDir));
    }

    private void saveTurn() throws IOException {
        String line = currentIdx + "," + hasDrawn;
        Files.write(turnFile, Collections.singletonList(line));
    }

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

    private void loadState() throws IOException {
        deck = Deck.load(gameDir);
        loadPlayers();
        loadTurn();
    }

    public void order(String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(user)) {
                currentIdx = i;
                saveTurn();
                break;
            }
        }
        System.out.println("Turn order:");
        for (int k = 0; k < players.size(); k++) {
            System.out.println("  " +
                players.get((currentIdx + k) % players.size()).getName());
        }
    }

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

    public void pass(String user) throws IOException {
        auth.requireUser(user, System.console());
        loadState();
        Player p = players.get(currentIdx);
        if (!p.getName().equals(user)) throw new SecurityException("Not your turn");
        if (!hasDrawn) throw new IllegalStateException("You must draw before passing");
        Card top = deck.topDiscard();
        boolean existeJugable = p.getHand().stream()
            .anyMatch(card -> card.matches(top));
        if (existeJugable) {
            throw new IllegalStateException(
                "You still have playable cards (" + top + "); cannot pass");
        }
        p.pass(deck);
        System.out.println("Top of Discard after passing: " + deck.topDiscard());
        int next = (currentIdx + 1) % players.size();
        currentIdx = next;
        hasDrawn   = false;
        saveTurn();
        System.out.println("Next up: " + players.get(currentIdx).getName()); 
    }
}
