package com.micro.userservice.models;

import com.micro.userservice.models.dto.AuthProvider;
import com.micro.userservice.models.dto.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_provider_id", columnList = "provider, provider_id")
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails, OAuth2User {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,63}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Email should be valid")
    private String email;

    @Column(nullable = true) // jer 02auth korisnici nemaju lozinke samo tokene
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified")
    private boolean emailVerified = false;


    @ElementCollection(fetch = FetchType.EAGER) // potrebno nam je pozvati korisnike prije
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<UserToken> tokens = new HashSet<>();


    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    @ColumnTransformer(write = "?::?::jsonb") // ekplicitno castanje u jsnob
    private Map<String, Object> attributes = new HashMap<>();

    // pomocne metode i factory metode

    public static User createLocalUser(String email, String password, String firstName, String lastName){
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.addRole(UserRole.USER);
        return user;
    }


    public static User createUserWithRoles(String email, String password, String firstName,
                                            String lastName, AuthProvider provider, UserRole... roles){ //... je arbitary number argm i mozemo proslijediti numb of parms u metodu
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAuthProvider(provider);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.getRoles().addAll(Arrays.asList(roles));
        return user;
    }

    public static User createOAuth2User(String email, String password, String firstName,
                                         String lastName, AuthProvider provider, String providerId){
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAuthProvider(provider);
        user.setProviderId(providerId);
        user.setEmailVerified(true);
        user.addRole(UserRole.USER);
        return user;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);

    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    // ovo nam sluzi za associjace posto imamo onetomany i obrnutno pa time hendlamo povezanosti ili ti veze
    public void addToken(UserToken token) {
       tokens.add(token);
       token.setUser(this);
    }

    private void removeToken(UserToken token) {
        tokens.remove(token);
        token.setUser(null);
    }

    public void deactivateAccount(){
        this.enabled = false;
        this.tokens.forEach(token -> token.setRevoked(true));
    }

    public void updateProfil(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean hasRole(UserRole role){
        return this.roles.contains(role);
    }


    @Override
    public String getName() {
        return email; // jer nam je unique i preko emaila dohvatamo korisnka
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
