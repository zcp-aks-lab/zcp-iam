package com.example.demo;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.skcc.cloudz.zcp.iam.api.user.service.UserService;

import io.kubernetes.client.ApiException;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "classpath:application-dev.properties")
public class ZcpPotalApplicationTests {

	@Autowired
	private UserService memberService;

    @Test
    public void test() throws IOException, ApiException {
//        Object obj = memberService.serviceAccountList("zcp-demo", "view");
//        assertThat(obj == null).isEqualTo("Good");
    }

	
}
