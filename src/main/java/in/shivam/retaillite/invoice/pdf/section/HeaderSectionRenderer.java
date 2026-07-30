package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import in.shivam.retaillite.invoice.pdf.style.StatusPresentation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Top-of-page section: company branding (logo or name, address, contact
 * details, GSTIN) on the left, invoice number/issue date/status badge on the
 * right, closed off with a single accent rule.
 */
@Slf4j
@Component
public class HeaderSectionRenderer implements InvoiceSectionRenderer {

    @Override
    public void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{6f, 4f});

        header.addCell(buildCompanyCell(company));
        header.addCell(buildInvoiceMetaCell(invoice));

        document.add(header);
        document.add(buildDivider());
    }

    private PdfPCell buildCompanyCell(CompanyProperties company) {
        PdfPCell cell = borderlessCell();
        cell.addElement(buildLogoOrBrandName(company));
        cell.addElement(spacer(InvoiceTheme.SPACING_SMALL));

        String address = buildAddressLine(company);
        if (!address.isBlank()) {
            cell.addElement(new Paragraph(address, InvoiceTheme.FONT_BODY_MUTED));
        }
        if (company.phone() != null && !company.phone().isBlank()) {
            cell.addElement(new Paragraph("Phone: " + company.phone(), InvoiceTheme.FONT_BODY_MUTED));
        }
        if (company.email() != null && !company.email().isBlank()) {
            cell.addElement(new Paragraph("Email: " + company.email(), InvoiceTheme.FONT_BODY_MUTED));
        }
        if (company.website() != null && !company.website().isBlank()) {
            cell.addElement(new Paragraph(company.website(), InvoiceTheme.FONT_BODY_MUTED));
        }
        if (company.gstNumber() != null && !company.gstNumber().isBlank()) {
            cell.addElement(new Paragraph("GSTIN: " + company.gstNumber(), InvoiceTheme.FONT_BODY_MUTED));
        }
        return cell;
    }

    private PdfPCell buildInvoiceMetaCell(InvoiceDocument invoice) {
        PdfPCell cell = borderlessCell();
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        if (invoice.invoiceStatus()== InvoiceStatus.PAID &&hasPaymentDetails(invoice)){
        cell.addElement(alignedParagraph("TAX INVOICE", InvoiceTheme.FONT_DOCUMENT_TITLE, Element.ALIGN_RIGHT));

        }else {
            cell.addElement(alignedParagraph("INVOICE", InvoiceTheme.FONT_DOCUMENT_TITLE, Element.ALIGN_RIGHT));
        }

        cell.addElement(spacer(InvoiceTheme.SPACING_SMALL));
        cell.addElement(labelValue("Invoice #", invoice.invoiceId()));
        cell.addElement(labelValue("Issued", invoice.createdAt() == null ? null : invoice.createdAt().format(InvoiceTheme.DATE_FORMATTER)));
        cell.addElement(spacer(InvoiceTheme.SPACING_SMALL));

        PdfPTable badgeWrapper = new PdfPTable(1);
        badgeWrapper.setWidthPercentage(45);
        badgeWrapper.setHorizontalAlignment(Element.ALIGN_RIGHT);
        badgeWrapper.addCell(buildStatusBadge(invoice));
        cell.addElement(badgeWrapper);

        return cell;
    }
    private boolean hasPaymentDetails(InvoiceDocument invoice) {
        return invoice.paymentId() != null || invoice.paymentMethod() != null || invoice.paymentStatus() != null;
    }
    private PdfPCell buildStatusBadge(InvoiceDocument invoice) {
        StatusPresentation.Badge badge = StatusPresentation.forInvoiceStatus(invoice.invoiceStatus());
        PdfPCell cell = new PdfPCell(new Paragraph(badge.label(), InvoiceTheme.statusBadgeFont(badge.textColor())));
        cell.setBackgroundColor(badge.background());
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private Element buildLogoOrBrandName(CompanyProperties company) {
        if (company.logo() != null) {
            try (InputStream inputStream = company.logo().getInputStream()) {
                Image logo = Image.getInstance(inputStream.readAllBytes());
                logo.scaleToFit(InvoiceTheme.LOGO_MAX_WIDTH, InvoiceTheme.LOGO_MAX_HEIGHT);
                return logo;
            } catch (IOException | BadElementException e) {
                log.warn("Unable to load company logo, falling back to text branding", e);
            }
        }
        return new Paragraph(company.name(), InvoiceTheme.FONT_BRAND);
    }

    private String buildAddressLine(CompanyProperties company) {
        CompanyProperties.Address address = company.address();
        if (address == null) {
            return "";
        }
        return Stream.of(address.line1(), address.line2())
                .filter(Objects::nonNull)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining(", "));
    }

    private Paragraph labelValue(String label, String value) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + ": ", InvoiceTheme.FONT_BODY_MUTED));
        paragraph.add(new Chunk(value == null || value.isBlank() ? "\u2014" : value, InvoiceTheme.FONT_BODY_BOLD));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private Paragraph alignedParagraph(String text, com.lowagie.text.Font font, int alignment) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        return paragraph;
    }

    private Paragraph buildDivider() {
        LineSeparator separator = new LineSeparator(1f, 100f, InvoiceTheme.COLOR_ACCENT, Element.ALIGN_CENTER, -2);
        Paragraph rule = new Paragraph(new Chunk(separator));
        rule.setSpacingBefore(6f);
        rule.setSpacingAfter(InvoiceTheme.SPACING_SECTION);
        return rule;
    }

    private Paragraph spacer(float height) {
        Paragraph spacer = new Paragraph(" ", InvoiceTheme.FONT_BODY);
        spacer.setSpacingAfter(height);
        return spacer;
    }

    private PdfPCell borderlessCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        return cell;
    }
}
