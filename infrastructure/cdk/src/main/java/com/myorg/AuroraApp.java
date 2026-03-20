package com.myorg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class AuroraApp {

    public static void main(String[] args) {
        SpringApplication.run(AuroraApp.class, args);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue myQueue() {
        return new Queue("my.queue", false);
    }

    @Service
    @Transactional
    public static class UserService {

        private final UserRepository userRepository;

        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        public User saveUser(User user) {
            User saved = userRepository.save(user);
            return saved;
        }

        public Optional<User> findUserById(Long id) {
            return userRepository.findById(id);
        }

        public List<User> findAllUsers() {
            return userRepository.findAll();
        }
    }

    public static class User {

        private Long id;
        private String username;
        private String email;

        public User() {}

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public interface UserRepository {
        User save(User user);
        Optional<User> findById(Long id);
        List<User> findAll();
    }
}