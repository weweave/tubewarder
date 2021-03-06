package net.weweave.tubewarder.dao;

import net.weweave.tubewarder.domain.ChannelTemplate;
import net.weweave.tubewarder.domain.Template;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.util.DbValueRetriever;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TemplateDao extends AbstractDao<Template> {
    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @Inject
    private UserGroupDao userGroupDao;

    @Override
    public void initObject(Template obj) {
        if (obj != null) {
            getUserGroupDao().initObject(obj.getUserGroup());
        }
    }

    public Template getByName(String name) throws ObjectNotFoundException {
        TypedQuery<Template> query = getEntityManager().createQuery("SELECT t FROM Template t WHERE t.name = :name", Template.class);
        query.setParameter("name", name);
        query.setMaxResults(1);
        Template result = (Template) DbValueRetriever.getObjectOrException(query);
        initObject(result);
        return result;
    }

    public List<Template> getAll() {
        TypedQuery<Template> query = getEntityManager().createQuery("SELECT t FROM Template t ORDER BY t.name", Template.class);
        List<Template> result = query.getResultList();
        initObject(result);
        return result;
    }

    public List<Long> getTemplateIdsWithGroups(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        TypedQuery<Long> query = getEntityManager().createQuery("SELECT t.id FROM Template t " +
                "WHERE t.userGroup.id IN :groupIds", Long.class);
        query.setParameter("groupIds", groupIds);
        return query.getResultList();
    }

    public boolean canUserAcccessTemplate(Template template, List<Long> userGroupMembershipIds) {
        return template == null ||
                template.getUserGroup() == null ||
                userGroupMembershipIds.contains(template.getUserGroup().getId());
    }

    @Override
    public void delete(Template item) {
        List<ChannelTemplate> channelTemplates = getChannelTemplateDao().getChannelTemplatesForTemplate(item.getId());
        for (ChannelTemplate ct : channelTemplates) {
            getChannelTemplateDao().delete(ct);
        }
        super.delete(item);
    }

    public ChannelTemplateDao getChannelTemplateDao() {
        return channelTemplateDao;
    }

    public void setChannelTemplateDao(ChannelTemplateDao channelTemplateDao) {
        this.channelTemplateDao = channelTemplateDao;
    }

    public UserGroupDao getUserGroupDao() {
        return userGroupDao;
    }

    public void setUserGroupDao(UserGroupDao userGroupDao) {
        this.userGroupDao = userGroupDao;
    }
}
