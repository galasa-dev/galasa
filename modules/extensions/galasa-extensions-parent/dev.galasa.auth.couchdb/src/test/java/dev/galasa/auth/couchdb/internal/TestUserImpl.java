/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import static org.assertj.core.api.Assertions.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import dev.galasa.auth.couchdb.internal.beans.FrontEndClient;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IUser;


public class TestUserImpl {

    @Test
    public void testCanConstructUserImplGivenAnIUser() throws Exception  {
        // Given...
        UserImpl docInput = new UserImpl();
        docInput.setUserNumber("1234");
        docInput.setLoginId("myLoginId");
        docInput.setRoleId("role-id");

        // When..
        UserImpl docOutput = new UserImpl(docInput);
        
        // Then...
        assertThat(docOutput.getLoginId()).isEqualTo("myLoginId");
        assertThat(docOutput.getUserNumber()).isEqualTo("1234");
    }



    /**
     * @throws Exception
     */
    @Test
    public void testDefaultUserRoleIdIsAlwaysSet() throws Exception  {
        // Given...
        UserImpl docInput = new UserImpl();
        docInput.setUserNumber("1234");
        docInput.setLoginId("myLoginId");
        // Don't set the user role.

        // When..
        UserImpl docOutput = new UserImpl(docInput);
        
        // Then...
        assertThat(docOutput.getRoleId()).isEqualTo(IUser.DEFAULT_ROLE_ID_WHEN_MISSING);
    }

    @Test
    public void testCanConstructUserDocGivenAUserDocWithClient() throws Exception  {

        UserImpl docInput = new UserImpl();

        docInput.setLoginId("myLoginId");
        docInput.addClient(new FrontEndClient("myClient1",Instant.MIN));

        IFrontEndClient clientGotBack = docInput.getClient("myClient1");
        assertThat(clientGotBack).isNotNull();
        assertThat(clientGotBack.getClientName()).isEqualTo("myClient1");
        assertThat(clientGotBack.getLastLogin()).isEqualTo(Instant.MIN);
    }

    @Test
    public void testCanLookForClientWhichIsntInTheUserDocReturnsNull() throws Exception  {
        
        UserImpl docInput = new UserImpl();

        docInput.setLoginId("myLoginId");
        docInput.addClient(new FrontEndClient("myClient1",Instant.MIN));

        IFrontEndClient clientGotBack = docInput.getClient("myClient2"); // Client2 isn't there!
        assertThat(clientGotBack).isNull();
    }

    @Test
    public void testCanTryLookingForClientWithNullNameShouldReturnNull() throws Exception  {
        
        UserImpl docInput = new UserImpl();

        docInput.setLoginId("myLoginId");
        docInput.addClient(new FrontEndClient("myClient1",Instant.MIN));

        IFrontEndClient clientGotBack = docInput.getClient(null); 
        assertThat(clientGotBack).isNull();
    }

    class MockIUser implements IUser {
        private String userNumber;
        private String loginId;
        private String roleId;

        private List<IFrontEndClient> clients;

        public MockIUser(String loginId, List<IFrontEndClient> clients, String roleId ) {
            this.loginId = loginId;
            this.clients = clients;
            this.roleId = roleId;
        }

        @Override
        public String getUserNumber(){
            return userNumber;
        }

        @Override
        public String getLoginId() {
            return loginId;
        }

        @Override
        public IFrontEndClient getClient(String clientName) {
            return null;
        }

        @Override
        public Collection<IFrontEndClient> getClients() {
            return this.clients;
        }

        @Override
        public void addClient(IFrontEndClient client) {
            clients.add( new FrontEndClient(client));
        }

        @Override
        public String getVersion() {
            return "0" ;
        }

        @Override
        public String getRoleId() {
            return this.roleId;
        }

        @Override
        public void setRoleId(String newRoleId) {
            this.roleId = newRoleId ;
        }

    }

    @Test
    public void testCanCloneUserDocFromIUser() throws Exception {

        UserImpl docInput = new UserImpl();

        docInput.setLoginId("myLoginId");
        docInput.setRoleId("myRoleId");
        docInput.addClient(new FrontEndClient("myClient1",Instant.MIN));

        assertThat(docInput).isNotNull();
        assertThat(docInput.getLoginId()).isEqualTo("myLoginId");
    }

    class MockIFrontEndClient implements IFrontEndClient {

        String name ; 
        Instant lastLogin ;

        public MockIFrontEndClient(String name, Instant lastLogin) {
            this.name = name;
            this.lastLogin = lastLogin;
        }

