package io.github.sps23.designpatterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

enum PaymentMethod {
    CARD, BANK_TRANSFER, DIGITAL_WALLET, BUY_NOW_PAY_LATER
}

record PaymentRequest(PaymentMethod paymentMethod, BigDecimal amount, String currency,
        boolean vipCustomer) {
    PaymentRequest {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method must not be null");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
    }
}

record FeeQuote(PaymentMethod paymentMethod, BigDecimal baseAmount, BigDecimal fee,
        BigDecimal totalAmount) {
}

@FunctionalInterface
interface PaymentFeeStrategy {
    BigDecimal calculateFee(PaymentRequest request);

    default PaymentFeeStrategy withMinimumFee(BigDecimal minimumFee) {
        return request -> PaymentFeeService.scale(calculateFee(request).max(minimumFee));
    }

    default PaymentFeeStrategy withCap(BigDecimal maximumFee) {
        return request -> PaymentFeeService.scale(calculateFee(request).min(maximumFee));
    }
}

/**
 * Selects the correct payment-fee algorithm at runtime so checkout code stays
 * free of branching and concrete strategy knowledge.
 */
public final class PaymentFeeService {
    private final Map<PaymentMethod, PaymentFeeStrategy> strategies;

    public PaymentFeeService(Map<PaymentMethod, PaymentFeeStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public static PaymentFeeService defaultService() {
        PaymentFeeStrategy card = request -> scale(
                request.amount().multiply(money("0.029")).add(money("0.30")));
        PaymentFeeStrategy bankTransfer = request -> scale(
                request.amount().multiply(money("0.008")));
        PaymentFeeStrategy digitalWallet = request -> {
            var baseFee = request.amount().multiply(money("0.017"));
            if (request.vipCustomer()) {
                baseFee = baseFee.multiply(money("0.50"));
            }
            return scale(baseFee);
        };

        return new PaymentFeeService(Map.of(PaymentMethod.CARD, card, PaymentMethod.BANK_TRANSFER,
                bankTransfer.withCap(money("7.50")), PaymentMethod.DIGITAL_WALLET,
                digitalWallet.withMinimumFee(money("0.25"))));
    }

    public FeeQuote quote(PaymentRequest request) {
        var strategy = strategies.get(request.paymentMethod());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No strategy configured for payment method: " + request.paymentMethod());
        }

        var baseAmount = scale(request.amount());
        var fee = scale(strategy.calculateFee(request));
        return new FeeQuote(request.paymentMethod(), baseAmount, fee, scale(baseAmount.add(fee)));
    }

    static BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
