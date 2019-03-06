package com.skcc.cloudz.zcp.iam.api.resource.repository;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import org.springframework.data.domain.Page;

import io.kubernetes.client.models.V1ListMeta;

public class PageResourceList<T> {
  @SerializedName("apiVersion")
  public String apiVersion = null;

  @SerializedName("items")
  public List<T> items = new ArrayList<T>();

  @SerializedName("kind")
  public String kind = null;

  @SerializedName("metadata")
  public V1ListMeta metadata = null;

  @SerializedName("total")
  public long total;

  public static <T> PageResourceList<T> create(List<T> items){
      PageResourceList<T> obj = new PageResourceList<T>();
      obj.items = items;
      return obj;
  }
  
  public <W> void setPage(Page<W> page){
      this.total = page.getTotalElements();
  }
}