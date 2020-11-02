package com.mashreq.transfercoreservice.swifttracker.service.impl;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.NOT_ABLE_TO_FETCH_GPI_TRACKER;
import static com.mashreq.transfercoreservice.middleware.SoapWebserviceClientFactory.soapClient;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.CRDT_STATUS;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.CREDITED;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.PNDG_STATUS;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G000;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G000_MESSAGE;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G001;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G001_MESSAGE;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G002;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G002_MESSAGE;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G003;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G003_MESSAGE;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G004;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REASON_G004_MESSAGE;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REJECTED;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.RJCT_STATUS;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.TRNSCTN;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.TRNSCTN_PNDG;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.DAY;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.HOUR;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.MIN;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.EMPTY;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
/**
 * @author SURESH PASUPULETI
 */
import org.springframework.stereotype.Service;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.account.mbcdm.SWIFTGPITransactionDetailsReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.SWIFTGPITransactionDetailsResType;
import com.mashreq.esbcore.bindings.account.mbcdm.TransactionStatusType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.swiftgpitransactiondetails.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapClient;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.swifttracker.dto.ForeignExchangeDetails;
import com.mashreq.transfercoreservice.swifttracker.dto.PaymentEventDetailsType;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsReq;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftTrackerMWService {
	
	private final HeaderFactory headerFactory;
	private final SoapServiceProperties soapServiceProperties;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";

	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(
			SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq, RequestMetaData metaData) {
		log.info("Swift GPI Transaction Details call initiated [ {} ]", swiftGpiTransactionDetailsReq);
		SoapClient soapClient = soapClient(soapServiceProperties,
                new Class[]{
                        HeaderType.class ,
                        EAIServices.class,
                        FundTransferReqType.class,
                        FundTransferResType.class,
                        ErrorType.class,
                });
		EAIServices response = (EAIServices) soapClient
				.exchange(generateSwiftTrackerEaiServices(swiftGpiTransactionDetailsReq, metaData));

		if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
			GenericExceptionHandler.handleError(NOT_ABLE_TO_FETCH_GPI_TRACKER, NOT_ABLE_TO_FETCH_GPI_TRACKER.getErrorMessage(), NOT_ABLE_TO_FETCH_GPI_TRACKER.getErrorMessage());
        }

		SWIFTGPITransactionDetailsResType responseDTO = response.getBody().getSWIFTGPITransactionDetailsRes();

		return SWIFTGPITransactionDetailsRes.builder().initiationTime(responseDTO.getInitiationTime())
				.totalProcessingTime(computeDiff(responseDTO.getInitiationTime(), responseDTO.getLastUpdateTime()))
				.completionTime(responseDTO.getPaymentEventDetails().size()>1?computeDiff(responseDTO.getPaymentEventDetails().get(1).getSenderAckReceipt(),
						responseDTO.getPaymentEventDetails().get(0).getSenderAckReceipt()):computeDiff(responseDTO.getInitiationTime(), responseDTO.getLastUpdateTime()))
				.lastUpdateTime(responseDTO.getLastUpdateTime())
				.paymentEventDetails(convertToPaymentEventDetails(responseDTO.getPaymentEventDetails()))
				.transactionStatus(setTransactionStatus(responseDTO.getTransactionStatus())).build();
	}
        
	 private EAIServices generateSwiftTrackerEaiServices(SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq, RequestMetaData metaData) {
		 EAIServices request = new EAIServices();
	     request.setBody(new EAIServices.Body());
	     HeaderType headerType =headerFactory.getHeader(soapServiceProperties.getServiceCodes().getSwiftGpiTransactionDetails(), metaData.getChannelTraceId());     
	     request.setHeader(headerType);
	     SWIFTGPITransactionDetailsReqType swiftGPITransactionDetailsReqType = new SWIFTGPITransactionDetailsReqType();
	     swiftGPITransactionDetailsReqType.setUETR(swiftGpiTransactionDetailsReq.getUetr());
	     request.getBody().setSWIFTGPITransactionDetailsReq(swiftGPITransactionDetailsReqType);
	     return request;
	 }
	 
	private List<PaymentEventDetailsType> convertToPaymentEventDetails(
			List<com.mashreq.esbcore.bindings.account.mbcdm.PaymentEventDetailsType> list) {
		return list.stream().map(paymentEventDetailsType -> PaymentEventDetailsType.builder()
				.businessService(paymentEventDetailsType.getBusinessService())
				.ConfirmedAmount(paymentEventDetailsType.getConfirmedAmount())
				.confirmedAmountCcy(paymentEventDetailsType.getConfirmedAmountCcy())
				.CopiedBusinessService(paymentEventDetailsType.getCopiedBusinessService())
				.duplicateMsgRef(paymentEventDetailsType.getDuplicateMsgRef())
				.foreignExchangeDetails(ForeignExchangeDetails.builder()
						.exchangeRate(paymentEventDetailsType.getForeignExchangeDetails().getExchangeRate())
						.FromCurrency(paymentEventDetailsType.getForeignExchangeDetails().getFromCurrency())
						.ToCurrency(paymentEventDetailsType.getForeignExchangeDetails().getToCurrency()).build())
				.instructionId(paymentEventDetailsType.getInstructionId()).from(paymentEventDetailsType.getFrom())
				.lastUpdateTime(paymentEventDetailsType.getLastUpdateTime())
				.msgNameId(paymentEventDetailsType.getMsgNameId())
				.networkReference(paymentEventDetailsType.getNetworkReference())
				.originator(paymentEventDetailsType.getOriginator())
				.senderAckReceipt(paymentEventDetailsType.getSenderAckReceipt()).to(paymentEventDetailsType.getTo())
				.transactionStatus(setHopTransactionStatus(paymentEventDetailsType.getTransactionStatus()))
				.updatePayment(paymentEventDetailsType.getUpdatePayment()).valid(paymentEventDetailsType.getValid())
				.msgNameId(paymentEventDetailsType.getMsgNameId()).build()).collect(Collectors.toList());
	}

	private String setHopTransactionStatus(TransactionStatusType transactionStatusType) {
		if (transactionStatusType.getStatus().equalsIgnoreCase(CRDT_STATUS)
				|| transactionStatusType.getStatus().equalsIgnoreCase(RJCT_STATUS)) {
			return TRNSCTN + setTransactionStatus(transactionStatusType);
		} else if (transactionStatusType.getStatus().equalsIgnoreCase(PNDG_STATUS)) {
			return TRNSCTN_PNDG + setTransactionStatus(transactionStatusType);
		} else
			return null;
	}

	private String setTransactionStatus(TransactionStatusType transactionStatusType) {
		switch (transactionStatusType.getStatus().toUpperCase()) {
		case CRDT_STATUS: {
			return CREDITED;
		}
		case RJCT_STATUS: {
			return REJECTED;
		}
		case PNDG_STATUS: {
			switch (transactionStatusType.getReason()) {
			case REASON_G000: {
				return REASON_G000_MESSAGE;
			}
			case REASON_G001: {
				return REASON_G001_MESSAGE;
			}
			case REASON_G002: {
				return REASON_G002_MESSAGE;
			}
			case REASON_G003: {
				return REASON_G003_MESSAGE;
			}
			case REASON_G004: {
				return REASON_G004_MESSAGE;
			}
			}
		}
		default: {
			return null;
		}
		}
	}
	
	private String computeDiff(String strDate1, String strDate2) {
		ZonedDateTime dt1 = ZonedDateTime.parse(strDate1);
		ZonedDateTime dt2 = ZonedDateTime.parse(strDate2);
		Duration d = Duration.between( dt1 , dt2 );
		long seconds = d.toMillis() / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		String day = days>0?days + DAY:EMPTY;
		String hour = (hours % 24)>0?hours % 24 + HOUR:EMPTY;
		String min = (minutes % 60)>0?minutes % 60 + MIN:EMPTY;
		String time = day + hour + min; 
		return time;
	}
}
