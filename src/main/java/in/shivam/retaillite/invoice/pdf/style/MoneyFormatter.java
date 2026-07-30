package in.shivam.retaillite.invoice.pdf.style;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Centralized money formatting for the invoice PDF, so every renderer displays
 * amounts identically instead of each rolling its own {@link java.text.DecimalFormat}.
 */
public final class MoneyFormatter {

    private static final Locale LOCALE_INDIA = Locale.forLanguageTag("en-IN");
    private static final String CURRENCY_SYMBOL = "\u20B9";

    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(LOCALE_INDIA);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return CURRENCY_SYMBOL + " " + numberFormat.format(value);
    }
}
