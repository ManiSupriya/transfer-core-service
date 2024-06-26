package com.mashreq.transfercoreservice.fundtransfer.strategy;


import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardType;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferCCMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author ThanigachalamP
 */
@ExtendWith(MockitoExtension.class)
public class LocalFundTransferStrategyCCTest {

    @InjectMocks
    private LocalFundTransferStrategy localFundTransferStrategy;

    @Mock
    private  IBANValidator ibanValidator;

    @Mock
    private  BeneficiaryValidator beneficiaryValidator;

    @Mock
    private  BeneficiaryService beneficiaryService;

    @Mock
    private CardService cardService;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CCBelongsToCifValidator ccBelongsToCifValidator;

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @Mock
    private  LimitValidator limitValidator;

    @Mock
    private FundTransferCCMWService fundTransferCCMWService;

    @Mock
    private  PaymentPurposeValidator paymentPurposeValidator;

    @Mock
    private  CCBalanceValidator ccBalanceValidator;

    @Mock
    private  MaintenanceService maintenanceService;

    @Mock
    private MobCommonService mobCommonService;

    @Mock
    private CountryRepository countryRepository;
    
    @Mock
    private DealValidator dealValidator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostTransactionService postTransactionService;

    @Mock
    private QRDealsService qrDealsService;
    
    @Mock
	private CCTransactionEligibilityValidator ccTrxValidator;

    @Mock
    private MinTransactionAmountValidator minTransactionAmountValidator;

    @Captor
    private ArgumentCaptor<FundTransferRequest> fundTransferRequest;

    @Captor
    private ArgumentCaptor<CustomerNotification> customerNotification;

    private final String CC_NO = "E6BD9127E95D80C2C0D46DB2A314514C315A21C8408729F99ECA3D22D123DB2D";



    private List<CardDetailsDTO> buildCardDetails(){
        List<CardDetailsDTO> cardDetailsDTOList = new ArrayList<>();
        CardDetailsDTO cardDetailsDTO = new CardDetailsDTO();
        cardDetailsDTO.setEncryptedCardNumber(CC_NO);
        cardDetailsDTO.setCardNo(CC_NO);
        cardDetailsDTOList.add(cardDetailsDTO);
        return cardDetailsDTOList;
    }

    private RequestMetaData buildRequestMetaData(){
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setPrimaryCif("012441750");
        requestMetaData.setChannel("MOB");
        requestMetaData.setUsername("TEST_CUSTOMER");
        requestMetaData.setCountry("AE");
        requestMetaData.setEmail("thanigachalamp@mashreq.com");
        requestMetaData.setSegment("NEO");
        return requestMetaData;
    }

    private BeneficiaryDto buildBeneficiaryDTO(){
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("AE610240041520084750901");
        beneficiaryDto.setSwiftCode("EBILAEADXXX");
        beneficiaryDto.setBankName("EMIRATES NBD PJSC");
        beneficiaryDto.setFullName("UNITED ARAB EMIRATES");
        return beneficiaryDto;
    }

    private QRDealDetails buildQRDealDetails(){
        QRDealDetails qrDealDetails = new QRDealDetails();
        qrDealDetails.setTotalLimitAmount(new BigDecimal("5000"));
        qrDealDetails.setUtilizedLimitAmount(new BigDecimal("100"));
        return qrDealDetails;
    }

    private FundTransferRequestDTO buildFundTransferRequest(){
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setServiceType(ServiceType.LOCAL.getName());
        fundTransferRequestDTO.setToAccount("AE610240041520084750901");
        fundTransferRequestDTO.setCardNo("E6BD9127E95D80C2C0D46DB2A314514C315A21C8408729F99ECA3D22D123DB2D");
        fundTransferRequestDTO.setAmount(new BigDecimal("10.10"));
        fundTransferRequestDTO.setSrcAmount(new BigDecimal("10.10"));
        fundTransferRequestDTO.setCurrency("AED");
        fundTransferRequestDTO.setFinTxnNo("363c5a1abb");
        fundTransferRequestDTO.setChargeBearer("O");
        fundTransferRequestDTO.setPurposeDesc("Personal, cultural, audiovisual and recreational services");
        fundTransferRequestDTO.setPurposeCode("PRS");
        fundTransferRequestDTO.setBeneficiaryId("1005771");
        return fundTransferRequestDTO;
    }

    private CoreFundTransferResponseDto buildResponse(){
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        return coreFundTransferResponseDto;
    }

    @BeforeEach
    public void before(){
        ReflectionTestUtils.setField(localFundTransferStrategy,"cardService", cardService);
        ReflectionTestUtils.setField(localFundTransferStrategy,"qrDealsService", qrDealsService);
        ReflectionTestUtils.setField(localFundTransferStrategy,"encryptionService", encryptionService);
        ReflectionTestUtils.setField(localFundTransferStrategy,"postTransactionService", postTransactionService);
        ReflectionTestUtils.setField(localFundTransferStrategy,"notificationService", notificationService);
    }

