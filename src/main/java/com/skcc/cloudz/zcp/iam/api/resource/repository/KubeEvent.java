package com.skcc.cloudz.zcp.iam.api.resource.repository;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.util.ObjectUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.mapping.Document;

import io.kubernetes.client.models.V1Event;

@Document(collection = "resources")
public class KubeEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String cluster = "";
    private String namespace;
    private V1Event event;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public V1Event getEvent() {
        return event;
    }
    public void setEvent(V1Event event) {
        this.event = event;
    }
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public static KubeEvent convert(V1Event event) {
        KubeEvent row = new KubeEvent();
        row.id = event.getMetadata().getName();
        row.event = event;
        row.namespace= event.getInvolvedObject().getNamespace();
        // row.createTimestamp = event.getMetadata().getCreationTimestamp();
        return row;
    }

    /**
     * https://github.com/spring-projects/spring-data-mongodb/blob/master/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/convert/MongoExampleMapper.java#L283
     * https://github.com/spring-projects/spring-data-mongodb/blob/master/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/query/MongoRegexCreator.java#L83
     * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example.matchers
     */
    public static Example<KubeEvent> cond(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            // return Example.of(new KubeEvent(), ExampleMatcher.matchingAll());
            return null;
        }

        KubeEvent probe = new KubeEvent();
        probe.id = probe.namespace= keyword;

        ExampleMatcher matcher = ExampleMatcher.matchingAny();
        matcher = matcher.withIgnorePaths("cluster");
        matcher = contains(matcher, "namespace", "id");
        Example<KubeEvent> e = Example.of(probe, matcher);
        return e;
    }

    public static ExampleMatcher contains(ExampleMatcher matcher, String... props){
        for (String p : props){
            matcher = matcher.withMatcher(p, m -> m.regex().contains());
        }
        return matcher;
    }
}