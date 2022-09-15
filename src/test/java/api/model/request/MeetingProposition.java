package api.model.request;

public class MeetingProposition {

    private final String title;
    private final String description;
    private final int minimumNumberOfParticipants;
    private final int maximumSignedUpUsers;
    private final String topic;

    public MeetingProposition(
            String title,
            String description,
            int minimumNumberOfParticipants,
            int maximumSignedUpUsers,
            String topic
    ){
        this.title = title;
        this.description = description;
        this.minimumNumberOfParticipants = minimumNumberOfParticipants;
        this.maximumSignedUpUsers = maximumSignedUpUsers;
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getMinimumNumberOfParticipants() {
        return minimumNumberOfParticipants;
    }

    public int getMaximumSignedUpUsers() {
        return maximumSignedUpUsers;
    }

    public String getTopic() {
        return topic;
    }
}