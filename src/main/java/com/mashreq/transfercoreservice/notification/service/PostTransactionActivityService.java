package com.mashreq.transfercoreservice.notification.service;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PostTransactionActivityService {

    public  void execute(List<PostTransactionActivityContext> contextList, RequestMetaData requestMetaData) throws IOException, TemplateException {
        for(PostTransactionActivityContext context : contextList){
            context.getPostTransactionActivity().execute(context.getPayload(), requestMetaData);
        }
    }
}
