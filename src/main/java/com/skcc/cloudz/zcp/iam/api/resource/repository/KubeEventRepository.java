package com.skcc.cloudz.zcp.iam.api.resource.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface KubeEventRepository extends MongoRepository<KubeEvent, String> {

}