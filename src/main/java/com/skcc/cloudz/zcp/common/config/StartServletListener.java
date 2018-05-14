package com.skcc.cloudz.zcp.common.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebListener
class StartServletListener implements ServletContextListener{

	final Logger LOG = LoggerFactory.getLogger(StartServletListener.class);
	
  @Override
  public void contextInitialized (ServletContextEvent sce) {
      LOG.debug("ServletContextListener start....");
  }

  @Override
  public void contextDestroyed (ServletContextEvent sce) {

  }
}