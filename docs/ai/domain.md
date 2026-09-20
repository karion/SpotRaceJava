# Domena parkingowa — kontekst dla AI

Ten opis odzwierciedla kod w chwili utworzenia dokumentu. Przy zmianach sprawdzaj
serwisy, konfigurację, migracje i testy. Jeśli reguły się zmieniły, wskaż potrzebę
aktualizacji opisu; edytuj dokumentację na prośbę użytkownika.
Rozbieżności traktuj jako temat do wyjaśnienia, nie jako zgodę na cichą zmianę zachowania.

## Pojęcia

| Pojęcie | Znaczenie i miejsce w kodzie |
| --- | --- |
| `User`, `Role` | Użytkownik i role; moduł `user`. |
| `Location`, `Spot` | Lokalizacja i miejsce parkingowe; moduł `spot`. Kod dopuszcza miejsce bez lokalizacji. |
| `Assignment` | Przydział miejsca użytkownikowi na zakres dat, także bez daty końcowej; moduł `assignment`. |
| `Reservation` | Rezerwacja konkretnego miejsca przez użytkownika na konkretny dzień; moduł `reservation`. |
| Kalendarz | Widok dostępności miejsc wyliczany z przydziałów, rezerwacji i czasu; moduł `calendar`. |

Przydział i rezerwacja to różne pojęcia: przydział daje pierwszeństwo, a rezerwacja
zajmuje miejsce na dzień. Nie zastępuj jednego drugim.

## Obecne reguły

- Jedno miejsce może mieć najwyżej jedną rezerwację na daną datę. Chroni to także ograniczenie bazy `uk_reservation_spot_date` w migracji V5.
- Sprawdzenie dostępności przed zapisem nie wystarcza przy równoczesnych żądaniach. Zachowuj ograniczenie bazy i uwzględniaj moment zapisu lub zatwierdzenia transakcji przy obsłudze konfliktu.
- Tworzenie rezerwacji dla innej osoby oraz usuwanie cudzej rezerwacji wymaga roli `ADMIN`. Rezerwacja jest tworzona w kontekście użytkownika, a nie admina.
- Dla miejsca bez aktywnego przydziału okno obejmuje dziś do dziś + `standardWindowDays`, włącznie z obiema granicami.
- Jeśli aktywny przydział należy do użytkownika, dla którego tworzona jest rezerwacja, walidacja stosuje `assignedWindowDays`, również włącznie z granicami.
- Miejsce przydzielone komuś innemu można rezerwować tylko na dziś, od `releaseAssignedSpotsAt` włącznie, jeśli nie jest już zarezerwowane.
- Konfiguracja w `application.properties` wynosi obecnie odpowiednio 1 dzień, 7 dni i 07:00. Używaj `ReservationProperties`, nie kopiuj tych wartości do logiki.
- Produkcyjny bean `Clock` używa strefy `Europe/Warsaw`.
- `AssignmentService` sprawdza nakładanie się przydziałów tego samego miejsca. Aktualizacja nie pozwala cofnąć daty początku względem zapisanej wartości.
- Kalendarz nadaje rezerwacji pierwszeństwo przed statusem przydziału. Od godziny zwolnienia niezarezerwowane przydzielone miejsce na dziś pokazuje jako wolne.

## Miejsca wymagające uwagi

- Zmieniając dostępność, sprawdzaj jednocześnie `ReservationAvailabilityService`, `CalendarService` i ich testy, aby widok kalendarza odpowiadał możliwości rezerwacji.
- Nie zakładaj dodatkowych zasad, takich jak zakaz rezerwacji w weekendy, limit jednego miejsca na użytkownika dziennie czy automatyczne pomijanie świąt. Proponuj je dopiero w ramach konkretnego wymagania.
