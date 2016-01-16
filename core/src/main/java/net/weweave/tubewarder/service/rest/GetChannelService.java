package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.exception.PermissionException;
import net.weweave.tubewarder.service.model.ChannelModel;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.response.GetChannelResponse;
import org.apache.commons.validator.GenericValidator;
import net.weweave.tubewarder.dao.ChannelDao;
import net.weweave.tubewarder.domain.Channel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@RequestScoped
@Path("/channel/get")
public class GetChannelService extends AbstractService {
    @Inject
    private ChannelDao channelDao;

    @GET
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public GetChannelResponse action(@QueryParam("token") @DefaultValue("") String token,
                                     @QueryParam("id") @DefaultValue("") String id) {
        GetChannelResponse response = new GetChannelResponse();

        try {
            Session session = getSession(token);
            checkPermissions(session.getUser());
            setResponseList(response, id);
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
                !(user.getAllowChannels() || user.getAllowTemplates())) {
            throw new PermissionException();
        }
    }

    private void setResponseList(GetChannelResponse response, String id) throws ObjectNotFoundException {
        if (GenericValidator.isBlankOrNull(id)) {
            List<Channel> channels = getChannelDao().getAll();
            for (Channel channel : channels) {
                response.channels.add(ChannelModel.factory(channel));
            }
        } else {
            Channel channel = getChannelDao().get(id);
            response.channels.add(ChannelModel.factory(channel));
        }
    }

    public ChannelDao getChannelDao() {
        return channelDao;
    }

    public void setChannelDao(ChannelDao channelDao) {
        this.channelDao = channelDao;
    }
}
