package com.myorg;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.data.jpa.repository.JpaRepository;
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
        private final StreamBridge streamBridge;

        public UserService(UserRepository userRepository, StreamBridge streamBridge) {
            this.userRepository = userRepository;
            this.streamBridge = streamBridge;
        }

        public User saveUser(User user) {
            User saved = userRepository.save(user);
            streamBridge.send("userCreated-out-0", saved);
            return saved;
        }

        public Optional<User> findUserById(Long id) {
            return userRepository.findById(id);
        }

        public List<User> findAllUsers() {
            return userRepository.findAll();
        }
    }

    @Entity
    @Table(name = "users")
    public static class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "username", nullable = false)
        private String username;

        @Column(name = "email", nullable = false)
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

    public interface UserRepository extends JpaRepository<User, Long> {}
}