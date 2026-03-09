package dk.bjom.hotPotato.webhook;

public class WebhookMessage {
    private static final String DEFAULT_USERNAME = "Hot potato bot";
    private static final String DEFAULT_AVATAR_URL = "https://i.imgur.com/4M34hi2.png";

    private String content;
    private String username;
    private String avatar_url;

    public WebhookMessage(String content) {
        this.content = content;
        this.username = DEFAULT_USERNAME;
        this.avatar_url = DEFAULT_AVATAR_URL;
    }

    public WebhookMessage(String content, String username, String avatar_url) {
        this.content = content;
        this.username = username != null ? username : DEFAULT_USERNAME;
        this.avatar_url = avatar_url != null ? avatar_url : DEFAULT_AVATAR_URL;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    @Override
    public String toString() {
        return "WebhookMessage{" +
                "content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                '}';
    }
}