		@Override
		public String getClientName() {
			return name;
		}

		@Override
		public Instant getLastLogin() {
			return lastLogin;
		}

		@Override
		public void setLastLogin(Instant lastLoginTime) {
		}

    }

    @Test
    public void testCanSetIFrontEndClientsInAndGetThemBack() throws Exception {
        
        MockIFrontEndClient mockClient = new MockIFrontEndClient("client2", Instant.MIN.plusSeconds(2));

        UserImpl docInput = new UserImpl();

        docInput.setLoginId("myLoginId");
        docInput.addClient(mockClient);

        // Then...
        IFrontEndClient gotBack = docInput.getClient("client2");
        assertThat(gotBack).isNotNull();
        assertThat(gotBack.getLastLogin()).isEqualTo(Instant.MIN.plusSeconds(2));
    }

    @Test
    public void testCanGetClientsWhenClientsArePresent() throws Exception {
        MockIFrontEndClient mockClient1 = new MockIFrontEndClient("client1", Instant.MIN.plusSeconds(2));
        MockIFrontEndClient mockClient2 = new MockIFrontEndClient("client2", Instant.MIN.plusSeconds(2));
        
        UserImpl docInput = new UserImpl();
        docInput.setLoginId("myLoginId");
        docInput.addClient(mockClient1);
        docInput.addClient(mockClient2);

        Collection<IFrontEndClient> clients = docInput.getClients();

        assertThat(clients).isNotNull();
        assertThat(clients).hasSize(2);
        assertThat(clients).extracting("clientName").containsExactlyInAnyOrder("client1", "client2");
    }

    @Test
    public void testCanGetClientsWhenNoClientsArePresent() throws Exception {
        
        UserImpl docInput = new UserImpl();

        Collection<IFrontEndClient> clients = docInput.getClients();

        assertThat(clients).isNotNull();
        assertThat(clients).isEmpty();
    }

    @Test
    public void testCanSetAndGetUserNumber() throws Exception {
        UserImpl docInput = new UserImpl();
        docInput.setUserNumber("12345");

        String userNumber = docInput.getUserNumber();

        assertThat(userNumber).isNotNull();
        assertThat(userNumber).isEqualTo("12345");
    }

    @Test
    public void testGetUserNumberReturnsNullWhenNotSet() throws Exception {
        UserImpl docInput = new UserImpl();
        String userNumber = docInput.getUserNumber();

        assertThat(userNumber).isNull();
    }

    @Test
    public void testCanSetAndGetVersion() throws Exception {
        UserImpl docInput = new UserImpl();
        docInput.setVersion("1.0");

        String version = docInput.getVersion();

        assertThat(version).isNotNull();
        assertThat(version).isEqualTo("1.0");
    }

    @Test
    public void testCanSetAndGetROleId() throws Exception {
        UserImpl docInput = new UserImpl();
        docInput.setRoleId("myRoleId1");

        String roleId = docInput.getRoleId();

        assertThat(roleId).isNotNull();
        assertThat(roleId).isEqualTo("myRoleId1");
    }

    @Test
    public void testGetVersionReturnsNullWhenNotSet() throws Exception {
        
        UserImpl docInput = new UserImpl();
        String version = docInput.getVersion();

        assertThat(version).isNull();
    }

    @Test
    public void testCanSetAndGetLoginId() throws Exception {
        UserImpl docInput = new UserImpl();
        docInput.setLoginId("newLoginId");

        String loginId = docInput.getLoginId();

        assertThat(loginId).isNotNull();
        assertThat(loginId).isEqualTo("newLoginId");
    }

    @Test
    public void testGetLoginIdReturnsNullWhenNotSet() throws Exception {
        UserImpl docInput = new UserImpl();

        String loginId = docInput.getLoginId();

        assertThat(loginId).isNull();
    }

    @Test
    public void testCreateUserFromDocRoleIUserTransfersRoleOK() {
        // Given...
        IUser userIn = new MockIUser("user1-login-id", null, "user1-role-id");

        // When...
        UserImpl userOut = new UserImpl(userIn);

        // Then...
        assertThat(userOut.getRoleId()).isEqualTo("user1-role-id");
    }

    @Test
    public void testCreateUserFromDocNullRoleIUserTransfersToDefaultRoleWhenMissing() {
        // Given...
        IUser userIn = new MockIUser("user1-login-id", null, null);

        // When...
        UserImpl userOut = new UserImpl(userIn);

        // Then...
        assertThat(userOut.getRoleId()).isEqualTo(IUser.DEFAULT_ROLE_ID_WHEN_MISSING);
    }
}
