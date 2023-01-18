package com.mashreq.transfercoreservice.notification.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CHARGE_BEARER;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer.getChargeBearerByName;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static com.mashreq.transfercoreservice.notification.service.EmailUtil.*;
import static java.lang.Long.valueOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.templates.freemarker.TemplateEngine;
import com.mashreq.templates.freemarker.TemplateRequest;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.client.service.BankChargesService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.config.notification.EmailConfig;
import com.mashreq.transfercoreservice.errors.ExceptionUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import com.mashreq.transfercoreservice.notification.model.EmailTemplateParameters;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import com.mashreq.transfercoreservice.paylater.utils.DateUtil;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PostTransactionService {

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private PostTransactionActivityService postTransactionActivityService;

    @Autowired
    private SendEmailActivity sendEmailActivity;

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private BankChargesService bankChargesService;

    @Value("${app.uae.address}")
    private String address;

    private static final Set<ServiceType> OWN_ACCOUNT_SERVICE_TYPES = new HashSet<>(Arrays.asList(WYMA, XAU, XAG));

    /**
     * Send Alerts via sms, email and push notification.
     *
     * @param requestMetaData

     * @param
     */

    @Async("generalTaskExecutor")
    public void performPostTransactionActivities(RequestMetaData requestMetaData, FundTransferRequest fundTransferRequest, FundTransferRequestDTO fundTransferRequestDTO){
        FundTransferEventType eventType = FundTransferEventType.EMAIL_NOTIFICATION;
        TransferErrorCode transferErrorCode = TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
        try {
            updateBankChargesInFTReq(fundTransferRequest,requestMetaData);
            final PostTransactionActivityContext<SendEmailRequest> emailPostTransactionActivityContext = getEmailPostTransactionActivityContext(requestMetaData, fundTransferRequest, fundTransferRequestDTO);
            postTransactionActivityService.execute(Arrays.asList(emailPostTransactionActivityContext), requestMetaData);
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
    private void updateBankChargesInFTReq(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData) {
        ServiceType serviceType = getServiceByType(fundTransferRequest.getServiceType());
        switch (serviceType){
            case WAMA:break;
            case WYMA:break;
            case LOCAL:
                fundTransferRequest.setBankFees(getBankFeesForCustomerByCharge(fundTransferRequest,requestMetaData,ServiceType.LOCAL));
                break;
            case INFT:
            	fundTransferRequest.setBankFees(getBankFeesForCustomerByCharge(fundTransferRequest,requestMetaData,ServiceType.INFT));
            	break;
            default:
                break;
        }
    }

    private String getBankFeesForCustomerByCharge(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData,ServiceType type) {
        if(StringUtils.isBlank(fundTransferRequest.getChargeBearer())){
            throw ExceptionUtils.genericException(INVALID_CHARGE_BEARER,INVALID_CHARGE_BEARER.getErrorMessage());
        }
        final TransactionChargesDto bankCharges = bankChargesService.getTransactionCharges(fundTransferRequest.getAccountClass(), fundTransferRequest.getTxnCurrency(), requestMetaData);
        String charges = EMPTY;
        Double bankCharge = ServiceType.INFT.equals(type) ? bankCharges.getInternationalTransactionalCharge() : bankCharges.getLocalTransactionCharge();
        switch(getChargeBearerByName(fundTransferRequest.getChargeBearer())){
            case U:
                charges = String.valueOf(bankCharge);
                break;
            case O:
                charges = String.valueOf(bankCharge);
                break;
            case B:
                break;
            default:
                throw ExceptionUtils.genericException(INVALID_CHARGE_BEARER,INVALID_CHARGE_BEARER.getErrorMessage());
        }
        return charges;

    }


    private PostTransactionActivityContext<SendEmailRequest> getEmailPostTransactionActivityContext(RequestMetaData requestMetaData,
                                                                                                    FundTransferRequest fundTransferRequest,
                                                                                                    FundTransferRequestDTO fundTransferRequestDTO) throws Exception {


        SendEmailRequest emailRequest = SendEmailRequest.builder().isEmailPresent(false).build();
        String contactLinkText;
        String htmlContent;

        if (StringUtils.isNotBlank(requestMetaData.getEmail())) {
            final EmailParameters emailParameters = emailConfig.getEmail().get(requestMetaData.getCountry());

            final String templateName = emailParameters.getEmailTemplate(fundTransferRequest.getNotificationType());
            final EmailTemplateParameters emailTemplateParameters = emailUtil.getEmailTemplateParameters(requestMetaData.getChannel(), requestMetaData.getSegment());
            boolean isMobile = requestMetaData.getChannel().contains(MOBILE);
            String channelType = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
            Segment segment = emailTemplateParameters.getSegment();
            final String subject = emailParameters.getEmailSubject(fundTransferRequest.getNotificationType(),fundTransferRequest.getTransferType(),channelType);

            String contactHtmlBody;
            String segmentSignOffCompanyName;
            String bankNameInFooter;
            String bankNameInFooterDesc;
            if(segment != null) {
                contactLinkText = StringUtils.defaultIfBlank(segment.getEmailContactUsLinkText(), DEFAULT_STR);
                htmlContent = segment.getEmailContactUsHtmlContent();
                if(StringUtils.isNotEmpty(htmlContent)) {
                    htmlContent = htmlContent.replaceAll("\\{contactUsLinkText}", contactLinkText);
                    htmlContent = htmlContent.replaceAll("\\$", DEFAULT_STR);
                } else {
                    htmlContent = DEFAULT_STR;
                }

                contactHtmlBody = htmlContent;
                segmentSignOffCompanyName = StringUtils.defaultIfBlank(segment.getEmailSignOffCompany(), DEFAULT_STR);
                bankNameInFooter = StringUtils.defaultIfBlank(segment.getEmailCprFooter(), DEFAULT_STR);
                bankNameInFooterDesc = StringUtils.defaultIfBlank(segment.getEmailCprBankDesc(), DEFAULT_STR);
            } else {
                contactHtmlBody = DEFAULT_STR;
                segmentSignOffCompanyName = DEFAULT_STR;
                bankNameInFooter = emailTemplateParameters.getChannelIdentifier().getChannelName();
                bankNameInFooterDesc = DEFAULT_STR;
            }

            TemplateRequest.Builder template = TemplateRequest.builder()
                    .templateName(templateName)
                    .params(TRANSFER_TYPE, StringUtils.defaultIfBlank(fundTransferRequest.getTransferType(), DEFAULT_STR))
                    .params(SEGMENT, StringUtils.defaultIfBlank(requestMetaData.getSegment(), DEFAULT_STR))
                    .params(CUSTOMER_NAME, StringUtils.defaultIfBlank(emailUtil.capitalizeFully(requestMetaData.getUsername()), CUSTOMER))
                    .params(SOURCE_OF_FUND, fundTransferRequest.getSourceOfFund() == null ? SOURCE_OF_FUND_ACCOUNT: fundTransferRequest.getSourceOfFund())
                    .params(BANK_NAME, StringUtils.defaultIfBlank(emailTemplateParameters.getChannelIdentifier().getChannelName(), DEFAULT_STR))
                    .params(CHANNEL_TYPE, StringUtils.defaultIfBlank(channelType, DEFAULT_STR))
                    .params(FACEBOOK_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(FACEBOOK), DEFAULT_STR))
                    .params(INSTAGRAM_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(INSTAGRAM), DEFAULT_STR))
                    .params(TWITTER_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(TWITTER), DEFAULT_STR))
                    .params(LINKED_IN_KEY, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(LINKED_IN), DEFAULT_STR))
                    .params(YOUTUBE_LINK, StringUtils.defaultIfBlank(emailTemplateParameters.getSocialMediaLinks().get(YOUTUBE), DEFAULT_STR))
                    .params(EMAIL_TEMPLATE_COPYRIGHT_YEAR_KEY, String.valueOf(LocalDateTime.now().getYear()))
                    .params(CONTACT_HTML_BODY_KEY, contactHtmlBody)
                    .params(SEGMENT_SIGN_OFF_COMPANY_NAME, segmentSignOffCompanyName)
                    .params(BANK_NAME_FOOTER, bankNameInFooter)
                    .params(BANK_NAME_FOOTER_DESC, bankNameInFooterDesc);


            getTemplateValuesForFundTransferBuilder(template, fundTransferRequest, fundTransferRequestDTO, requestMetaData, segment);

            emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(emailParameters.getFromEmailAddress())
                    .toEmailAddress(requestMetaData.getEmail())
                    .subject(subject)
                    .text(templateEngine.generate(template.configure()))
                    .fromEmailName(emailParameters.getFromEmailName())
                    .isEmailPresent(true)
                    .build();
        }
        return PostTransactionActivityContext.<SendEmailRequest>builder().payload(emailRequest).postTransactionActivity(sendEmailActivity).build();
    }
    private void getTemplateValuesForFundTransferBuilder(TemplateRequest.Builder builder, FundTransferRequest fundTransferRequest,
                                                         FundTransferRequestDTO fundTransferRequestDTO, RequestMetaData requestMetaData, Segment segment) {
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
        builder.params(LOCAL_CURRENCY,AED);

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

        if((fundTransferRequest.getNotificationType().contains("PL") || fundTransferRequest.getNotificationType().contains("SI"))){

            ServiceType serviceType = getServiceByType(fundTransferRequest.getServiceType());
            if(OWN_ACCOUNT_SERVICE_TYPES.contains(serviceType)){
                builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(segment.getEmailCprFooter(), DEFAULT_STR));
                builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(address, DEFAULT_STR));
            }
            else{
                final BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(requestMetaData.getPrimaryCif(), valueOf(fundTransferRequestDTO.getBeneficiaryId()), fundTransferRequestDTO.getJourneyVersion(), requestMetaData);
                builder.params(BENEFICIARY_BANK_NAME, StringUtils.defaultIfBlank(beneficiaryDto.getBankName(), DEFAULT_STR));
                builder.params(BENEFICIARY_BANK_COUNTRY, StringUtils.defaultIfBlank(beneficiaryDto.getBankCountry(), DEFAULT_STR));
            }

            builder.params(CUSTOMER_CARE_NO, StringUtils.defaultIfBlank(segment.getCustomerCareNumber(), DEFAULT_STR));
            builder.params(TRANSACTION_DATE, StringUtils.defaultIfBlank(
                    DateUtil.instantToDate(Instant.now(), "yyyy-MM-dd HH:mm:ss"), DEFAULT_STR)
            );
            builder.params(TRANSACTION_TYPE, StringUtils.defaultIfBlank(
                    fundTransferRequestDTO.getOrderType().equals("PL") ? "Pay Later" : "Standing Instructions", DEFAULT_STR)
            );

            builder.params(EXECUTION_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(START_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getStartDate(), DEFAULT_STR));
            builder.params(END_DATE,StringUtils.defaultIfBlank(fundTransferRequestDTO.getEndDate(), DEFAULT_STR));
            builder.params(FREQUENCY,StringUtils.defaultIfBlank(fundTransferRequestDTO.getFrequency(), DEFAULT_STR));
        }
    }
}
