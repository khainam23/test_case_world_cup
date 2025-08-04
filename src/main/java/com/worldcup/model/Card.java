package com.worldcup.model;

public class Card {
    private int id;
    private Player player;
    private Team team;
    private Match match;
    private int minutes;
    private CardType type;

    public enum CardType {
        RED("Red"),
        YELLOW("Yellow");

        private final String label;

        CardType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static CardType fromLabel(String label) {
            for (CardType type : values()) {
                if (type.label.equalsIgnoreCase(label)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No card type for label: " + label);
        }
    }

}
