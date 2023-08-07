package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "User", password = "654q321e", authorities = "ROLE_MODERATOR")
public class MyTest extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realmItm;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;

    private RealmResource realmResourceMock;
    private UsersResource usersResourceMock;
    private UserResource userResource;
    private UserRepresentation userRepresentationMock;
    private UUID testId;
    @BeforeEach
    void setUp(){
        realmResourceMock = mock(RealmResource.class);
        usersResourceMock = mock(UsersResource.class);
        userRepresentationMock = mock(UserRepresentation.class);
        userResource = mock(UserResource.class);
        testId = UUID.randomUUID();
    }

    @Test
    @SneakyThrows
    public void helloTest() {
        MockHttpServletResponse response = mvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User", response.getContentAsString());
    }
    @Test
    @SneakyThrows
    public void createUserTest() {

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());

        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                        new UserRequest(
                                "Pushkin",
                                "bobr@dobr.ru",
                                "654q321e",
                                "Alex",
                                "Pushkin")))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realmItm);
        verify(realmResourceMock).users();
        verify(usersResourceMock).create(any(UserRepresentation.class));
    }
    @Test
    @SneakyThrows
    public void createUserWrongRequestTest() {
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                        new UserRequest(
                                "",
                                "321",
                                "",
                                "",
                                "Невалид")))
                .andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
    @Test
    @SneakyThrows
    public void userCreateValid() {

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());

        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                new UserRequest(
                        "Qqqqq",
                        "qqqq@qqqq.com",
                        "654q321e",
                        "Qqqq",
                        "Aaaa"))).andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realmItm);
        verify(realmResourceMock).users();
        verify(usersResourceMock).create(any(UserRepresentation.class));

    }
    @Test
    @SneakyThrows
    public void getUserByIdTest() {

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(String.valueOf(testId))).thenReturn(userResource);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(String.valueOf(testId));
        userRepresentation.setFirstName("Lelick");
        userRepresentation.setLastName("Bolick");
        userRepresentation.setEmail("Lelick@mail.ru");

        when(userResource.toRepresentation()).thenReturn(userRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        MockHttpServletResponse response = mvc.perform(get("/api/users/" + testId))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    @SneakyThrows
    public void getUserById_UserNotFound_Test() {

        when(keycloak.realm("realm")).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(eq(String.valueOf(testId)))).thenReturn(null);

        MockHttpServletResponse response = mvc.perform(get("/api/users/{id}", testId))
                .andExpect(status().isInternalServerError())
//                .andExpect(status().is4xxClientError())
                .andReturn().getResponse();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

    }

}
