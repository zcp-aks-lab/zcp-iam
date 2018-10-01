package com.skcc.cloudz.zcp.iam.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.skcc.cloudz.zcp"))
                .paths(PathSelectors.any())
                .build();

    }
	
	@Component
	public class MultipartParameterPlugin implements ExpandedParameterBuilderPlugin {
		@Autowired
		private TypeResolver resolver;
  
		public boolean supports(DocumentationType delimiter) { return true; }

		public void apply(ParameterExpansionContext context) {
			/*
			 * ExpandedParameterBuilder.apply()
			 * https://springfox.github.io/springfox/docs/current/#plugins-available-for-extensibility
			 */
		    ResolvedType resolved = resolver.resolve(context.getField().getType());
		    String parameterType = "query";

		    if(resolved.isInstanceOf(MultipartFile.class)) {
		    	parameterType = "form";
		    }
		    
		    context.getParameterBuilder()
		        .parameterType(parameterType);
		}
	}
}
