package net.weweave.tubewarder.outputhandler;

import net.weweave.tubewarder.domain.ChannelTemplate;
import net.weweave.tubewarder.domain.SendQueueItem;
import net.weweave.tubewarder.outputhandler.api.*;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.logging.Logger;

@Singleton
public class OutputHandlerDispatcher {
    private static final Logger LOG = Logger.getLogger(OutputHandlerDispatcher.class.getName());

    @Inject
    private OutputHandlerFactory outputHandlerFactory;

    @Asynchronous
    public void processSendQueueItem(SendQueueItem item, SendQueueCallback callback) {
        log(item, "Start processing");

        Config config = OutputHandlerConfigUtil.configJsonStringToMap(item.getConfigJson());

        SendItem payload = new SendItem();
        IOutputHandler handler = getOutputHandlerFactory().getOutputHandler(config);
        ChannelTemplate channelTemplate = item.getChannelTemplate();
        payload.setSender(new Address(channelTemplate.getSenderName(), channelTemplate.getSenderAddress()));
        payload.setRecipient(new Address(item.getRecipientName(), item.getRecipientAddress()));
        payload.setSubject(item.getSubject());
        payload.setContent(item.getContent());
        createAttachmentList(item, payload);

        invoke(item, callback, handler, config, payload);

        log(item, "Finished processing");
    }

    private void createAttachmentList(SendQueueItem item, SendItem payload) {
        for (net.weweave.tubewarder.domain.Attachment atm : item.getAttachments()) {
            Attachment attachment = new Attachment();
            attachment.setContentType(atm.getContentType());
            attachment.setFilename(atm.getFilename());
            attachment.setPayload(atm.getPayload());
            payload.getAttachments().add(attachment);
        }
    }

    public void invoke(SendQueueItem item, SendQueueCallback callback, IOutputHandler handler, Config config, SendItem payload) {
        log(item, "Invoking " + handler.getClass().getName());
        try {
            handler.process(config, payload);
            callback.success(item);
        } catch (TemporaryProcessingException e) {
            log(item, "TemporaryProcessingException while processing in " + handler.getClass().getName() + ": " + e.getMessage());
            callback.temporaryError(item);
        } catch (PermanentProcessingException e) {
            log(item, "PermanentProcessingException while processing in " + handler.getClass().getName() + ": " + e.getMessage());
            callback.permanentError(item);
        }
    }

    private void log(SendQueueItem item, String message) {
        LOG.info("Queue item ID = " + item.getExposableId() + ": " + message);
    }

    public OutputHandlerFactory getOutputHandlerFactory() {
        return outputHandlerFactory;
    }

    public void setOutputHandlerFactory(OutputHandlerFactory outputHandlerFactory) {
        this.outputHandlerFactory = outputHandlerFactory;
    }
}
