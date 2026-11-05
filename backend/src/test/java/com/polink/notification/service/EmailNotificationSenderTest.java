package com.polink.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationSender emailNotificationSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationSender, "fromAddress", "no-reply@polink.local");
    }

    @Test
    void sendEmailAsyncShouldBuildAndSendMailMessage() {
        emailNotificationSender.sendEmailAsync("estudiante@polink.edu.co", "[Polink] Reserva aceptada", "Tu reserva fue confirmada.");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getFrom()).isEqualTo("no-reply@polink.local");
        assertThat(msg.getTo()).containsExactly("estudiante@polink.edu.co");
        assertThat(msg.getSubject()).isEqualTo("[Polink] Reserva aceptada");
        assertThat(msg.getText()).isEqualTo("Tu reserva fue confirmada.");
    }

    @Test
    void sendEmailAsyncShouldIgnoreBlankEmail() {
        emailNotificationSender.sendEmailAsync("   ", "Asunto", "Mensaje");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
