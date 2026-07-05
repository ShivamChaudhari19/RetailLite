package in.shivam.retaillite.invoice.controller;

import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
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
            Integer page,
            @RequestParam(defaultValue = "15")
            Integer size,
            @RequestParam(defaultValue = "invoiceId")
            String sorBy,
            @RequestParam(defaultValue = "asc")
            String orderedBy
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(invoiceService.findAll(page,size,sorBy,orderedBy));
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
}
