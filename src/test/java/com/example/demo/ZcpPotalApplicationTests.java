package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.skcc.cloudz.zcp.member.service.MemberService;

import io.kubernetes.client.ApiException;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "classpath:application-dev.properties")
public class ZcpPotalApplicationTests {

	@Autowired
	private MemberService memberService;

    @Test
    public void test() throws IOException, ApiException {
//        Object obj = memberService.serviceAccountList("zcp-demo", "view");
//        assertThat(obj == null).isEqualTo("Good");
    }

	
}
