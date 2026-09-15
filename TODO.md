# TODO — kolejne kroki w SpotRacer

Lista do samodzielnej realizacji podczas nauki Javy. AI pomaga jako mentor i w code review.
Kolejność uwzględnia działanie aplikacji i wartość edukacyjną; nie trzeba realizować wszystkiego naraz.

## Podstawa analizy

Przejrzano moduły użytkowników, bezpieczeństwa, miejsc, przydziałów, rezerwacji
i kalendarza, migracje, istniejące testy oraz konfigurację środowiska i CI.
`make status` nie wykazało działających kontenerów tego projektu. Nie uruchamiano
środowiska ani testów; poniższe obserwacje wynikają z analizy statycznej, a nie
z odtworzenia błędów przez HTTP. Nie wykonano pomiaru pokrycia ani wydajności.
Numery linii wskazują stan kodu podczas przeglądu i mogą się zmieniać.

**Oznaczenia:**

- **Problem w kodzie** — konkretna niespójność widoczna w implementacji.
- **Do weryfikacji** — ryzyko wymagające testu lub pomiaru.
- **Decyzja / rozwój** — propozycja wymagania, nie stwierdzony błąd.

## P1 — podstawowy przepływ i błędy API

### 1. Odczyt zalogowanego użytkownika przy żądaniach JWT

- [ ] **Problem w kodzie.** Sprawdzić i dopasować `CurrentUserProvider` do uwierzytelniania Bearer JWT.
- **Punkt startowy:** [SpringSecurityCurrentUserProvider.java](src/main/java/pl/net/karion/SpotRacer/security/service/SpringSecurityCurrentUserProvider.java), linia 21; [SecurityConfig.java](src/main/java/pl/net/karion/SpotRacer/config/SecurityConfig.java), linia 63.
- **Scenariusz i skutek:** provider rzutuje principal na własne `UserDetails`, podczas gdy skonfigurowany konwerter JWT nie tworzy takiego obiektu. Żądanie kalendarza lub rezerwacji z tokenem trafia na niezgodny typ zamiast odczytać użytkownika.
- **Pierwszy krok:** prześledzić typ principal osobno podczas logowania i podczas kolejnego żądania z tokenem.
- **Gotowe, gdy:** test obejmuje logowanie i użycie wydanego tokenu w chronionym endpoincie; ID i role są odczytywane poprawnie. Samo podstawienie użytkownika przez `.with(user(...))` nie sprawdza tego przepływu.
- **Nauka:** `Authentication`, principal, JWT i granice odpowiedzialności adaptera.

### 2. Obsługa wyjątków rezerwacji przez API

- [ ] **Problem w kodzie.** Ustalić i wdrożyć odpowiedzi HTTP dla wyjątków modułu `reservation`.
- **Punkt startowy:** [GlobalExceptionHandler.java](src/main/java/pl/net/karion/SpotRacer/common/api/GlobalExceptionHandler.java), linia 15; [wyjątki rezerwacji](src/main/java/pl/net/karion/SpotRacer/reservation/exception).
- **Scenariusz i skutek:** zajęte miejsce, brak rezerwacji lub próba usunięcia cudzej rezerwacji powodują wyjątki `RuntimeException`, których obecny handler nie obsługuje. Oczekiwane odmowy biznesowe nie mają zdefiniowanej odpowiedzi API i mogą skończyć się 500.
- **Pierwszy krok:** rozpisać tabelę wyjątek → status → komunikat. Punkty wyjścia: 404 dla braku rezerwacji, 403 dla braku uprawnień, 409 dla zajętego miejsca; ustalić kontrakt dla ograniczeń czasowych.
- **Gotowe, gdy:** testy HTTP sprawdzają status oraz treść odpowiedzi dla każdego wyjątku rezerwacji.
- **Nauka:** wyjątki domenowe, `@RestControllerAdvice`, kontrakt API.

### 3. Walidacja przydziału przy brakującej dacie początku

