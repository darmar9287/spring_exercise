package com.spring.exercise.unittests;

import com.spring.exercise.repository.UserRepository;
import org.mockito.Mockito;

public class RegisterUserTests {

    private UserRepository userRepository = Mockito.mock(UserRepository.class);
//
//    private UserService userService;
//
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @BeforeEach
//    void initUseCase() {
//        userService = new UserServiceImpl(userRepository,bCryptPasswordEncoder);
//    }
//
//    @Test
//    void savedUserHasRegistrationDate() {
//        User user = new User("zaphod", "zaphod@mail.com");
//        User savedUser = registerUseCase.registerUser(user);
//        assertThat(savedUser.getRegistrationDate()).isNotNull();
//    }
//

}
