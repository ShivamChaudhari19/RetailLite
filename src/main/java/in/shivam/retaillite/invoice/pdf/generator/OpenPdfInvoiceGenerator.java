package in.shivam.retaillite.invoice.pdf.generator;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.event.PageFooterEventHandler;
import in.shivam.retaillite.invoice.pdf.section.HeaderSectionRenderer;
import in.shivam.retaillite.invoice.pdf.section.ItemsTableSectionRenderer;
import in.shivam.retaillite.invoice.pdf.section.PartiesSectionRenderer;
import in.shivam.retaillite.invoice.pdf.section.PaymentSectionRenderer;
import in.shivam.retaillite.invoice.pdf.section.TotalsSectionRenderer;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * OpenPDF-backed {@link PdfGenerator}. Owns only the {@link Document}/{@link PdfWriter}
 * lifecycle (open, sequence the sections, close) - every visual detail is
 * delegated to the injected section renderers, and the page footer/page
 * numbers are delegated to a fresh {@link PageFooterEventHandler} per call.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenPdfInvoiceGenerator implements PdfGenerator {

    private final HeaderSectionRenderer headerSectionRenderer;
    private final PartiesSectionRenderer partiesSectionRenderer;
    private final ItemsTableSectionRenderer itemsTableSectionRenderer;
    private final TotalsSectionRenderer totalsSectionRenderer;
    private final PaymentSectionRenderer paymentSectionRenderer;

    @Override
    public byte[] generate(InvoiceDocument document, CompanyProperties company) {
        Document pdfDocument = new Document(
                PageSize.A4,
                InvoiceTheme.PAGE_MARGIN_LEFT,
                InvoiceTheme.PAGE_MARGIN_RIGHT,
                InvoiceTheme.PAGE_MARGIN_TOP,
                InvoiceTheme.PAGE_MARGIN_BOTTOM
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(pdfDocument, outputStream);
            writer.setPageEvent(new PageFooterEventHandler());

            pdfDocument.open();
            headerSectionRenderer.render(pdfDocument, document, company);
            partiesSectionRenderer.render(pdfDocument, document, company);
            itemsTableSectionRenderer.render(pdfDocument, document, company);
            totalsSectionRenderer.render(pdfDocument, document, company);
            paymentSectionRenderer.render(pdfDocument, document, company);
            pdfDocument.close();

            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("Failed to render invoice PDF. invoiceId={}", document.invoiceId(), e);
            throw new PdfGenerationException("Unable to generate PDF for invoice " + document.invoiceId(), e);
        }
    }
}
