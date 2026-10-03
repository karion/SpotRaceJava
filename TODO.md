# TODO — następne zadania w SpotRacer

Lista do samodzielnej realizacji. Poprzednie zadania zostały usunięte; celowo pominięte punkty nie wracają na listę.

## 1. Równoczesne tworzenie użytkowników z tym samym e-mailem

- [ ] Sprawdź odpowiedź API, gdy dwa żądania `POST /api/user` jednocześnie użyją tego samego, jeszcze niezapisanego e-maila.
- **Punkt startowy:** [UserService.java](src/main/java/pl/net/karion/SpotRacer/user/service/UserService.java), metoda `create()`; [UserControllerTest.java](src/test/java/pl/net/karion/SpotRacer/user/api/UserControllerTest.java), test `shouldFailWhenCreateWithSameEmail()`.
- **Dlaczego:** oba żądania mogą przejść `existsByEmail()`. Ograniczenie `uc_user_email` ochroni bazę, ale błąd może pojawić się dopiero podczas zapisu zmian lub zatwierdzania transakcji, poza obecnym `catch` otaczającym `save()`.
- **Pierwszy krok:** przygotuj test z dwoma niezależnymi żądaniami, które przechodzą sprawdzenie e-maila przed zapisem.
- **Gotowe, gdy:** w bazie pozostaje jeden użytkownik, a odpowiedzi mają statusy 201 i 409 z ustalonym formatem błędu.

## 2. Rzeczywisty token JWT na chronionym endpoincie

- [ ] Sprawdź pełny przepływ logowania i użycia wydanego tokenu jako `Authorization: Bearer`.
- **Punkt startowy:** [LoginControllerTest.java](src/test/java/pl/net/karion/SpotRacer/security/api/controller/LoginControllerTest.java), [SpringSecurityCurrentUserProvider.java](src/main/java/pl/net/karion/SpotRacer/security/service/SpringSecurityCurrentUserProvider.java) oraz chroniony endpoint rezerwacji lub kalendarza.
- **Dlaczego:** obecny test logowania sprawdza termin ważności JWT, a testy chronionych endpointów korzystają z pomocniczego `.with(jwt())`. Nie sprawdzają wspólnie dekodowania wydanego tokenu, ról i identyfikatora użytkownika.
- **Pierwszy krok:** zaloguj użytkownika w teście HTTP i użyj otrzymanego tokenu w kolejnym żądaniu bez podstawiania uwierzytelnienia przez testowy postprocessor.
- **Gotowe, gdy:** żądanie jest autoryzowane jako właściwy użytkownik, a brak tokenu i token bez potrzebnej roli dają oczekiwane odpowiedzi.

## Weryfikacja

Testy uruchamiaj przez cele `make` w działającym kontenerze `app`. Ostatni przegląd był statyczny: `make status` nie wykazało działających kontenerów projektu, więc nowych scenariuszy nie odtworzono.
