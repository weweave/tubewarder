package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.dao.UserGroupDao;
import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.InvalidInputParametersException;
import net.weweave.tubewarder.exception.PermissionException;
import net.weweave.tubewarder.outputhandler.OutputHandlerFactory;
import org.apache.commons.validator.GenericValidator;
import net.weweave.tubewarder.dao.ChannelTemplateDao;
import net.weweave.tubewarder.dao.TemplateDao;
import net.weweave.tubewarder.domain.ChannelTemplate;
import net.weweave.tubewarder.domain.Template;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.service.model.ChannelTemplateModel;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.response.GetChannelTemplateResponse;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@RequestScoped
@Path("/channeltemplate/get")
public class GetChannelTemplateService extends AbstractService {
    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @Inject
    private TemplateDao templateDao;

    @Inject
    private OutputHandlerFactory outputHandlerFactory;

    @Inject
    private UserGroupDao userGroupDao;

    @GET
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public GetChannelTemplateResponse action(@QueryParam("token") @DefaultValue("") String token,
                                             @QueryParam("id") @DefaultValue("") String id,
                                             @QueryParam("templateId") @DefaultValue("") String templateId) {
        GetChannelTemplateResponse response = new GetChannelTemplateResponse();

        try {
            Session session = getSession(token);
            checkPermissions(session.getUser());
            validateInputParameters(id, templateId);
            setResponseList(session.getUser(), response, id, templateId);
        } catch (ObjectNotFoundException e) {
            response.error = ErrorCode.OBJECT_LOOKUP_ERROR;
        } catch (PermissionException e) {
            response.error = ErrorCode.PERMISSION_DENIED;
        } catch (AuthRequiredException e) {
            response.error = ErrorCode.AUTH_REQUIRED;
        } catch (InvalidInputParametersException e) {
            response.error = ErrorCode.INVALID_INPUT_PARAMETERS;
        }

        return response;
    }

    private void validateInputParameters(String id, String templateId) throws InvalidInputParametersException {
        if (GenericValidator.isBlankOrNull(id) && GenericValidator.isBlankOrNull(templateId)) {
            throw new InvalidInputParametersException();
        }
    }

    private void checkPermissions(User user) throws PermissionException {
        if (user == null ||
                !user.getAllowTemplates()) {
            throw new PermissionException();
        }
    }

    private void setResponseList(User user, GetChannelTemplateResponse response, String id, String templateId) throws ObjectNotFoundException {
        List<Long> userGroupMembershipIds = getUserGroupDao().getGroupMembershipIds(user);

        if (!GenericValidator.isBlankOrNull(id)) {
            ChannelTemplate channelTemplate = getChannelTemplateDao().get(id);
            if (getTemplateDao().canUserAcccessTemplate(channelTemplate.getTemplate(), userGroupMembershipIds)) {
                response.channelTemplates.add(ChannelTemplateModel.factory(channelTemplate, getOutputHandlerFactory()));
            } else {
                throw new ObjectNotFoundException();
            }
        } else if (!GenericValidator.isBlankOrNull(templateId)) {
            Template template = getTemplateDao().get(templateId);
            List<ChannelTemplate> channelTemplates = getChannelTemplateDao().getChannelTemplatesForTemplate(template.getId());
            for (ChannelTemplate channelTemplate : channelTemplates) {
                if (getTemplateDao().canUserAcccessTemplate(channelTemplate.getTemplate(), userGroupMembershipIds)) {
                    response.channelTemplates.add(ChannelTemplateModel.factory(channelTemplate, getOutputHandlerFactory()));
                }
            }
        }
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
