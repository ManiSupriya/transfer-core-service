package com.mashreq.transfercoreservice.notification.service;


import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PostTransactionActivityService {

    public  void execute(List<PostTransactionActivityContext> contextList) throws IOException, TemplateException {
        for(PostTransactionActivityContext context : contextList){
            context.getPostTransactionActivity().execute(context.getPayload());
        }
    }
}
