# Load .env variables
ifneq (,$(wildcard .env))
    include .env
    export
endif

run:  # Start springboot (local development)
	mvn spring-boot:run

dev: # Initialize development container
	 docker compose -f compose.dev.yml up -d

prod: # Initialize production container
	 docker compose -f compose.prod.yml up -d --build

ci: # Install project dependencies
	mvn clean install

sonar-init: # Initialize SonarQube project
	chmod +x ./sonarqube-init.sh
	./sonarqube-init.sh

sonar: # SonarQube analysis
	mvn clean verify sonar:sonar \
        -Dsonar.projectKey=$(SONAR_PROJECT_KEY) \
        -Dsonar.projectName='$(SONAR_PROJECT_KEY)' \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.token=$(SONAR_PROJECT_TOKEN) \
        -Dsonar.sources=src/main/java \
        -Dsonar.tests= \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.qualitygate.wait=true

test: # Launch test
	mvn clean test

check: # Formatting check
	mvn spotless:check -X

fix: # Formatting fix
	mvn spotless:apply
