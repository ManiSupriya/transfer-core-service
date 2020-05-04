/*
package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils.generateBeneficiaryAddress;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitIndiaStrategyTest {

    @InjectMocks
    private QuickRemitIndiaStrategy quickRemitIndiaStrategy;

    @Mock
    private  AccountService accountService;
    @Mock
    private  MobCommonService mobCommonService;
    @Mock
    private  MaintenanceService maintenanceService;
    @Mock
    private  QuickRemitFundTransferMWService quickRemitFundTransferMWService;
    @Mock
    private  FinTxnNoValidator finTxnNoValidator;
    @Mock
    private  AccountBelongsToCifValidator accountBelongsToCifValidator;
    @Mock
    private  PaymentPurposeValidator paymentPurposeValidator;
    @Mock
    private  BeneficiaryValidator beneficiaryValidator;
    @Mock
    private  BalanceValidator balanceValidator;
    @Mock
    private  LimitValidator limitValidator;

    @Test
    public void test() {

        //Given
        BigDecimal limitUsageAmount = new BigDecimal(200);
        String limitVersionUuid = "uuid1234";
        String fromAcct = "019010050532";
        String toAcct = "AE120260001015673975601";
        String channelTraceId = "traceId123";
        String cif = "12345";
        String beneId = "121";
        String productId = "TRTPTIN";
        String purposeDesc = "Medical Expenses";
        String purposeCode = "P130106";
        String finTxnNo = "fin123";
        String branchCode = "083";
        String fullName = "Deepa Shivakumar";
        String bankName = "EMIRATES NBD PJSC";
        String address = "UNITED ARAB EMIRATES";
        String destCurrency = "INR";
        String srcCurrency = "AED";
        BigDecimal srcAmount = new BigDecimal(1000);
        BigDecimal destAmount = new BigDecimal(50);


        QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .amountDESTCurrency(Big)
                .amountSRCCurrency(amountSRCCurrency)
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryAddress(generateBeneficiaryAddress(beneficiaryDto))
                .beneficiaryBankIFSC(beneficiaryDto.getRoutingCode())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCountry(beneficiaryDto.getBankCountry())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .beneficiaryName(StringUtils.defaultIfBlank(beneficiaryDto.getFinalName(), beneficiaryDto.getFullName()))
                .channelTraceId(channelTraceId)
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .destCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .destISOCurrency(INDIA_COUNTRY_ISO)
                .exchangeRate(exchangeRate)
                .finTxnNo(channelTraceId)
                .originatingCountry(ORIGINATING_COUNTRY_ISO)
                .reasonCode(request.getPurposeCode())
                .reasonText(request.getPurposeDesc())
                .senderBankAccount(accountDetails.getNumber())
                .senderCountryISOCode(customerDetails.getNationality())
                .senderBankBranch(customerDetails.getCifBranch())
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderName(accountDetails.getCustomerName())
                .srcCurrency(accountDetails.getCurrency())
                //.srcISOCurrency("784") for AED
                .transactionAmount(request.getAmount().toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .build();
        requestDTO.setToAccount(toAcct);
        requestDTO.setFromAccount(fromAcct);
        requestDTO.setPurposeDesc(purposeDesc);
        requestDTO.setChargeBearer(chargeBearer);
        requestDTO.setPurposeCode(purposeCode);
        requestDTO.setFinTxnNo(finTxnNo);
        requestDTO.setAmount(new BigDecimal(200));
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setBeneficiaryId(beneId);

        FundTransferMetadata metadata =  FundTransferMetadata.builder().primaryCif(cif).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaryCurrency(srcCurrency);
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setSwiftCode(swift);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        final List<AccountDetailsDTO> accountsFromCore = Arrays.asList(AccountDetailsDTO.builder()
                .number(fromAcct).currency(srcCurrency).branchCode(branchCode).build());
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.builder().build()));


    }

}
*/
