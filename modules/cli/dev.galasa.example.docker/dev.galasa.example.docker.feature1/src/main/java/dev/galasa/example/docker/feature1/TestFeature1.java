package dev.galasa.example.docker.feature1;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.core.manager.*;
import dev.galasa.Test;
import dev.galasa.example.docker.DockerResource;
import dev.galasa.example.docker.IDockerResource;

/**
 * A sample galasa test class 
 */
@Test
public class TestFeature1 {

	// Galasa will inject an instance of the core manager into the following field
	@CoreManager
	public ICoreManager core;

	// Galasa will inject a Docker resource instance via the DockerManager
	@DockerResource
	public IDockerResource dockerResource;

	/**
	 * Test which demonstrates that the managers have been injected ok.
	 */
	@Test
	public void simpleSampleTest() {
		assertThat(core).isNotNull();
		assertThat(dockerResource).isNotNull();
		assertThat(dockerResource.getTag()).isEqualTo("PRIMARY");
	}

}
