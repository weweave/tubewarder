package net.weweave.tubewarder.test;

import net.weweave.tubewarder.dao.AppTokenDao;
import net.weweave.tubewarder.dao.ChannelDao;
import net.weweave.tubewarder.dao.ChannelTemplateDao;
import net.weweave.tubewarder.dao.TemplateDao;
import net.weweave.tubewarder.domain.*;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class TestSendServiceCommon {
    @Inject
    private ChannelDao channelDao;

    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @Inject
    private TemplateDao templateDao;

    @Inject
    private AppTokenDao appTokenDao;

    public AppToken createAppToken() {
        AppToken token = new AppToken();
        token.setName("Default");
        getAppTokenDao().store(token);
        return token;
    }

    public Channel createChannel(String name, UserGroup group) {
        JSONObject config = getSysoutChannelConfigJson();

        Channel channel = new Channel();
        channel.setUserGroup(group);
        channel.setName(name);
        channel.setRewriteRecipientName("{{recipientName}}");
        channel.setRewriteRecipientAddress("{{recipientAddress}}");
        channel.setRewriteSubject("{{subject}}");
        channel.setRewriteContent("{{content}}");
        channel.setConfigJson(config.toString());
        getChannelDao().store(channel);
        return channel;
    }

    public Channel createChannel(String name) {
        return createChannel(name, null);
    }

    public JSONObject getSysoutChannelConfigJson() {
        JSONObject config = new JSONObject();
        config.put("id", "SYSOUT");
        config.put("prefix", "Debug: [");
        config.put("suffix", "]");
        return config;
    }

    public Channel createChannel(UserGroup group) {
        return createChannel("sms", group);
    }

    public Channel createChannel() {
        return createChannel("sms", null);
    }

    public Template createTemplate() {
        return createTemplate(null);
    }

    public Template createTemplate(UserGroup group) {
        Template template = new Template();
        template.setName("DOI");
        template.setUserGroup(group);
        getTemplateDao().store(template);
        return template;
    }

    public ChannelTemplate createChannelTemplate(Template template, Channel channel, String subject, String content) {
        ChannelTemplate ct = new ChannelTemplate();
        ct.setTemplate(template);
        ct.setChannel(channel);
        ct.setSubject(subject);
        ct.setContent(content);
        ct.setSenderName("weweave");
        ct.setSenderAddress("noreply@weweave.net");
        getChannelTemplateDao().store(ct);
        return ct;
    }

    public JSONObject getSendRequestJsonPayload(String token,
                                                String templateName,
                                                String channelName,
                                                Map<String, Object> model,
                                                String recipientName,
                                                String recipientAddress,
                                                String keyword,
                                                String details) {
        JSONObject recipient = new JSONObject();
        recipient.put("name", recipientName);
        recipient.put("address", recipientAddress);

        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("echo", true);
        payload.put("template", templateName);
        payload.put("channel", channelName);
        payload.put("recipient", recipient);
        if (model != null) {
            payload.put("model", new JSONObject(model));
        }
        payload.put("keyword", keyword);
        payload.put("details", details);
        return payload;
    }

    public ChannelDao getChannelDao() {
        return channelDao;
    }

    public void setChannelDao(ChannelDao channelDao) {
        this.channelDao = channelDao;
    }

    public ChannelTemplateDao getChannelTemplateDao() {
        return channelTemplateDao;
    }

    public void setChannelTemplateDao(ChannelTemplateDao channelTemplateDao) {
        this.channelTemplateDao = channelTemplateDao;
    }

    public TemplateDao getTemplateDao() {
        return templateDao;
    }

    public void setTemplateDao(TemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    public AppTokenDao getAppTokenDao() {
        return appTokenDao;
    }

    public void setAppTokenDao(AppTokenDao appTokenDao) {
        this.appTokenDao = appTokenDao;
    }
}