- [ ] **Problem w kodzie.** Zapewnić bezpieczną walidację obu DTO przydziału przy niepełnych danych.
- **Punkt startowy:** [AssignmentCreateRequest.java](src/main/java/pl/net/karion/SpotRacer/assignment/api/controller/AssignmentCreateRequest.java), linia 28; [AssignmentUpdateRequest.java](src/main/java/pl/net/karion/SpotRacer/assignment/api/controller/AssignmentUpdateRequest.java), linia 24.
- **Scenariusz i skutek:** `startDate = null`, ale podane `endDate`. Metoda `isValidDateRange()` przekazuje null do `isBefore`. Adnotacja `@NotNull` nie gwarantuje, że inne walidatory nie zostaną wykonane; walidacja może rzucić wyjątek zamiast zwrócić błąd pola.
- **Pierwszy krok:** przygotować tabelę przypadków: brak początku, brak końca, obie daty puste, równe daty, koniec przed początkiem.
- **Gotowe, gdy:** POST i PUT z brakującym początkiem zwracają 400, a poprawny przydział bez końca nadal jest akceptowany.
- **Nauka:** null, walidacja pojedynczego pola i zależności między polami.

### 4. Testy całego cyklu rezerwacji

- [ ] **Do weryfikacji — luka testowa.** Dodać testy API rezerwacji i kalendarza oraz test zapisu i usuwania rezerwacji.
- **Punkt startowy:** [ReservationController.java](src/main/java/pl/net/karion/SpotRacer/reservation/api/controller/ReservationController.java), [CalendarController.java](src/main/java/pl/net/karion/SpotRacer/calendar/api/controller/CalendarController.java), [obecne testy dostępności](src/test/java/pl/net/karion/SpotRacer/reservation/service/ReservationAvailabilityServiceTest.java).
- **Obserwacja:** istnieją testy serwisów dostępności i kalendarza, ale nie ma odpowiadających im klas testujących te endpointy ani `ReservationService`.
- **Gotowe, gdy:** sprawdzone są utworzenie → widoczność w kalendarzu → anulowanie → ponowna dostępność, odmowa dla innej osoby, działanie administratora, brak tokenu i nieistniejące ID. Co najmniej jeden scenariusz używa rzeczywistego tokenu z logowania.
- **Nauka:** różnica między testem jednostkowym, integracyjnym i testem całego przepływu.

## P2 — reguły biznesowe i integralność danych

### 5. Administrator rezerwujący dla właściciela przydziału

- [ ] **Decyzja biznesowa na podstawie kodu.** Ustalić, czy okno rezerwacji zależy od osoby wykonującej operację, czy od odbiorcy rezerwacji.
- **Punkt startowy:** [ReservationAvailabilityService.java](src/main/java/pl/net/karion/SpotRacer/reservation/service/ReservationAvailabilityService.java), linia 69.
- **Scenariusz:** administrator bez przydziału rezerwuje na jutro dla jego właściciela. Kod porównuje właściciela z administratorem, więc przechodzi do reguły dla cudzego przydziału i odrzuca datę.
- **Gotowe, gdy:** decyzja jest zapisana w opisie domeny i przetestowana dla rezerwacji dla siebie, dla właściciela przydziału i dla osoby trzeciej. Rola ADMIN nie powinna otrzymywać dodatkowych wyjątków bez świadomej decyzji.
- **Nauka:** oddzielenie uprawnień wykonawcy od reguł dotyczących odbiorcy operacji.

### 6. Równoczesne rezerwacje i konflikty przy zatwierdzaniu transakcji

- [ ] **Do weryfikacji.** Sprawdzić obsługę konfliktu, gdy dwa żądania rezerwują to samo miejsce na ten sam dzień.
- **Punkt startowy:** [ReservationService.java](src/main/java/pl/net/karion/SpotRacer/reservation/service/ReservationService.java), linia 68; [migracja V5](src/main/resources/db/migration/V5__sp_reservation.sql). Analogiczny wzorzec występuje w [UserService.java](src/main/java/pl/net/karion/SpotRacer/user/service/UserService.java), linia 55.
- **Ryzyko:** oba żądania mogą przejść sprawdzenie istnienia. Ograniczenie bazy chroni dane, ale `catch` otaczający `save()` może nie przechwycić błędu ujawnionego dopiero przy flush lub commit.
- **Pierwszy krok:** ustalić, kiedy faktycznie jest wykonywany INSERT i gdzie kończy się transakcja.
- **Gotowe, gdy:** test na PostgreSQL z niezależnymi transakcjami potwierdza jedną rezerwację i przewidywalną odpowiedź konfliktu dla drugiej operacji. Sprawdzić także równoczesne tworzenie użytkownika z tym samym e-mailem.
- **Nauka:** transakcja, flush, commit, ograniczenia unikalności i wyścigi żądań.

