package in.shivam.retaillite.invoice.pdf.generator;

/**
 * Raised when the underlying PDF engine fails to render an invoice document.
 * Wraps checked, library-specific failures (e.g. OpenPDF's {@code DocumentException})
 * so callers higher up the stack don't need to know which PDF engine is in use.
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
