package io.github.sps23.interview.preparation.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates transaction fees for different payment methods.
 *
 * <p>
 * This class demonstrates exhaustive pattern matching with sealed classes in
 * Java 21. Because {@link PaymentMethod} is sealed and permits only
 * {@link CreditCard}, {@link BankTransfer}, and {@link DigitalWallet}, the
 * compiler verifies that all cases are handled - no {@code default} case is
 * needed.
 *
 * <p>
 * Key concepts demonstrated:
 * <ul>
 * <li>Exhaustive switch expressions - compiler guarantees all cases are
 * covered</li>
 * <li>Record patterns - destructuring records directly in case labels</li>
 * <li>Pattern guards using {@code when} - conditional matching within
 * patterns</li>
 * </ul>
 *
 * <p>
 * Fee structure:
 * <ul>
 * <li>Credit Card: 2.9% + $0.30 per transaction</li>
 * <li>Bank Transfer: flat $2.50 for amounts under $1000, $5.00 otherwise</li>
 * <li>Digital Wallet: 2.5% with $0.50 minimum fee</li>
 * </ul>
 */
public final class FeeCalculator {

    private static final BigDecimal CREDIT_CARD_PERCENTAGE = new BigDecimal("0.029");
    private static final BigDecimal CREDIT_CARD_FIXED_FEE = new BigDecimal("0.30");
    private static final BigDecimal BANK_TRANSFER_LOW_FEE = new BigDecimal("2.50");
    private static final BigDecimal BANK_TRANSFER_HIGH_FEE = new BigDecimal("5.00");
    private static final BigDecimal BANK_TRANSFER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal DIGITAL_WALLET_PERCENTAGE = new BigDecimal("0.025");
    private static final BigDecimal DIGITAL_WALLET_MIN_FEE = new BigDecimal("0.50");

    private FeeCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates the transaction fee for a payment method.
     *
     * <p>
     * This method uses exhaustive pattern matching with record patterns. The
     * compiler verifies that all permitted subtypes of {@link PaymentMethod} are
     * handled, eliminating the need for a default case.
     *
     * @param payment
     *            the payment method to calculate fee for
     * @return the calculated fee amount, rounded to 2 decimal places
     */
    public static BigDecimal calculateFee(PaymentMethod payment) {
        // Exhaustive switch expression - no default needed because PaymentMethod is
        // sealed
        return switch (payment) {
            case CreditCard(var cardNumber, var expiry, var amount) ->
                calculateCreditCardFee(amount);
            case BankTransfer(var iban, var bankCode, var amount) ->
                calculateBankTransferFee(amount);
            case DigitalWallet(var provider, var accountId, var amount) ->
                calculateDigitalWalletFee(amount);
        };
    }

    /**
     * Calculates fee using pattern matching with guards (when clause).
     *
     * <p>
     * Demonstrates conditional pattern matching using the {@code when} keyword.
     * This is similar to Scala's pattern guards.
     *
     * @param payment
     *            the payment method
     * @return a description of the fee calculation
     */
    public static String describeFee(PaymentMethod payment) {
        return switch (payment) {
            case CreditCard(var num, var exp, var amt) when amt
                    .compareTo(new BigDecimal("100")) > 0 ->
                "Credit card (high value): 2.9%% + $0.30 on %s".formatted(num.substring(12));
            case CreditCard(var num, var exp, var amt) ->
                "Credit card (standard): 2.9%% + $0.30 on %s".formatted(num.substring(12));
            case BankTransfer(var iban, var code, var amt) when amt
                    .compareTo(BANK_TRANSFER_THRESHOLD) >= 0 ->
                "Bank transfer (high value): flat $5.00 to %s".formatted(code);
            case BankTransfer(var iban, var code, var amt) ->
                "Bank transfer (standard): flat $2.50 to %s".formatted(code);
            case DigitalWallet(var provider, var id, var amt) ->
                "Digital wallet (%s): 2.5%% (min $0.50)".formatted(provider);
        };
    }

    /**
     * Demonstrates simple type pattern matching (without record destructuring).
     *
     * @param payment
     *            the payment method
     * @return the payment type name
     */
    public static String getPaymentTypeName(PaymentMethod payment) {
        return switch (payment) {
            case CreditCard cc -> "Credit Card";
            case BankTransfer bt -> "Bank Transfer";
            case DigitalWallet dw -> "Digital Wallet";
        };
    }

    private static BigDecimal calculateCreditCardFee(BigDecimal amount) {
        // 2.9% + $0.30
        BigDecimal percentageFee = amount.multiply(CREDIT_CARD_PERCENTAGE);
        return percentageFee.add(CREDIT_CARD_FIXED_FEE).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateBankTransferFee(BigDecimal amount) {
        // Flat fee: $2.50 for < $1000, $5.00 for >= $1000
        return amount.compareTo(BANK_TRANSFER_THRESHOLD) < 0
                ? BANK_TRANSFER_LOW_FEE
                : BANK_TRANSFER_HIGH_FEE;
    }

    private static BigDecimal calculateDigitalWalletFee(BigDecimal amount) {
        // 2.5% with $0.50 minimum
        BigDecimal percentageFee = amount.multiply(DIGITAL_WALLET_PERCENTAGE);
        return percentageFee.max(DIGITAL_WALLET_MIN_FEE).setScale(2, RoundingMode.HALF_UP);
    }
}
