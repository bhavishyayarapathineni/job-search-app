package com.jobsearch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobSearchApplicationTest {

    @Test
    void main_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            // Just test the class exists and is instantiable
            JobSearchApplication app = new JobSearchApplication();
            assertNotNull(app);
        });
    }
}
