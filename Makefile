SHELL := /bin/bash

PROJECT_NAME := spotracejava
COMPOSE := docker compose
APP_SERVICE := app
DB_SERVICE := db
MAIL_SERVICE := mailpit

APP_PORT := 8081
DB_PORT := 5432
MAILPIT_UI_PORT := 8025

.DEFAULT_GOAL := help

.PHONY: help
help: ## Pokaż listę komend
	@echo ""
	@echo "$(PROJECT_NAME) - dostępne komendy:"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z0-9_.-]+:.*##/ {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""

# =========================
# Docker / Compose
# =========================

.PHONY: up
up: ## Uruchom cały stack
	$(COMPOSE) up -d

.PHONY: up-build
up-build: ## Zbuduj i uruchom cały stack
	$(COMPOSE) up -d --build

.PHONY: down
down: ## Zatrzymaj stack
	$(COMPOSE) down

.PHONY: down-v
down-v: ## Zatrzymaj stack i usuń volume
	$(COMPOSE) down -v

.PHONY: stop
stop: ## Zatrzymaj kontenery
	$(COMPOSE) stop

.PHONY: start
start: ## Uruchom zatrzymane kontenery
	$(COMPOSE) start

.PHONY: restart
restart: ## Restart stacka
	$(COMPOSE) restart

.PHONY: ps
ps: ## Pokaż status kontenerów
	$(COMPOSE) ps

.PHONY: logs
logs: ## Pokaż logi ze wszystkich serwisów
	$(COMPOSE) logs -f --tail=200

.PHONY: logs-app
logs-app: ## Pokaż logi aplikacji
	$(COMPOSE) logs -f --tail=200 $(APP_SERVICE)

.PHONY: logs-db
logs-db: ## Pokaż logi bazy
	$(COMPOSE) logs -f --tail=200 $(DB_SERVICE)

.PHONY: logs-mail
logs-mail: ## Pokaż logi mailpit
	$(COMPOSE) logs -f --tail=200 $(MAIL_SERVICE)

.PHONY: build
build: ## Zbuduj obrazy
	$(COMPOSE) build

.PHONY: rebuild
rebuild: ## Przebuduj obrazy bez cache
	$(COMPOSE) build --no-cache

.PHONY: pull
pull: ## Pobierz najnowsze obrazy bazowe
	$(COMPOSE) pull

.PHONY: prune
prune: ## Wyczyść nieużywane zasoby Dockera
	docker system prune -f

# =========================
# Aplikacja / Maven
# =========================

.PHONY: app-sh
app-sh: ## Wejdź do shella w kontenerze app
	$(COMPOSE) exec $(APP_SERVICE) bash || $(COMPOSE) exec $(APP_SERVICE) sh

.PHONY: db-sh
db-sh: ## Wejdź do shella w kontenerze db
	$(COMPOSE) exec $(DB_SERVICE) bash || $(COMPOSE) exec $(DB_SERVICE) sh

.PHONY: psql
psql: ## Połącz się z PostgreSQL przez psql
	$(COMPOSE) exec $(DB_SERVICE) psql -U myapp -d myapp

.PHONY: mvn-test
mvn-test: ## Uruchom testy Maven w kontenerze app
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw test

.PHONY: mvn-clean
mvn-clean: ## Wyczyść build Maven w kontenerze app
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw clean

.PHONY: mvn-package
mvn-package: ## Zbuduj jar przez Maven w kontenerze app
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw clean package -DskipTests

.PHONY: mvn-verify
mvn-verify: ## Pełna walidacja projektu
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw verify

.PHONY: mvn-deps
mvn-deps: ## Pobierz zależności Maven
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw dependency:resolve

.PHONY: run mvn-run
mvn-run: ## Uruchamia spring-boot
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw spring-boot:run

run: mvn-run

.PHONY: test
test: mvn-test ## Alias na testy

.PHONY: clean
clean: mvn-clean ## Alias na clean

# =========================
# Developer workflow
# =========================

.PHONY: init
init: up-build ## Pierwsze uruchomienie projektu
	@echo ""
	@echo "Aplikacja:    http://localhost:$(APP_PORT)"
	@echo "Mailpit UI:   http://localhost:$(MAILPIT_UI_PORT)"
	@echo "PostgreSQL:   localhost:$(DB_PORT)"
	@echo ""

.PHONY: reset
reset: down-v up-build ## Twardy reset środowiska

.PHONY: status
status: ps ## Alias na status

.PHONY: health
health: ## Szybki check endpointów
	@echo "Sprawdzam app..."
	@curl -fsS http://localhost:$(APP_PORT) >/dev/null && echo "OK app" || echo "FAIL app"
	@echo "Sprawdzam mailpit..."
	@curl -fsS http://localhost:$(MAILPIT_UI_PORT) >/dev/null && echo "OK mailpit" || echo "FAIL mailpit"

.PHONY: watch
watch: ## Uruchom compose w trybie watch
	$(COMPOSE) up --watch

.PHONY: restart-app
restart-app: ## Restart tylko serwisu app
	$(COMPOSE) restart $(APP_SERVICE)

.PHONY: restart-db
restart-db: ## Restart tylko serwisu db
	$(COMPOSE) restart $(DB_SERVICE)

.PHONY: restart-mail
restart-mail: ## Restart tylko serwisu mailpit
	$(COMPOSE) restart $(MAIL_SERVICE)

# =========================
# Database
# =========================

.PHONY: db-reset
db-reset: ## Usuń volume bazy i uruchom ponownie db
	$(COMPOSE) stop $(DB_SERVICE)
	$(COMPOSE) rm -f $(DB_SERVICE)
	docker volume rm $$(docker volume ls -q | grep $(PROJECT_NAME) | grep postgres || true)
	$(COMPOSE) up -d $(DB_SERVICE)

# =========================
# Local convenience
# =========================

.PHONY: open-mail
open-mail: ## Pokaż URL do Mailpit
	@echo "Mailpit UI: http://localhost:$(MAILPIT_UI_PORT)"

.PHONY: open-app
open-app: ## Pokaż URL do aplikacji
	@echo "Aplikacja: http://localhost:$(APP_PORT)"

.PHONY: aa
aa: ## Uruchom testy Maven w kontenerze app
	$(COMPOSE) exec $(APP_SERVICE) ./mvnw -U test