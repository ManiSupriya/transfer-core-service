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
    }};

    public static TransferErrorCode getTransferErrorCode(String errorCode) {
        // TODO Handle default null case
        return errorLookUp.get(errorCode);
    }

}
