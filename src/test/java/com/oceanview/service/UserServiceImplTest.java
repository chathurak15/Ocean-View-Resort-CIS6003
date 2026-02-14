package com.oceanview.service;
import com.oceanview.dao.FakeUserDAO;
import com.oceanview.dto.user.LoginRequestDTO;
import com.oceanview.dto.user.RegisterDTO;
import com.oceanview.exception.ForbiddenOperationException;
import com.oceanview.model.User;
import com.oceanview.model.enums.UserType;
import com.oceanview.service.impl.UserServiceImpl;
import com.oceanview.util.PasswordHasher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserServiceImplTest {
    private FakeUserDAO fakeDAO;
    private UserServiceImpl service;

    @Before
    public void setUp() {
        fakeDAO = new FakeUserDAO();
        service = new UserServiceImpl(fakeDAO);
    }

    //Test findById
    @Test
    public void findById_shouldReturnUser_whenExists() {
        User user = new User();
        user.setUserName("u1");
        user.setPassword("x");
        user.setUserType(UserType.RECEPTIONIST);
        user.setActive(true);

        fakeDAO.seed(user);
        Assert.assertTrue(fakeDAO.findById(user.getUserId()).isPresent());
    }
    //Test findById when user not found
    @Test
    public void findById_shouldReturnEmpty_whenNotExists() {
        Assert.assertFalse(fakeDAO.findById(999).isPresent());
    }

    //Test getAllUsers
    @Test
    public void getAllUsers_shouldReturnList() {
        User u1 = new User();
        u1.setUserName("u1");
        u1.setPassword(PasswordHasher.hash("123"));
        u1.setUserType(UserType.RECEPTIONIST);
        u1.setActive(true);

        User u2 = new User();
        u2.setUserName("u2");
        u2.setPassword(PasswordHasher.hash("123"));
        u2.setUserType(UserType.RECEPTIONIST);
        u2.setActive(true);
        fakeDAO.seed(u1);
        fakeDAO.seed(u2);
        Assert.assertEquals(2, service.getAllUsers().size());
    }

    //authenticate Null Username
    @Test
    public void authenticate_shouldReturnNull_whenUsernameNull() {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUserName(null);
        login.setPassword("123");

        Assert.assertNull(service.authenticate(login));
    }
    //User Not Found
    @Test
    public void authenticate_shouldReturnNull_whenUserNotFound() {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUserName("unknown");
        login.setPassword("123");

        Assert.assertNull(service.authenticate(login));
    }
    //Correct Password
    @Test
    public void authenticate_shouldReturnUser_whenPasswordMatches() {
        User user = new User();
        user.setUserName("admin");
        user.setPassword(PasswordHasher.hash("123"));
        user.setUserType(UserType.ADMINISTRATOR);
        user.setActive(true);

        fakeDAO.seed(user);

        LoginRequestDTO login = new LoginRequestDTO();
        login.setUserName("admin");
        login.setPassword("123");

        Assert.assertNotNull(service.authenticate(login));
    }
    //Incorrect Password
    @Test
    public void authenticate_shouldReturnNull_whenPasswordIncorrect() {
        User user = new User();
        user.setUserName("admin");
        user.setPassword(PasswordHasher.hash("correct"));
        user.setUserType(UserType.ADMINISTRATOR);
        user.setActive(true);

        fakeDAO.seed(user);

        LoginRequestDTO login = new LoginRequestDTO();
        login.setUserName("admin");
        login.setPassword("wrong");

        Assert.assertNull(service.authenticate(login));
    }

    //Valid Registration
    @Test
    public void createUser_shouldReturnUser_whenValid() {
        RegisterDTO reg = new RegisterDTO();
        reg.setUserName("newUser");
        reg.setPassword("123");
        reg.setUserType(UserType.ADMINISTRATOR);

        Assert.assertNotNull(service.createUser(reg));
    }
    //Empty Username - Registration
    @Test
    public void createUser_shouldReturnNull_whenUsernameInvalid() {

        RegisterDTO reg = new RegisterDTO();
        reg.setUserName("");
        reg.setPassword("123");

        Assert.assertNull(service.createUser(reg));
    }
    //Duplicate Username Registration
    @Test
    public void createUser_shouldReturnNull_whenUsernameExists() {

        User existing = new User();
        existing.setUserName("admin");
        existing.setPassword(PasswordHasher.hash("123"));
        existing.setUserType(UserType.ADMINISTRATOR);
        existing.setActive(true);

        fakeDAO.seed(existing);

        RegisterDTO register = new RegisterDTO();
        register.setUserName("admin");
        register.setPassword("123");
        register.setUserType(UserType.ADMINISTRATOR);

        Assert.assertNull(service.createUser(register));
    }

    //Status Update Failure(Admin Cannot Be Deactivated)
    @Test(expected = RuntimeException.class)
    public void updateUserStatus_shouldThrow_whenAdminDeactivated() {
        User admin = new User();
        admin.setUserName("admin");
        admin.setPassword(PasswordHasher.hash("123"));
        admin.setUserType(UserType.ADMINISTRATOR);
        admin.setActive(true);

        fakeDAO.seed(admin);
        service.updateUserStatus(admin.getUserId(), false);
    }
    //Status Update Success
    @Test
    public void updateUserStatus_shouldUpdate_whenValidUser() {
        User user = new User();
        user.setUserName("staff");
        user.setPassword(PasswordHasher.hash("123"));
        user.setUserType(UserType.RECEPTIONIST);
        user.setActive(true);

        fakeDAO.seed(user);

        boolean result = service.updateUserStatus(user.getUserId(), false);

        Assert.assertTrue(result);
    }
    //Status Update Success
    @Test
    public void updateUserStatus_shouldReturnTrue_whenStatusAlreadySame() {
        User user = new User();
        user.setUserName("staff2");
        user.setPassword(PasswordHasher.hash("123"));
        user.setUserType(UserType.RECEPTIONIST);
        user.setActive(true);
        fakeDAO.seed(user);
        boolean result = service.updateUserStatus(user.getUserId(), true);
        Assert.assertTrue(result);
    }
    //Status Update Failure(User Not Found)
    @Test(expected = RuntimeException.class)
    public void updateUserStatus_shouldThrow_whenUserNotFound() {
        service.updateUserStatus(999, true);
    }

    //Test deleteUser
    @Test
    public void deleteUser_shouldReturnTrue_whenValidUser() {
        User user = new User();
        user.setUserName("staff3");
        user.setPassword(PasswordHasher.hash("123"));
        user.setUserType(UserType.RECEPTIONIST);
        user.setActive(true);
        fakeDAO.seed(user);
        Assert.assertTrue(service.deleteUser(user.getUserId()));
    }
    //Test deleteUser when user not found
    @Test
    public void deleteUser_shouldReturnFalse_whenUserNotFound() {
        Assert.assertFalse(service.deleteUser(999));
    }
    //Admin Cannot Be Deleted
    @Test(expected = ForbiddenOperationException.class)
    public void deleteUser_shouldThrowException_whenAdmin() {

        User admin = new User();
        admin.setUserName("admin");
        admin.setPassword(PasswordHasher.hash("123"));
        admin.setUserType(UserType.ADMINISTRATOR);
        admin.setActive(true);

        fakeDAO.seed(admin);

        service.deleteUser(admin.getUserId());
    }

}
