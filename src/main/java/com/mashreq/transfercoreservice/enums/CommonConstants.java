package com.mashreq.transfercoreservice.enums;

import java.math.BigInteger;

public interface CommonConstants {

    interface Profiles {
        String DEV_PROFILE = "dev";
    }

    interface EsbSrv {
        String FUND_TRANSFER = "fundTransfer";
    }

    interface AccType {
        String CURRENTS = "CURRENTS";
    }

    interface General {
        String N = "N";
    }

    interface EsbError {
        String ERROR_014 = "EAI-EMP-BRK-014";
    }

    interface FundTransfer {
        String FUND_TRANSFER = "Fund Transfer";
    }

    interface Common {
        String FILE_TEMP_LOCATION = "/data/";
    }

    interface FreezingLienAmount {
        String OPERATION_CODE = "CollateralCreate";
        String ACTION = "NEW";
        String COLLATERAL_DESCRIPTION = "Lien Card Freezing from DIGITAL";
        String CATEGORY_NAME = "CRD";
        BigInteger MODULE_ID = BigInteger.valueOf(1);
    }

    interface CasaConstants {
        String NEOUSER_CASA_ACCOUNTS = "NEOUSER";
    }
}
