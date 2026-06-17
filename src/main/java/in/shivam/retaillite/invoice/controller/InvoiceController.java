package in.shivam.retaillite.invoice.controller;

import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    @PostMapping("/invoice")
    public ResponseEntity<InvoiceResponse> create(@RequestBody
                                                  InvoiceRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> invoices(
            @RequestParam
            Integer page,
            @RequestParam
            Integer size,
            @RequestParam
            String sorBy,
            @RequestParam
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

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getInvoiceByStatus(
            @RequestParam
            Integer page,
            @RequestParam
            Integer size,
            @RequestParam
            String status
    ){
        return ResponseEntity.ok(
                invoiceService.findByInvoiceStatus(page,size,status)
        );
    }
}
