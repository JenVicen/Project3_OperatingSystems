/*
By: Jennifer Vicentes
Purpose: This class represents a player in the Crazy Eights game.
It manages the player's hand of cards, including dealing, drawing, and playing cards.
It also handles saving and loading the player's hand from a file.
It ensures that the player cannot play cards that do not match the top discard card.

All the comments I wrote were put for me to keep track while developing, they are not AI generated. 
*/

import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Player {
    private final String name; // The name of the player
    private final Path handFile; // Path to the file storing the player's hand
    private final List<Card> hand = new ArrayList<>(); // List to store the player's hand of cards

    // Constructor to initialize the player with a name and directory for saving the hand
    public Player(String name, Path dir) throws IOException {
        if ("admin".equalsIgnoreCase(name)) throw new IllegalArgumentException("'admin' not allowed"); // Restrict 'admin' as a player name
        this.name = name;
        this.handFile = dir.resolve(name + ".txt"); // Resolve the file path for the player's hand
        if (Files.exists(handFile)) loadHand(); // Load the hand from the file if it exists
    }

    // Getter for the player's name
    public String getName() { return name; }

    // Getter for the player's hand (returns an unmodifiable view)
    public List<Card> getHand() { return Collections.unmodifiableList(hand); }

    // Deals the initial set of cards to the player
    public void dealInitial(List<Card> cards) {
        hand.clear(); // Clear the current hand
        hand.addAll(cards); // Add the new cards to the hand
        saveHand(); // Save the updated hand to the file
    }

    // Saves the player's hand to the file
    public void saveHand() {
        try {
            List<String> out = new ArrayList<>();
            for (Card c: hand) out.add(c.toString()); // Convert each card to a string
            Files.write(handFile, out); // Write the strings to the file
        } catch (IOException e) { 
            throw new RuntimeException(e); // Wrap and rethrow any IOException
        }
    }

    // Loads the player's hand from the file
    private void loadHand() throws IOException {
        hand.clear(); // Clear the current hand
        for (String l: Files.readAllLines(handFile)) 
            hand.add(Card.fromString(l.trim())); // Convert each line to a Card and add to the hand
    }

    // Plays a card from the player's hand
    public void play(Card c, Deck deck) {
        if (!hand.contains(c)) throw new IllegalArgumentException("You don't have that card"); // Ensure the player has the card
        if (!c.matches(deck.topDiscard())) throw new IllegalArgumentException("You can't discard that card"); // Ensure the card matches the discard pile
        hand.remove(c); // Remove the card from the hand
        deck.play(c); // Add the card to the discard pile
        saveHand(); // Save the updated hand to the file
    }

    // Draws a card from the deck and adds it to the player's hand
    public Card draw(Deck deck) {
        Card c = deck.draw(); // Draw a card from the deck
        hand.add(c); // Add the card to the hand
        saveHand(); // Save the updated hand to the file
        return c; // Return the drawn card
    }

    // Checks if the player has won (i.e., their hand is empty)
    public boolean hasWon() {
        return hand.isEmpty();
    }

    // Calculates the score of the player's hand
    public int score() {
        return hand.stream().mapToInt(c -> {
            switch (c.getRank()) {
                case J: case Q: case K: return 10; // Face cards are worth 10 points
                default: return c.getRank().ordinal() + 1; // Other cards are worth their rank value
            }
        }).sum(); // Sum up the points for all cards in the hand
    }
}
