/*
By: Jennifer Vicentes
Purpose: This class represents a playing card with a suit and rank. 
It provides functionality to compare cards, convert them to and from strings, 
and calculate hash codes for use in collections.
All the comments I wrote were put for me to keep track while developing, they are not AI generated. 
*/

public class Card {
    // Enum to represent the four suits of a card: Clubs, Diamonds, Hearts, Spades
    public enum Suit {C, D, H, S}

    // Enum to represent the ranks of a card, including Ace, numbered cards, and face cards
    public enum Rank {
        A("A"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"),
        SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), J("J"), Q("Q"), K("K");
        
        private final String code; // String representation of the rank

        // Constructor to initialize the rank with its string code
        Rank(String code) { this.code = code; }

        // Override toString to return the string code of the rank
        @Override public String toString() { return code; }
    }

    private final Suit suit; // The suit of the card
    private final Rank rank; // The rank of the card

    // Constructor to create a card with a specific rank and suit
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    // Checks if this card matches another card based on rank, suit, or if it's an "Eight"
    public boolean matches(Card other) {
        return this.rank == other.rank || this.suit == other.suit || this.rank == Rank.EIGHT;
    }

    // Converts the card to a string representation (e.g., "8H" for Eight of Hearts)
    @Override
    public String toString() {
        return rank.toString() + suit.name();
    }

    // Static method to create a Card object from its string representation
    public static Card fromString(String s) {
        if (s == null || s.length() < 2) throw new IllegalArgumentException("Invalid format");
        String r = s.substring(0, s.length() - 1); // Extract the rank part
        char sc = s.charAt(s.length() - 1); // Extract the suit character
        Rank rank = null;
        for (Rank x : Rank.values()) if (x.toString().equals(r)) rank = x; // Find the matching rank
        Suit suit = null;
        for (Suit x : Suit.values()) if (x.name().charAt(0) == sc) suit = x; // Find the matching suit
        if (rank == null || suit == null) throw new IllegalArgumentException("Unknown card: " + s);
        return new Card(rank, suit); // Create and return the card
    }

    // Overrides equals to compare two cards based on their rank and suit
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if the objects are the same
        if (!(o instanceof Card)) return false; // Ensure the object is a Card
        Card other = (Card) o;
        return this.rank == other.rank && this.suit == other.suit; // Compare rank and suit
    }

    // Overrides hashCode to generate a hash based on rank and suit
    @Override
    public int hashCode() {
        return rank.hashCode() * 31 + suit.hashCode(); // Combine hash codes of rank and suit
    }

    // Getter for the rank of the card
    public Rank getRank() { return rank; }
}
