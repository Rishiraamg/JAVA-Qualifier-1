package config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String regNo;
    private String email;
    private Solve solve = new Solve();
    private Endpoints endpoints = new Endpoints();

    public static class Solve {
        
        private String source = "file";
        
        private String file = "classpath:solution.sql";
        
        private String inline = "SELECT 1";

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        public String getInline() { return inline; }
        public void setInline(String inline) { this.inline = inline; }
    }

    public static class Endpoints {
        private String generate = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        private String submitFallback = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
        public String getGenerate() { return generate; }
        public void setGenerate(String generate) { this.generate = generate; }
        public String getSubmitFallback() { return submitFallback; }
        public void setSubmitFallback(String submitFallback) { this.submitFallback = submitFallback; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Solve getSolve() { return solve; }
    public void setSolve(Solve solve) { this.solve = solve; }
    public Endpoints getEndpoints() { return endpoints; }
    public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }
}
