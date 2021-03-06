package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.dao.ChannelDao;
import net.weweave.tubewarder.dao.UserGroupDao;
import net.weweave.tubewarder.domain.Channel;
import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.domain.UserGroup;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.InvalidInputParametersException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.exception.PermissionException;
import net.weweave.tubewarder.outputhandler.OutputHandlerConfigUtil;
import net.weweave.tubewarder.outputhandler.OutputHandlerFactory;
import net.weweave.tubewarder.outputhandler.api.FieldInvalidException;
import net.weweave.tubewarder.outputhandler.api.FieldRequiredException;
import net.weweave.tubewarder.outputhandler.api.IOutputHandler;
import net.weweave.tubewarder.outputhandler.api.InvalidConfigException;
import net.weweave.tubewarder.service.model.ChannelModel;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.request.SetChannelRequest;
import net.weweave.tubewarder.service.response.SetObjectRestResponse;
import org.apache.commons.validator.GenericValidator;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/channel/set")
public class SetChannelService extends AbstractSetObjectService<ChannelModel, Channel> {
    @Inject
    private ChannelDao channelDao;

    @Inject
    private OutputHandlerFactory outputHandlerFactory;

    @Inject
    private UserGroupDao userGroupDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public SetObjectRestResponse action(SetChannelRequest request) {
        SetObjectRestResponse response = new SetObjectRestResponse();
        try {
            Session session = getSession(request.token);
            checkPermissions(session.getUser(), request.object);
            validateInputParameters(request.object);
            Channel object = createUpdateObject(request.object);
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

    private void checkPermissions(User user, ChannelModel model) throws PermissionException {
        if (user == null ||
                !user.getAllowChannels()) {
            throw new PermissionException();
        }

        // Check if user is allowed to assign specified UserGroup
        try {
            UserGroup group = getUserGroupDao().get(model.group.id);
            if (!getUserGroupDao().isUserMemberOfGroup(user, group)) {
                throw new PermissionException();
            }
        } catch (ObjectNotFoundException e) {
            throw new PermissionException();
        }
    }

    @Override
    protected void validateInputParameters(ChannelModel model) throws InvalidInputParametersException {
        if (GenericValidator.isBlankOrNull(model.name)) {
            throw new InvalidInputParametersException("name", ErrorCode.FIELD_REQUIRED);
        }
        if (model.config == null) {
            throw new InvalidInputParametersException("config", ErrorCode.FIELD_REQUIRED);
        }
        if (model.group == null || GenericValidator.isBlankOrNull(model.group.id)) {
            throw new InvalidInputParametersException("group", ErrorCode.FIELD_REQUIRED);
        }
        if (!getOutputHandlerFactory().isValidId(model.config)) {
            throw new InvalidInputParametersException("config", ErrorCode.FIELD_INVALID);
        }

        // Check if object is to be created, but name already exists
        if (GenericValidator.isBlankOrNull(model.id)) {
            try {
                getObjectDao().getByName(model.name);
                throw new InvalidInputParametersException("name", ErrorCode.FIELD_NAME_ALREADY_EXISTS);
            } catch (ObjectNotFoundException e) {
                // This is okay
            }
        }

        // Check if object is to be updated, but (new) name already exists
        if (!GenericValidator.isBlankOrNull(model.id)) {
            try {
                Channel channel = getObjectDao().getByName(model.name);
                // Match found - okay if it's the object to be updated itself
                if (!model.id.equals(channel.getExposableId())) {
                    throw new InvalidInputParametersException("name", ErrorCode.FIELD_NAME_ALREADY_EXISTS);
                }
            } catch (ObjectNotFoundException e) {
                // This is okay
            }
        }

        // Check if config is valid for specified output handler
        IOutputHandler outputHandler = getOutputHandlerFactory().getOutputHandler(model.config);
        try {
            outputHandler.checkConfig(model.config);
        } catch (FieldRequiredException e) {
            throw new InvalidInputParametersException("config."+e.getField(), ErrorCode.FIELD_REQUIRED);
        } catch (FieldInvalidException e) {
            throw new InvalidInputParametersException("config."+e.getField(), ErrorCode.FIELD_INVALID);
        } catch (InvalidConfigException e) {
            throw new InvalidInputParametersException(e.getMessage());
        }
    }

    @Override
    protected Channel createObject(ChannelModel model) throws ObjectNotFoundException {
        Channel object = new Channel();
        getObjectDao().store(object);
        return object;
    }

    @Override
    protected void updateObject(Channel object, ChannelModel model) throws ObjectNotFoundException {
        String configJson = OutputHandlerConfigUtil.configMapToJsonString(model.config);
        UserGroup group = getUserGroupDao().get(model.group.id);

        object.setName(model.name.trim());
        object.setUserGroup(group);
        object.setRewriteRecipientName(model.rewriteRecipientName);
        object.setRewriteRecipientAddress(model.rewriteRecipientAddress);
        object.setRewriteSubject(model.rewriteSubject);
        object.setRewriteContent(model.rewriteContent);
        object.setConfigJson(configJson);
        getObjectDao().update(object);
    }

    @Override
    protected ChannelDao getObjectDao() {
        return channelDao;
    }

    public OutputHandlerFactory getOutputHandlerFactory() {
        return outputHandlerFactory;
    }

    public void setOutputHandlerFactory(OutputHandlerFactory outputHandlerFactory) {
        this.outputHandlerFactory = outputHandlerFactory;
    }

    public UserGroupDao getUserGroupDao() {
        return userGroupDao;
    }

    public void setUserGroupDao(UserGroupDao userGroupDao) {
        this.userGroupDao = userGroupDao;
    }
}
