package com.mashreq.transfercoreservice.notification.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class PostTransactionActivityContext<P> {

    private PostTransactionActivity<P> postTransactionActivity;
    private P payload;
}
