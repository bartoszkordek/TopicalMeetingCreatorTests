package api.model.request;

import java.util.List;

public class UserRegistration {

    private final String email;
    private final String birthDate;
    private final String login;
    private final List<String> topics;

    public UserRegistration(
            String email,
            String birthDate,
            String login,
            List<String> topics
    ){
        this.email = email;
        this.birthDate = birthDate;
        this.login = login;
        this.topics = topics;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getLogin() {
        return login;
    }

    public List<String> getTopics() {
        return topics;
    }
}