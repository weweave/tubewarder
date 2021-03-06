package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.dao.ChannelTemplateDao;
import net.weweave.tubewarder.dao.UserGroupDao;
import net.weweave.tubewarder.domain.ChannelTemplate;
import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.exception.PermissionException;
import net.weweave.tubewarder.outputhandler.OutputHandlerFactory;
import net.weweave.tubewarder.service.response.GetTemplateResponse;
import org.apache.commons.validator.GenericValidator;
import net.weweave.tubewarder.dao.TemplateDao;
import net.weweave.tubewarder.domain.Template;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.model.TemplateModel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@RequestScoped
@Path("/template/get")
public class GetTemplateService extends AbstractService {
    @Inject
    private TemplateDao templateDao;

    @Inject
    private OutputHandlerFactory outputHandlerFactory;

    @Inject
    private UserGroupDao userGroupDao;

    @Inject
    private ChannelTemplateDao channelTemplateDao;

    @GET
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public GetTemplateResponse action(@QueryParam("token") @DefaultValue("") String token,
                                      @QueryParam("id") @DefaultValue("") String id) {
        GetTemplateResponse response = new GetTemplateResponse();

        try {
            Session session = getSession(token);
            checkPermissions(session.getUser());
            setResponseList(session.getUser(), response, id);
        } catch (ObjectNotFoundException e) {
            response.error = ErrorCode.OBJECT_LOOKUP_ERROR;
        } catch (PermissionException e) {
            response.error = ErrorCode.PERMISSION_DENIED;
        } catch (AuthRequiredException e) {
            response.error = ErrorCode.AUTH_REQUIRED;
        }

        return response;
    }

    private void checkPermissions(User user) throws PermissionException {
        if (user == null ||
                !user.getAllowTemplates()) {
            throw new PermissionException();
        }
    }

    private void setResponseList(User user, GetTemplateResponse response, String id) throws ObjectNotFoundException {
        List<Long> userGroupMembershipIds = getUserGroupDao().getGroupMembershipIds(user);
        if (GenericValidator.isBlankOrNull(id)) {
            List<Template> templates = getTemplateDao().getAll();
            for (Template template : templates) {
                if (getTemplateDao().canUserAcccessTemplate(template, userGroupMembershipIds)) {
                    List<ChannelTemplate> channelTemplates = getChannelTemplateDao().getChannelTemplatesForTemplate(template.getId());
                    response.templates.add(TemplateModel.factory(template, channelTemplates, getOutputHandlerFactory()));
                }
            }
        } else {
            Template template = getTemplateDao().get(id);
            if (getTemplateDao().canUserAcccessTemplate(template, userGroupMembershipIds)) {
                List<ChannelTemplate> channelTemplates = getChannelTemplateDao().getChannelTemplatesForTemplate(template.getId());
                response.templates.add(TemplateModel.factory(template, channelTemplates, getOutputHandlerFactory()));
            } else {
                throw new ObjectNotFoundException();
            }
        }
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

    public ChannelTemplateDao getChannelTemplateDao() {
        return channelTemplateDao;
    }

    public void setChannelTemplateDao(ChannelTemplateDao channelTemplateDao) {
        this.channelTemplateDao = channelTemplateDao;
    }
}