    @Test
    public void testCCAsSourceOfFundSuccess() {
        //Given
        FundTransferRequestDTO requestDTO = buildFundTransferRequest();
        RequestMetaData metadata = buildRequestMetaData();
        UserDTO userDTO = new UserDTO();
        setMockObject(requestDTO, metadata, userDTO);
        QRDealDetails qrDealDetails = buildQRDealDetails();
        ValidationResult validationResult = ValidationResult.builder().success(Boolean.TRUE).build();
        when(minTransactionAmountValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        when(qrDealsService.getQRDealDetails(Mockito.any(), Mockito.any())).thenReturn(qrDealDetails);
		when(ccTrxValidator.validate(any(), any())).thenReturn(validationResult);
        FundTransferResponse fundTransferResponse = FundTransferResponse.builder().responseDto(buildResponse()).
                build();
         when(fundTransferCCMWService.transfer(fundTransferRequest.capture(),eq(metadata)))
                .thenReturn(fundTransferResponse);
        final FundTransferResponse response = localFundTransferStrategy.execute(requestDTO, metadata, userDTO);
        CoreFundTransferResponseDto coreFundTransferResponseDto = response.getResponseDto();
       assertEquals(coreFundTransferResponseDto.getMwResponseStatus().getName(), MwResponseStatus.S.getName());
    }

    @Test
    public void testCCAsSourceOfFundQRNoDeals() {
        //Given
        FundTransferRequestDTO requestDTO = buildFundTransferRequest();
        RequestMetaData metadata = buildRequestMetaData();
        UserDTO userDTO = new UserDTO();
        ValidationResult validationResult = ValidationResult.builder().success(Boolean.TRUE).build();
        setMockObject(requestDTO, metadata, userDTO);
		when(ccTrxValidator.validate(any(), any())).thenReturn(validationResult);
        when(minTransactionAmountValidator.validate(any(), any(),any())).thenReturn(validationResult);
//        when(qrDealsService.getQRDealDetails(metadata.getPrimaryCif(), metadata.getCountry())).thenReturn(null);
        Throwable exception = Assertions.assertThrows(Exception.class, () -> localFundTransferStrategy.execute(requestDTO, metadata, userDTO));
        assertEquals(exception.getMessage(), TransferErrorCode.FT_CC_NO_DEALS.getErrorMessage());
    }


    @Test
    public void testCCAsSourceOfFundLessAmount() {
        //Given
        FundTransferRequestDTO requestDTO = buildFundTransferRequest();
        requestDTO.setAmount(new BigDecimal("9900"));
        RequestMetaData metadata = buildRequestMetaData();
        UserDTO userDTO = new UserDTO();
        setMockObject(requestDTO, metadata, userDTO);
        QRDealDetails qrDealDetails = buildQRDealDetails();
        ValidationResult validationResult = ValidationResult.builder().success(Boolean.TRUE).build();
        when(minTransactionAmountValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(ccTrxValidator.validate(any(), any())).thenReturn(validationResult);
        when(qrDealsService.getQRDealDetails(any(), any())).thenReturn(qrDealDetails);
        Throwable exception = Assertions.assertThrows(Exception.class, () -> localFundTransferStrategy.execute(requestDTO, metadata, userDTO));
        assertEquals(TransferErrorCode.FT_CC_BALANCE_NOT_SUFFICIENT.getErrorMessage(), exception.getMessage() );
    }


    private void setMockObject(FundTransferRequestDTO requestDTO, RequestMetaData metadata, UserDTO userDTO) {
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));
        BeneficiaryDto beneficiaryDto = buildBeneficiaryDTO();
        final ValidationResult validationResult = ValidationResult.builder().success(true).build();
        String srcCurrency = "AED";
        String address = "UNITED ARAB EMIRATES";
        String limitVersionUuid = "uuid1234";
        ReflectionTestUtils.setField(localFundTransferStrategy, "localCurrency", srcCurrency);
        ReflectionTestUtils.setField(localFundTransferStrategy, "address", address);
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(validationResult);
        when(cardService.getCardsFromCore(eq(metadata.getPrimaryCif()), eq(CardType.CC))).thenReturn(buildCardDetails());
        when(encryptionService.decrypt(eq(CC_NO))).thenReturn(CC_NO);
        when(mobCommonService.getPaymentPurposes( eq("LOCAL"), eq(""), eq("I"))).thenReturn(popList);

        when(ccBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(beneficiaryService.getByIdWithoutValidation(eq(metadata.getPrimaryCif()), any(),any(), any())).thenReturn(beneficiaryDto);
        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ibanValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ccBalanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
        limitValidatorResponse.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("LOCAL"), any(), eq(metadata), any()))
        .thenReturn(limitValidatorResponse);
        when(dealValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        Country country = new Country();
        country.setName("AE");
        Optional<Country> optionalCountry = Optional.of(country);
        //when(countryRepository.findByIsoCode2(anyString())).thenReturn(optionalCountry);
    }


}
