package com.rayanfadhlaoui.domain.services.user;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.rayanfadhlaoui.domain.model.entities.User;
import com.rayanfadhlaoui.domain.model.other.State;
import com.rayanfadhlaoui.domain.model.other.State.Status;
import com.rayanfadhlaoui.domain.services.user.InMemoryUserRepository;
import com.rayanfadhlaoui.domain.services.user.UserService;
import com.rayanfadhlaoui.domain.services.user.UserUpdater;
import com.rayanfadhlaoui.domain.services.utils.Generator;

public class UserManagementTest {

	final DateTimeFormatter MY_PATTERN = DateTimeFormatter.ofPattern("dd/MM/yyyy");	
	private UserService userManagement;
	
	@Before
	public void setUp() {
		Generator generator = Generator.getInstance();
		userManagement = new UserService(new InMemoryUserRepository(), generator);
	}

	@Test
	public void testUserCreationOK() {

		String firstName = "Rayan";
		String lastName = "Fadhlaoui";
		LocalDate birthdate = LocalDate.parse("19/09/1989", MY_PATTERN);
		String address = "16 B Avenue Albert 1ER 94210";
		String phoneNumber = "0664197893";
		State state = userManagement.createUser(firstName, lastName, birthdate, address, phoneNumber);

		assertEquals(Status.OK, state.getStatus());

		User user = userManagement.getAllUsers().get(0);
		assertEquals(firstName, user.getFirstName());
		assertEquals(lastName, user.getLastName());
		assertEquals(birthdate, user.getBirthdate());
		assertEquals(address, user.getAddress());
		assertEquals(phoneNumber, user.getPhoneNumber());
	}

	@Test
	public void testUserCreationWithErrors() {

		State state = userManagement.createUser(null, null, null, null, null);
		assertEquals(Status.KO, state.getStatus());
		assertEquals("Missing fields: (First name, Last name, Birthdate, Address, Phone number )", state.getMessages().get(0));
	}

	@Test
	public void testUserCreationWithIncompletePhoneNumber() {

		String firstName = "Rayan";
		String lastName = "Fadhlaoui";
		LocalDate birthdate = LocalDate.parse("19/09/1989", MY_PATTERN);
		String address = "16 B Avenue Albert 1ER 94210";
		State state9Numbers = userManagement.createUser(firstName, lastName, birthdate, address, "+011150459");
		State stateSpecialCharac = userManagement.createUser(firstName, lastName, birthdate, address, "0-11150459");
		State stateLetters = userManagement.createUser(firstName, lastName, birthdate, address, "01457664te");

		assertEquals(Status.KO, state9Numbers.getStatus());
		assertEquals("Invalid phone number", state9Numbers.getMessages().get(0));

		assertEquals(Status.KO, stateSpecialCharac.getStatus());
		assertEquals("Invalid phone number", stateSpecialCharac.getMessages().get(0));

		assertEquals(Status.KO, stateLetters.getStatus());
		assertEquals("Invalid phone number", stateLetters.getMessages().get(0));
	}

	@Test
	public void testUserUpdateOK() {

		User user = createAndAddUser("Rayan", "Fadhlaoui", LocalDate.parse("19/09/1989", MY_PATTERN), userManagement);

		UserUpdater userUpdater = userManagement.getUserUpdater(user);
		userUpdater.setFirstName("Jean");
		userUpdater.setLastName("Dupont");
		userUpdater.setBirthdate(LocalDate.parse("19/08/1989", MY_PATTERN));
		userUpdater.setAddress("3 Avenue Albert 94430");
		userUpdater.setPhoneNumber("0145766419");

		State state = userManagement.updateUser(userUpdater);

		assertEquals(Status.OK, state.getStatus());

		user = userManagement.getAllUsers().get(0);
		assertEquals("Jean", user.getFirstName());
		assertEquals("Dupont", user.getLastName());
		assertEquals(LocalDate.parse("19/08/1989", MY_PATTERN), user.getBirthdate());
		assertEquals("3 Avenue Albert 94430", user.getAddress());
		assertEquals("0145766419", user.getPhoneNumber());

	}

	@Test
	public void testUserUpdateWithMultipleError() {

		User user = createAndAddUser("Rayan", "Fadhlaoui", LocalDate.parse("19/09/1989", MY_PATTERN), userManagement);

		UserUpdater userUpdater = userManagement.getUserUpdater(user);
		userUpdater.setFirstName("");
		userUpdater.setLastName("Dupont");
		userUpdater.setBirthdate(LocalDate.parse("19/08/1989", MY_PATTERN));
		userUpdater.setAddress("3 Avenue Albert 94430");
		userUpdater.setPhoneNumber("014766419");

		State state = userManagement.updateUser(userUpdater);

		assertEquals(Status.KO, state.getStatus());
		assertEquals("Missing fields: (First name )", state.getMessages().get(0));
		assertEquals("Invalid phone number", state.getMessages().get(1));

	}

	@Test
	public void testDeleteUser() {

		User rayanUser = createAndAddUser("Rayan", "Fadhlaoui", LocalDate.parse("19/09/1989", MY_PATTERN), userManagement);

		State state = userManagement.deleteUser(rayanUser);
		assertEquals(Status.OK, state.getStatus());
		assertEquals(0, userManagement.getAllUsers().size());

		state = userManagement.deleteUser(rayanUser);
		assertEquals(Status.KO, state.getStatus());
		assertEquals("User does not exist", state.getMessages().get(0));
	}

	@Test
	public void testFindAndDisplayUser() {
		mockLoginGenerator("AB12345678");
		
		Generator generator = Generator.getInstance();
		userManagement = new UserService(new InMemoryUserRepository(), generator);


		User rayanUser = createAndAddUser("Rayan", "Fadhlaoui", LocalDate.parse("19/09/1989", MY_PATTERN), userManagement);
		User user = userManagement.findUser(rayanUser.getLogin());
		
		assertEquals(rayanUser.getFirstName(), user.getFirstName());
		assertEquals(rayanUser.getLastName(), user.getLastName());
		assertEquals(rayanUser.getBirthdate(), user.getBirthdate());
		assertEquals(rayanUser.getAddress(), user.getAddress());
		assertEquals(rayanUser.getPhoneNumber(), user.getPhoneNumber());
		String expectedUserDisplay = "Login: AB12345678\n" + 
				"First name : Rayan\n" + 
				"Last name : Fadhlaoui\n" + 
				"Birthdate : 19/09/1989\n" + 
				"Adrress: 16 B Avenue Albert 1ER 94210\n" + 
				"Phone number : 0664197893";
		assertEquals(expectedUserDisplay, user.toString());
	}
	
	private User createAndAddUser(String firstName, String lastName, LocalDate birthdate, UserService userManagement) {
		String address = "16 B Avenue Albert 1ER 94210";
		String phoneNumber = "0664197893";
		userManagement.createUser(firstName, lastName, birthdate, address, phoneNumber);

		User user = userManagement.getAllUsers().get(0);
		return user;
	}
	
	private void mockLoginGenerator(String login) {
		Generator loginGeneratorMock = Mockito.mock(Generator.class);

		Mockito.when(loginGeneratorMock.generateLogin()).thenReturn(login);

		Field field;
		try {
			field = Generator.class.getDeclaredField("INSTANCE");
			field.setAccessible(true);
			field.set(null, loginGeneratorMock);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.getStackTrace();
		}
	}
}
