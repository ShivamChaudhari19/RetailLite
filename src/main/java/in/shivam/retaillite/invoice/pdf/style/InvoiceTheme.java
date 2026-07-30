package in.shivam.retaillite.invoice.pdf.style;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

/**
 * Single source of truth for the invoice PDF's visual design: page geometry,
 * color palette, typography, and spacing. No other class in the {@code pdf}
 * package should declare a raw color, font size, or margin - everything is
 * defined once, here.
 * <p>
 * Palette follows the restrained, single-accent-color convention used by
 * commercial ERP invoicing (SAP Business One, Dynamics 365): a dark slate/navy
 * for headings and structure, one steel-blue accent, and muted status colors
 * rather than saturated ones.
 */
public final class InvoiceTheme {

    private InvoiceTheme() {
    }

    // ---- Page geometry ----
    public static final float PAGE_MARGIN_LEFT = 40f;
    public static final float PAGE_MARGIN_RIGHT = 40f;
    public static final float PAGE_MARGIN_TOP = 50f;
    public static final float PAGE_MARGIN_BOTTOM = 55f;

    // ---- Palette ----
    public static final Color COLOR_PRIMARY = new Color(31, 45, 61);
    public static final Color COLOR_ACCENT = new Color(37, 99, 154);
    public static final Color COLOR_TABLE_HEADER_BG = new Color(238, 241, 245);
    public static final Color COLOR_TABLE_ALT_ROW = new Color(248, 249, 251);
    public static final Color COLOR_BORDER = new Color(210, 214, 220);
    public static final Color COLOR_TEXT_PRIMARY = new Color(33, 37, 41);
    public static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    public static final Color COLOR_TOTALS_BG = new Color(31, 45, 61);
    public static final Color COLOR_TOTALS_TEXT = Color.WHITE;

    public static final Color STATUS_PAID_BG = new Color(219, 242, 227);
    public static final Color STATUS_PAID_TEXT = new Color(23, 128, 71);
    public static final Color STATUS_PENDING_BG = new Color(254, 240, 210);
    public static final Color STATUS_PENDING_TEXT = new Color(158, 104, 8);
    public static final Color STATUS_CANCELED_BG = new Color(250, 222, 222);
    public static final Color STATUS_CANCELED_TEXT = new Color(168, 39, 39);
    public static final Color STATUS_NEUTRAL_BG = new Color(232, 234, 237);
    public static final Color STATUS_NEUTRAL_TEXT = new Color(84, 92, 100);

    // ---- Typography ----
    // NOTE: these Font instances are shared, read-only singletons - never call
    // a mutator (setColor/setStyle/...) on them at render time. Anything that
    // needs a color not covered here (e.g. status badges) must go through
    // statusBadgeFont(Color), which creates a fresh instance.
    public static final Font FONT_BRAND = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, COLOR_PRIMARY);
    public static final Font FONT_DOCUMENT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, COLOR_TEXT_MUTED);
    public static final Font FONT_SECTION_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, COLOR_TEXT_MUTED);
    public static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, COLOR_TEXT_PRIMARY);
    public static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_TEXT_PRIMARY);
    public static final Font FONT_BODY_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 9f, COLOR_TEXT_MUTED);
    public static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, COLOR_TEXT_PRIMARY);
    public static final Font FONT_TOTALS_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 10f, COLOR_TOTALS_TEXT);
    public static final Font FONT_TOTALS_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, COLOR_TOTALS_TEXT);
    public static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8f, COLOR_TEXT_MUTED);

    // ---- Spacing ----
    public static final float SPACING_SECTION = 16f;
    public static final float SPACING_SMALL = 4f;
    public static final float CELL_PADDING = 6f;
    public static final float LOGO_MAX_WIDTH = 90f;
    public static final float LOGO_MAX_HEIGHT = 50f;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Status badges each need their own text color, so they cannot share one of
     * the static {@code FONT_*} constants above without mutating shared state.
     * This returns a fresh {@link Font} instance per call instead.
     */
    public static Font statusBadgeFont(Color textColor) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, textColor);
    }
}
