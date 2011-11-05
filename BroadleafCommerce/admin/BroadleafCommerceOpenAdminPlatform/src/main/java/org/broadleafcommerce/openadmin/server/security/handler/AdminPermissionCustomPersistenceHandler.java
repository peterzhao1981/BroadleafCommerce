/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.server.security.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.openadmin.client.dto.Entity;
import org.broadleafcommerce.openadmin.client.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.client.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.client.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.client.dto.Property;
import org.broadleafcommerce.openadmin.client.service.ServiceException;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.security.domain.AdminPermission;
import org.broadleafcommerce.openadmin.server.security.service.type.PermissionType;
import org.broadleafcommerce.openadmin.server.service.handler.CustomPersistenceHandlerAdapter;
import org.broadleafcommerce.openadmin.server.service.persistence.module.RecordHelper;

import java.util.Map;

/**
 * @author Jeff Fischer
 */
public class AdminPermissionCustomPersistenceHandler extends CustomPersistenceHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(AdminPermissionCustomPersistenceHandler.class);

    @Override
    public Boolean canHandleAdd(PersistencePackage persistencePackage) {
        String ceilingEntityFullyQualifiedClassname = persistencePackage.getCeilingEntityFullyQualifiedClassname();
        return AdminPermission.class.getName().equals(ceilingEntityFullyQualifiedClassname);
    }

    @Override
    public Boolean canHandleUpdate(PersistencePackage persistencePackage) {
        return canHandleAdd(persistencePackage);
    }

    @Override
    public Entity add(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        if (persistencePackage.getEntity().findProperty("id") != null && !StringUtils.isEmpty(persistencePackage.getEntity().findProperty("id").getValue())) {
            return update(persistencePackage, dynamicEntityDao, helper);
        }
        Entity entity = checkPermissionName(persistencePackage);
		try {
			PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
			AdminPermission adminInstance = (AdminPermission) Class.forName(entity.getType()[0]).newInstance();
			Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(AdminPermission.class.getName(), persistencePerspective);
			adminInstance = (AdminPermission) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);

            adminInstance = (AdminPermission) dynamicEntityDao.merge(adminInstance);

			Entity adminEntity = helper.getRecord(adminProperties, adminInstance, null, null);

			return adminEntity;
		} catch (Exception e) {
            LOG.error("Unable to add entity for " + entity.getType()[0], e);
			throw new ServiceException("Unable to add entity for " + entity.getType()[0], e);
		}
    }

    protected Entity checkPermissionName(PersistencePackage persistencePackage) throws ServiceException {
        Entity entity  = persistencePackage.getEntity();
        Property prop = entity.findProperty("name");
        String name = prop.getValue();
        name = name.toUpperCase();
        if (!name.startsWith("PERMISSION_")) {
            throw new ServiceException("All Permission names must start with PERMISSION_");
        }
        String[] parts = name.split("_");
        if (parts.length < 3) {
            throw new ServiceException("All Permission names must adhere to the naming standard: PERMISSION_[Permission Type]_[User Defined Section]. E.g. PERMISSION_READ_CATEGORY");
        }
        if (PermissionType.getInstance(parts[1]) == null) {
            throw new ServiceException("All Permission names must specify a valid permission type as part of the name. The permission name you specified (" + name + ") denotes the permission type of (" + parts[1] + "), which is not valid. See org.broadleafcommerce.openadmin.server.security.service.type.PermissionType for valid permission types.");
        }
        prop.setValue(name);
        return entity;
    }

    @Override
    public Entity update(PersistencePackage persistencePackage, DynamicEntityDao dynamicEntityDao, RecordHelper helper) throws ServiceException {
        Entity entity = checkPermissionName(persistencePackage);
		try {
			PersistencePerspective persistencePerspective = persistencePackage.getPersistencePerspective();
			Map<String, FieldMetadata> adminProperties = helper.getSimpleMergedProperties(AdminPermission.class.getName(), persistencePerspective);
			Object primaryKey = helper.getPrimaryKey(entity, adminProperties);
            AdminPermission adminInstance = (AdminPermission) dynamicEntityDao.retrieve(Class.forName(entity.getType()[0]), primaryKey);
			adminInstance = (AdminPermission) helper.createPopulatedInstance(adminInstance, entity, adminProperties, false);

            adminInstance = (AdminPermission) dynamicEntityDao.merge(adminInstance);

			Entity adminEntity = helper.getRecord(adminProperties, adminInstance, null, null);

			return adminEntity;
		} catch (Exception e) {
            LOG.error("Unable to update entity for " + entity.getType()[0], e);
			throw new ServiceException("Unable to update entity for " + entity.getType()[0], e);
		}
    }

}
