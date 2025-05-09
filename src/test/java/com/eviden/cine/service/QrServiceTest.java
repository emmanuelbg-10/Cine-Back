package com.eviden.cine.service;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrServiceTest {

    private final QrService qrService = new QrService();

    @Test
    void testGenerateQr_returnByteArray() throws WriterException, IOException {
        String content = "https://eviden.com/entrada/123";

        byte[] qrBytes = qrService.generateQr(content);

        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);
    }

    @Test
    void testGenerateQrToFile_createsFile() throws WriterException, IOException {
        String content = "entrada-archivo-456";
        String filePath = "test-qr.png";

        qrService.generateQrToFile(content, filePath);

        File file = new File(filePath);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // Limpieza del archivo creado después del test
        assertTrue(file.delete());
    }
}

