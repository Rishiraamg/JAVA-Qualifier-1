package runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.AppProperties;
import service.SolutionProvider;

@Component
public class ChallengeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChallengeRunner.class);

    private final RestTemplate restTemplate;
    private final AppProperties props;
    private final SolutionProvider solutionProvider;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChallengeRunner(RestTemplate restTemplate, AppProperties props, SolutionProvider solutionProvider) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.solutionProvider = solutionProvider;
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting hiring flow...");
        try {
            
            String generateUrl = props.getEndpoints().getGenerate();
            Map<String, String> body = new HashMap<>();
            body.put("name", props.getName());
            body.put("regNo", props.getRegNo());
            body.put("email", props.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,String>> req = new HttpEntity<>(body, headers);

            log.info("Calling generateWebhook: {}", generateUrl);
            ResponseEntity<String> resp = restTemplate.exchange(generateUrl, HttpMethod.POST, req, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("generateWebhook failed with status: " + resp.getStatusCode());
            }
            String raw = resp.getBody();
            log.debug("generateWebhook response: {}", raw);
            JsonNode json = mapper.readTree(raw == null ? "{}" : raw);

            String webhook = readText(json, "webhook");
            String accessToken = readText(json, "accessToken");
            if (webhook == null || webhook.isBlank()) {
                webhook = props.getEndpoints().getSubmitFallback(); 
                log.warn("No 'webhook' in response; using fallback submit URL: {}", webhook);
            }
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("No 'accessToken' received from generateWebhook response");
            }
            log.info("Received webhook={}, accessToken(len={})", webhook, accessToken.length());

            
            String finalQuery = solutionProvider.getFinalSql();
            if (finalQuery == null || finalQuery.isBlank()) {
                throw new IllegalStateException("Final SQL query is empty");
            }
            
            Path out = Path.of("target", "finalQuery.txt");
            Files.createDirectories(out.getParent());
            Files.writeString(out, finalQuery, StandardCharsets.UTF_8);
            log.info("Stored final SQL at {}", out.toAbsolutePath());

           
            Map<String, String> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken);

            HttpEntity<Map<String, String>> submitReq = new HttpEntity<>(submitBody, submitHeaders);
            log.info("Submitting finalQuery to {}", webhook);
            ResponseEntity<String> submitResp = restTemplate.exchange(webhook, HttpMethod.POST, submitReq, String.class);

            log.info("Submission status: {}", submitResp.getStatusCode());
            log.info("Submission response: {}", submitResp.getBody());

        } catch (RestClientException | IOException ex) {
            log.error("Error during flow", ex);
        } finally {
           
            SpringApplication.exit(SpringApplication.run(ChallengeRunner.class), () -> 0);
        }
    }

    private static String readText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }
}
