/*
By: Jennifer Vicentes
Purpose: This class represents a deck of playing cards for the Crazy Eights game.
It provides functionality to shuffle, deal cards, manage draw and discard piles,
and save/load the state of the deck to/from files.
It also handles reshuffling the discard pile into the draw pile when needed.

All the comments I wrote were put for me to keep track while developing, they are not AI generated. 
*/
import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Deck {
    // Deque to represent the draw pile of cards
    private final Deque<Card> drawPile = new ArrayDeque<>();
    // Deque to represent the discard pile of cards
    private final Deque<Card> discardPile = new ArrayDeque<>();

    // Constructor to initialize the deck with all cards shuffled
    public Deck() {
        List<Card> all = new ArrayList<>();
        // Add all cards (each combination of rank and suit) to the list
        for (Card.Suit s : Card.Suit.values())
            for (Card.Rank r : Card.Rank.values())
                all.add(new Card(r, s));
        Collections.shuffle(all); // Shuffle the cards
        for (Card c: all) drawPile.add(c); // Add shuffled cards to the draw pile
    }

    // Deals a specified number of cards from the draw pile
    public List<Card> deal(int n) {
        if (drawPile.size() < n) reshuffleDiscardIntoDraw(); // Reshuffle if not enough cards
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < n; i++) hand.add(drawPile.poll()); // Remove cards from the draw pile
        return hand;
    }

    // Starts the game by moving the top card from the draw pile to the discard pile
    public void start() {
        discardPile.add(drawPile.poll());
    }

    // Returns the top card of the discard pile without removing it
    public Card topDiscard() {
        return discardPile.peekLast();
    }

    // Plays a card by adding it to the discard pile
    public void play(Card c) {
        discardPile.add(c);
    }

    // Draws a card from the draw pile, reshuffling if necessary
    public Card draw() {
        if (drawPile.isEmpty()) reshuffleDiscardIntoDraw(); // Reshuffle if draw pile is empty
        return drawPile.poll(); // Remove and return the top card from the draw pile
    }

    // Reshuffles the discard pile into the draw pile, leaving the top discard card
    private void reshuffleDiscardIntoDraw() {
        if (discardPile.size() <= 1) throw new IllegalStateException("No cards to reshuffle"); // Ensure enough cards
        Card top = discardPile.pollLast(); // Keep the top card of the discard pile
        List<Card> rest = new ArrayList<>(discardPile); // Collect the rest of the discard pile
        discardPile.clear(); // Clear the discard pile
        Collections.shuffle(rest); // Shuffle the cards
        drawPile.addAll(rest); // Add shuffled cards to the draw pile
        discardPile.add(top); // Add the top card back to the discard pile
    }

    // Saves the current state of the draw and discard piles to files
    public void savePiles(Path dir) throws IOException {
        writePile(drawPile, dir.resolve("draw.txt")); // Save the draw pile to "draw.txt"
        writePile(discardPile, dir.resolve("discard.txt")); // Save the discard pile to "discard.txt"
    }

    // Helper method to write a pile of cards to a file
    private void writePile(Collection<Card> pile, Path f) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Card c: pile) lines.add(c.toString()); // Convert each card to a string
        Files.write(f, lines); // Write the strings to the file
    }

    // Loads a deck from files representing the draw and discard piles
    public static Deck load(Path dir) throws IOException {
        Deck d = new Deck();
        d.drawPile.clear(); // Clear the draw pile
        d.discardPile.clear(); // Clear the discard pile
        for (String line: Files.readAllLines(dir.resolve("draw.txt")))
            d.drawPile.add(Card.fromString(line)); // Load cards into the draw pile
        for (String line: Files.readAllLines(dir.resolve("discard.txt")))
            d.discardPile.add(Card.fromString(line)); // Load cards into the discard pile
        return d; // Return the loaded deck
    }
}
