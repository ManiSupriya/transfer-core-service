package com.mashreq.transfercoreservice.errors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
public class FundTransferErrorMapper {

    private static final Map<String, TransferErrorCode> errorLookUp = new HashMap<String, TransferErrorCode>() {{
        put("ACC-ESB-13239", TransferErrorCode.FROM_ACCOUNT_IS_INVALID);
        put("ACC-5001", TransferErrorCode.TO_ACCOUNT_IS_INVALID);
        put("ACC-CORE-412", TransferErrorCode.FROM_ACCOUNT_IS_NON_ACTIVE);
        put("ACC-ESB-2262", TransferErrorCode.SAME_DEBIT_CREDIT_ACC);
        put("ACC-CORE-400", TransferErrorCode.INVALID_REQ_BODY);
        put("ACC_CORE_406", TransferErrorCode.REQ_PARAM_MISSING);
        put("ACC_CORE_407", TransferErrorCode.NO_HANDLER_FOUND);
        put("ACC_CORE_999", TransferErrorCode.SOMETHING_WRONG_IN_ACCOUNT_SEVICE);
        put("ACC_ESB_TIMEOUT", TransferErrorCode.ACC_ESB_TIMEOUT);
        put("QACLNTN_EAI_FCI_BRK_115", TransferErrorCode.QACLNTN_EAI_FCI_BRK_115);
        put("TFTN_EAI_FCI_BRK_2471", TransferErrorCode.TFTN_EAI_FCI_BRK_2471);
    }};

    public static TransferErrorCode getTransferErrorCode(String errorCode) {
        // TODO Handle default null case
        return errorLookUp.get(errorCode);
    }

}
