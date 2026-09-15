package com.ecommerce.userservice.Service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KeycloakService {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    public String createUser(
            String email,
            String password,
            String firstName,
            String lastName) {

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("ecommerce")
                .username(adminUsername)
                .password(adminPassword)
                .clientId(adminClientId)
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        UserRepresentation user = new UserRepresentation();

        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);

        CredentialRepresentation credential =
                new CredentialRepresentation();

        credential.setType(
                CredentialRepresentation.PASSWORD
        );

        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(
                Collections.singletonList(credential)
        );

        Response response = keycloak
                .realm(realm)
                .users()
                .create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException(
                    "Failed to create user in Keycloak. Status: "
                            + response.getStatus()
            );
        }

        String location = response
                .getHeaderString("Location");

        return location.substring(
                location.lastIndexOf("/") + 1
        );
    }
}