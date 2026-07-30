package in.shivam.retaillite.invoice.pdf.style;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;

import java.awt.Color;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Translates the domain's {@link InvoiceStatus} / {@link PaymentStatus} / {@link PaymentMethod}
 * enums into how they should look on the printed invoice (badge label + colors).
 * Keeps that decision out of the renderers, which should only draw what they're told.
 */
public final class StatusPresentation {

    private StatusPresentation() {
    }

    public record Badge(String label, Color background, Color textColor) {
    }

    public static Badge forInvoiceStatus(InvoiceStatus status) {
        if (status == null) {
            return new Badge("UNKNOWN", InvoiceTheme.STATUS_NEUTRAL_BG, InvoiceTheme.STATUS_NEUTRAL_TEXT);
        }
        return switch (status) {
            case PAID -> new Badge("PAID", InvoiceTheme.STATUS_PAID_BG, InvoiceTheme.STATUS_PAID_TEXT);
            case PENDING -> new Badge("PENDING", InvoiceTheme.STATUS_PENDING_BG, InvoiceTheme.STATUS_PENDING_TEXT);
            case CANCELED -> new Badge("CANCELED", InvoiceTheme.STATUS_CANCELED_BG, InvoiceTheme.STATUS_CANCELED_TEXT);
        };
    }

    public static Badge forPaymentStatus(PaymentStatus status) {
        if (status == null) {
            return new Badge("N/A", InvoiceTheme.STATUS_NEUTRAL_BG, InvoiceTheme.STATUS_NEUTRAL_TEXT);
        }
        return switch (status) {
            case SUCCESS -> new Badge("SUCCESS", InvoiceTheme.STATUS_PAID_BG, InvoiceTheme.STATUS_PAID_TEXT);
            case REFUNDED -> new Badge("REFUNDED", InvoiceTheme.STATUS_NEUTRAL_BG, InvoiceTheme.STATUS_NEUTRAL_TEXT);
            default -> new Badge(status.name(), InvoiceTheme.STATUS_PENDING_BG, InvoiceTheme.STATUS_PENDING_TEXT);
        };
    }

    public static String formatPaymentMethod(PaymentMethod method) {
        if (method == null) {
            return "\u2014";
        }
        String raw = method.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Arrays.stream(raw.split(" "))
                .filter(word -> !word.isBlank())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
