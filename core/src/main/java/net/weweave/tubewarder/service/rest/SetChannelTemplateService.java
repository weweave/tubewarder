package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.dao.UserGroupDao;
import net.weweave.tubewarder.domain.*;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.PermissionException;
import org.apache.commons.validator.GenericValidator;
import net.weweave.tubewarder.dao.ChannelDao;
import net.weweave.tubewarder.dao.ChannelTemplateDao;
import net.weweave.tubewarder.dao.TemplateDao;
import net.weweave.tubewarder.exception.InvalidInputParametersException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.service.model.ChannelTemplateModel;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.request.SetChannelTemplateRequest;
import net.weweave.tubewarder.service.response.SetObjectRestResponse;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestScoped
@Path("/channeltemplate/set")
public class SetChannelTemplateService extends AbstractSetObjectService<ChannelTemplateModel, ChannelTemplate> {
    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @Inject
    private TemplateDao templateDao;

    @Inject
    private ChannelDao channelDao;

    @Inject
    private UserGroupDao userGroupDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public SetObjectRestResponse action(SetChannelTemplateRequest request) {
        SetObjectRestResponse response = new SetObjectRestResponse();
        try {
            Session session = getSession(request.token);
            checkPermissions(session.getUser(), request.object);
            validateInputParameters(request.object);
            ChannelTemplate object = createUpdateObject(request.object);
            response.id = object.getExposableId();
        } catch (InvalidInputParametersException e) {
            addErrorsToResponse(response, e);
        } catch (ObjectNotFoundException e) {
            response.error = ErrorCode.OBJECT_LOOKUP_ERROR;
        } catch (PermissionException e) {
            response.error = ErrorCode.PERMISSION_DENIED;
        } catch (AuthRequiredException e) {
            response.error = ErrorCode.AUTH_REQUIRED;
        }
        return response;
    }

    private void checkPermissions(User user, ChannelTemplateModel model) throws PermissionException {
        if (user == null ||
                !user.getAllowTemplates()) {
            throw new PermissionException();
        }

        // Check if user is allowed to assign specified UserGroup
        try {
            List<Long> userGroupMembershipIds = getUserGroupDao().getGroupMembershipIds(user);
            Template template = getTemplateDao().get(model.template.id);
            Channel channel = getChannelDao().get(model.channel.id);
            if (!getTemplateDao().canUserAcccessTemplate(template, userGroupMembershipIds) ||
                    !getChannelDao().canUserAcccessChannel(channel, userGroupMembershipIds)) {
                throw new PermissionException();
            }
        } catch (ObjectNotFoundException e) {
            throw new PermissionException();
        }
    }

    @Override
    protected void validateInputParameters(ChannelTemplateModel model) throws InvalidInputParametersException {
        if (model.template == null) {
            throw new InvalidInputParametersException("template", ErrorCode.FIELD_REQUIRED);
        }
        if (GenericValidator.isBlankOrNull(model.template.id)) {
            throw new InvalidInputParametersException("template.id", ErrorCode.FIELD_REQUIRED);
        }
        if (model.channel == null || GenericValidator.isBlankOrNull(model.channel.id)) {
            throw new InvalidInputParametersException("channel", ErrorCode.FIELD_REQUIRED);
        }
        if (GenericValidator.isBlankOrNull(model.content)) {
            throw new InvalidInputParametersException("content", ErrorCode.FIELD_REQUIRED);
        }

        // Check if object is to be created, but channel/template combination already exists
        if (GenericValidator.isBlankOrNull(model.id)) {
            try {
                getObjectDao().getChannelTemplateById(model.template.id, model.channel.id);
                throw new InvalidInputParametersException("channel", ErrorCode.FIELD_INVALID);
            } catch (ObjectNotFoundException e) {
                // This is okay
            }
        }

        // Check if object is to be updated, but (new) channel/template combination already exists
        if (!GenericValidator.isBlankOrNull(model.id)) {
            try {
                ChannelTemplate channelTemplate = getObjectDao().getChannelTemplateById(model.template.id, model.channel.id);
                // Match found - okay if it's the object to be updated itself
                if (!model.id.equals(channelTemplate.getExposableId())) {
                    throw new InvalidInputParametersException("channel", ErrorCode.FIELD_INVALID);
                }
            } catch (ObjectNotFoundException e) {
                // This is okay
            }
        }
    }

    @Override
    protected ChannelTemplate createObject(ChannelTemplateModel model) throws ObjectNotFoundException {
        ChannelTemplate object = new ChannelTemplate();
        getObjectDao().store(object);
        return object;
    }

    @Override
    protected void updateObject(ChannelTemplate object, ChannelTemplateModel model) throws ObjectNotFoundException {
        Template template = getTemplateDao().get(model.template.id);
        Channel channel = getChannelDao().get(model.channel.id);

        object.setTemplate(template);
        object.setChannel(channel);
        object.setSubject(model.subject);
        object.setContent(model.content);
        object.setSenderAddress(model.senderAddress);
        object.setSenderName(model.senderName);
        getObjectDao().update(object);
    }

    @Override
    public ChannelTemplateDao getObjectDao() {
        return channelTemplateDao;
    }

    public TemplateDao getTemplateDao() {
        return templateDao;
    }

    public void setTemplateDao(TemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    public ChannelDao getChannelDao() {
        return channelDao;
    }

    public void setChannelDao(ChannelDao channelDao) {
        this.channelDao = channelDao;
    }

    public UserGroupDao getUserGroupDao() {
        return userGroupDao;
    }

    public void setUserGroupDao(UserGroupDao userGroupDao) {
        this.userGroupDao = userGroupDao;
    }
}
