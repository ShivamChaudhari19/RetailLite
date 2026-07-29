package in.shivam.retaillite.invoice.controller;

import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.service.InvoicePdfService;
import in.shivam.retaillite.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Validated
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;


    @PostMapping("/invoice")
    public ResponseEntity<InvoiceResponse> create(
            @Valid
            @RequestBody
            InvoiceRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> invoices(
            @RequestParam(defaultValue = "0")
            @Min(0) Integer page,
            @RequestParam(defaultValue = "15")
            @Min(0) @Max(50)Integer size,
            @RequestParam(defaultValue = "invoiceId")
            String sortBy,
            @RequestParam(defaultValue = "asc")
            String orderedBy
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(invoiceService.findAll(page,size,sortBy,orderedBy));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable String invoiceId)
    {
        return ResponseEntity.ok(invoiceService.findInvoice(invoiceId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<InvoiceResponse>> getInvoiceByStatus(
            @PathVariable
            String status, //Allowed values paid, canceled, pending
            @RequestParam(defaultValue = "0")
            Integer page,
            @RequestParam(defaultValue = "15")
            Integer size
    ){
        return ResponseEntity.ok(
                invoiceService.findByInvoiceStatus(page,size,status)
        );
    }


    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String invoiceId){



        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+invoiceId+".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(invoicePdfService.generateInvoice(invoiceId));
    }
}
