
package com.eviden.cine.service;

import com.eviden.cine.model.Reservation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoConfirmacion(String emailDestino, Reservation reserva, byte[] qrImage) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(emailDestino);
        helper.setSubject("🎟️ Confirmación de tu reserva en Cine Eviden");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es", "ES"));
        String fechaFormateada = reserva.getEmision().getFechaHoraInicio().format(formatter);

        String cuerpo = """
                <p>Hola <strong>%s</strong>,</p>
                <p>Tu reserva ha sido confirmada:</p>
                <ul>
                  <li><strong>Reserva Nº:</strong> %d</li>
                  <li><strong>Película:</strong> %s</li>
                  <li><strong>Fecha y hora:</strong> %s</li>
                  <li><strong>Sala:</strong> %s</li>
                  <li><strong>Asientos:</strong> %s</li>
                  <li><strong>Total pagado:</strong> %.2f €</li>
                </ul>
                <p>Adjunto encontrarás un código QR con tu entrada.<br>
                Muestralo al ingresar al cine.</p>
                <p>Pasos a seguir:</p>
                <p>1. Asegurate de las palomitas</p>
                <p>2. Dirigete a la cola de la entrada para que escaneen tu código </p>
                <p>3. Entra a tu sala asignada y sientate en los asientos elegidos</p>
                <p>4. Disfruta de tu película!</p>
                
                <p>¡Gracias por tu compra!</p>
                """.formatted(
                reserva.getUser().getUsername(),
                reserva.getIdReserve(),
                reserva.getEmision().getMovie().getTitle(),
                fechaFormateada,
                reserva.getEmision().getRoom().getNombreroom(),
                reserva.getReserveDetails().stream()
                        .map(d -> d.getAsiento().getFila() + d.getAsiento().getColumna())
                        .reduce((a, b) -> a + ", " + b).orElse("Ninguno"),
                 reserva.getTotalPrice()
        );

        helper.setText(cuerpo, true);
        helper.addAttachment("ticket_qr.png", new ByteArrayResource(qrImage));

        mailSender.send(mensaje);
    }

    public void sendPasswordResetEmail(String emailDestino, String token) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(emailDestino);
        helper.setSubject("🔐 Recuperación de contraseña");

        String link = "http://localhost:5173/reset-password?token=" + token;

        String cuerpo = """
        <p>Hola,</p>
        <p>Recibimos una solicitud para restablecer tu contraseña.</p>
        <p>Haz clic en el siguiente enlace para establecer una nueva contraseña:</p>
        <p><a href="%s">Restablecer contraseña</a></p>
        <p>Este enlace es válido por un único uso. Si no solicitaste el cambio, ignora este mensaje.</p>
        """.formatted(link);

        helper.setText(cuerpo, true);
        mailSender.send(mensaje);
    }


}