package net.weweave.tubewarder.dao;

import net.weweave.tubewarder.domain.Attachment;
import net.weweave.tubewarder.domain.SendQueueItem;
import net.weweave.tubewarder.util.DbValueRetriever;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

@Stateless
public class SendQueueItemDao extends AbstractDao<SendQueueItem> {
    @Inject
    private AttachmentDao attachmentDao;

    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @Inject
    private LogDao logDao;

    @Override
    public void initObject(SendQueueItem obj) {
        if (obj != null) {
            getChannelTemplateDao().initObject(obj.getChannelTemplate());
            getLogDao().initObject(obj.getLog());
        }
    }

    @Override
    public void store(SendQueueItem item) {
        super.store(item);
        getEntityManager().flush();
    }

    @Override
    public void update(SendQueueItem item) {
        super.update(item);
        getEntityManager().flush();
    }

    public void recoverUnprocessedItems(Integer systemId) {
        try {
            Query query = getEntityManager().createQuery("UPDATE SendQueueItem i " +
                    "SET i.inProcessing = FALSE " +
                    "WHERE i.inProcessing = TRUE AND i.systemId = :systemId");
            query.setParameter("systemId", systemId);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveItems(Integer sourceSystemId, Integer targetSystemId) {
        try {
            Query query = getEntityManager().createQuery("UPDATE SendQueueItem i " +
                    "SET i.inProcessing = FALSE, i.tryCount = 1, i.systemId = :targetSystemId " +
                    "WHERE i.systemId = :sourceSystemId");
            query.setParameter("sourceSystemId", sourceSystemId);
            query.setParameter("targetSystemId", targetSystemId);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Long> getUnprocessedItemIds(Integer systemId) {
        TypedQuery<Long> query = getEntityManager().createQuery("SELECT i.id FROM SendQueueItem i " +
                "WHERE i.systemId = :systemId " +
                "ORDER BY i.createDate ASC", Long.class);
        query.setParameter("systemId", systemId);
        return query.getResultList();
    }

    public List<Long> getFailedUnqueuedItemIds(Integer systemId, int minAgeSeconds) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.SECOND, (-1)*minAgeSeconds);
        TypedQuery<Long> query = getEntityManager().createQuery("SELECT i.id FROM SendQueueItem i " +
                "WHERE i.inProcessing = FALSE AND i.tryCount > 0 AND i.lastTryDate <= :refDate AND i.systemId = :systemId " +
                "ORDER BY i.lastTryDate ASC", Long.class);
        query.setParameter("refDate", cal.getTime());
        query.setParameter("systemId", systemId);
        return query.getResultList();
    }

    public Long getTotalCount() {
        TypedQuery<Long> query = getEntityManager().createQuery("SELECT COUNT(i.id) FROM SendQueueItem i", Long.class);
        return DbValueRetriever.getLongValueOrZero(query);
    }

    public Long getRetryCount() {
        TypedQuery<Long> query = getEntityManager().createQuery("SELECT COUNT(i.id) FROM SendQueueItem i " +
                "WHERE i.tryCount > 0", Long.class);
        return DbValueRetriever.getLongValueOrZero(query);
    }

    public void setItemInProcessing(SendQueueItem item) {
        getEntityManager()
                .createQuery("UPDATE SendQueueItem i SET i.inProcessing = TRUE WHERE i.id = :id")
                .setParameter("id", item.getId())
                .executeUpdate();
    }

    public void setItemNextTry(SendQueueItem item) {
        getEntityManager()
                .createQuery("UPDATE SendQueueItem i SET i.inProcessing = FALSE, i.lastTryDate = :date, i.tryCount = i.tryCount+1 WHERE i.id = :id")
                .setParameter("id", item.getId())
                .setParameter("date", new Date())
                .executeUpdate();
    }

    @Override
    public void delete(SendQueueItem item) {
        List<Attachment> attachments = getAttachmentDao().getAttachmentsForSendQueueItem(item.getId());
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                getAttachmentDao().delete(attachment);
            }
        }
        getEntityManager()
                .createQuery("DELETE FROM SendQueueItem i WHERE i.id = :id")
                .setParameter("id", item.getId())
                .executeUpdate();
    }

    public AttachmentDao getAttachmentDao() {
        return attachmentDao;
    }

    public void setAttachmentDao(AttachmentDao attachmentDao) {
        this.attachmentDao = attachmentDao;
    }

    public ChannelTemplateDao getChannelTemplateDao() {
        return channelTemplateDao;
    }

    public void setChannelTemplateDao(ChannelTemplateDao channelTemplateDao) {
        this.channelTemplateDao = channelTemplateDao;
    }

    public LogDao getLogDao() {
        return logDao;
    }

    public void setLogDao(LogDao logDao) {
        this.logDao = logDao;
    }
}