### 7. Ochrona przed równoczesnymi nakładającymi się przydziałami

- [ ] **Do weryfikacji.** Zapewnić integralność zakresów przy równoległym tworzeniu i aktualizacji przydziałów.
- **Punkt startowy:** [AssignmentService.java](src/main/java/pl/net/karion/SpotRacer/assignment/service/AssignmentService.java), linia 68; [migracja V4](src/main/resources/db/migration/V4__sp_assignment.sql).
- **Ryzyko:** obecna kontrola odczytuje kolizje przed zapisem; migracja nie zawiera ochrony przed nakładaniem zakresów. Dwie transakcje mogą zobaczyć brak przydziału i zapisać sprzeczne dane. `findActiveAssignment()` oczekuje najwyżej jednego wyniku.
- **Pierwszy krok:** odtworzyć wyścig, następnie porównać możliwe sposoby ochrony danych i ich koszt.
- **Gotowe, gdy:** dwa konkurencyjne przydziały nie mogą naruszyć reguły; uwzględnione są zakresy bez końca, wspólna data graniczna oraz aktualizacja istniejącego przydziału.
- **Nauka:** izolacja transakcji i ochrona reguł biznesowych na poziomie bazy.

### 8. Spójne źródło czasu i walidacja ustawień

- [ ] **Do weryfikacji.** Sprawdzić zgodność walidacji DTO, serwisów i kalendarza na granicy dnia w `Europe/Warsaw`.
- **Punkt startowy:** [ClockConfiguration.java](src/main/java/pl/net/karion/SpotRacer/config/ClockConfiguration.java), [ReservationRequest.java](src/main/java/pl/net/karion/SpotRacer/reservation/api/controller/ReservationRequest.java), [ReservationProperties.java](src/main/java/pl/net/karion/SpotRacer/reservation/config/ReservationProperties.java).
- **Ryzyko:** serwisy korzystają z wstrzykniętego `Clock`, DTO z `@FutureOrPresent`; sam bean `Clock` nie dokumentuje spójnej konfiguracji czasu walidatora. Ustawienia okien nie mają walidacji.
- **Gotowe, gdy:** test HTTP z kontrolowanym czasem potwierdza spójność przy północy i zmianie czasu; niepoprawne okna, np. ujemne, powodują czytelny błąd konfiguracji. Ustalić również dopuszczalną relację długości obu okien.
- **Dalszy mały krok:** [JwtService.java](src/main/java/pl/net/karion/SpotRacer/security/service/JwtService.java), linia 30, i [LoginController.java](src/main/java/pl/net/karion/SpotRacer/security/api.controller/LoginController.java), linia 50, wyznaczają czas niezależnie. Ujednolicić go tak, aby termin ważności w odpowiedzi odpowiadał `exp` tokenu.
- **Nauka:** deterministyczne testy, strefy czasowe, walidacja konfiguracji.

## P3 — wygoda rozwoju i kolejne funkcje

### 9. Powtarzalne uruchomienie i testowanie przez make

- [ ] **Rozwój warsztatu.** Przygotować README z drogą od świeżego repozytorium do działającej aplikacji i testów.
- **Punkt startowy:** [Makefile](Makefile), [docker-compose.yml](docker-compose.yml), [workflow CI](.github/workflows/build.yml).
- **Zakres:** opisać `make up`, `make status`, `make run`, `make test`, `make mvn-verify`, przygotowanie kluczy developerskich JWT i pierwszego administratora. Rozróżnić uruchomienie kontenera od uruchomienia aplikacji — kontener `app` domyślnie wykonuje `sleep infinity`.
- **Małe usprawnienie:** zastąpić cel `aa` czytelnym sposobem wyboru klasy testowej przez make. Obecnie na stałe wskazuje `CalendarServiceTest`.
- **Gotowe, gdy:** instrukcja działa bez lokalnego JDK, a wybrany test można uruchomić udokumentowanym poleceniem make. Klucze testowe nie wymagają używania kluczy produkcyjnych.
- **Nauka:** odtwarzalne środowisko i automatyzacja codziennej pracy.

