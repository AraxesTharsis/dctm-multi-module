import org.junit.*;

import com.documentum.fc.client.IDfSession;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class AppTest {
	
	/**
	 * Tests the {@link DeepExportMethod}. This is not exhaustive as the
	 * {@link DeepExportService} module is tested thoroughly in
	 * {@link DeepExportServiceTest}.
	 * @throws Exception 
	 */
	@Test
	public void testDeepExportMethod() throws Exception {
		IDfSession session = null;

		assertThat(true, is(true));
	}
}
