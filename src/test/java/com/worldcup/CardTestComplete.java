package com.worldcup;

import com.worldcup.model.Card;
import com.worldcup.model.Card.CardType;

public class CardTestComplete {

    public static void main(String[] args) {
        runAllTests();
        System.out.println("All CardTest tests passed!");
    }

    public static void runAllTests() {
        // ========== CARDTYPE ENUM TESTS ==========
        CardType_RedCard_GetLabelSuccess();
        CardType_YellowCard_GetLabelSuccess();
        CardType_FromLabel_RedExact_ReturnRed();
        CardType_FromLabel_YellowExact_ReturnYellow();
        CardType_FromLabel_RedLowercase_ReturnRed();
        CardType_FromLabel_YellowLowercase_ReturnYellow();
        CardType_FromLabel_RedUppercase_ReturnRed();
        CardType_FromLabel_YellowUppercase_ReturnYellow();
        CardType_FromLabel_RedMixedCase_ReturnRed();
        CardType_FromLabel_YellowMixedCase_ReturnYellow();
        CardType_FromLabel_LabelNull_ThrowException();
        CardType_FromLabel_LabelEmpty_ThrowException();
        CardType_FromLabel_LabelInvalid_ThrowException();
        CardType_FromLabel_LabelWhitespace_ThrowException();
        CardType_FromLabel_LabelWithSpaces_ThrowException();
        CardType_FromLabel_LabelNumeric_ThrowException();
        CardType_FromLabel_LabelSpecialChars_ThrowException();
        CardType_Values_ContainsAllValues();
        CardType_ValueOf_RedSuccess();
        CardType_ValueOf_YellowSuccess();
        CardType_ValueOf_InvalidValue_ThrowException();
        CardType_FromLabel_CheckAllEnums_PathCoverage();
        CardType_FromLabel_SingleCharLower_ThrowException();
        CardType_FromLabel_SingleCharUpper_ThrowException();
        CardType_FromLabel_ExceptionMessage_Correct();
    }
    
    public static void CardType_RedCard_GetLabelSuccess() {
        assert "Red".equals(CardType.RED.getLabel()) : "RED label should be 'Red'";
    }
    
    public static void CardType_YellowCard_GetLabelSuccess() {
        assert "Yellow".equals(CardType.YELLOW.getLabel()) : "YELLOW label should be 'Yellow'";
    }
    
    // Test equivalence partitioning for fromLabel
    public static void CardType_FromLabel_RedExact_ReturnRed() {
        assert CardType.RED == CardType.fromLabel("Red") : "fromLabel('Red') should return RED";
    }
    
    public static void CardType_FromLabel_YellowExact_ReturnYellow() {
        assert CardType.YELLOW == CardType.fromLabel("Yellow") : "fromLabel('Yellow') should return YELLOW";
    }
    
    public static void CardType_FromLabel_RedLowercase_ReturnRed() {
        assert CardType.RED == CardType.fromLabel("red") : "fromLabel('red') should return RED";
    }
    
    public static void CardType_FromLabel_YellowLowercase_ReturnYellow() {
        assert CardType.YELLOW == CardType.fromLabel("yellow") : "fromLabel('yellow') should return YELLOW";
    }
    
    public static void CardType_FromLabel_RedUppercase_ReturnRed() {
        assert CardType.RED == CardType.fromLabel("RED") : "fromLabel('RED') should return RED";
    }
    
    public static void CardType_FromLabel_YellowUppercase_ReturnYellow() {
        assert CardType.YELLOW == CardType.fromLabel("YELLOW") : "fromLabel('YELLOW') should return YELLOW";
    }
    
    public static void CardType_FromLabel_RedMixedCase_ReturnRed() {
        assert CardType.RED == CardType.fromLabel("ReD") : "fromLabel('ReD') should return RED";
    }
    
    public static void CardType_FromLabel_YellowMixedCase_ReturnYellow() {
        assert CardType.YELLOW == CardType.fromLabel("YeLLoW") : "fromLabel('YeLLoW') should return YELLOW";
    }
    
    // Test boundary values for fromLabel - Invalid cases
    public static void CardType_FromLabel_LabelNull_ThrowException() {
        try {
            CardType.fromLabel(null);
            assert false : "fromLabel(null) should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelEmpty_ThrowException() {
        try {
            CardType.fromLabel("");
            assert false : "fromLabel('') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelInvalid_ThrowException() {
        try {
            CardType.fromLabel("Blue");
            assert false : "fromLabel('Blue') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelWhitespace_ThrowException() {
        try {
            CardType.fromLabel("   ");
            assert false : "fromLabel('   ') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelWithSpaces_ThrowException() {
        try {
            CardType.fromLabel("Red Card");
            assert false : "fromLabel('Red Card') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelNumeric_ThrowException() {
        try {
            CardType.fromLabel("123");
            assert false : "fromLabel('123') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_LabelSpecialChars_ThrowException() {
        try {
            CardType.fromLabel("@#$");
            assert false : "fromLabel('@#$') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    // Test branch coverage for all enum values
    public static void CardType_Values_ContainsAllValues() {
        CardType[] values = CardType.values();
        assert values.length == 2 : "Should have 2 CardType values";
        
        boolean hasRed = false, hasYellow = false;
        for (CardType type : values) {
            if (type == CardType.RED) hasRed = true;
            if (type == CardType.YELLOW) hasYellow = true;
        }
        assert hasRed : "Should contain RED";
        assert hasYellow : "Should contain YELLOW";
    }
    
    // Test statement coverage for valueOf
    public static void CardType_ValueOf_RedSuccess() {
        assert CardType.RED == CardType.valueOf("RED") : "valueOf('RED') should return RED";
    }
    
    public static void CardType_ValueOf_YellowSuccess() {
        assert CardType.YELLOW == CardType.valueOf("YELLOW") : "valueOf('YELLOW') should return YELLOW";
    }
    
    public static void CardType_ValueOf_InvalidValue_ThrowException() {
        try {
            CardType.valueOf("BLUE");
            assert false : "valueOf('BLUE') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    // Test path coverage for fromLabel loop
    public static void CardType_FromLabel_CheckAllEnums_PathCoverage() {
        // Test to ensure the loop in fromLabel is fully covered
        for (CardType type : CardType.values()) {
            assert type == CardType.fromLabel(type.getLabel()) : "fromLabel should work for all enum values";
        }
    }
    
    // Test case sensitivity boundaries
    public static void CardType_FromLabel_SingleCharLower_ThrowException() {
        try {
            CardType.fromLabel("r");  // Will fail as it doesn't match
            assert false : "fromLabel('r') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void CardType_FromLabel_SingleCharUpper_ThrowException() {
        try {
            CardType.fromLabel("Y"); // Will fail as it doesn't match
            assert false : "fromLabel('Y') should throw exception";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    // Test exception message
    public static void CardType_FromLabel_ExceptionMessage_Correct() {
        try {
            CardType.fromLabel("InvalidLabel");
            assert false : "fromLabel('InvalidLabel') should throw exception";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("No card type for label: InvalidLabel") : "Exception message should be correct";
        }
    }
}