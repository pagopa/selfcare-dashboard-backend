package it.pagopa.selfcare.dashboard.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PartyAuthenticationProviderTest {

    @Test
    void additionalAuthenticationChecks() {
    }

    @Test
    void retrieveUser() {
        // given
        String username = "username";
        PartyAuthenticationProvider authenticationProvider = new PartyAuthenticationProvider(null);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "credentials");
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        assertNotNull(userDetails);
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        Optional<? extends GrantedAuthority> grantedAuthority = userDetails.getAuthorities().stream().findAny();
        assertTrue(grantedAuthority.isPresent());
        assertEquals("ADMIN", grantedAuthority.get().getAuthority());
    }

}