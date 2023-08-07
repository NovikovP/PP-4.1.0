package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "User", password = "user", authorities = "ROLE_MODERATOR")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realmItm;

    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;

    /*
Данный код тестирует метод hello() контроллера UserController.
В этом тесте отправляется GET-запрос по адресу /api/users/hello.
Метод andReturn() возвращает объект MvcResult, который содержит информацию о выполненном запросе и ответе сервера.
Метод getResponse() возвращает объект MockHttpServletResponse, который содержит информацию о HTTP-ответе сервера.
В этом тесте проверяется, что HTTP-статус ответа равен 200 (HttpStatus.OK.value()), а содержимое ответа равно строке "User".
Если эти условия выполняются, то тест считается пройденным.
     */
    @Test
    public void helloMethodTest() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/api/users/hello")).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("User", response.getContentAsString());
    }
    /*
    Объявление тестового метода userCreatedTest().
Создание заглушек объектов RealmResource, UsersResource и UserRepresentation.
Настройка заглушек. Указывается, что при вызове метода realm() объекта Keycloak с аргументом realmItm должен возвращаться объект realmResourceMock,
при вызове метода users() объекта realmResourceMock должен возвращаться объект usersResourceMock, при вызове метода create() объекта usersResourceMock
 с любым аргументом должен возвращаться HTTP-ответ со статусом CREATED, а при вызове метода getId() объекта userRepresentationMock должен возвращаться
случайно сгенерированный идентификатор пользователя.
Отправка POST-запроса на адрес /api/users с телом запроса, содержащим информацию о новом пользователе.
Для этого используется метод requestWithContent(), который создает объект MockHttpServletRequestBuilder с указанным телом запроса.
Получение HTTP-ответа на отправленный запрос. Для этого используется метод andReturn(),
который возвращает объект MvcResult, содержащий информацию о выполненном запросе и ответе сервера, и метод getResponse(),
который возвращает объект MockHttpServletResponse, содержащий информацию о HTTP-ответе сервера.
Проверка, что HTTP-статус ответа равен 200 (HttpStatus.OK.value()), используя метод assertEquals().
Проверка, что были вызваны методы realm(), users() и create() соответствующих объектов, используя методы verify().
Если эти методы не были вызваны, то тест не пройден.
     */
    @Test
    public void userCreatedTest() throws Exception {
        RealmResource realmResourceMock = mock(RealmResource.class);
        UsersResource usersResourceMock = mock(UsersResource.class);
        UserRepresentation userRepresentationMock = mock(UserRepresentation.class);

        when(keycloak.realm(realmItm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentationMock.getId()).thenReturn(UUID.randomUUID().toString());

        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                new UserRequest(
                        "User",
                        "user@mail.ru",
                        "user",
                        "User",
                        "User"))).andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void createUserInvalidRequestTest() throws Exception {
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"),
                        new UserRequest(
                                "",
                                "user@mail.com",
                                "user",
                                "User",
                                "User")))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
    /*
    Объявление тестового метода getUserByIdTest().
    Генерация случайного идентификатора пользователя.
    Создание заглушек объектов RealmResource, UserResource и UsersResource.
    Настройка заглушек. Указывается, что при вызове метода realm() объекта Keycloak с аргументом realmItm должен возвращаться объект realmResource,
    при вызове метода users() объекта realmResource должен возвращаться объект usersResource, а при вызове метода get() объекта usersResource с аргументом,
    равным строковому представлению случайного идентификатора, должен возвращаться объект userResource.
    Создание объекта UserRepresentation и заполнение его полями.
    Настройка поведения заглушки userResource. Указывается, что при вызове метода toRepresentation() должен возвращаться созданный объект UserRepresentation,
    при вызове метода roles() должна возвращаться заглушка объекта RoleMappingResource, а при вызове метода getAll() заглушки объекта RoleMappingResource
    должна возвращаться заглушка объекта MappingsRepresentation.
    Отправка GET-запроса на адрес /api/users/ с указанным идентификатором пользователя. Для этого используется метод get() объекта MockMvc.
    Вывод информации о запросе и ответе в консоль.
    Проверка HTTP-статуса ответа (ожидается статус 200 OK), а также проверка значений полей JSON-объекта, содержащегося в теле ответа.
    Для этого используются методы andExpect() и jsonPath() объекта ResultActions.
    Получение HTTP-ответа из объекта MvcResult.
    Проверка HTTP-статуса ответа (ожидается статус 200 OK) с помощью метода assertEquals().
     */
    @Test
    public void getUserByIdTest() throws Exception {
        UUID testId = UUID.randomUUID();
        RealmResource realmResource = Mockito.mock(RealmResource.class);
        UserResource userResource = Mockito.mock(UserResource.class);
        UsersResource usersResource = Mockito.mock(UsersResource.class);

        when(keycloak.realm(realmItm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(testId))).thenReturn(userResource);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(String.valueOf(testId));
        userRepresentation.setFirstName("test");
        userRepresentation.setLastName("test");
        userRepresentation.setEmail("test@mail.ru");

        when(userResource.toRepresentation()).thenReturn(userRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        MockHttpServletResponse response = mvc.perform(get("/api/users/" + testId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("test"))
                .andExpect(jsonPath("$.lastName").value("test"))
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
}
