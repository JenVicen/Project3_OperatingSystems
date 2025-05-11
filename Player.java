import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Player {
    private final String name;
    private final Path handFile;
    private final List<Card> hand = new ArrayList<>();

    public Player(String name, Path dir) throws IOException {
        if ("admin".equalsIgnoreCase(name)) throw new IllegalArgumentException("'admin' not allowed");
        this.name = name;
        this.handFile = dir.resolve(name + ".txt");
        if (Files.exists(handFile)) loadHand();
    }

    public String getName() { return name; }
    public List<Card> getHand() { return Collections.unmodifiableList(hand); }

    public void dealInitial(List<Card> cards) {
        hand.clear(); hand.addAll(cards); saveHand();
    }

    public void saveHand() {
        try {
            List<String> out = new ArrayList<>();
            for (Card c: hand) out.add(c.toString());
            Files.write(handFile, out);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private void loadHand() throws IOException {
        hand.clear();
        for (String l: Files.readAllLines(handFile)) hand.add(Card.fromString(l.trim()));
    }

    public void play(Card c, Deck deck) {
        if (!hand.contains(c)) throw new IllegalArgumentException("You don't have that card");
        if (!c.matches(deck.topDiscard())) throw new IllegalArgumentException("You can't discard that card");
        hand.remove(c); deck.play(c);
        saveHand();
    }

    public Card draw(Deck deck) {
        Card c = deck.draw();
        hand.add(c);
        saveHand();
        return c;
    }

    public boolean hasWon() {
        return hand.isEmpty();
    }

    public int score() {
        return hand.stream().mapToInt(c -> {
            switch (c.getRank()) {
                case J: case Q: case K: return 10;
                default: return c.getRank().ordinal()+1;
            }
        }).sum();
    }
}
