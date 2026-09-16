package com.example.bank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Banking Application REST API",
                version = "v1",
                description = """
                        REST API for the Banking Application.

                        The API provides customer, employee, account,
                        transaction, loan, investment, notification,
                        audit and reporting operations.

                        Financial operations are transactional and protected
                        by role-based authorization and ownership rules.
                        """,
                contact = @Contact(
                        name = "Banking Application"
                ),
                license = @License(
                        name = "Internal Application License"
                )
        )
//        security = {
//                @SecurityRequirement(
//                        name = "bearerAuth"
//                )
//        }
)
//@SecurityScheme(
//        name = "bearerAuth",
//        type = SecuritySchemeType.HTTP,
//        scheme = "bearer",
//        bearerFormat = "JWT",
//        in = SecuritySchemeIn.HEADER
//)
public class OpenApiConfig {
}