/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketbox.cdi.test.idm;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketbox.cdi.PicketBoxIdentity;
import org.picketbox.cdi.test.arquillian.ArchiveUtil;
import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.identity.impl.EntityManagerContext;
import org.picketlink.cdi.credential.Credential;
import org.picketlink.cdi.credential.LoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.internal.jpa.AbstractJPAIdentityManagerTestCase;

/**
 * <p>Test for the PicketLink IDM support.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@RunWith(Arquillian.class)
public class IdentityManagerTestCase extends AbstractJPAIdentityManagerTestCase {

    protected static final String USER_NAME = "pedroigor";
    protected static final String USER_PASSWORD = "123";

    @Inject
    private PicketBoxIdentity identity;

    @Inject
    private LoginCredentials credential;

    @Inject
    private IdentityManager identityManager;
    
    @Override
    public void onSetupTest() throws Exception {
        super.onSetupTest();
        EntityManagerContext.set(this.entityManager);
    }
    
    @Override
    public void onFinishTest() throws Exception {
        super.onFinishTest();
        EntityManagerContext.clear();
    }
    
    /**
     * <p>
     * Creates a simple {@link WebArchive} for deployment with the necessary structure/configuration to run the tests.
     * </p>
     *
     * @return
     */
    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ArchiveUtil.createTestArchive();

        archive.addPackages(true, IdentityManagerTestCase.class.getPackage());

        return archive;
    }

    /**
     * <p>Creates an user using the PicketLink IDM API and performs an authentication.</p>
     *
     * @throws Exception
     */
    @Test
    public void testAuthentication() throws Exception {
        User abstractj = this.identityManager.createUser("pedroigor");

        abstractj.setEmail("pedroigor@picketbox.com");
        abstractj.setFirstName("Pedro");
        abstractj.setLastName("Igor");
        
        this.identityManager.updatePassword(abstractj, "123");
        
        Role roleDeveloper = this.identityManager.createRole("developer");
        Role roleAdmin = this.identityManager.createRole("admin");

        Group groupCoreDeveloper = identityManager.createGroup("PicketBox Group");

        this.identityManager.grantRole(roleDeveloper, abstractj, groupCoreDeveloper);
        this.identityManager.grantRole(roleAdmin, abstractj, groupCoreDeveloper);
        
        populateUserCredential();

        this.identity.login();

        Assert.assertTrue(this.identity.isLoggedIn());

        Assert.assertTrue(identity.hasRole("developer"));
        Assert.assertTrue(identity.hasRole("admin"));
    }

    /**
     * <p>
     * Populates the {@link LoginCredential} with the username and password.
     * </p>
     */
    private void populateUserCredential() {
        credential.setUserId(USER_NAME);
        credential.setCredential(new Credential<UsernamePasswordCredential>() {

            @Override
            public UsernamePasswordCredential getValue() {
                return new UsernamePasswordCredential(USER_NAME, USER_PASSWORD);
            }
        });
    }

}