/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.xdi.oxauth.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.oxauth.model.ldap.UserGroup;

import com.unboundid.ldap.sdk.Filter;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 27/07/2012
 * @author Yuriy Movchan Date: 04/11/2014
 */
@Scope(ScopeType.APPLICATION)
@Name("userGroupService")
@AutoCreate
public class UserGroupService {

    @Logger
    private Log log;

    @In
    private LdapEntryManager ldapEntryManager;

    public UserGroup loadGroup(String p_groupDN) {
        try {
            if (StringUtils.isNotBlank(p_groupDN)) {
                return ldapEntryManager.find(UserGroup.class, p_groupDN);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return null;
    }

    public boolean isUserInGroup(String p_groupDN, String p_userDN) {
        final UserGroup group = loadGroup(p_groupDN);
        if (group != null) {
            final String[] member = group.getMember();
            if (member != null) {
                return Arrays.asList(member).contains(p_userDN);
            }
        }
        return false;
    }

    public boolean isUserInGroupOrMember(String groupDn, String personDn) {
		Filter ownerFilter = Filter.createEqualityFilter("owner", personDn);
		Filter memberFilter = Filter.createEqualityFilter("member", personDn);
		Filter searchFilter = Filter.createORFilter(ownerFilter, memberFilter);

		boolean isMemberOrOwner = false;
		try {
			isMemberOrOwner = ldapEntryManager.findEntries(groupDn, UserGroup.class, searchFilter, 1).size() > 0;

		} catch (EntryPersistenceException ex) {
			log.error("Failed to determine if person '{0}' memeber or owner of group '{1}'", ex, personDn, groupDn);
		}

		return isMemberOrOwner;
	}

    public boolean isInAnyGroup(String[] p_groupDNs, String p_userDN) {
        return p_groupDNs != null && isInAnyGroup(Arrays.asList(p_groupDNs), p_userDN);
    }

    public boolean isInAnyGroup(List<String> p_groupDNs, String p_userDN) {
        if (p_groupDNs != null && !p_groupDNs.isEmpty() && p_userDN != null && !p_userDN.isEmpty()) {
            for (String groupDN : p_groupDNs) {
                if (isUserInGroup(groupDN, p_userDN)) {
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * Get UserGroupService instance
	 * 
	 * @return UserGroupService instance
	 */
	public static UserGroupService instance() {
		return (UserGroupService) Component.getInstance(UserGroupService.class);
	}
}
