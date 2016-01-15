package net.weweave.tubewarder.test.rest;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.ResponseSpecification;
import net.weweave.tubewarder.dao.UserDao;
import net.weweave.tubewarder.domain.User;
import net.weweave.tubewarder.test.DbTestAssist;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONObject;
import org.junit.Before;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import java.net.URL;

import static com.jayway.restassured.RestAssured.given;

public abstract class AbstractRestTest {
    @ArquillianResource
    public URL deploymentUrl;

    @Inject
    private DbTestAssist dbTestAssist;

    @Inject
    private UserDao userDao;

    @Before
    public final void initialize() {
        getDbTestAssist().cleanDb();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "net.weweave.tubewarder")
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource("resources.xml")
                .addAsWebInfResource("openejb-jar.xml");
        return war;
    }

    public String getUri(String serviceName) {
        return deploymentUrl + "rs/" + serviceName;
    }

    protected ResponseSpecification getResponseSpecificationGet(String var1, Object var2, Object... var3) {
        ResponseSpecification response = given()
                .parameters(var1, var2, var3)
                .contentType(ContentType.JSON)
        .expect()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK);
        return response;
    }

    protected ResponseSpecification getResponseSpecificationPost(JSONObject payload) {
        ResponseSpecification response = given()
                .body(payload.toString())
                .contentType(ContentType.JSON)
        .expect()
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.SC_OK);
        return response;
    }

    protected void setExpectedBodies(ResponseSpecification response, Object... body) {
        String oName = "";
        int i = 1;
        for (Object o : body) {
            if (i%2 == 1) {
                oName = (String)o;
            } else {
                response.body(oName, (Matcher)o);
            }
            i++;
        }
    }

    protected JSONObject getPostResponse(ResponseSpecification response, String service) {
        String responseString = response.when()
                .post(getUri(service))
                .asString();
        return new JSONObject(responseString);
    }

    protected JSONObject getGetResponse(ResponseSpecification response, String service) {
        String responseString = response.when()
                .get(getUri(service))
                .asString();
        return new JSONObject(responseString);
    }

    protected User createAdminUser() {
        User user = new User();
        user.setUsername("admin");
        user.setDisplayName("System Administrator");
        user.setHashedPassword(BCrypt.hashpw("admin", BCrypt.gensalt()));
        user.setEnabled(true);
        user.setAllowAppTokens(true);
        user.setAllowChannels(true);
        user.setAllowTemplates(true);
        user.setAllowUsers(true);
        getUserDao().store(user);
        return user;
    }

    protected String authAdminGetToken() {
        JSONObject payload = new JSONObject();
        payload.put("username", "admin");
        payload.put("password", "admin");

        JSONObject result = new JSONObject(given()
                .body(payload.toString())
                .contentType(ContentType.JSON)
        .when()
                .post(getUri("auth"))
                .asString());
        return result.getString("token");
    }

    public DbTestAssist getDbTestAssist() {
        return dbTestAssist;
    }

    public void setDbTestAssist(DbTestAssist dbTestAssist) {
        this.dbTestAssist = dbTestAssist;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
