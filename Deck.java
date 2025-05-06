import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Deck {
    private final Deque<Card> drawPile = new ArrayDeque<>();
    private final Deque<Card> discardPile = new ArrayDeque<>();

    public Deck() {
        List<Card> all = new ArrayList<>();
        for (Card.Suit s : Card.Suit.values())
            for (Card.Rank r : Card.Rank.values())
                all.add(new Card(r, s));
        Collections.shuffle(all);
        for (Card c: all) drawPile.add(c);
    }

    public List<Card> deal(int n) {
        if (drawPile.size() < n) reshuffleDiscardIntoDraw();
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < n; i++) hand.add(drawPile.poll());
        return hand;
    }

    public void start() {
        discardPile.add(drawPile.poll());
    }

    public Card topDiscard() {
        return discardPile.peekLast();
    }

    public void play(Card c) {
        discardPile.add(c);
    }

    public Card draw() {
        if (drawPile.isEmpty()) reshuffleDiscardIntoDraw();
        return drawPile.poll();
    }

    private void reshuffleDiscardIntoDraw() {
        if (discardPile.size() <= 1) throw new IllegalStateException("No cards to reshuffle");
        Card top = discardPile.pollLast();
        List<Card> rest = new ArrayList<>(discardPile);
        discardPile.clear();
        Collections.shuffle(rest);
        drawPile.addAll(rest);
        discardPile.add(top);
    }

    // Serialización a archivos
    public void savePiles(Path dir) throws IOException {
        writePile(drawPile, dir.resolve("draw.txt"));
        writePile(discardPile, dir.resolve("discard.txt"));
    }
    private void writePile(Collection<Card> pile, Path f) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Card c: pile) lines.add(c.toString());
        Files.write(f, lines);
    }
    public static Deck load(Path dir) throws IOException {
        Deck d = new Deck();
        d.drawPile.clear(); d.discardPile.clear();
        for (String line: Files.readAllLines(dir.resolve("draw.txt")))
            d.drawPile.add(Card.fromString(line));
        for (String line: Files.readAllLines(dir.resolve("discard.txt")))
            d.discardPile.add(Card.fromString(line));
        return d;
    }
}
