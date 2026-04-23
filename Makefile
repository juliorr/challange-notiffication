## Messages Challenge · Makefile
## All targets run inside Docker (docker compose). Host never runs mvn/npm directly.

# ---------- Config ----------
COMPOSE        := docker compose
BACKEND_EXEC   := $(COMPOSE) exec backend
FRONTEND_EXEC  := $(COMPOSE) exec frontend

.DEFAULT_GOAL := help

# ---------- Help ----------
.PHONY: help
help: ## Show this help
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n\nTargets:\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

.PHONY: up
up: ## Start the full stack (postgres, redis, backend, frontend)
	$(COMPOSE) up -d

.PHONY: down
down: ## Stop and remove containers, networks
	$(COMPOSE) down

.PHONY: rebuild
rebuild: ## Rebuild images without cache
	$(COMPOSE) build --no-cache

.PHONY: verify
verify: ## Run backend unit + integration tests (mvn verify, Testcontainers)
	$(BACKEND_EXEC) mvn verify

.PHONY: style-check
style-check: ## Check Google Java Style compliance (Spotless)
	$(BACKEND_EXEC) mvn spotless:check

.PHONY: style-apply
style-apply: ## Auto-format Java sources with Google Java Style (Spotless)
	$(BACKEND_EXEC) mvn spotless:apply

.PHONY: fe-test
fe-test: ## Run Vitest
	$(FRONTEND_EXEC) npm test

.PHONY: logs
logs: ## Tail logs from all services
	$(COMPOSE) logs -f
