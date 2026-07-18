package in.shivam.retaillite.invoice.service.impl;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.mapper.InvoiceItemMapper;
import in.shivam.retaillite.invoice.mapper.InvoiceMapper;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setUp() {
        InvoiceItemMapper invoiceItemMapper = new InvoiceItemMapper();
        InvoiceMapper invoiceMapper = new InvoiceMapper(invoiceItemMapper);

        invoiceService = new InvoiceServiceImpl(
                invoiceRepository,
                inventoryRepository,
                invoiceItemMapper,
                invoiceMapper,
                userRepository
        );
    }


    //   CREATE INVOICE TESTS

    @Test
    void createInvoice() {

        List<InvoiceItemRequest> invoiceItemRequest= List.of(new InvoiceItemRequest("productId",1));
        InvoiceRequest request=new InvoiceRequest(
                "customer name",
                "0000000000",
                "customer@retail.lite",
                invoiceItemRequest);


        User user=User.builder()
                .userId("user id").name("shivam").username("shivam@example.com").password("encrypted password").role(Role.ROLE_ADMIN).isEnable(true).build();

        Product product=Product.builder()
                .productId("product id")
                .price(BigDecimal.valueOf(100L))
                .taxRate(BigDecimal.ZERO).build();

        Inventory inventory=Inventory.builder()
                .inventoryId("invoice id")
                .product(product)
                .availableQuantity(100)
                .lowStockThreshold(5).active(true)
                .build();

        UserDetails userDetails=mock(UserDetails.class);
        Authentication authentication=mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("shivam@example.com");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));



        try (MockedStatic<SecurityContextHolder> contextHolderMockedStatic=mockStatic(SecurityContextHolder.class)){
            SecurityContext securityContext=mock(SecurityContext.class);
            contextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

           invoiceService.createInvoice(request);
           ArgumentCaptor<Invoice> captor=ArgumentCaptor.forClass(Invoice.class);
           verify(invoiceRepository).save(captor.capture());
           Invoice savedInvoice=captor.getValue();
           assertEquals(BigDecimal.valueOf(100L),savedInvoice.getGrandTotal());
           assertEquals(BigDecimal.ZERO,savedInvoice.getTax());
           assertEquals(BigDecimal.valueOf(100L),savedInvoice.getSubTotal());

        }
    }

    @Test
    void shouldThrowResourceNotFound_WhenUserNotInDBToCreateInvoice() {

        List<InvoiceItemRequest> invoiceItemRequest= List.of(new InvoiceItemRequest("productId",1));
        InvoiceRequest request=new InvoiceRequest(
                "customer name",
                "0000000000",
                "customer@retail.lite",
                invoiceItemRequest);

        UserDetails userDetails=mock(UserDetails.class);
        Authentication authentication=mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("shivam@example.com");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());


        try (MockedStatic<SecurityContextHolder> contextHolderMockedStatic=mockStatic(SecurityContextHolder.class)){
            SecurityContext securityContext=mock(SecurityContext.class);
            contextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->invoiceService.createInvoice(request));
            assertEquals("User does not exists", exception.getMessage());
        }
    }

    @Test
    void shouldThrowResourceNotFound_WhenProductNotFoundForCreateInvoice() {

        List<InvoiceItemRequest> invoiceItemRequest= List.of(new InvoiceItemRequest("productId",1));
        InvoiceRequest request=new InvoiceRequest(
                "customer name",
                "0000000000",
                "customer@retail.lite",
                invoiceItemRequest);


        User user=User.builder()
                .userId("user id").name("shivam").username("shivam@example.com").password("encrypted password").role(Role.ROLE_ADMIN).isEnable(true).build();




        UserDetails userDetails=mock(UserDetails.class);
        Authentication authentication=mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("shivam@example.com");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> contextHolderMockedStatic=mockStatic(SecurityContextHolder.class)){
            SecurityContext securityContext=mock(SecurityContext.class);
            contextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->invoiceService.createInvoice(request));
            assertEquals("Product not found: "+invoiceItemRequest.getFirst().productId(),exception.getMessage());


        }
    }

    @Test
    void shouldThrowQuantityOutOfBoundException_WhenRequestedQuantityIsGreaterThanInventoryCategory() {

        List<InvoiceItemRequest> invoiceItemRequest= List.of(new InvoiceItemRequest("productId",101));
        InvoiceRequest request=new InvoiceRequest(
                "customer name",
                "0000000000",
                "customer@retail.lite",
                invoiceItemRequest);


        User user=User.builder()
                .userId("user id").name("shivam").username("shivam@example.com").password("encrypted password").role(Role.ROLE_ADMIN).isEnable(true).build();

        Product product=Product.builder()
                .productId("product id")
                .price(BigDecimal.valueOf(100L))
                .taxRate(BigDecimal.ZERO).build();

        Inventory inventory=Inventory.builder()
                .inventoryId("invoice id")
                .product(product)
                .availableQuantity(100)
                .lowStockThreshold(5).active(true)
                .build();

        UserDetails userDetails=mock(UserDetails.class);
        Authentication authentication=mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("shivam@example.com");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));


        try (MockedStatic<SecurityContextHolder> contextHolderMockedStatic=mockStatic(SecurityContextHolder.class)){
            SecurityContext securityContext=mock(SecurityContext.class);
            contextHolderMockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            QuantityOutOfBoundException quantityOutOfBoundException=assertThrows(QuantityOutOfBoundException.class,()->invoiceService.createInvoice(request));
            assertEquals("Stock is Running out.... try again some time",quantityOutOfBoundException.getMessage());


        }
    }



    @ParameterizedTest
    @CsvSource(value = {
            "paid, PAID",
            "canceled, CANCELED",
            "penDing, PENDING"
    })
    void shouldReturnInvoices_WhenStatusIs(
            String input,
            InvoiceStatus invoiceStatus
    ) {
        int page=0;
        int size=10;
        
        List<Invoice> invoices=List.of(
                    Invoice.builder().id(1L).invoiceId("invoice id1").invoiceStatus(invoiceStatus).build(),
                    Invoice.builder().id(2L).invoiceId("invoice id2").invoiceStatus(invoiceStatus).build(),
                    Invoice.builder().id(3L).invoiceId("invoice id3").invoiceStatus(invoiceStatus).build()
        );
        Page<Invoice> invoicePage= new PageImpl<>(invoices);;

        when(invoiceRepository.findAllInvoiceByInvoiceStatus(any(Pageable.class),eq(invoiceStatus))).thenReturn(invoicePage);

        User user=User.builder()
                .username("shivam@retail.lite").build();
        User user1=User.builder()
                .username("shivam1@retail.lite").build();
        User user2=User.builder()
                .username("shivam2@retail.lite").build();
        InvoiceItem invoiceItem=InvoiceItem.builder()
                .id(1L).invoiceItemId("invoice item id").product(new Product()).quantity(0).unitPrice(BigDecimal.ZERO).lineTotal(BigDecimal.ZERO).build();
        List<Invoice> invoiceList=List.of(
                Invoice.builder().id(1L).invoiceId("invoice id1").user(user).customerName("").customerNumber(" ").customerEmail(" ").subTotal(BigDecimal.ONE).tax(BigDecimal.ZERO).grandTotal(BigDecimal.ONE).invoiceStatus(invoiceStatus).payment(null).createdAt(null).updatedAt(null).invoiceItems(List.of(invoiceItem)).build(),
                Invoice.builder().id(2L).user(user1).invoiceId("invoice id2").invoiceStatus(invoiceStatus).customerName("").customerNumber(" ").customerEmail(" ").subTotal(BigDecimal.ONE).tax(BigDecimal.ZERO).grandTotal(BigDecimal.ONE).invoiceStatus(invoiceStatus).payment(null).createdAt(null).updatedAt(null).invoiceItems(List.of(invoiceItem)).build(),
                Invoice.builder().id(3L).user(user2).invoiceId("invoice id3").invoiceStatus(invoiceStatus).customerName("").customerNumber(" ").customerEmail(" ").subTotal(BigDecimal.ONE).tax(BigDecimal.ZERO).grandTotal(BigDecimal.ONE).invoiceStatus(invoiceStatus).payment(null).createdAt(null).updatedAt(null).invoiceItems(List.of(invoiceItem)).build()
        );
        when(invoiceRepository.findByInvoiceIds(any())).thenReturn(invoiceList);
        Page<InvoiceResponse> invoiceResponses=invoiceService.findByInvoiceStatus(page,size,input);
        invoiceResponses.forEach(invoiceResponse -> assertEquals(invoiceStatus,invoiceResponse.invoiceStatus()));
        verify(invoiceRepository,times(1)).findAllInvoiceByInvoiceStatus(any(Pageable.class),eq(invoiceStatus));
    }

    @ParameterizedTest
    @CsvSource({
            "user,user",
            "grandtotal,grandTotal",
            "invoicestatus,invoiceStatus",
            "createdat,createdAt",
            "updatedat,updatedAt",
            "invoiceid,invoiceId",
            "unknown,invoiceId"
    })
    void shouldMapSortFieldCorrectly(
            String inputSortBy,
            String expectedProperty
    ) {

        Page<Invoice> invoicePage = new PageImpl<>(Collections.emptyList());

        when(invoiceRepository.findAllInvoices(any(Pageable.class)))
                .thenReturn(invoicePage);

        when(invoiceRepository.findAllInvoiceAndUsers(anyList()))
                .thenReturn(Collections.emptyList());

        // Act
        invoiceService.findAll(0, 10, inputSortBy, "ASC");

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(invoiceRepository)
                .findAllInvoices(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();

        Sort.Order order = captured.getSort()
                .iterator()
                .next();

        assertEquals(expectedProperty, order.getProperty());
        assertTrue(order.isAscending());
    }

    @Test
    void findInvoice() {
        Invoice invoice=Invoice.builder()
                .id(1L).invoiceId("invoce id")
                .user(User.builder().username("shivam@retaillite.com").build())
                .customerName("shivam").customerNumber("8989899989").customerEmail("customer@email.com").subTotal(BigDecimal.ONE).tax(BigDecimal.ZERO).grandTotal(BigDecimal.ONE).invoiceStatus(InvoiceStatus.PENDING)
                .payment(null)
                .createdAt(null).updatedAt(null)
                .invoiceItems(List.of(InvoiceItem.builder().id(1L).invoice(null).invoiceItemId("invoice item id")
                        .product(Product.builder().productId("product id").price(BigDecimal.ONE).taxRate(BigDecimal.ZERO).build())
                        .quantity(1).unitPrice(BigDecimal.ZERO).lineTotal(BigDecimal.ZERO).build())
                ).build();
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));
        assertDoesNotThrow(()->invoiceService.findInvoice("invoice id"));

    }
}