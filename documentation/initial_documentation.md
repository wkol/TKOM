## TKOM projekt - Wojciech Kołodziejak

Wojciech Kołodziejak 310747
GitLab: [wkolodz2](https://gitlab-stud.elka.pw.edu.pl/wkolodz2)
Język programowania - Kotlin

## Interpreter własnego języka

### Założenia:

* statyczne, silne typowanie
* przekazywanie zmiennych przez wartość
* blok kodu reprezentowany przez nawiasy klamrowe `{ }`

* #### Zmienne:
    * cztery podstawowe typy zmiennych: [`int`, `float`, `string`, `bool`]
    * możliwość specyfikacji typów jako nullowalnych dodając znak zapytania przy deklaracji (np. `bool? a = null`)
    * zmienna jest widoczna/dostępna w danym bloku kodu od momentu deklaracji do końca bloku kodu
    * mutowalność zmiennych zależy od słowa kluczowego poprzedzającego typ zmiennej w deklaracji (`var`, `const`) - w
      przypadku modyfikacji zmiennej typu `const`, zostanie zgłoszony błąd
    * escapowanie znaków specjalnych w stringach poprzez `\` (np. `\n`, `\t`, `\\`)
    * zmienne nie-nullowalne muszą być zainicjalizowane przy deklaracji

* #### Operatory:
    * operatory arytmetyczne (`+, -, *, /, %`) oraz wyrażenia nawiasowe działające na typach int i double
      (dzielenie przez zero rzuca wyjątek)
    * operatory logiczne (`&&, ||, !`) działające na typach bool
    * konkatenacja zmiennych tekstowych (string) za pomocą operatora `+`
    * operatory porównujące:
        * `==` - sprawdza czy wartości obu zmiennych są równe i zwraca wartość bool odpowiadający temu warunkowi
        * `!=` - sprawdza czy wartości obu zmiennych są różne i zwraca wartość bool odpowiadający temu warunkowi
        * `>`, `<`, `>=`, `<=` - tylko dla wartości typu int i double (dla innych typów zgłaszany jest błąd)

* #### Instrukcje warunkowe:
    * `if (<warunek>) wyrażenie | blok kodu` - warunek musi być typu bool
    * `if (<warunek>) wyrażenie | blok kodu else wyrażenie | blok kodu` - warunek musi być typu bool

* #### Pętle:
    * `while (<warunek>) wyrażenie | blok kodu` - warunek musi być typu bool

* #### Jawne rzutowanie
  Język udostępnia operacje jawnego rzutowania typów przy użyciu słowa kluczowego `as`:
    * int as double
    * double as int - > utrata części dziesiętnej (np. `2.5 as Int` -> 2)
    * int as string -> konwersja do stringa (np. `-2 as String` -> "-2")
    * string as int -> konwersja do inta (np. `"02123" as Int` -> 2123) w przypadku niepoprawnego stringa(np.
      składającego się nie tylko z cyfr) zostanie rzucony wyjątek
    * bool as Int -> 1 dla `true`, 0 dla `false`
    * int as bool -> `true` dla wartości różnych od 0, `false` dla 0
    * string as bool -> `true` dla tekstu "true" (niezależnie od wielkości liter), w innych przypadkach `false`
    * bool as string -> "true" dla `true`, "false" dla `false`

* #### Funkcje:
    * możliwość definicji funkcji przyjmujących dowolną liczbę argumentów oraz opcjonalnie zwracających wartość
    * funkcja jest widoczna w obrębie bloku kodu w którym została zdefiniowana
    * w przypadku konfliktu nazw funkcji z nazwami funkcji wbudowanych w języku, w trakcie wywoływania funkcja
      użytkownika ma pierwszeństwo - chyba, że zostanie poprzedzona `builtin.` (np. `builtin.print()`)
    * możliwość definicji funkcji przez użytkownika w postaci:
    ```
    fun nazwaFunkcji(typ1 parametr1, typ2 parametr2 ...) -> typZwracany (jeśli funkcja ma zwracać wartość) {
        ...
    }
    ```
    * wbudowane funkcje:
        * `print(arg)` - wypisuje na standardowe wyjście podany argument (może być dowolnego typu)
        * `isNumber(string arg)` - sprawdza, czy podany string jest liczbą
        * "null-safety" operator `<nullable_value> ?: <non_nullable_value>` działający analogicznie jak elvis
          operator(`?:`) w Kotlinie (lub `??` w C#) (jeśli nullable_value jest null, zwraca non_nullable_value, w
          przeciwnym wypadku zwraca nullable_value)
        * `readInput()` - odczytuje linię ze standardowego wejścia i zwraca ją jako string (w przypadku błędu odczytu
          zwraca null)
        * `type(arg)` - zwraca typ podanego argumentu jako string (np. `type(1)` -> "int")

### Przykładowy program:

```
fun average(double a, double b) -> double {
    return (a + b) / 2.0
}

fun print(string text) {
    builtin.print("Exiting program with i = " + text )
}

fun main() {
    var int i = 0
    const double c = 2.0
    while(i < 3) {
        print("Type a number")
        const string input = readInput() ?: "0"
        if (isNumber(input) && input as int > 0) {
            const double number = input as double
            builtin.print("The average of 2 and " + number + " is " + average(c, number))
        } else {
            builtin.print("The input is not a number")
        }
        i = i + 1 // incrementing i
    }
    print(i as string)
}

main()
```

```
    [OUT]: Type a number
    [IN]: 2
    [OUT]: The average of 2 and 2.0 is 2.0
    [IN]: aaa
    [OUT]: The input is not a number
    [IN]: 3
    [OUT]: The average of 2 and 3.0 is 2.5
    [IN]: 4
    [OUT]: The average of 2 and 4.0 is 3.0
           Exiting program with i = 3
```
### Gramatyka języka
Gramatyka języka zapisana w notacji EBNF znajduję się w pliku [grammar.ebnf](grammar.ebnf)
### Analiza wymagań

* z racji, że język jest silnie, statycznie typowany, należy zaimplementować analizę statyczną typów
  (czy typy zmiennych są zgodne z typami wyrażeń, które ich dotyczą) oraz zaimplementować operator jawnego
  rzutowania, który musi sprawdzić, czy rzutowanie jest możliwe
* z racji dopuszczenia zarówno mutowalnych oraz niemutowalnych zmiennych, należy podczas przypisywania wartości
  do zmiennych sprawdzać, czy jest to dozwolone w danym miejscu (np. czy to jest deklaracja czy zwykłe przypisane,
  w wypadku którym należy zgłosić błąd gdy zmienna jest oznaczona jako niemutowalna)
* należy rozróżniać czy zmienna niezainicjowana jest deklarowana jako nullowalna, i gdy nie jest informować
  użytkownika o błędzie
* z racji możliwości definiowania funkcji o takiej samej sygnaturze jak funkcje wbudowane należy sprawdzać czy w danym
  bloku kodu wywołana powinna zostać funkcja użytkownika czy wbudowana
* z racji, że pozwalamy na funkcję niezwracające wartości, należy sprawdzać czy wywołanie takiej funkcji nie jest użyte
  jako wyrażenie (np. w instrukcji warunkowej lub w wyrażeniu przypisania) i zgłaszać wtedy błąd.
* należy sprawdzać w miejscach używania zmiennych czy są one dostępne w tym miejscu

### Uruchomienie interpretera:

Przed uruchomieniem interpretera należy wygenerować plik jar za pomocą polecenia `./gradlew shadowJar`.
Następnie interpreter można uruchomić za pomocą polecenia `java -jar build/libs/interpreter-all.jar <program>`.
Uruchomienie interpretera następuje poprzez wywołanie interpretera przekazując mu strumień
wejściowy kod: `java -jar build/libs/interpreter-all.jar -stdin strumień_wejściowy` (
np. `java -jar build/libs/interpreter-all.jar -stdin "builtin.print(1)"`) lub plik z kodem
źródłowym: `java -jar build/libs/interpreter-all.jar ścieżka_do_pliku` (
np. `java -jar build/libs/interpreter-all.jar ./test.ktx`).

### Podział na moduły:

* Moduł wejściowy - zadaniem modułu jest otwarcie źródła kodu i utworzenie interfejsu, pozwalającego na odczytywanie
  wejścia jako strumień, z którego korzystać będzie moduł Lexera.
* Lexer - zadaniem modułu jest analiza leksykalna, której celem jest leniwe generowanie tokenów na podstawie strumienia
  znaków wejściowych, otrzymywanych z modułu wejściowego.
* Parser - odpowiedzialany za analizę składniową - sprawdzanie gramatyki i zgłaszanie błędów w przypadku ich
  wystąpienia, tworzenie drzewa składniowego.
* Weryfikator semantyczny - zadaniem modułu jest sprawdzenie poprawności semantycznej kodu na podstawie otrzymanego
  drzewa składniowego - czy nie występują wyrażanie będące zgodne z gramatyką, ale niemożliwe do wykonania (np.
  widoczoność zmiennych, zgodność typów argumentów)
* Interpreter - jego zadaniem jest wykonywanie instrukcji na podstawie drzewa składniowego, które zostało
  wygenerowane przez parser. Również powinien powiadamiać użytkownika o błędach w czasie wykonywania wraz z krótkim
  opisem, jeśli wystąpią. Interpreter kończy swoją pracę po wykonaniu wszystkich instrukcji, albo po napotkaniu błędu w
  obu przypadkach informując o tym użytkownika.

### Obsługa błędów:

Interpreter w przypadku napotkania błędu powinien wypisać na standardowe wyjście błąd
wraz z jego opisem oraz kodem powodującym błąd. Następnie interpreter powinien poinformować użytkownika o kończeniu
działania i zakończyć się.

### Testowanie:

Do testowania została użyta biblioteka `kotlin-junit` służąca do przeprowadzania testów jednostkowych i integracyjnych
oraz
biblioteka `Kover` służąca do sprawdzania pokrycia kodu testami.
Możemy uruchamiać testy jednostkowe i integracyjne oraz sprawdzać pokrycie kodu testami:

* per moduł
    * `./gradlew <nazwa_modułu>:test` (np. `gradlew lexer:test`) - uruchomienie testów i zapisyanie wyników w pliku
      `<folder_modulu>/build/reports/tests/test/index.html`
    * `./gradlew <nazwa_modulu>:koverHtmlReport` - uruchomienie testów i sprawdzenie pokrycia kodu testami, wyniki
      zapisywane są w pliku `<folder_modulu>/build/reports/kover/html/index.html`
* dla całego projektu:
    * `./gradlew test` - uruchomienie testów i zapisyanie wyników w
      pliku `build/reports/tests/unit-test/aggregated-results/index.html`
    * `./gradlew koverMergedHtmlReport` - uruchomienie testów i sprawdzenie pokrycia kodu testami, wyniki
      zapisywane są w pliku `build/reports/kover/merged/html/index.html `

### Code style

Ponadto w projekcie jest skonfigurowana biblioteka `detekt`, pozwalająca na statyczną analizę kodu
w celu sprawdzenia jego poprawności i stylu. Możemy uruchomić analizę kodu poleceniem `./gradlew detekt`. Wyniki dla
poszczególnych
modułów znajdują się w folderze `<folder_modulu>/build/reports/detekt/`. 