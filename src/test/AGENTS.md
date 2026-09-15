# Wytyczne dla testów

Ten plik uzupełnia główny `AGENTS.md` dla drzewa `src/test`.
AI analizuje testy, wyjaśnia ich działanie i proponuje scenariusze do samodzielnego
napisania przez użytkownika. Nie pisze ani nie poprawia testów. Poniższe zasady
są kryteriami CR i wskazówek mentorskich; można uruchamiać istniejące testy.

- Istniejące testy uruchamiaj przez `make test` / `make mvn-test`, a pełną weryfikację przez `make mvn-verify`. Java i Maven działają w kontenerze `app`; nie uruchamiaj ich lokalnie. Dostępność celów i parametrów sprawdzaj w `Makefile`.
- Korzystaj z JUnit Jupiter i Mockito zgodnie z istniejącymi testami. Proste reguły serwisów testuj bez uruchamiania Springa.
- Testy wymagające kontekstu, HTTP lub bazy opieraj na istniejących wzorcach, w tym `support/IntegrationTest`, MockMvc i `PostgresTestContainer`.
- Testy bazy uruchamiaj na PostgreSQL przez Testcontainers. Nie zastępuj go H2: migracje, ograniczenia i zapytania powinny być sprawdzane na właściwym silniku.
- Wykorzystuj istniejące fixture'y w modułach. Każdy test powinien przygotować potrzebne dane i działać niezależnie od kolejności wykonania.
- Nazywaj testy po angielsku, opisując oczekiwane zachowanie. Oddziel przygotowanie, działanie i asercje; nie wymagaj komentarzy do oczywistych kroków.
- Kontroluj czas przez `Clock.fixed` lub istniejący sposób mockowania `Clock`. Ustalaj strefę jawnie; nie uzależniaj wyniku od zegara komputera ani `Thread.sleep`.
- Dla okien rezerwacji sprawdzaj dni przed i po granicy, obie granice oraz moment przed, dokładnie o i po godzinie zwolnienia. Przy zmianach dotyczących stref uwzględniaj `Europe/Warsaw`.
- Dla zmienianych uprawnień uwzględniaj właściciela, inną osobę i administratora. W testach HTTP sprawdzaj także brak uwierzytelnienia, jeśli dotyczy endpointu.
- Przy zmianach zapisu rezerwacji sprawdzaj konflikt miejsca i daty; sam mock `existsBySpotIdAndDate` nie dowodzi działania ograniczenia w bazie.
- Test regresyjny powinien wykrywać konkretny naprawiany błąd. Sprawdzaj wynik, błąd lub stan danych, zamiast kopiować do testu algorytm implementacji.
- Nie osłabiaj asercji i nie pomijaj testów tylko po to, aby uzyskać zielony wynik. Oddziel błąd aplikacji od braku Dockera, kluczy JWT lub innego problemu środowiska.
