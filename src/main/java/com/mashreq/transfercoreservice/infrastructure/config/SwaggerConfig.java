package com.mashreq.transfercoreservice.infrastructure.config;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mashreq.transfercoreservice.common.CommonConstants;


/*
@Configuration
public class SwaggerConfig {

    */
/**
     * Config
     *//*

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors
                        .basePackage(CommonConstants.BASE_PACKAGE))
                .paths(PathSelectors.regex("/.*"))
                .build().apiInfo(apiEndPointsInfo());
    }
    
    private OpenAPI apiEndPointsInfo() {

        return new OpenAPI().info(new Info().title(CommonConstants.TITLE)
                .description(CommonConstants.TITLE)
              //  .contact(new Contact(CommonConstants.MS_SQUAD, "", CommonConstants.MAIL))
                .license(CommonConstants.LICENCE_TEXT)
                .licenseUrl(CommonConstants.LICENCE_URL)
                .version(CommonConstants.VERSION));
    }

}
*/
