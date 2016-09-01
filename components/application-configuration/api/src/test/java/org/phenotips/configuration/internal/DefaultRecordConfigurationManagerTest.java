/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.configuration.internal;

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationManager;
import org.phenotips.configuration.RecordConfigurationModule;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

/**
 * Tests for the default {@link RecordConfigurationManager} implementation,
 * {@link GlobalRecordConfiguration}.
 *
 * @version $Id$
 */
public class DefaultRecordConfigurationManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<RecordConfigurationManager> mocker = new MockitoComponentMockingRule<RecordConfigurationManager>(
            DefaultRecordConfigurationManager.class);

    @Mock
    private XWikiContext context;

    @Mock
    private XWiki xwiki;

    @Mock
    private RecordConfiguration config;

    @Mock
    private RecordConfigurationModule moduleOne;

    @Mock
    private RecordConfigurationModule moduleTwo;

    @Mock
    private RecordConfigurationModule moduleThree;

    @Mock
    private Provider<List<RecordConfigurationModule>> modules;

    private List<RecordConfigurationModule> moduleList;

    @Before
    public void setup() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
        resetMocks();
        ReflectionUtils.setFieldValue(this.mocker.getComponentUnderTest(), "modules", modules);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void callsGetActiveConfiguration() throws ComponentLookupException
    {
        // Fix me
        Assert.assertEquals(this.config, this.mocker.getComponentUnderTest().getActiveConfiguration());
    }

    @Test
    public void defaultDecisionIsDeny() throws ComponentLookupException
    {
        doReturn(new LinkedList<>()).when(this.modules).get();
        Assert.assertNotEquals(this.config, this.mocker.getComponentUnderTest().getConfiguration(""));
    }

    @Test
    public void moduleDecisionIsUsed() throws Exception
    {
        // Fix me
        Set<RecordConfigurationModule> moduleList;

        moduleList = Collections.singleton(this.moduleOne);
        doReturn(moduleList).when(this.modules).get();

        when(this.moduleOne.process(this.config)).thenReturn(null);
        Assert.assertNull(this.mocker.getComponentUnderTest().getConfiguration(""));

        when(this.moduleOne.process(this.config)).thenReturn(this.config);
        Assert.assertEquals(null, this.mocker.getComponentUnderTest().getConfiguration(""));

    }

    @Test
    public void modulesAreCascadedUntilNonNullIsReturned() throws Exception
    {
        this.moduleList = Arrays.asList(this.moduleOne, this.moduleTwo, this.moduleThree);
        doReturn(this.moduleList).when(this.modules).get();

        // By default all modules return null
        Assert.assertNull(this.mocker.getComponentUnderTest().getConfiguration(""));
        InOrder order = Mockito.inOrder(this.moduleOne, this.moduleTwo, this.moduleThree);
        order.verify(this.moduleOne).process(this.config);
        order.verify(this.moduleTwo).process(this.config);
        order.verify(this.moduleThree).process(this.config);

        resetMocks();
        when(this.moduleOne.process(this.config)).thenReturn(this.config);
        Assert.assertEquals(this.config, this.mocker.getComponentUnderTest().getConfiguration(""));
        order.verify(this.moduleOne).process(this.config);
        order.verify(this.moduleTwo, never()).process(this.config);
        order.verify(this.moduleThree, never()).process(this.config);

        resetMocks();
        when(this.moduleOne.process(this.config)).thenReturn(null);
        Assert.assertEquals(this.config, this.mocker.getComponentUnderTest().getConfiguration(""));
        order.verify(this.moduleOne).process(this.config);
        order.verify(this.moduleTwo, never()).process(this.config);
        order.verify(this.moduleThree, never()).process(this.config);

        resetMocks();
        when(this.moduleOne.process(this.config)).thenReturn(this.config);
        Assert.assertEquals(this.config, this.mocker.getComponentUnderTest().getConfiguration(""));
        order.verify(this.moduleOne).process(this.config);
        order.verify(this.moduleTwo).process(this.config);
        order.verify(this.moduleThree, never()).process(this.config);
    }

    @Test
    public void firstNonNullDecisionIsReturned() throws Exception
    {
        this.moduleList = Arrays.asList(this.moduleOne, this.moduleTwo);
        doReturn(this.moduleList).when(this.modules).get();

        when(this.moduleOne.process(this.config)).thenReturn(config);
        when(this.moduleTwo.process(this.config)).thenReturn(null);

        Assert.assertEquals(config, this.mocker.getComponentUnderTest().getConfiguration(""));

        when(this.moduleOne.process(this.config)).thenReturn(null);
        when(this.moduleTwo.process(this.config)).thenReturn(config);

        Assert.assertNotEquals(config, this.mocker.getComponentUnderTest().getConfiguration(""));
    }

    @Test(expected = NullPointerException.class)
    public void exceptionsInModulesAreIgnored() throws Exception
    {
        this.moduleList = Arrays.asList(this.moduleOne, this.moduleTwo);
        doReturn(this.moduleList).when(this.modules).get();

        when(this.moduleOne.process(this.config)).thenThrow(new NullPointerException());
        when(this.moduleTwo.process(this.config)).thenReturn(this.config);
        Assert.assertNull(this.mocker.getComponentUnderTest().getConfiguration(""));
    }

    private void resetMocks()
    {
        Mockito.reset(this.moduleOne, this.moduleTwo, this.moduleThree);
        when(this.moduleOne.process(this.config)).thenReturn(null);
        when(this.moduleTwo.process(this.config)).thenReturn(null);
        when(this.moduleThree.process(this.config)).thenReturn(null);
    }

}
