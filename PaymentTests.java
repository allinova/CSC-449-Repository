package com.example.payments;

import org.junit.Test;                 
import static org.junit.Assert.*;      
import java.math.BigDecimal;

public class PaymentTests {

   
    record Card(String number, String exp, String cvv) {}
    interface CardValidator { boolean isValid(Card c); }

    static class PaymentResult {
        enum Status { INSUFFICIENT_FUNDS, APPROVED }

        private final boolean success;
        private final Status status;
        private final String message;

        PaymentResult(boolean success, Status status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }

        boolean isSuccess() { return success; }
        Status getStatus() { return status; }
        String getMessage() { return message; }
    }

    static class PaymentProcessor {
        // case A: cash
        PaymentResult payWithCash(BigDecimal total, BigDecimal given) {
            int cmp = given.compareTo(total);
            if (cmp < 0) {
                return new PaymentResult(
                    false,
                    PaymentResult.Status.INSUFFICIENT_FUNDS,
                    "Insufficient funds."
                );
            }
            // exact or more = treat as approved 
            return new PaymentResult(
                true,
                PaymentResult.Status.APPROVED,
                "Thank you for shopping."
            );
        }

        // case B: card
        PaymentResult payWithCard(BigDecimal total, Card card, boolean canceled, CardValidator validator) {
            if (canceled) {
                
                return new PaymentResult(false, PaymentResult.Status.INSUFFICIENT_FUNDS,
                        "Transaction canceled. Returning to main menu.");
            }
            if (validator.isValid(card)) {
                return new PaymentResult(true, PaymentResult.Status.APPROVED,
                        "Payment approved. Thank you for shopping.");
            } else {
                return new PaymentResult(false, PaymentResult.Status.INSUFFICIENT_FUNDS,
                        "Payment failed. Please try again.");
            }
        }
    }
   

    private final PaymentProcessor sut = new PaymentProcessor();

 @Test
public void cash_lessThanTotal_showsInsufficientFunds() {
    var res = sut.payWithCash(new BigDecimal("25.00"), new BigDecimal("20.00"));
    assertFalse(res.isSuccess());
    assertEquals(PaymentResult.Status.INSUFFICIENT_FUNDS, res.getStatus());
    assertEquals("Insufficient funds.", res.getMessage());
}

@Test
public void card_validInfo_paymentApproved() {
    Card card = new Card("1234567890123456", "12/25", "123");
    CardValidator validator = c -> true;

    var res = sut.payWithCard(new BigDecimal("25.00"), card, false, validator);
    assertTrue(res.isSuccess());
    assertEquals(PaymentResult.Status.APPROVED, res.getStatus());
    assertEquals("Payment approved. Thank you for shopping.", res.getMessage());
}

}