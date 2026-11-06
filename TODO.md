# TODO — obsługa firm w SpotRacer

Plan do samodzielnej realizacji, w kolejności zależności między etapami. Ukończone zadania poprzedniej tury zostały usunięte. Poniższy opis dotyczy planowanego rozwoju; aktualne zachowanie aplikacji opisuje [domena](docs/ai/domain.md).

## Ustalenia biznesowe

- Zarządca parkingów zarządza miejscami i przydziela konkretne miejsca firmom.
- `Location` jest informacją porządkującą miejsca, a nie granicą dostępu ani poziomem przydziału. Jedna lokalizacja może obejmować miejsca różnych firm.
- Pracownik należy do jednej firmy. Każda firma samodzielnie zarządza swoimi pracownikami i polityką udostępniania miejsc.
- Przydział miejsca firmie ma datę początku i opcjonalną datę końca. Przydziały tego samego miejsca nie mogą się nakładać.
- Przydział miejsca firmie i obecne `Assignment` pracownika to osobne pojęcia. Robocza nazwa nowego pojęcia: `CompanySpotAllocation`.
- Przydział pracownika musi mieścić się w okresie przydziału miejsca jego firmie. Rezerwacja musi dotyczyć dnia, w którym firma dysponuje miejscem.
- Obowiązuje zasada **najpierw porządek, potem zmiana**: skrócenie przydziału firmie jest odrzucane, jeśli pozostawiłoby poza okresem przydziały pracowników lub rezerwacje. Uporządkowanie danych to osobne operacje; skrócenie nie anuluje ich automatycznie.
- Zarządca może interweniować w przydziały pracowników i rezerwacje, w tym anulować rezerwacje. Jest to uprawnienie do interwencji, a nie podstawa codziennego zarządzania firmą.

## Etap 1. Firma, pracownicy i uprawnienia

**Cel:** administrator firmy zarządza wyłącznie pracownikami swojej firmy.

### Zadania

- [ ] Ustal minimalne dane `Company` oraz kontrakt tworzenia, odczytu i aktualizacji firmy przez zarządcę.
- [ ] Rozpisz uprawnienia zarządcy parkingów, administratora firmy i pracownika. Ustal nazwy ról, możliwość łączenia roli administratora firmy z pracownikiem oraz sposób działania konta zarządcy bez firmy.
- [ ] Ustal sposób utworzenia pierwszego administratora firmy i zasady nadawania kolejnych uprawnień. Administrator firmy nie może nadać sobie ani innemu użytkownikowi uprawnień zarządcy parkingów.
- [ ] Zaplanuj przypisanie istniejących użytkowników do firm i przejście z obecnej roli `ADMIN` na nowy podział. Dodawaj kolejne migracje Flyway; zachowaj istniejące dane i migracje.
- [ ] Dodaj model `Company` i powiązanie pracownika z jedną firmą, wraz z potrzebnymi operacjami API.
- [ ] Dostosuj tworzenie pracowników do zarządzania przez firmę. Rozstrzygnij rolę obecnego publicznego `POST /api/user`, aby nie pozwalał samodzielnie dołączyć do dowolnej firmy.
- [ ] Ogranicz odczyty, wyszukiwanie, aktualizacje użytkowników i zarządzanie rolami do dozwolonej firmy. Sprawdzaj zakres także przy bezpośrednim podaniu identyfikatora i wartości firmy w żądaniu.
- [ ] Ustal sposób uzyskiwania firmy zalogowanego użytkownika przez istniejący `CurrentUserProvider` i dostosuj logowanie/JWT. Określ zachowanie już wydanych tokenów po zmianie uprawnień.
- [ ] Ustal, czy pierwsza wersja dopuszcza przenoszenie pracownika między firmami. Jeśli tak, określ postępowanie z jego przydziałami, rezerwacjami i dostępem do wcześniejszych danych przed udostępnieniem takiej operacji.

**Punkt startowy:** moduły `user` i `security`, zwłaszcza `User`, `Role`, `UserService`, `RoleController`, `CurrentUserProvider` i `JwtService`. Wzorce testów: `UserControllerTest`, `RoleControllerTest` oraz `LoginControllerTest`.

**Scenariusze testowe:** administrator A zarządza pracownikiem A; odczyt i zmiana pracownika B są niedozwolone; lista i wyszukiwanie nie ujawniają pracowników B; podmiana firmy w żądaniu i próba nadania roli zarządcy są odrzucane; rzeczywisty JWT zachowuje właściwy zakres dostępu; brak uwierzytelnienia nie daje dostępu do operacji firmy.

**Gotowe, gdy:** firma może rozpocząć zarządzanie pracownikami, a jej administrator nie uzyskuje dostępu do pracowników innych firm przez żaden z udostępnionych endpointów.

## Etap 2. Przydziały miejsc firmom i dostępność

**Cel:** firma i jej pracownicy korzystają z miejsca tylko w okresie przydzielonym przez zarządcę.

### 2.1. Przydział miejsca firmie