### 10. Lista własnych rezerwacji i możliwość anulowania po ponownym wejściu

- [ ] **Propozycja funkcji.** Zaprojektować odczyt rezerwacji zalogowanej osoby.
- **Punkt startowy:** [ReservationController.java](src/main/java/pl/net/karion/SpotRacer/reservation/api/controller/ReservationController.java), [CalendarDayAvailability.java](src/main/java/pl/net/karion/SpotRacer/calendar/api/controller/CalendarDayAvailability.java).
- **Uzasadnienie:** API rezerwacji ma POST i DELETE; kalendarz nie zwraca ID rezerwacji potrzebnego do DELETE. Klient, który nie zachował odpowiedzi z POST, nie ma obecnie dedykowanej drogi odczytu tego ID.
- **Pierwszy krok:** wybrać listę „moje rezerwacje” lub świadome rozszerzenie odpowiedzi kalendarza. Ustalić zakres dat i zasady widoczności.
- **Gotowe, gdy:** po ponownym wejściu użytkownik znajduje i anuluje własną rezerwację; nie uzyskuje cudzych danych poza ustalonym kontraktem.
- **Nauka:** projektowanie API od potrzeb użytkownika, filtrowanie i autoryzacja odczytu.

### 11. Decyzje o rolach i cyklu życia danych

- [ ] **Decyzje biznesowe.** Spisać niewielką tabelę uprawnień oraz wpływ zmian przydziału na istniejące rezerwacje.
- **Punkt startowy:** [CalendarController.java](src/main/java/pl/net/karion/SpotRacer/calendar/api/controller/CalendarController.java), [RoleController.java](src/main/java/pl/net/karion/SpotRacer/user/api/controller/RoleController.java), [AssignmentService.java](src/main/java/pl/net/karion/SpotRacer/assignment/service/AssignmentService.java).
- **Pytania:** czy ADMIN bez USER ma widzieć kalendarz (obecnie wymagane USER)? Czy odebranie roli ma wpływać natychmiast na wydane JWT, czy dopiero po ich wygaśnięciu? Co zrobić z rezerwacjami po zmianie przydziału?
- **Gotowe, gdy:** wybrane zasady są opisane i mają konkretne scenariusze testowe. Nie dodawać mechanizmu unieważniania tokenów ani kasowania rezerwacji bez ustalenia potrzeby.
- **Nauka:** świadome wymagania i konsekwencje zmian stanu.

### 12. Pomiar zapytań kalendarza przed optymalizacją

- [ ] **Do weryfikacji — zadanie późniejsze.** Zmierzyć liczbę zapytań i czas odpowiedzi dla wielu miejsc, lokalizacji i przydziałów.
- **Punkt startowy:** [CalendarService.java](src/main/java/pl/net/karion/SpotRacer/calendar/service/CalendarService.java), linia 80 i metoda `createDay()`.
- **Hipoteza:** odczyt powiązanych encji podczas budowania odpowiedzi może powodować dodatkowe zapytania. Nie potwierdzono problemu N+1 pomiarem.
- **Gotowe, gdy:** istnieje wynik pomiaru dla ustalonego zestawu danych; ewentualna optymalizacja zmniejsza koszt bez zmiany statusów, kolejności miejsc ani obsługi miejsc bez lokalizacji.
- **Nauka:** lazy loading, plany pobierania danych i optymalizacja oparta na pomiarach.

## Jak pracować z tą listą

1. Zacznij od zadania 1, następnie 2–4. Zadanie 3 jest dobrym małym ćwiczeniem na początek, jeśli chcesz najpierw przećwiczyć walidację.
2. Dla jednego zadania opisz oczekiwane zachowanie, napisz test wykrywający problem i samodzielnie popraw kod.
3. Poproś AI o wskazówkę, wyjaśnienie lub CR konkretnego diffu.
4. Uruchom testy przez `make test`; po spójnym etapie wykonaj `make mvn-verify` w działającym kontenerze.
5. Zaznacz zadanie po spełnieniu jego kryteriów i uaktualnij opis domeny, jeśli zmieniły się reguły.
