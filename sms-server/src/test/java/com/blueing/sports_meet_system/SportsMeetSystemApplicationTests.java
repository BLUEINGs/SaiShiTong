package com.blueing.sports_meet_system;

import ai.onnxruntime.OrtException;
import com.blueing.sports_meet_system.service.imp.DetectorServiceA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SportsMeetSystemApplicationTests {

    @Autowired
    private DetectorServiceA detectorServiceA;

    @Test
    void onnxLoader() throws OrtException {

        detectorServiceA.initOnnxSession();

    }

}
