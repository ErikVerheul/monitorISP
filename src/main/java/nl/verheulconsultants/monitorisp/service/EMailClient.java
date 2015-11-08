/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.verheulconsultants.monitorisp.service;

import java.io.IOException;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author erik
 */
public class EMailClient {
static final Logger logger = LoggerFactory.getLogger(EMailClient.class);
    private Globals g;

    EMailClient(Globals g) {
        this.g = g;
    }

    private void showErrorMessage(String msg, int replyCode, String errMsg) {
        logger.error("{}" + "\n"
                + "De errorcode = " + "{}\nDe error message is: {}", new Object[]{msg, replyCode, errMsg});
    }

//    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DLS_DEAD_LOCAL_STORE", justification="I know what I'm doing")
    @SuppressWarnings("static-access")
    boolean sendEMail(String sender, String recipient, String message, String subject) {
        SMTPClient client = new SMTPClient();
        String SMTPserver = g.getCurrentSMTPserver();
        try {
            int replyCode;

            client.connect(SMTPserver);
            // After connection attempt, check the reply code to verify success.
            replyCode = client.getReplyCode();
            if (!SMTPReply.isPositiveCompletion(replyCode)) {
                client.disconnect();
                showErrorMessage("De server " + SMTPserver + " weigert de verbindingsaanvraag.", replyCode, client.getReplyString());
                return false;
            }

            // Do useful stuff here.
            if (!client.login()) {
                logger.info("Inloggen met HELO commando is mislukt.");
                return false;
            }

            replyCode = client.mail(recipient); //(specifies who's sending mail)
            if (!SMTPReply.isPositiveCompletion(replyCode)) {
                showErrorMessage("Fout bij het verzenden van het MAIL bericht.", replyCode, client.getReplyString());
                return false;
            }
            replyCode = client.rcpt(recipient); //(specifies who's getting it)
            if (!SMTPReply.isPositiveCompletion(replyCode)) {
                showErrorMessage("Fout bij het verzenden van het RCPT bericht.", replyCode, client.getReplyString());
                return false;
            }

            try (java.io.Writer writer = client.sendMessageData()) {
                if (writer == null) {
                    showErrorMessage("Fout bij het verzenden van het sendMessageData bericht.", client.getReplyCode(), client.getReplyString());
                    return false;
                }
                
                SimpleSMTPHeader header = new SimpleSMTPHeader(sender, recipient, subject);
                writer.write(header.toString());
                writer.write(message);
            }
            if (!client.completePendingCommand()) {
                showErrorMessage("Het bericht kon niet worden verzonden.", client.getReplyCode(), client.getReplyString());
                return false;
            }

            return true;
        } catch (IOException e) {
            logger.error("Kan geen verbinding maken met de server {0}\n. De oorzaak is:\n{1}", new Object[]{SMTPserver, e});
            return false;
        } finally {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException ignore) {
                    // do nothing
                }
            }
        }
    }
}
