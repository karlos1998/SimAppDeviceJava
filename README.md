<a name="readme-top"></a>

[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]


<img src="assets/langs/pl.svg" alt="Logo" width="80" height="80">

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/karlos1998/SimAppDeviceJava">
    <img src="assets/logo.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Simply Connect App</h3>

  <p align="center">
    Użyj swojego telefonu jako bramki SMS!
    <br />
    <a href="https://simply-connect.ovh"><strong>Strona projektu »</strong></a>
    <br />
    <br />
    <a href="https://panel.simply-connect.ovh">Panel Użytkownika</a>
    ·
    <a href="https://github.com/karlos1998/SimAppDeviceJava/issues/new?labels=bug&template=bug-report---.md">Zgłoś problem</a>
    ·
    <a href="https://github.com/karlos1998/SimAppDeviceJava/issues/new?labels=enhancement&template=feature-request---.md">Zaproponuj zmiany</a>
    ·   
    <a href="https://letscode.it">Autor</a>
  </p>
</div>

<!-- ABOUT THE PROJECT -->
## O projekcie

![product-screenshot]

Założeniem aplikacji jest wykorzystanie własnych telefonów z androidem, które jakiś czas zalegają Ci w szufladzie i wysyłanie z nich SMS przez API czy z panelu administracyjnego.
Oto dlaczego miałbyś wybrać to rozwiązanie:
* Korzysztasz ze starego telefonu, nie płacisz za drogie bramki SMS
* Wykorzystujsz własną kartę SIM, więc korzystasz z własnego numeru telefonu
* Aplikacja ciągle się rozwija i to Ty jako klient jesteś wyznacznikiem kolejnych funkcjonalności

Prócz podstawowe założenia wysyłki SMS w aplikacji znajdziesz również listę połączeń przychodzących na wypadek, gdyby ktoś dzwonił na Twój numer. Co do SMS, są również SMS i MMS przychodzące.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Użyte technologie

