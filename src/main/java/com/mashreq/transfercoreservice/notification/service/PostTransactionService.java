package com.mashreq.transfercoreservice.notification.service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;
import static java.lang.Long.valueOf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.mashreq.notification.client.freemarker.TemplateRequest;
import com.mashreq.notification.client.freemarker.TemplateType;
import com.mashreq.notification.client.notification.service.NotificationService;
import com.mashreq.transfercoreservice.fundtransfer.service.TransferBankChargesService;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.utils.DateUtil;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PostTransactionService {

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private TransferBankChargesService transferBankChargesService;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.local.address}")
    private String address;

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${default.notification.language}")
    private String defaultLanguage;

    @Value("${app.local.bankName}")
    private String bankName;

    private static final Set<ServiceType> OWN_ACCOUNT_SERVICE_TYPES = new HashSet<>(Arrays.asList(WYMA, XAU, XAG));

    /**
     * Send Alerts via sms, email and push notification.
     *
     * @param
     * @param requestMetaData
     * @param beneficiaryDto
     */

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, FundTransferRequest fundTransferRequest, FundTransferRequestDTO fundTransferRequestDTO, Optional<BeneficiaryDto> beneficiaryDto){
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            updateBankChargesInFTReq(fundTransferRequest,requestMetaData);
            TemplateRequest templateRequest = getEmailPostTransactionActivityContext(requestMetaData, fundTransferRequest, fundTransferRequestDTO, beneficiaryDto);
            notificationService.sendNotification(templateRequest);
            userEventPublisher.publishSuccessEvent(eventType, requestMetaData, eventType.getDescription());
        }catch (Exception exception){
            GenericExceptionHandler.logOnly(exception, transferErrorCode.getErrorMessage());
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }
    }

    /** updates bank charges in fundTransferRequest based on serviceType
     * @param fundTransferRequest
     * @param requestMetaData
     * @throws NullPointerException - if charges returned from response are null
     */
    public void updateBankChargesInFTReq(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData) {
        ServiceType serviceType = getServiceByType(fundTransferRequest.getServiceType());
        switch (serviceType){
            case WAMA:break;
            case WYMA:break;
            case LOCAL:
                fundTransferRequest.setBankFees(transferBankChargesService.getBankFeesForCustomerByCharge(fundTransferRequest,
                        requestMetaData,ServiceType.LOCAL));
                break;
            case INFT:
                fundTransferRequest.setBankFees(transferBankChargesService.getBankFeesForCustomerByCharge(fundTransferRequest,
                        requestMetaData,ServiceType.INFT));
                break;
            default:
                break;
        }
    }

    private TemplateRequest getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                   FundTransferRequest fundTransferRequest,
                                                                   FundTransferRequestDTO fundTransferRequestDTO, Optional<BeneficiaryDto> beneficiaryDto) throws Exception {
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
        String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            String templateName = getTemplateName(fundTransferRequest.getNotificationType());

            TemplateRequest.EmailBuilder template = buildEmailTemplate(templateName,requestMetaData,channelType)
                    .params(TRANSFER_TYPE, StringUtils.defaultIfBlank(fundTransferRequest.getTransferType(), DEFAULT_STR))
                    .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                    .params(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                    .params(SOURCE_OF_FUND, fundTransferRequest.getSourceOfFund() == null ? SOURCE_OF_FUND_ACCOUNT: fundTransferRequest.getSourceOfFund())
                    .params(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR))
                    .params(EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY, String.valueOf(LocalDateTime.now().getYear()));

            getTemplateValuesForFundTransferBuilder(template, fundTransferRequest, fundTransferRequestDTO, requestMetaData, beneficiaryDto);
            template.subjectParams(TRANSFER_TYPE,fundTransferRequest.getTransferType());
            template.subjectParams(CHANNEL_TYPE,channelType);
            template.toEmailAddress(requestMetaData.getEmail());
            return template.configure();
        } else {
            log.error("Email notification did not trigger since email is not available for user: {}",
                    htmlEscape(requestMetaData.getUsername()));
            userEventPublisher.publishFailureEvent(eventType, requestMetaData, eventType.getDescription(),
                    transferErrorCode.getCustomErrorCode(), transferErrorCode.getErrorMessage(), transferErrorCode.getErrorMessage());
        }
        return null;
    }

    private boolean checkForPlAndSi(String type){
        return type.contains("PL") || type.contains("SI");
    }
    private String getTemplateName(String type) {
        if (type.equalsIgnoreCase(NotificationType.LOCAL)) {
            return LOCAL_FUND_TRANSFER;
        }
        else if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_BUY_SUCCESS)){
            return GOLD_SILVER_BUY_SUCCESS;
        }
        else if(type.equalsIgnoreCase(NotificationType.GOLD_SILVER_SELL_SUCCESS)){
            return GOLD_SILVER_SELL_SUCCESS;
        }
        else if(checkForPlAndSi(type)){
            return PL_SI_FUND_TRANSFER;
        }
        else return OTHER_FUND_TRANSFER;
    }

    private TemplateRequest.EmailBuilder buildEmailTemplate(String templateName,RequestMetaData metaData,String channelType) {
        return  TemplateRequest.emailBuilder()
                .templateType(TemplateType.EMAIL)
                .templateName(templateName)
                .country(metaData.getCountry())
                .segment(metaData.getSegment())
                .channel(channelType)
                .businessType(BUSINESS_TYPE)
                .language(defaultLanguage);
    }
    private void getTemplateValuesForFundTransferBuilder(TemplateRequest.EmailBuilder builder, FundTransferRequest fundTransferRequest,
                                                         FundTransferRequestDTO fundTransferRequestDTO, RequestMetaData requestMetaData, Optional<BeneficiaryDto> beneficiaryDto) {
        builder.params(MASKED_ACCOUNT, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getFromAccount()), DEFAULT_STR));
        builder.params(TO_ACCOUNT_NO, StringUtils.defaultIfBlank(emailUtil.doMask(fundTransferRequest.getToAccount()), DEFAULT_STR));
        builder.params(BENEFICIARY_NICK_NAME, StringUtils.defaultIfBlank(fundTransferRequest.getBeneficiaryFullName(), DEFAULT_STR));
        builder.params(CURRENCY, StringUtils.defaultIfBlank(fundTransferRequest.getTxnCurrency(), DEFAULT_STR) );
        builder.params(DESTINATION_ACCOUNT_CURRENCY,StringUtils.defaultIfBlank(fundTransferRequest.getDestinationCurrency(), DEFAULT_STR));
        builder.params(ACCOUNT_CURRENCY,StringUtils.defaultIfBlank(fundTransferRequest.getSourceCurrency(), DEFAULT_STR));
        builder.params(SOURCE_AMOUNT,fundTransferRequest.getSrcCcyAmt() != null ? EmailUtil.formattedAmount(fundTransferRequest.getSrcCcyAmt()) : DEFAULT_STR);
        builder.params(BANK_FEES,StringUtils.defaultIfBlank(fundTransferRequest.getBankFees(), DEFAULT_STR));
        builder.params(FX_DEAL_CODE,StringUtils.defaultIfBlank(fundTransferRequest.getDealNumber(), DEFAULT_STR));
        builder.params(ORDER_TYPE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getOrderType(), DEFAULT_STR));
        builder.params(EXCHANGE_RATE,StringUtils.defaultIfBlank(fundTransferRequest.getExchangeRateDisplayTxt(), DEFAULT_STR));
        builder.params(LOCAL_CURRENCY,localCurrency);
        builder.params(REFERENCE_NUMBER,StringUtils.defaultIfBlank(fundTransferRequest.getLimitTransactionRefNo(), DEFAULT_STR));


        if(fundTransferRequest.getAmount() != null) {
            builder.params(AMOUNT, EmailUtil.formattedAmount(fundTransferRequest.getAmount()));
        }
        else if(fundTransferRequest.getSrcAmount() != null){
            builder.params(AMOUNT, EmailUtil.formattedAmount(fundTransferRequest.getSrcAmount()));
        }
        else {
            builder.params(AMOUNT, DEFAULT_STR);
        }
        builder.params(STATUS, STATUS_SUCCESS);

        if(checkForPlAndSi(fundTransferRequest.getNotificationType())){

            builder.params(TRANSACTION_TYPE, StringUtils.defaultIfBlank(
                    fundTransferRequestDTO.getOrderType().equals("PL") ? "Pay Later" : "Standing Instructions", DEFAULT_STR)
            );

            builder.params(EXECUTION_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(START_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(END_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getEndDate(), DEFAULT_STR));
            builder.params(FREQUENCY,StringUtils.defaultIfBlank(fundTransferRequestDTO.getFrequency(), DEFAULT_STR));

            builder.subjectParams(PL_TYPE,fundTransferRequestDTO.getOrderType().equals("PL")? "Pay Later" : "Standing Instruction");
        }

        builder.params(TRANSACTION_DATE, StringUtils.defaultIfBlank(
                DateUtil.instantToDate(Instant.now(), "yyyy-MM-dd HH:mm:ss"), DEFAULT_STR)
        );
        ServiceType serviceType = getServiceByType(fundTransferRequest.getServiceType());
        if(OWN_ACCOUNT_SERVICE_TYPES.contains(serviceType)){
            builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(address, DEFAULT_STR));
            builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(bankName, DEFAULT_STR));
        }
        else {
            BeneficiaryDto beneficiaryDtoOptional = beneficiaryDto.orElseGet(() -> beneficiaryService.getByIdWithoutValidation(requestMetaData.getPrimaryCif(), valueOf(fundTransferRequestDTO.getBeneficiaryId()), fundTransferRequestDTO.getJourneyVersion(), requestMetaData));
            builder.params(BENEFICIARY_BANK_BRANCH_NAME, StringUtils.defaultIfBlank(beneficiaryDtoOptional.getBankBranchName(), DEFAULT_STR));
            builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(beneficiaryDtoOptional.getBankName(), DEFAULT_STR));
            builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(beneficiaryDtoOptional.getBankCountry(), DEFAULT_STR));
        }
    }
}