- [ ] Opisz kontrakt `CompanySpotAllocation`: firma, konkretne miejsce oraz zakres dat. Zachowaj obie granice włącznie, zgodnie z obecnym `Assignment`; brak końca oznacza okres otwarty.
- [ ] Zaplanuj przydziały firmowe dla istniejących miejsc tak, aby obejmowały zachowywane przydziały pracowników i rezerwacje. Ustal sposób rozpoznawania niespójnych danych przed migracją.
- [ ] Dodaj tworzenie, odczyt i zmianę przydziałów firmowych przez zarządcę oraz odczyt własnych przydziałów przez firmę.
- [ ] Waliduj poprawność zakresu i brak nakładania się przydziałów tego samego miejsca, również dla tej samej firmy i okresów otwartych. Zapewnij ochronę przy równoczesnych zapisach.
- [ ] Odrzucaj skrócenie okresu, jeśli istnieją zależne przydziały pracowników lub rezerwacje poza nowymi granicami. Zadbaj, aby inne udostępnione operacje, np. usunięcie przydziału, nie pozwalały ominąć tej zasady.
- [ ] Ustal kontrakt informacji o konflikcie, tak aby uprawniona osoba mogła odnaleźć dane wymagające uporządkowania.

### 2.2. Powiązanie z istniejącymi funkcjami

- [ ] Ogranicz tworzenie i zmianę `Assignment` do pracownika własnej firmy, jej miejsca i okresu objętego firmowym przydziałem.
- [ ] W tworzeniu rezerwacji sprawdzaj firmę pracownika, dla którego wykonywana jest rezerwacja, oraz przydział miejsca tej firmie w dniu rezerwacji. Uwzględnij rezerwowanie przez administratora dla pracownika.
- [ ] Dostosuj listy miejsc, odczyty szczegółów, przydziały i rezerwacje do zakresu uprawnień. Firma może korzystać z miejsc z kilku lokalizacji bez uzyskiwania dostępu do pozostałych miejsc tych lokalizacji.
- [ ] Dostosuj `CalendarService`, aby uwzględniał firmowy przydział osobno dla każdego dnia. Ustal sposób prezentacji dni poza przydziałem; nie pokazuj ich jako wolnych dla pracownika tej firmy.
- [ ] Zachowaj spójność kalendarza i `ReservationAvailabilityService`: miejsce bez przydziału firmowego nie jest ogólnodostępne, a zwolnienie pierwszeństwa pracownika udostępnia je tylko w obrębie uprawnionej firmy.
- [ ] Zapewnij spójność przy równoczesnym skracaniu przydziału firmie i tworzeniu rezerwacji lub przydziału pracownika. Samo wcześniejsze sprawdzenie zależności nie wystarcza.

**Punkt startowy:** `AssignmentService`, `AssignmentRepository`, migracja `V6__sp_assignment_range_validation.sql` jako przykład istniejącej ochrony zakresów, `ReservationService`, `ReservationAvailabilityService` i `CalendarService`. Wzorce testów: `AssignmentServiceTest`, `AssignmentControllerTest`, `ReservationControllerTest` i `CalendarServiceTest`.

**Scenariusze testowe:** dzień przed początkiem, obie granice i dzień po końcu; okres otwarty; nakładające się zakresy; kolejne przydziały A i B bez nakładania; dwie firmy w tej samej lokalizacji; rezerwacja cudzym `spotId`; przydział pracownika dłuższy od firmowego; odrzucone skrócenie nie zmienia danych; skrócenie po uporządkowaniu zależności działa; równoczesne operacje nie pozostawiają danych poza firmowym przydziałem.

**Gotowe, gdy:** zakres firmowego przydziału obowiązuje w zapisach i odczytach, a kalendarz odpowiada możliwości rezerwacji. Na tym etapie można nadal korzystać ze wspólnych ustawień polityki rezerwacji.

## Etap 3. Polityka rezerwacji firmy

**Cel:** każda firma ustala zasady udostępniania własnych miejsc pracownikom.

### Zadania

- [ ] Potwierdź zakres pierwszej wersji polityki. Proponowany punkt wyjścia to obecne parametry: `standardWindowDays`, `assignedWindowDays` i `releaseAssignedSpotsAt`.
- [ ] Ustal wartości początkowe dla nowych i istniejących firm oraz zasady ich walidacji, w tym relację między długościami okien.
- [ ] Dodaj zapis, odczyt i aktualizację polityki przez administratora właściwej firmy.
- [ ] Ustal wpływ zmiany polityki na już zapisane rezerwacje oraz moment obowiązywania nowych ustawień przed implementacją aktualizacji.
- [ ] Dostosuj walidację rezerwacji i kalendarz do tej samej polityki firmy pracownika. Zachowaj korzystanie z `Clock` i obecną strefę czasu.
- [ ] Ogranicz okno rezerwacji okresem przydziału miejsca firmie. Dłuższe okno firmowe nie daje dostępu poza tym okresem.

**Punkt startowy:** `ReservationProperties`, `ReservationAvailabilityService`, `CalendarService`, `ReservationPropertiesValidationTest` oraz testy serwisów rezerwacji i kalendarza.

