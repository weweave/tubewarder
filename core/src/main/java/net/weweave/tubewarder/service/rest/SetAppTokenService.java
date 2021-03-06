package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.exception.AuthRequiredException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.dao.AppTokenDao;
import net.weweave.tubewarder.domain.AppToken;
import net.weweave.tubewarder.exception.InvalidInputParametersException;
import net.weweave.tubewarder.exception.PermissionException;
import net.weweave.tubewarder.service.model.AppTokenModel;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.request.SetAppTokenRequest;
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
@Path("/apptoken/set")
public class SetAppTokenService extends AbstractSetObjectService<AppTokenModel, AppToken> {
    @Inject
    private AppTokenDao appTokenDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public SetObjectRestResponse action(SetAppTokenRequest request) {
        SetObjectRestResponse response = new SetObjectRestResponse();
        try {
            Session session = getSession(request.token);
            checkPermissions(session.getUser());
            validateInputParameters(request.object);
            AppToken object = createUpdateObject(request.object);
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

    private void checkPermissions(User user) throws PermissionException {
        if (user == null ||
                !user.getAllowAppTokens()) {
            throw new PermissionException();
        }
    }

    @Override
    protected void validateInputParameters(AppTokenModel model) throws InvalidInputParametersException {
        if (GenericValidator.isBlankOrNull(model.name)) {
            throw new InvalidInputParametersException("name", ErrorCode.FIELD_REQUIRED);
        }
    }

    @Override
    protected AppToken createObject(AppTokenModel model) throws ObjectNotFoundException {
        AppToken object = new AppToken();
        getObjectDao().store(object);
        return object;
    }

    @Override
    protected void updateObject(AppToken object, AppTokenModel model) throws ObjectNotFoundException {
        object.setName(model.name.trim());
        getObjectDao().update(object);
    }

    @Override
    protected AppTokenDao getObjectDao() {
        return appTokenDao;
    }
}
