package dev.vality.fraudbusters.notificator.service.sender;

import dev.vality.fraudbusters.notificator.domain.Message;
import dev.vality.fraudbusters.notificator.service.iface.MailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;

    public boolean send(Message message) {
        try {
            log.info("Trying to send message to mail, partyId={}, claimId={}, email={}", message.getPartyId(),
                    message.getClaimId(), message.getTo());
            MimeMessage mimeMessage = createMimeMessage(message);
            mailSender.send(mimeMessage);
            return true;
        } catch (MessagingException | MailException | IOException ex) {
            throw new MailSendException(
                    String.format("Received exception while sending message to mail, partyId=%s, claimId=%s, email=%s",
                            message.getPartyId(), message.getClaimId(), Arrays.toString(message.getTo())), ex);
        }
    }

    private MimeMessage createMimeMessage(Message message) throws MessagingException, IOException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        helper.setFrom(message.getFrom());
        helper.setTo(message.getTo());
        helper.setSubject(message.getSubject());
        helper.setText(message.getContent(), false);
        if (message.getAttachment() != null) {
            helper.addAttachment(message.getAttachment().getFileName(),
                    new ByteArrayResource(message.getAttachment().getContent().getBytes()));
        }
        return mimeMessage;
    }

}
