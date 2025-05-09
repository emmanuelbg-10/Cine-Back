package com.eviden.cine.service;

import com.eviden.cine.model.Reservation;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class PdfService {

    // Paleta de colores
    private static final DeviceRgb ORANGE = new DeviceRgb(242, 125, 22);    // #F27D16
    private static final DeviceRgb GRAY_800 = new DeviceRgb(31, 41, 55);    // #1F2937
    private static final DeviceRgb WHITE = (DeviceRgb) ColorConstants.WHITE;

    public byte[] generateReservationPdf(Reservation reservation) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            // Logo
            InputStream logoStream = new ClassPathResource("static/img/logo-cine.png").getInputStream();
            Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()))
                    .setWidth(200)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginBottom(15);
            doc.add(logo);

            // Título
            doc.add(new Paragraph("🎟 Entrada de Cine")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(ORANGE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            doc.add(new Paragraph("Reserva Nº: " + reservation.getIdReserve())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(GRAY_800)
                    .setMarginBottom(20));

            // Tabla con fondo gris oscuro y texto blanco
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20)
                    .setBorder(new SolidBorder(GRAY_800, 1));

            table.addCell(cell("Película")).setBackgroundColor(GRAY_800);
            table.addCell(text(reservation.getEmision().getMovie().getTitle()));

            table.addCell(cell("Fecha")).setBackgroundColor(GRAY_800);
            table.addCell(text(reservation.getEmision().getFechaHoraInicio().toLocalDate().toString()));

            table.addCell(cell("Hora")).setBackgroundColor(GRAY_800);
            table.addCell(text(reservation.getEmision().getFechaHoraInicio().toLocalTime().toString()));

            table.addCell(cell("Sala")).setBackgroundColor(GRAY_800);
            table.addCell(text(reservation.getEmision().getRoom().getNombreroom()));

            table.addCell(cell("Precio")).setBackgroundColor(GRAY_800);
            table.addCell(text(reservation.getTotalPrice() + " €"));

            String asientos = reservation.getReserveDetails().stream()
                    .map(d -> d.getAsiento().getFila() + d.getAsiento().getColumna())
                    .reduce((a, b) -> a + ", " + b).orElse("No especificado");

            table.addCell(cell("Asientos")).setBackgroundColor(GRAY_800);
            table.addCell(text(asientos));

            doc.add(table);

            // Código QR
            if (reservation.getQrContent() != null) {
                Image qrImage = new Image(ImageDataFactory.create(reservation.getQrContent()))
                        .setWidth(150)
                        .setHeight(150)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);

                doc.add(new Paragraph("Código QR de tu entrada")
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(GRAY_800));
                doc.add(qrImage);
            }

            // Footer
            doc.add(new Paragraph("Gracias por confiar en nosotros 🎬")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(GRAY_800)
                    .setFontSize(10)
                    .setItalic()
                    .setMarginTop(30));

            doc.add(new Paragraph("© 2025 CineApp · Entrada digital válida solo con identificación")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(8));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private Cell cell(String label) {
        return new Cell().add(new Paragraph(label))
                .setFontColor(WHITE)
                .setPadding(5)
                .setBold();
    }

    private Paragraph text(String value) {
        return new Paragraph(value)
                .setFontColor(WHITE)
                .setPaddingLeft(5);
    }
}
