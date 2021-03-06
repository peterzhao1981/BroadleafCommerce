/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.common.currency.domain;

import java.util.Currency;

public class NullBroadleafCurrency implements BroadleafCurrency {
    private static final long serialVersionUID = 7926395625817119455L;

    @Override
    public String getCurrencyCode() {
        return null;
    }

    @Override
    public void setCurrencyCode(String code) {
        // Do nothing
    }

    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        // Do nothing
    }

    @Override
    public boolean getDefaultFlag() {
        return false;
    }

    @Override
    public void setDefaultFlag(boolean defaultFlag) {
        // Do nothing
    }

    @Override
    public Currency getJavaCurrency() {
        return null;
    }

}
