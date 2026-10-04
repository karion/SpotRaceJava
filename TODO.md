# TODO — następne zadania w SpotRacer

Lista do samodzielnej realizacji. Poprzednie zadania zostały usunięte; celowo pominięte punkty nie wracają na listę.

## 1. Równoczesne tworzenie użytkowników z tym samym e-mailem

- [x] Sprawdź odpowiedź API, gdy dwa żądania `POST /api/user` jednocześnie użyją tego samego, jeszcze niezapisanego e-maila.
- **Punkt startowy:** [UserService.java](src/main/java/pl/net/karion/SpotRacer/user/service/UserService.java), metoda `create()`; [UserControllerTest.java](src/test/java/pl/net/karion/SpotRacer/user/api/UserControllerTest.java), test `shouldFailWhenCreateWithSameEmail()`.
- **Dlaczego:** oba żądania mogą przejść `existsByEmail()`. Ograniczenie `uc_user_email` ochroni bazę, ale błąd może pojawić się dopiero podczas zapisu zmian lub zatwierdzania transakcji, poza obecnym `catch` otaczającym `save()`.
- **Pierwszy krok:** przygotuj test z dwoma niezależnymi żądaniami, które przechodzą sprawdzenie e-maila przed zapisem.
- **Gotowe, gdy:** w bazie pozostaje jeden użytkownik, a odpowiedzi mają statusy 201 i 409 z ustalonym formatem błędu.

## 2. Rzeczywisty token JWT na chronionym endpoincie

- [x] Sprawdź pełny przepływ logowania i użycia wydanego tokenu jako `Authorization: Bearer`.
- **Punkt startowy:** [LoginControllerTest.java](src/test/java/pl/net/karion/SpotRacer/security/api/controller/LoginControllerTest.java), [SpringSecurityCurrentUserProvider.java](src/main/java/pl/net/karion/SpotRacer/security/service/SpringSecurityCurrentUserProvider.java) oraz chroniony endpoint rezerwacji lub kalendarza.
- **Dlaczego:** obecny test logowania sprawdza termin ważności JWT, a testy chronionych endpointów korzystają z pomocniczego `.with(jwt())`. Nie sprawdzają wspólnie dekodowania wydanego tokenu, ról i identyfikatora użytkownika.
- **Pierwszy krok:** zaloguj użytkownika w teście HTTP i użyj otrzymanego tokenu w kolejnym żądaniu bez podstawiania uwierzytelnienia przez testowy postprocessor.
- **Gotowe, gdy:** żądanie jest autoryzowane jako właściwy użytkownik, a brak tokenu i token bez potrzebnej roli dają oczekiwane odpowiedzi.

## 3. Stabilna struktura JSON dla odpowiedzi stronicowanych

- [x] Zastąp bezpośrednią serializację `PageImpl` stabilnym kontraktem odpowiedzi API.
- **Punkt startowy:** endpointy zwracające `Page` w [UserController.java](src/main/java/pl/net/karion/SpotRacer/user/api/controller/UserController.java), [SpotController.java](src/main/java/pl/net/karion/SpotRacer/spot/api/controller/SpotController.java), [LocationController.java](src/main/java/pl/net/karion/SpotRacer/spot/api/controller/LocationController.java) i [MyReservationController.java](src/main/java/pl/net/karion/SpotRacer/reservation/api/controller/MyReservationController.java).
- **Dlaczego:** Spring Data ostrzega, że struktura JSON powstała przez bezpośrednią serializację `PageImpl` nie jest gwarantowana między wersjami biblioteki. Udokumentowane możliwości to `PagedModel` lub jawnie zdefiniowane DTO odpowiedzi; wybierz jeden kontrakt dla projektu.
- **Pierwszy krok:** zapisz oczekiwany kształt odpowiedzi: `content`, numer i rozmiar strony, liczba wszystkich elementów i stron. Porównaj go z aktualnym JSON, aby świadomie ocenić wpływ zmiany na klientów API.
- **Gotowe, gdy:** wszystkie cztery endpointy zwracają ustalony format, a testy HTTP sprawdzają zawartość i metadane dla pustej strony, pierwszej strony oraz kolejnej strony. Opisz zmianę kontraktu, jeśli obecne pola JSON się zmienią.
- **Źródło:** [Spring Data — `PageSerializationMode`](https://docs.spring.io/spring-data/commons/reference/api/java/org/springframework/data/web/config/EnableSpringDataWebSupport.PageSerializationMode.html).

## Weryfikacja

Testy uruchamiaj przez cele `make` w działającym kontenerze `app`. Ostatni przegląd był statyczny: `make status` nie wykazało działających kontenerów projektu, więc nowych scenariuszy nie odtworzono.
