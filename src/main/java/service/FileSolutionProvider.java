package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import config.AppProperties;

@Service
public class FileSolutionProvider implements SolutionProvider {

    private static final Logger log = LoggerFactory.getLogger(FileSolutionProvider.class);

    private final AppProperties props;
    private final ResourceLoader resourceLoader;

    public FileSolutionProvider(AppProperties props, ResourceLoader resourceLoader) {
        this.props = props;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public String getFinalSql() {
        try {
            if ("inline".equalsIgnoreCase(props.getSolve().getSource())) {
                return normalize(props.getSolve().getInline());
            }
            String loc = props.getSolve().getFile();
            Resource r = resourceLoader.getResource(loc);
            if (!r.exists()) {
                throw new IllegalStateException("Solution file not found: " + loc);
            }
            byte[] bytes = r.getInputStream().readAllBytes();
            String sql = new String(bytes, StandardCharsets.UTF_8);
            return normalize(sql);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load final SQL", e);
        }
    }

    private String normalize(String sql) {
        if (sql == null) return null;
        
        String s = sql.trim();
        
        return s;
    }
}
