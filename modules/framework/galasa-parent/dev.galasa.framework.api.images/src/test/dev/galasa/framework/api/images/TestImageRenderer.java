package dev.galasa.framework.api.images;

import java.time.Instant;
import static javax.servlet.http.HttpServletResponse.*;
import java.util.*;

import org.junit.Test;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;

import static org.assertj.core.api.Assertions.*;

public class TestImageRenderer {

  @Test
  public void testSomething() throws Exception {
    ImageRenderer renderer = new ImageRenderer();
    renderer.myFunction();
  }
}
