package net.weweave.tubewarder.service.rest;

import net.weweave.tubewarder.dao.UserDao;
import net.weweave.tubewarder.domain.Session;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.exception.InvalidInputParametersException;
import net.weweave.tubewarder.exception.ObjectNotFoundException;
import net.weweave.tubewarder.service.model.ErrorCode;
import net.weweave.tubewarder.service.model.UserModel;
import net.weweave.tubewarder.service.request.AuthRequest;
import net.weweave.tubewarder.service.response.AuthResponse;
import org.apache.commons.validator.GenericValidator;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.logging.Logger;

@RequestScoped
@Path("/auth")
public class AuthService extends AbstractService {
    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    @Inject
    private UserDao userDao;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JaxApplication.APPLICATION_JSON_UTF8)
    public AuthResponse action(AuthRequest request) {
        AuthResponse response = new AuthResponse();
        try {
            LOG.info("REST Login attempt for username = " + request.username);
            validateInputParameters(request);
            checkAuthAndCreateSession(request, response);
            LOG.info("REST Login success for username = " + request.username + " - assigned session token = " + response.token);
        } catch (Exception e) {
            LOG.info("REST Login failure for username = " + request.username);
            response.error = ErrorCode.INVALID_INPUT_PARAMETERS;
        }
        return response;
    }

    private void validateInputParameters(AuthRequest request) throws InvalidInputParametersException {
        if (GenericValidator.isBlankOrNull(request.username) ||
                GenericValidator.isBlankOrNull(request.password)) {
            throw new InvalidInputParametersException();
        }
    }

    private void checkAuthAndCreateSession(AuthRequest request, AuthResponse response) throws ObjectNotFoundException, InvalidInputParametersException {
        User user = getUserDao().getByUsername(request.username);
        checkPassword(user, request.password);
        Session session = createSession(user);
        updateUserLastLoginDate(user);
        response.token = session.getExposableId();
        response.user = UserModel.factory(user);
    }

    private void checkPassword(User user, String expectedPassword) throws InvalidInputParametersException {
        if (!BCrypt.checkpw(expectedPassword, user.getHashedPassword())) {
            throw new InvalidInputParametersException();
        }
    }

    private Session createSession(User user) {
        Session session = new Session();
        session.setUser(user);
        session.setLoginDate(new Date());
        session.setLastActionDate(new Date());
        getSessionDao().store(session);
        return session;
    }

    private void updateUserLastLoginDate(User user) {
        user.setLastLogin(new Date());
        getUserDao().update(user);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
