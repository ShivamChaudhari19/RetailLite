package in.shivam.retaillite.invoice.service.impl;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.InvoiceItemsRepository;
import in.shivam.retaillite.invoice.InvoiceRepository;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.mapper.InvoiceItemMapper;
import in.shivam.retaillite.invoice.mapper.InvoiceMapper;
import in.shivam.retaillite.invoice.service.InvoiceService;
import in.shivam.retaillite.payment.service.PaymentService;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemsRepository invoiceItemsRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InvoiceItemMapper invoiceItemMapper;
    private final InvoiceMapper invoiceMapper;


    private final PaymentService paymentService;

    private final UserRepository userRepository;
    private User findUserByUserName(String userName){
        log.debug("finding user by username");
        return userRepository.findByUsername(userName)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User does not exists"
                ));
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        log.debug("creating invoice for the incoming request......");
        User user=findUserByUserName(request.userName());
        List<InvoiceItem> invoiceItems =new ArrayList<>();
        for(InvoiceItemRequest itemRequest: request.items()){
            Inventory productInventory=inventoryRepository.findByProduct_productId(itemRequest.productId())
                    .orElseThrow(()->new ResourceNotFoundException("Product not found: "+itemRequest.productId()));
            if(productInventory.getAvailableQuantity()<itemRequest.quantity()){
                log.warn("product stock is low");
                throw new QuantityOutOfBoundException("product out of Stock: "+itemRequest.productId());
            }
            invoiceItems.add(invoiceItemMapper.toInvoiceItem(itemRequest,productInventory.getProduct()));
        }


        BigDecimal grandTotal = invoiceItems.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subTotal= invoiceItems.stream()
                .map(x->x.getUnitPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal tax=grandTotal.subtract(subTotal);

        Invoice invoice=invoiceMapper.toInvoice(
                request,
                user,
                subTotal,
                tax,
                grandTotal,
                InvoiceStatus.PENDING,
                invoiceItems
        );

        Invoice placedOrder=invoiceRepository.save(invoice);
        log.debug("Invoice is saved with PENDING status.");
        return invoiceMapper.toInvoiceResponse(placedOrder);
    }
}