* [![Vue][Vue.js]][Vue-url]
* [![Laravel][Laravel.com]][Laravel-url]
* [![Java][Java.com]][Java-url]
* [![TypeScript][TypeScript.org]][TypeScript-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>



## Od czego zacząć

Na początek upewnij się, że masz odpowiedni telefon z Androidem.

> [!WARNING]  
> Uwaga! Aplikacja działa od wersji Android 6.0 w górę.
> Testowałem ją na Androidze 6.0.0, 6.0.1, 9.0 i 13.0
> Niestety w wersji 13.0 są problemy z uprawnieniami i telefon nie uruchamia providera potwierdzającego wysyłkę SMS.
> Innymi slowy - w panelu na liście wysłanych SMS nie będziemy mieli informacji czy wiadomość została w ogóle wysłana, czy wystąpił jakiś błąd.


### Panel zarządzania

Całe zarządzanie odbywa się z panelu, więc koniecznie utwórz swoje konto w <a href="https://panel.simply-connect.ovh">https://panel.simply-connect.ovh</a>


### Instalacja aplikcji

Instalacja aplikacji na telefonie nie jest zbyt wygodna, ale pracuje nad tym.
1. Pobierz oficjlany plik .apk tutaj: [https://download.simply-connect.ovh](https://download.simply-connect.ovh), lub zbuduj go sam z tego repozytorium.
2. Uruchom aplikacje i koniecznie wyraź zgodę na wszystkie komunikaty.
   ```
   Koniecznie wyszukaj w swoim telefonie wszystkie możliwe konfiguracje związane z oszczędzaniem energii i pozbądź się ich!
   Telefon będzie szybko wyłączał aplikację gdy tylko zablokujesz ekran by ograniczać zużycie energii.
   Najlepszym rozwiązaniem jest trzymanie telefonu ciągle pod ładowarką, ale zalecam również zrobić root'a, 
   przenieść apkę do aplikacji systemowych i dedykowanymi aplikacjami wyłączyć wszelkie ograniczenia energii, 
   a nawet zrobić skrypt do uruchamiania aplikacji od razu gdyby przypadkiem się wyłączyła. 
   Na to jednak nie mam jeszcze złotego środka i poradnika. Na testowanych przeze mnie urządzeniach jest raczej OK :)
   ```
3. Konfiguracja, połaczenie z kontrolerem
   ```
   Po uruchomieniu aplikacji w jej widoku nie ma nic ciekawego, prócz linka do lokalnego adresu...
   Tak, to była moja pierwsza aplikacja android, więc widok to nie moja bajka... :D
   Z tego powodu (na ten moment!) aplikacja ma webserwer, ponieważ początkowo chciałem tam zrobić znacznie więcej konfiguracji, informacji, itp.
   
   Wracając: wejdź z urządzenia używając dowolnej przeglądarki na adres http://localhost:8888/ lub z kompuera, jeśli urządzenie jest w tej samej sieci wifi.
   Aplikacja pokazuje Twoje ip lokalne, więc łatwo znajdziesz adres :)
   ```
   
4. Zaloguj się w web panelu urządzenia

    <b>Domyślne hasło logowania:</b> lci123password

   ![screen-login1]


5. Przejdź do zakładki "Controller Configuration"

    ![screen-controller1]

    ```
   Tutaj musisz wpisać token, który połączy urządzenie z Twoim kontem w panelu Simply Connect.
   W tym celu zaloguj się w panelu lub zrób konto jeśli tego nie zrobiłeś i przejdź do zakładki z listą udządzeń.
   Następnie kliknij "Dodaj nowe urządzenie"
   Wyświetlony token wklej konfiguracji swojego urządzenia.
    ```

    ![screen-controller2]

    Spokojnie! Z czasem dodowanie będzie po np. skanowaniu kodu QR :D
    

<p align="right">(<a href="#readme-top">back to top</a>)</p>




## Użycie

To wszystko. Teraz możesz używać aplikacji. Wysyłanie SMS z panelu nie jest na ten moment zbyt rozbudowane, więc jedynie w podglądzie urządzenia jest mały formularz na wpisanie treści SMS.
W późniejszym etapie wysyłanie z panelu będzie znacznie bardziej rozwinięte - wybieranie wielu odbiorców, planowanie wysyłki, wysyłanie z wielu urządzeń na raz, a nawet MMS.

Aktualnie założonymi odbiorcami aplikacji są programiści, stąd stawiam na użycie API.
W zakładce kluczy API możesz stworzyc klucz api, dodać uprawnienia do danego urządzenia.
Znajdziesz tam również dokumentacje Swagger :)

## Coś specjalnego dla programistów Laravel

_W celu łatwiej implementacji dla aplikacji napisanych w Laravel 10/11 rozszerzyłem klasę Notifcations.
Szczegóły znajdziesz tutaj [Github - Simply Connect Laravel Notifications](https://github.com/karlos1998/simply-connect-laravel-notifications)_

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ROADMAP -->
## Roadmap

- [x] Wysyłanie SMS
- [x] Odbieranie SMS
- [x] Zapisywanie historii połączeń przychodzących
- [ ] Wysyłanie kodów USSD
- [ ] Odbieranie kodów USSD
- [ ] Wysyłanie MMS
- [x] Odbieranie MMS
- [ ] Multi-language Support
    - [x] Polski
    - [ ] Angielski
- [x] Przekierowanie Emaili na SMS
- [ ] Przekierowanie SMS na Email
- [x] Powiadomienia Email o statusie Offline urządzenia
- [x] Powiadomienia Email o odebranym SMS przez urządzenie
- [x] API dla Developerów
  - [x] Wysyłka SMS
  - [x] Wysyłka SMS do wielu odbiorców
  - [ ]  Wysyłanie SMS z losowego/aktywnego urządzenia

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Kontakt

Karol Sójka - [@Linkedin.com][linkedin-url] - kontakt@letscode.it

Link do projektu: [https://github.com/karlos1998/SimAppDeviceJava](https://github.com/karlos1998/SimAppDeviceJava)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<img src="assets/langs/en.svg" alt="Englisih" width="80" height="80">

```
TODO... :D
```

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.com/karlos1998/SimAppDeviceJava/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=for-the-badge
[forks-url]: https://github.com/karlos1998/SimAppDeviceJava/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=for-the-badge
[stars-url]: https://github.com/karlos1998/SimAppDeviceJava/stargazers
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=for-the-badge
[issues-url]: https://github.com/karlos1998/SimAppDeviceJava/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge
[license-url]: https://github.com/karlos1998/SimAppDeviceJava/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/karol-sójka-17952825b/

[product-screenshot]: assets/screen1.png
[screen-login1]: assets/screen-login1.png
[screen-controller1]: assets/screen-controller1.png
[screen-controller2]: assets/screen-controller2.png


[Vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[Vue-url]: https://vuejs.org/
[Laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[Laravel-url]: https://laravel.com
[Java.com]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://java.com
[TypeScript.org]: https://shields.io/badge/TypeScript-3178C6?logo=TypeScript&logoColor=FFF&style=flat-square
[TypeScript-url]: https://www.typescriptlang.org/