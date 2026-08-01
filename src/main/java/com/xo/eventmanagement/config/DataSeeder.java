package com.xo.eventmanagement.config;

import com.xo.eventmanagement.entity.Category;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.repository.CategoryRepository;
import com.xo.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default admin account if none exists
        if (!userRepository.existsByEmail("admin@xoevents.com")) {
            User admin = new User();
            admin.setName("XO Admin");
            admin.setEmail("admin@xoevents.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ADMIN);
            admin.setPhone("9999999999");
            admin.setEmailVerified(true);
            userRepository.save(admin);
            System.out.println(">> Default admin created: admin@xoevents.com / Admin@123");
        }

        // Seed default categories if none exist
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "Music", "Concerts, gigs and music festivals"));
            categoryRepository.save(new Category(null, "Technology", "Tech talks, hackathons and conferences"));
            categoryRepository.save(new Category(null, "Sports", "Sporting events and tournaments"));
            categoryRepository.save(new Category(null, "Business", "Business summits, networking and expos"));
            categoryRepository.save(new Category(null, "Arts & Theatre", "Drama, exhibitions and art shows"));
            categoryRepository.save(new Category(null, "Education", "Workshops, seminars and webinars"));
            System.out.println(">> Default categories seeded");
        }
    }
}
