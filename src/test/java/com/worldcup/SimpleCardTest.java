package com.worldcup;

import com.worldcup.model.Card;

public class SimpleCardTest {
    
    public static void main(String[] args) {
        testCardTypeEnum();
        System.out.println("All CardType tests passed!");
    }
    
    public static void testCardTypeEnum() {
        // Test getLabel
        assert Card.CardType.RED.getLabel().equals("Red") : "RED label should be 'Red'";
        assert Card.CardType.YELLOW.getLabel().equals("Yellow") : "YELLOW label should be 'Yellow'";
        
        // Test fromLabel - valid cases
        assert Card.CardType.fromLabel("Red") == Card.CardType.RED : "fromLabel('Red') should return RED";
        assert Card.CardType.fromLabel("Yellow") == Card.CardType.YELLOW : "fromLabel('Yellow') should return YELLOW";
        assert Card.CardType.fromLabel("red") == Card.CardType.RED : "fromLabel('red') should return RED";
        assert Card.CardType.fromLabel("yellow") == Card.CardType.YELLOW : "fromLabel('yellow') should return YELLOW";
        assert Card.CardType.fromLabel("RED") == Card.CardType.RED : "fromLabel('RED') should return RED";
        assert Card.CardType.fromLabel("YELLOW") == Card.CardType.YELLOW : "fromLabel('YELLOW') should return YELLOW";
        
        // Test fromLabel - invalid cases
        try {
            Card.CardType.fromLabel(null);
            assert false : "fromLabel(null) should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            Card.CardType.fromLabel("");
            assert false : "fromLabel('') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            Card.CardType.fromLabel("Blue");
            assert false : "fromLabel('Blue') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Test values()
        Card.CardType[] values = Card.CardType.values();
        assert values.length == 2 : "Should have 2 CardType values";
        
        // Test valueOf
        assert Card.CardType.valueOf("RED") == Card.CardType.RED : "valueOf('RED') should return RED";
        assert Card.CardType.valueOf("YELLOW") == Card.CardType.YELLOW : "valueOf('YELLOW') should return YELLOW";
        
        try {
            Card.CardType.valueOf("BLUE");
            assert false : "valueOf('BLUE') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}