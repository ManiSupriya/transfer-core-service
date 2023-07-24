package com.mashreq.transfercoreservice.config.notification;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Loads email template based on the template name
 *
 * @author Thangiachalam P
 */
@Slf4j
@Component
public class EmailTemplateHelper {

    @Autowired
    @Qualifier("freeMarkerGenericTemplateConfiguration")
    private Configuration freemarkerConfig;

    /**
     * Returns email template based on template name
     *
     * @param templateName
     * @param templateValues
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String getEmailTemplate(String templateName, Map<String, String> templateValues) throws IOException, TemplateException {
        //freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");

        Template template = freemarkerConfig.getTemplate(templateName);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, templateValues);

    }
}