package de.rainlessrouting.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrecipitationDataProviderApplication {

	public static void main(String[] args) 
	{
//		SpringApplication app = new SpringApplication(PrecipitationDataProviderApplication.class);
//        app.setDefaultProperties(Collections.singletonMap("server.port", "4444"));
//        app.run(args);
        
		SpringApplication.run(PrecipitationDataProviderApplication.class, args);
	}
}
