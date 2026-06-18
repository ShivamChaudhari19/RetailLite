package in.shivam.retaillite.invoice.service.impl;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.InvoiceRepository;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.mapper.InvoiceItemMapper;
import in.shivam.retaillite.invoice.mapper.InvoiceMapper;
import in.shivam.retaillite.invoice.service.InvoiceService;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InventoryRepository inventoryRepository;
    private final InvoiceItemMapper invoiceItemMapper;
    private final InvoiceMapper invoiceMapper;



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


        /// get current User from security context holder and user repository
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails=(UserDetails)authentication.getPrincipal();
        User user=findUserByUserName(userDetails.getUsername());



        ///map invoiceRequestItem to InvoiceItem
//        List<InvoiceItem> invoiceItems =new ArrayList<>();
//        for(InvoiceItemRequest itemRequest: request.items()){
//            Inventory productInventory=inventoryRepository.findByProduct_productId(itemRequest.productId())
//                    .orElseThrow(()->new ResourceNotFoundException("Product not found: "+itemRequest.productId()));
//
//            ///check availability of requested items
//            /// deduction will be after successful payment
//            if(productInventory.getAvailableQuantity()<itemRequest.quantity()){
//                log.warn("product stock is low");
//                throw new QuantityOutOfBoundException("product out of Stock: "+itemRequest.productId());
//            }
//            invoiceItems.add(invoiceItemMapper.toInvoiceItem(itemRequest,productInventory.getProduct()));
//        }

        //same as above, using stream api
        List<InvoiceItem> invoiceItems=request.items().stream()
                .map(
                        invoiceItemRequest -> {

                            //check the product is valid or not
                            //if not throw new resource not found exception
                            Inventory InvoiceItemInventory =inventoryRepository.findByProduct_productId(invoiceItemRequest.productId())
                                    .orElseThrow(()->new ResourceNotFoundException("Product not found: "+invoiceItemRequest.productId()));

                            //check  if requested quantity is greater than available quantity
                            // if true throw quantity out of bound exception
                            if (InvoiceItemInventory.getAvailableQuantity()<invoiceItemRequest.quantity()){
                                log.warn("requested quantity is greater than Available Quantity.");
                                throw new QuantityOutOfBoundException("Stock is Running out.... try again some time");
                            }

                            //map InvoiceItemRequest to InvoiceItem
                            return invoiceItemMapper.toInvoiceItem(invoiceItemRequest, InvoiceItemInventory.getProduct());
                        }
                ).toList();


        ///calculate grand total for Invoice
        BigDecimal grandTotal = invoiceItems.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ///calculate sub total for Invoice
        BigDecimal subTotal= invoiceItems.stream()
                .map(x->x.getUnitPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        ///calculate  total tax for Invoice
        BigDecimal tax=grandTotal.subtract(subTotal);


        ///Converting the request to invoice for persistent
        Invoice invoice=invoiceMapper.toInvoice(
                request,
                user,
                subTotal,
                tax,
                grandTotal,
                InvoiceStatus.PENDING,
                invoiceItems
        );


        /// save the invoice in db with pending status
        Invoice placedOrder=invoiceRepository.save(invoice);
        log.debug("Invoice is saved with PENDING status.");
        return invoiceMapper.toInvoiceResponse(placedOrder);
    }

    @Override
    public Page<InvoiceResponse> findByInvoiceStatus(
            Integer page,
            Integer size,
            String invoiceStatus
    ) {
        Pageable pageable= PageRequest.of(page,size);
        Page<Invoice> invoices=invoiceRepository.findByInvoiceStatus(pageable, InvoiceStatus.valueOf(invoiceStatus));
        return invoices.map(invoiceMapper::toInvoiceResponse);
    }

    @Override
    public Page<InvoiceResponse> findAll(
            Integer page,
            Integer size,
            String sortBy,
            String orderedBy
    ) {
        List<String> allowedSort= Arrays.asList(
                "invoiceid",
                "user",
                "grandtotal",
                "invoicestatus",
                "createdat" ,
                "updatedat"
        );
        if (!allowedSort.contains(sortBy.toLowerCase())) sortBy="updatedat";
        Sort sort=orderedBy.equalsIgnoreCase("ASC")? Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable=PageRequest.of(page,size,sort);
        Page<Invoice> invoices=invoiceRepository.findAll(pageable);
        return invoices.map(invoiceMapper::toInvoiceResponse);
    }

    @Override
    public InvoiceResponse findInvoice(String invoiceId) {
        return invoiceMapper.toInvoiceResponse(
                invoiceRepository.findByInvoiceId(invoiceId)
                        .orElseThrow(()->new ResourceNotFoundException("Invoice Not Found....."))
        );
    }
}
