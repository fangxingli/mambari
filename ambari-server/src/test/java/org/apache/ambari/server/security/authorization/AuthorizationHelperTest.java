/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.security.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.ambari.server.orm.entities.PermissionEntity;
import org.apache.ambari.server.orm.entities.PrincipalEntity;
import org.apache.ambari.server.orm.entities.PrincipalTypeEntity;
import org.apache.ambari.server.orm.entities.PrivilegeEntity;
import org.apache.ambari.server.orm.entities.ResourceEntity;
import org.apache.ambari.server.orm.entities.ResourceTypeEntity;
import org.apache.ambari.server.orm.entities.RoleAuthorizationEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthorizationHelperTest {

  @Test
  public void testConvertPrivilegesToAuthorities() throws Exception {
    Collection<PrivilegeEntity> privilegeEntities = new ArrayList<PrivilegeEntity>();

    ResourceTypeEntity resourceTypeEntity = new ResourceTypeEntity();
    resourceTypeEntity.setId(1);
    resourceTypeEntity.setName("CLUSTER");

    ResourceEntity resourceEntity = new ResourceEntity();
    resourceEntity.setId(1L);
    resourceEntity.setResourceType(resourceTypeEntity);

    PrincipalTypeEntity principalTypeEntity = new PrincipalTypeEntity();
    principalTypeEntity.setId(1);
    principalTypeEntity.setName("USER");

    PrincipalEntity principalEntity = new PrincipalEntity();
    principalEntity.setPrincipalType(principalTypeEntity);
    principalEntity.setId(1L);

    PermissionEntity permissionEntity1 = new PermissionEntity();
    permissionEntity1.setPermissionName("Permission1");
    permissionEntity1.setResourceType(resourceTypeEntity);
    permissionEntity1.setId(2);
    permissionEntity1.setPermissionName("CLUSTER.USER");

    PermissionEntity permissionEntity2 = new PermissionEntity();
    permissionEntity2.setPermissionName("Permission1");
    permissionEntity2.setResourceType(resourceTypeEntity);
    permissionEntity2.setId(3);
    permissionEntity2.setPermissionName("CLUSTER.ADMINISTRATOR");

    PrivilegeEntity privilegeEntity1 = new PrivilegeEntity();
    privilegeEntity1.setId(1);
    privilegeEntity1.setPermission(permissionEntity1);
    privilegeEntity1.setPrincipal(principalEntity);
    privilegeEntity1.setResource(resourceEntity);

    PrivilegeEntity privilegeEntity2 = new PrivilegeEntity();
    privilegeEntity2.setId(1);
    privilegeEntity2.setPermission(permissionEntity2);
    privilegeEntity2.setPrincipal(principalEntity);
    privilegeEntity2.setResource(resourceEntity);

    privilegeEntities.add(privilegeEntity1);
    privilegeEntities.add(privilegeEntity2);

    Collection<GrantedAuthority> authorities = new AuthorizationHelper().convertPrivilegesToAuthorities(privilegeEntities);

    assertEquals("Wrong number of authorities", 2, authorities.size());

    Set<String> authorityNames = new HashSet<String>();

    for (GrantedAuthority authority : authorities) {
      authorityNames.add(authority.getAuthority());
    }
    Assert.assertTrue(authorityNames.contains("CLUSTER.USER@1"));
    Assert.assertTrue(authorityNames.contains("CLUSTER.ADMINISTRATOR@1"));
  }

  @Test
  public void testAuthName() throws Exception {
    String user = AuthorizationHelper.getAuthenticatedName();
    Assert.assertNull(user);

    Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    user = AuthorizationHelper.getAuthenticatedName();
    Assert.assertEquals("admin", user);

  }

  @Test
  public void testIsAuthorized() {
    RoleAuthorizationEntity readOnlyRoleAuthorizationEntity = new RoleAuthorizationEntity();
    readOnlyRoleAuthorizationEntity.setAuthorizationId(RoleAuthorization.CLUSTER_VIEW_METRICS.getId());

    RoleAuthorizationEntity privilegedRoleAuthorizationEntity = new RoleAuthorizationEntity();
    privilegedRoleAuthorizationEntity.setAuthorizationId(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS.getId());

    RoleAuthorizationEntity administratorRoleAuthorizationEntity = new RoleAuthorizationEntity();
    administratorRoleAuthorizationEntity.setAuthorizationId(RoleAuthorization.AMBARI_MANAGE_USERS.getId());

    ResourceTypeEntity clusterResourceTypeEntity = new ResourceTypeEntity();
    clusterResourceTypeEntity.setId(1);
    clusterResourceTypeEntity.setName(ResourceType.CLUSTER.name());

    ResourceTypeEntity cluster2ResourceTypeEntity = new ResourceTypeEntity();
    cluster2ResourceTypeEntity.setId(2);
    cluster2ResourceTypeEntity.setName(ResourceType.CLUSTER.name());

    ResourceEntity clusterResourceEntity = new ResourceEntity();
    clusterResourceEntity.setResourceType(clusterResourceTypeEntity);
    clusterResourceEntity.setId(1L);

    ResourceEntity cluster2ResourceEntity = new ResourceEntity();
    cluster2ResourceEntity.setResourceType(cluster2ResourceTypeEntity);
    cluster2ResourceEntity.setId(2L);

    PermissionEntity readOnlyPermissionEntity = new PermissionEntity();
    readOnlyPermissionEntity.setAuthorizations(Collections.singleton(readOnlyRoleAuthorizationEntity));

    PermissionEntity privilegedPermissionEntity = new PermissionEntity();
    privilegedPermissionEntity.setAuthorizations(Arrays.asList(readOnlyRoleAuthorizationEntity,
        privilegedRoleAuthorizationEntity));

    PermissionEntity administratorPermissionEntity = new PermissionEntity();
    administratorPermissionEntity.setAuthorizations(Arrays.asList(readOnlyRoleAuthorizationEntity,
        privilegedRoleAuthorizationEntity,
        administratorRoleAuthorizationEntity));

    PrivilegeEntity readOnlyPrivilegeEntity = new PrivilegeEntity();
    readOnlyPrivilegeEntity.setPermission(readOnlyPermissionEntity);
    readOnlyPrivilegeEntity.setResource(clusterResourceEntity);

    PrivilegeEntity readOnly2PrivilegeEntity = new PrivilegeEntity();
    readOnly2PrivilegeEntity.setPermission(readOnlyPermissionEntity);
    readOnly2PrivilegeEntity.setResource(cluster2ResourceEntity);

    PrivilegeEntity privilegedPrivilegeEntity = new PrivilegeEntity();
    privilegedPrivilegeEntity.setPermission(privilegedPermissionEntity);
    privilegedPrivilegeEntity.setResource(clusterResourceEntity);

    PrivilegeEntity privileged2PrivilegeEntity = new PrivilegeEntity();
    privileged2PrivilegeEntity.setPermission(privilegedPermissionEntity);
    privileged2PrivilegeEntity.setResource(cluster2ResourceEntity);

    PrivilegeEntity administratorPrivilegeEntity = new PrivilegeEntity();
    administratorPrivilegeEntity.setPermission(administratorPermissionEntity);
    administratorPrivilegeEntity.setResource(clusterResourceEntity);

    GrantedAuthority readOnlyAuthority = new AmbariGrantedAuthority(readOnlyPrivilegeEntity);
    GrantedAuthority readOnly2Authority = new AmbariGrantedAuthority(readOnly2PrivilegeEntity);
    GrantedAuthority privilegedAuthority = new AmbariGrantedAuthority(privilegedPrivilegeEntity);
    GrantedAuthority privileged2Authority = new AmbariGrantedAuthority(privileged2PrivilegeEntity);
    GrantedAuthority administratorAuthority = new AmbariGrantedAuthority(administratorPrivilegeEntity);

    Authentication noAccessUser = new TestAuthentication(Collections.<AmbariGrantedAuthority>emptyList());
    Authentication readOnlyUser = new TestAuthentication(Collections.singleton(readOnlyAuthority));
    Authentication privilegedUser = new TestAuthentication(Arrays.asList(readOnlyAuthority, privilegedAuthority));
    Authentication privileged2User = new TestAuthentication(Arrays.asList(readOnly2Authority, privileged2Authority));
    Authentication administratorUser = new TestAuthentication(Collections.singleton(administratorAuthority));

    SecurityContext context = SecurityContextHolder.getContext();

    // No user (explicit)...
    assertFalse(AuthorizationHelper.isAuthorized(null, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));

    // No user (from context)
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));

    // Explicit user tests...
    assertFalse(AuthorizationHelper.isAuthorized(noAccessUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(noAccessUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(noAccessUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    assertTrue(AuthorizationHelper.isAuthorized(readOnlyUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(readOnlyUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(readOnlyUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    assertTrue(AuthorizationHelper.isAuthorized(privilegedUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertTrue(AuthorizationHelper.isAuthorized(privilegedUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(privilegedUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    assertFalse(AuthorizationHelper.isAuthorized(privileged2User, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(privileged2User, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(privileged2User, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    assertTrue(AuthorizationHelper.isAuthorized(administratorUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertTrue(AuthorizationHelper.isAuthorized(administratorUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertTrue(AuthorizationHelper.isAuthorized(administratorUser, ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    // Context user tests...
    context.setAuthentication(noAccessUser);
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));

    context.setAuthentication(readOnlyUser);
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    context.setAuthentication(privilegedUser);
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    context.setAuthentication(privileged2User);
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertFalse(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));

    context.setAuthentication(administratorUser);
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_VIEW_METRICS)));
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.CLUSTER_TOGGLE_KERBEROS)));
    assertTrue(AuthorizationHelper.isAuthorized(ResourceType.CLUSTER, 1L, EnumSet.of(RoleAuthorization.AMBARI_MANAGE_USERS)));
  }

  private class TestAuthentication implements Authentication {
    private final Collection<? extends GrantedAuthority> grantedAuthorities;

    public TestAuthentication(Collection<? extends GrantedAuthority> grantedAuthorities) {
      this.grantedAuthorities = grantedAuthorities;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return grantedAuthorities;
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return null;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
      return null;
    }
  }
}