**Scenariusze testowe:** firmy A i B z różnymi oknami i godzinami zwolnienia; granice dat; chwila przed, dokładnie o i po godzinie zwolnienia; odmowa edycji cudzej polityki; koniec przydziału przed końcem okna; spójność kalendarza i walidacji przy rezerwacji przez administratora dla pracownika.

**Gotowe, gdy:** ustawienia jednej firmy nie wpływają na dostępność miejsc drugiej firmy, a zmiana polityki ma ustalone i przetestowane skutki dla istniejących rezerwacji.

## Etap 4. Interwencje zarządcy parkingów

**Cel:** zarządca może uporządkować dane blokujące zmianę przydziału miejsca firmie.

### Zadania

- [ ] Określ szczegółowy zakres odczytu danych potrzebnych zarządcy do interwencji oraz operacje porządkowe na przydziałach pracowników i rezerwacjach.
- [ ] Udostępnij zarządcy zamykanie zakresów przydziałów pracowników i anulowanie rezerwacji blokujących planowaną zmianę przydziału firmie.
- [ ] Zachowaj osobne operacje porządkowe i zmianę przydziału firmie. Przy końcowej zmianie ponownie sprawdzaj zależności i zabezpiecz ich spójność z równoczesnymi zapisami.
- [ ] Sprawdź, że uprawnienia interwencyjne zarządcy nie nadają administratorowi firmy dostępu do innych firm.
- [ ] Ustal, czy interwencja wymaga podania powodu i zapisu wykonawcy, czasu oraz zmienionych danych. Propozycja do potwierdzenia: historia interwencji dostępna administratorowi zainteresowanej firmy.

**Punkt startowy:** operacje aktualizacji i usuwania w `AssignmentService`, anulowanie w `ReservationService` oraz obsługa konfliktów firmowego przydziału z etapu 2.

**Scenariusze testowe:** firma ma miejsce do grudnia i zależne dane na grudzień; skrócenie do listopada jest odrzucone; zarządca porządkuje przydziały i rezerwacje; ponowiona zmiana działa; nowa zależność powstała przed końcową zmianą nadal ją blokuje; administrator innej firmy nie może przeprowadzić interwencji.

**Gotowe, gdy:** zarządca może przeprowadzić cały scenariusz porządkowania przez API, a zmiana przydziału nadal przestrzega zasady „najpierw porządek, potem zmiana”.

## Weryfikacja i dokumentacja

- [ ] W każdym etapie dopisz testy związane z nowym zachowaniem i dostosuj istniejące fixture'y do firm. Stosuj [wytyczne testów](src/test/AGENTS.md).
- [ ] Zweryfikuj migracje na pustej bazie i na bazie zawierającej dotychczasowe dane, w odizolowanym środowisku testowym.
- [ ] Przejdź pełny scenariusz: zarządca tworzy firmę i przydziela jej miejsce, administrator firmy dodaje pracownika i nadaje pierwszeństwo, pracownik loguje się i rezerwuje, zarządca porządkuje zależności przed skróceniem okresu.
- [ ] Sprawdź regresje: równoczesną rezerwację tego samego miejsca, pierwszeństwo pracownika, JWT, format błędów, stronicowanie oraz wydajność kalendarza po dodaniu danych firmowych. Wykorzystaj istniejący `CalendarPerformanceTest`.
- [ ] Po wdrożeniu kolejnych etapów aktualizuj `docs/ai/domain.md` i opis kontraktów API tak, aby przedstawiały faktycznie działające zachowanie.
- [ ] Przed testami sprawdź `make status`. Testy uruchamiaj przez `make test` / `make mvn-test`, a pełną weryfikację całości przez `make mvn-verify`. Wymagane są działający kontener `app`, infrastruktura testowa i klucze JWT zgodnie z wytycznymi projektu.

Przy przygotowaniu tego planu przejrzano statycznie bieżący kod i układ testów. Testów nie uruchamiano; wykonano wyłącznie zmianę dokumentacji.

## Pomysły na przyszłość

### Udostępnienie przypisanego miejsca wszystkim na wybrany dzień

- **Pomysł:** użytkownik posiadający przydział może wcześniej zrezygnować z pierwszeństwa na konkretny dzień, udostępniając miejsce wszystkim uprawnionym użytkownikom swojej firmy.
- **Korzyść:** osoba planująca nieobecność pozwala innym wcześniej zarezerwować miejsce i zaplanować dojazd.
- **Koszt złożoności:** wyjątki dla poszczególnych dni trzeba przechowywać i pobierać wraz z przydziałami oraz spójnie uwzględniać w `CalendarService` i `ReservationAvailabilityService`.
- **Do ustalenia:** okno rezerwacji udostępnionego miejsca oraz zasady cofania udostępnienia, szczególnie po dokonaniu rezerwacji przez inną osobę.
- **Status:** odłożone do czasu potwierdzenia potrzeby biznesowej uzasadniającej dodatkową złożoność.
