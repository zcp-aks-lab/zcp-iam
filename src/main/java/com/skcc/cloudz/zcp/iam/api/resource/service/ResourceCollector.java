package com.skcc.cloudz.zcp.iam.api.resource.service;

import java.util.List;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.iam.api.resource.repository.KubeEvent;
import com.skcc.cloudz.zcp.iam.api.resource.repository.KubeEventRepository;
import com.skcc.cloudz.zcp.iam.api.resource.repository.PageResourceList;
import com.skcc.cloudz.zcp.iam.manager.KubeResourceManager;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyHolder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1EventList;

@Service
public class ResourceCollector {
    private final Logger log = LoggerFactory.getLogger(ResourceCollector.class);

    @Autowired
    private KubeResourceManager manager;

    @Autowired
    private KubeEventRepository repository;

    @Value("${keycloak.master.username}")
    private String username;

    @Scheduled(fixedDelay = 5000)
    public void collect() {
        collect("");
    }

    public void collect(String cluster) {
        try {
            ServiceAccountApiKeyHolder.instance().setToken(username);
            V1EventList ev = manager.getList("", "event");
            List<KubeEvent> kev = ev.getItems().stream().map(KubeEvent::convert).collect(Collectors.toList());
            kev = repository.save(kev);

            int saved = kev.size();
            log.debug("{} event(s) saved in the cluster '{}'.", saved, cluster);
        } catch (Exception e) {
            log.info("Fail to collect events. [err-msg={}]", e.getMessage());
            log.debug("", e);
        }
    }

    public PageResourceList<V1Event> getList(String namespace, String keyword, Pageable pageable) {
        Page<KubeEvent> page = null;
        pageable = toAggregation(pageable);

        if (isEmpty(keyword)) {
            page = repository.findAll(pageable);
        } else {
            Example<KubeEvent> cond = KubeEvent.cond(keyword);
            page = repository.findAll(cond, pageable);
        }

        List<V1Event> items = page.getContent().stream().map(KubeEvent::getEvent).collect(Collectors.toList());
        PageResourceList<V1Event> res = PageResourceList.create(items);
        res.setPage(page);

        return res;
    }

	public Object getResource(String namesapce, String kind, String name, String type) {
        KubeEvent page = repository.findOne(name);
		return page != null ? page.getEvent() : null;
    }
    
    private Pageable toAggregation(Pageable p) {
        if (p.getSort() == null)
            return p;
        
        Order o = p.getSort().iterator().next();
        // AggregationOperation sort = Aggregation.sort(o.getDirection(), "event." + o.getProperty());
        return new PageRequest(p.getPageNumber(), p.getPageSize(), o.getDirection(), "event." + o.getProperty());
    }

    private boolean isEmpty(String s){
        return StringUtils.isBlank(s) || "undefined".equals(s) || "null".equals(s);
    }
}