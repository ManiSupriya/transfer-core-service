package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.QuickRemitType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumMap;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitStrategyTest {

    @InjectMocks
    private QuickRemitStrategy quickRemitStrategy;

    @Mock
    private QuickRemitIndiaStrategy quickRemitIndiaStrategy;

    @Mock
    private QuickRemitPakistanStrategy quickRemitPakistanStrategy;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private EnumMap<QuickRemitType, QuickRemitFundTransfer> fundTransferStrategies;

    @Test
    public void test() {

    }


}
