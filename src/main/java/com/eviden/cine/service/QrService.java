package com.eviden.cine.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Service
public class QrService {

    public byte[] generateQr(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);
            return stream.toByteArray(); // Devuelve la imagen en formato byte[]
        }
    }

    public void generateQrToFile(String content, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
}
