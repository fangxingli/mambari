/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.state.quicklinks;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link{
  @JsonProperty("name")
  private String name;

  @JsonProperty("label")
  private String label;

  @JsonProperty("requires_user_name")
  private String requiresUserName;

  @JsonProperty("url")
  private String url;

  @JsonProperty("template")
  private String template;

  @JsonProperty("port")
  private Port port;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getRequiresUserName() {
    return requiresUserName;
  }

  public void setRequiresUserName(String requiresUserName) {
    this.requiresUserName = requiresUserName;
  }

  public Port getPort() {
    return port;
  }

  public void setPort(Port port) {
    this.port = port;
  }

  public boolean isRemoved(){
    //treat a link as removed if the section only contains a name
    return (null == port && null == url && null == template && null == label && null == requiresUserName);
  }

  public void mergeWithParent(Link parentLink) {
    if (null == parentLink)
        return;

    /* merge happens when a child link has some infor but not all of them.
     * If a child link has nothing but a name, it's treated as being removed from the link list
     */
    if(null == template && null != parentLink.getTemplate())
      template = parentLink.getTemplate();

    if(null == label && null != parentLink.getLabel())
      label = parentLink.getLabel();

    if(null == url && null != parentLink.getUrl())
      url = parentLink.getUrl();

    if(null == template && null != parentLink.getTemplate())
      template = parentLink.getTemplate();

    if(null == requiresUserName && null != parentLink.getRequiresUserName())
      requiresUserName = parentLink.getRequiresUserName();

    if(null == port){
        port = parentLink.getPort();
    } else {
      port.mergetWithParent(parentLink.getPort());
    }
  }
}