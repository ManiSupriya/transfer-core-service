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
        put("ACC-CORE-412", TransferErrorCode.NOT_ENOUGH_RESOURCES);
        put("ACC-ESB-2262", TransferErrorCode.SAME_DEBIT_CREDIT_ACC);
        put("ACC-CORE-400", TransferErrorCode.INVALID_REQ_BODY);
        put("ACC-CORE-406", TransferErrorCode.REQ_PARAM_MISSING);
        put("ACC-CORE-407", TransferErrorCode.NO_HANDLER_FOUND);
        put("ACC-CORE-999", TransferErrorCode.SOMETHING_WRONG_IN_ACCOUNT_SEVICE);
        put("ACC-ESB-TIMEOUT", TransferErrorCode.ACC_ESB_TIMEOUT);
        put("QACLNTN-EAI-FCI-BRK-115", TransferErrorCode.QACLNTN_EAI_FCI_BRK_115);
        put("TFTN-EAI-FCI-BRK-2471", TransferErrorCode.TFTN_EAI_FCI_BRK_2471);
        put("EAI-FCI-BRK-15401", TransferErrorCode.FROM_ACCOUNT_DEBIT_NOT_ALLOWED);

    }};

    public static TransferErrorCode getTransferErrorCode(String errorCode) {
        // TODO Handle default null case
        return errorLookUp.get(errorCode);
    }

}
