package com.mashreq.transfercoreservice.client;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.webcore.dto.response.Response;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.split;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * @author shahbazkh
 * @date 4/1/20
 */
public class ErrorUtils {


    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails() + "," +  response.getMessage();
        }
        return response.getErrorCode();
    }
    
    /**
    *
    * @param externalErrorMap
    * @param receivedErrorCodes
    * @return
    */
   public static Optional<String> getErrorHandlingStrategy(Map<String, String> externalErrorMap, String... receivedErrorCodes) {
       if (null == externalErrorMap || null == receivedErrorCodes)
           return Optional.empty();

       return asList(receivedErrorCodes).stream()
               .filter(error -> externalErrorMap.containsKey(error))
               .map(error -> externalErrorMap.get(error))
               .findFirst();

   }
   
   /**
   *
   * @param genericException
   * @return
   */
  public static String[] getAllErrorCodesFromGenericException(GenericException genericException) {
      return split(genericException.getErrorDetails(), ',');
  }

}
