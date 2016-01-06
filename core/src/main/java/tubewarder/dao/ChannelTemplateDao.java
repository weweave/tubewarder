package tubewarder.dao;

import tubewarder.domain.ChannelTemplate;
import tubewarder.exception.ObjectNotFoundException;
import tubewarder.util.DbValueRetriever;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;

@ApplicationScoped
public class ChannelTemplateDao extends AbstractDao<ChannelTemplate> {
    public ChannelTemplate getChannelTemplateByNames(String template, String channel) throws ObjectNotFoundException {
        TypedQuery<ChannelTemplate> query = getEntityManager().createQuery("SELECT ct FROM ChannelTemplate ct " +
                "WHERE ct.template.name = :template AND ct.channel.name = :channel", ChannelTemplate.class);
        query.setMaxResults(1);
        query.setParameter("template", template);
        query.setParameter("channel", channel);
        return (ChannelTemplate) DbValueRetriever.getObjectOrException(query);
    }

    public ChannelTemplate getChannelTemplateById(String templateId, String channelId) throws ObjectNotFoundException {
        TypedQuery<ChannelTemplate> query = getEntityManager().createQuery("SELECT ct FROM ChannelTemplate ct " +
                "WHERE ct.template.id = :templateId AND ct.channel.id = :channelId", ChannelTemplate.class);
        query.setMaxResults(1);
        query.setParameter("templateId", templateId);
        query.setParameter("channelId", channelId);
        return (ChannelTemplate) DbValueRetriever.getObjectOrException(query);
    }
}