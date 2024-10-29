/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import java.time.Instant;
import org.junit.Test;

import dev.galasa.framework.api.beans.generated.FrontEndClient;
import dev.galasa.framework.api.beans.generated.UserData;
import dev.galasa.framework.api.common.mocks.*;
import dev.galasa.framework.spi.auth.*;


public class BeanTransformerTest {


    class FrontEndComparator implements Comparator<FrontEndClient> {
        @Override
        public int compare(FrontEndClient a, FrontEndClient b) {
            return a.getClientName().compareToIgnoreCase(b.getClientName());
        }
    }

    class UserDatComparator implements Comparator<UserData> {
        @Override
        public int compare(UserData a, UserData b) {
            return a.getLoginId().compareToIgnoreCase(b.getLoginId());
        }
    }

    public static final String BASE_URL = "http://my.server/api/";
    
    @Test 
    public void testTransformsANullList() throws Exception {
        BeanTransformer xform = new BeanTransformer(BASE_URL);

        Collection<UserData> data = xform.convertAllUsersToUserBean(null);
        assertThat(data).isNotNull();
        assertThat(data).hasSize(0);
    }

    @Test
    public void testTransformsEmptyList() throws Exception {
        BeanTransformer xform = new BeanTransformer(BASE_URL);

        Collection<UserData> data = xform.convertAllUsersToUserBean(List.of());

        assertThat(data).isNotNull();
        assertThat(data).hasSize(0);
    }

    @Test
    public void testTransformsListWithOneUserNoClients() throws Exception {
        BeanTransformer xform = new BeanTransformer(BASE_URL);

        MockUser userIn = new MockUser();
        userIn.userNumber = "56789";
        userIn.version = "7890";
        userIn.loginId = "arvind";
        userIn.clients = null ;

        Collection<UserData> data = xform.convertAllUsersToUserBean(List.of(userIn));

        assertThat(data).isNotNull();
        assertThat(data).hasSize(1);

        Iterator<UserData> walker = data.iterator();

        UserData userGotBack1 = walker.next();
        assertThat(userGotBack1.getLoginId()).isEqualTo(userIn.loginId);
        assertThat(userGotBack1.getid()).isEqualTo(userIn.userNumber);
        assertThat(userGotBack1.geturl()).isEqualTo("http://my.server/api/users/56789");

        assertThat(userGotBack1.getclients()).isNotNull().hasSize(0);
    }



    @Test
    public void testTransformsListWithTwoUsersNoClients() throws Exception {

        BeanTransformer xform = new BeanTransformer(BASE_URL);

        MockUser userIn1 = new MockUser();
        userIn1.userNumber = "56789";
        userIn1.version = "7890";
        userIn1.loginId = "arvind";
        userIn1.clients = null ;


        MockUser userIn2 = new MockUser();
        userIn2.userNumber = "5678asdasd9";
        userIn2.version = "789asdasd0";
        userIn2.loginId = "bilbo";
        userIn2.clients = null ;

        Collection<UserData> data = xform.convertAllUsersToUserBean(List.of(userIn1, userIn2));

        assertThat(data).isNotNull();
        assertThat(data).hasSize(2);

        List<UserData> usersOut = new ArrayList<UserData>();
        for(UserData userOut : data) {
            usersOut.add(userOut);
        }

        Collections.sort(usersOut,new UserDatComparator());

        assertThat(usersOut.get(0).getLoginId()).isEqualTo(userIn1.loginId);
        assertThat(usersOut.get(1).getLoginId()).isEqualTo(userIn2.loginId);
    }

    @Test
    public void testTransformsListWithOneUserOneClient() throws Exception {
        BeanTransformer xform = new BeanTransformer(BASE_URL);

        MockUser userIn = new MockUser();
        userIn.userNumber = "56789";
        userIn.version = "7890";
        userIn.loginId = "arvind";
        userIn.clients = new ArrayList<IFrontEndClient>();

        IFrontEndClient clientIn1 = new MockFrontEndClient("web-ui");
        Instant now = Instant.MIN.plusSeconds(25);
        clientIn1.setLastLogin(now);
        userIn.clients.add(clientIn1);

        Collection<UserData> data = xform.convertAllUsersToUserBean(List.of(userIn));

        assertThat(data).isNotNull();
        assertThat(data).hasSize(1);

        Iterator<UserData> walker = data.iterator();

        UserData userGotBack1 = walker.next();
        assertThat(userGotBack1.getLoginId()).isEqualTo(userIn.loginId);
        assertThat(userGotBack1.getid()).isEqualTo(userIn.userNumber);

        assertThat(userGotBack1.getclients()).isNotNull().hasSize(1);
        List<FrontEndClient> clientsOut = List.of(userGotBack1.getclients());
        assertThat(clientsOut.get(0).getClientName()).isEqualTo("web-ui");
        assertThat(clientsOut.get(0).getLastLogin()).isEqualTo(now.toString());

    }

    @Test
    public void testTransformsListWithOneUserTwoClients() throws Exception {
        BeanTransformer xform = new BeanTransformer(BASE_URL);

        MockUser userIn = new MockUser();
        userIn.userNumber = "56789";
        userIn.version = "7890";
        userIn.loginId = "arvind";
        userIn.clients = new ArrayList<IFrontEndClient>();

        Instant now = Instant.MIN.plusSeconds(25);

        IFrontEndClient clientIn1 = new MockFrontEndClient("web-ui");
        clientIn1.setLastLogin(now);
        userIn.clients.add(clientIn1);

        IFrontEndClient clientIn2 = new MockFrontEndClient("rest-api");
        clientIn2.setLastLogin(now.plusSeconds(2));
        userIn.clients.add(clientIn2);

        Collection<UserData> data = xform.convertAllUsersToUserBean(List.of(userIn));

        assertThat(data).isNotNull();
        assertThat(data).hasSize(1);

        Iterator<UserData> walker = data.iterator();

        UserData userGotBack1 = walker.next();
        assertThat(userGotBack1.getLoginId()).isEqualTo(userIn.loginId);
        assertThat(userGotBack1.getid()).isEqualTo(userIn.userNumber);

        assertThat(userGotBack1.getclients()).isNotNull().hasSize(2);
        List<FrontEndClient> clientsOut = new ArrayList<FrontEndClient>();
        clientsOut.addAll(List.of(userGotBack1.getclients()));


    
        Collections.sort( clientsOut, new FrontEndComparator() );

        assertThat(clientsOut.get(0).getClientName()).isEqualTo("rest-api");
        assertThat(clientsOut.get(0).getLastLogin()).isEqualTo(now.plusSeconds(2).toString());

        assertThat(clientsOut.get(1).getClientName()).isEqualTo("web-ui");
        assertThat(clientsOut.get(1).getLastLogin()).isEqualTo(now.toString());

    }

}