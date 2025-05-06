public class Card {
    public enum Suit {C, D, H, S}
    public enum Rank {
        A("A"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"),
        SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), J("J"), Q("Q"), K("K");
        private final String code;
        Rank(String code){ this.code = code; }
        @Override public String toString() { return code; }
    }
    private final Suit suit;
    private final Rank rank;
    public Card(Rank rank, Suit suit) {
        this.rank = rank; this.suit = suit;
    }
    public boolean matches(Card other) {
        return this.rank == other.rank || this.suit == other.suit || this.rank == Rank.EIGHT;
    }
    @Override
    public String toString() {
        return rank.toString() + suit.name();
    }
    public static Card fromString(String s) {
        if (s == null || s.length() < 2) throw new IllegalArgumentException("Invalid format");
        String r = s.substring(0, s.length()-1);
        char sc = s.charAt(s.length()-1);
        Rank rank = null; for (Rank x: Rank.values()) if (x.toString().equals(r)) rank = x;
        Suit suit = null; for (Suit x: Suit.values()) if (x.name().charAt(0)==sc) suit = x;
        if (rank==null||suit==null) throw new IllegalArgumentException("Unknown card: "+s);
        return new Card(rank, suit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card other = (Card) o;
        return this.rank == other.rank && this.suit == other.suit;
    }

    @Override
    public int hashCode() {
        return rank.hashCode() * 31 + suit.hashCode();
    }

    public Rank getRank() { return rank; }
}
