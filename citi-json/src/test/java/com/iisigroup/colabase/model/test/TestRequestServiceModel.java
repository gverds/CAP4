package com.iisigroup.colabase.model.test;

import com.iisigroup.colabase.model.RequestContent;
import com.iisigroup.colabase.model.ResponseContent;
import com.iisigroup.colabase.service.test.TestService2;
import com.iisigroup.colabase.service.test.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author AndyChen
 * @version <ul>
 *          <li>2018/5/16 AndyChen,new
 *          </ul>
 * @since 2018/5/16
 */
public class TestRequestServiceModel extends RequestContent {

    @Autowired
    private TestServiceImpl testService;
    @Autowired
    private TestService2 testService2;

    @Override
    public void afterSendRequest(ResponseContent responseContent) {
        testService.testMethod();
        testService2.testMethod();
    }
}
