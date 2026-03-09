package dk.bjom.hotPotato.webhook;

import dk.bjom.hotPotato.HotPotato;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Logger;

public class WebhookHandler {
    private final String url;
    private final HotPotato plugin;
    private final Logger LOGGER = Logger.getLogger(WebhookHandler.class.getName());
    private String username;
    private String avatarUrl;

    public WebhookHandler(
            String url,
            HotPotato plugin) {
        this.url = url;
        this.plugin = plugin;
    }

    public void SetUsername(String username) {
        this.username = username;
    }

    public void SetAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void SendMessage(String message) throws IOException {
        if (!plugin.getConfig().getBoolean("webhook.enabled")) return;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            WebhookMessage webhookMessage = getWebhookMessage(message);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(webhookMessage);
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
                    .setEntity(entity)
                    .build();

            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    LOGGER.info("Message sent successfully!");
                } else {
                    LOGGER.warning("Failed to send message. HTTP Status: " + statusCode);
                }
                final HttpEntity responseEntity = response.getEntity();
                EntityUtils.consume(responseEntity);
                return null;
            });
        }
    }

    private WebhookMessage getWebhookMessage(String message) {
        WebhookMessage webhookMessage = new WebhookMessage(message);
        if (this.username != null) {
            webhookMessage.setUsername(this.username);
        }
        if (this.avatarUrl != null) {
            webhookMessage.setAvatar_url(this.avatarUrl);
        }
        return webhookMessage;
    }
}
