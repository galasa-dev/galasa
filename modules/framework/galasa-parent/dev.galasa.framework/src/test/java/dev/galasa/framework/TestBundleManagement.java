/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import dev.galasa.framework.mocks.MockBundle;
import dev.galasa.framework.mocks.MockBundleContext;

public class TestBundleManagement {

    private static final String UNRESOLVABLE_MESSAGE = "Unable to resolve bundle: uses constraint violation";

    class StartableMockBundle extends MockBundle {
        private int state;
        private BundleException startException;
        private boolean isFragment;

        public StartableMockBundle(String symbolicName, int state, BundleException startException, boolean isFragment) {
            super(Map.of(), symbolicName);
            this.state = state;
            this.startException = startException;
            this.isFragment = isFragment;
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public void start() throws BundleException {
            if (startException != null) {
                throw startException;
            }
            state = Bundle.ACTIVE;
        }

        @Override
        public Dictionary<String, String> getHeaders() {
            Dictionary<String, String> headers = new Hashtable<>();
            if (isFragment) {
                headers.put(Constants.FRAGMENT_HOST, "some.host.bundle");
            }
            return headers;
        }
    }

    private MockBundleContext createContext(Bundle... bundles) {
        return new MockBundleContext(Map.of(), new ArrayList<>(List.of(bundles)));
    }

    @Test
    public void TestFindBundleStartFailuresReturnsStartExceptionMessageForBundleWhichCannotStart() throws Exception {
        // Given...
        Bundle activeBundle = new StartableMockBundle("good.bundle", Bundle.ACTIVE, null, false);
        Bundle failingBundle = new StartableMockBundle("bad.bundle", Bundle.RESOLVED,
            new BundleException(UNRESOLVABLE_MESSAGE), false);
        MockBundleContext context = createContext(activeBundle, failingBundle);

        // When...
        List<String> failures = BundleManagement.findBundleStartFailures(context, Set.of("good.bundle", "bad.bundle"));

        // Then...
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).contains("bad.bundle").contains("Resolved").contains(UNRESOLVABLE_MESSAGE);
    }

    @Test
    public void TestFindBundleStartFailuresReturnsNothingWhenAllBundlesAreActive() throws Exception {
        // Given...
        Bundle activeBundle = new StartableMockBundle("good.bundle", Bundle.ACTIVE, null, false);
        MockBundleContext context = createContext(activeBundle);

        // When...
        List<String> failures = BundleManagement.findBundleStartFailures(context, Set.of("good.bundle"));

        // Then...
        assertThat(failures).isEmpty();
    }

    @Test
    public void TestFindBundleStartFailuresIgnoresBundlesWhichWereNotDeployed() throws Exception {
        // Given...
        Bundle unrelatedBundle = new StartableMockBundle("unrelated.bundle", Bundle.RESOLVED,
            new BundleException(UNRESOLVABLE_MESSAGE), false);
        MockBundleContext context = createContext(unrelatedBundle);

        // When...
        List<String> failures = BundleManagement.findBundleStartFailures(context, Set.of("good.bundle"));

        // Then...
        assertThat(failures).isEmpty();
    }

    @Test
    public void TestFindBundleStartFailuresIgnoresFragmentBundles() throws Exception {
        // Given...
        Bundle fragmentBundle = new StartableMockBundle("fragment.bundle", Bundle.RESOLVED,
            new BundleException("Fragment bundles cannot be started"), true);
        MockBundleContext context = createContext(fragmentBundle);

        // When...
        List<String> failures = BundleManagement.findBundleStartFailures(context, Set.of("fragment.bundle"));

        // Then...
        assertThat(failures).isEmpty();
    }

    @Test
    public void TestFindBundleStartFailuresReportsBundleWhichStartsSuccessfullyWhenRetried() throws Exception {
        // Given...
        Bundle installedBundle = new StartableMockBundle("slow.bundle", Bundle.INSTALLED, null, false);
        MockBundleContext context = createContext(installedBundle);

        // When...
        List<String> failures = BundleManagement.findBundleStartFailures(context, Set.of("slow.bundle"));

        // Then...
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).contains("slow.bundle").contains("Installed").contains("started successfully when retried");
    }
}
