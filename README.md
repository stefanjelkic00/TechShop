TechShop je moderna online prodavnica tehnologije razvijena kao full-stack aplikacija.
Tehnologije koje su koriscene su Spring Boot, React, MySQL i ElasticSearch.

Elastic Search: 
Pokretanje elasticsearcha regularno: 
  1.Mora biti instaliran elasticsearch fajl 
  2.Pristupiti ovoj putanji : \ElasticSearch\elasticsearch-8.17.2/bin
  3.Nakon toga kliknuti na elasticsearch.bat i pokretanje ce se zapoceti 
  4.Sacekati (2-3 min) da se pokrene u potpunosti pa nakon toga mozemo preci na bekend deo aplikacije
  5.Sistem ce biti dostupan na: http://localhost:9200
  Docker: 


Bekend:  
  Java 17	                        -Verzija koja je koriscena 
  Spring Boot	                    -Framework za razvijanje REST API-ja
  Spring Security + JWT      	    -Autentifikacija i autorizacija korisnika
  Spring Data JPA	                -ORM sloj za rad sa bazom podataka
  Hibernate	                      -Povezuje Java objekte sa relacionim bazama
  MySQL	                          -Relaciona baza podataka
  Lombok	                        -Getteri, setteri itd. - radi lepseg i manjeg koda
  Elasticsearch	                  -Napredna pretraga proizvoda i filtriranje 
  Spring Mail (Gmail SMTP)	      -Slanje email notifikacija korisnicima prilikom aktivnosti
  Swagger UI	                    -Testiranje API-ja
  Maven	                          -Upravljanje projektom i zavisnostima
  Logging                  	      -Logika za pracenje aktivnosti 

Pokretanje bekenda regularno:            
  1.Uveriti se da je MySql i ElasticSearch pokrenut .
  2.Nakon toga se moze pokrenuti app preko TechShopApplication fajla za pokretanje 
  3.Aplikacija će biti dostupna na: http://localhost:8001
  Docker : 


Frontend:
  React	                          -Biblioteka za razvoj korisničkog interfejsa
  Axios	                          -HTTP klijent za komunikaciju sa backend-om
  Bootstrap	CSS                   -Framework za responzivan dizajn
  React Bootstrap	                -React komponente bazirane na Bootstrapu
  Formik	                        -Upravlja formama i validacijom u React-u
  Yup	Biblioteka                  -Biblioteka za validaciju formi (koristi se uz Formik)
  JWT Decode	                    -Dekodiranje JWT tokena na klijentskoj strani
  React Toastify	                -Prikazivanje notifikacija i obaveštenja


Pokretanje frontenda regularno:
  1. Otvori terminal u techshopfront folderu.
  2.Instalirati sve zavisnosti: npm install
  3.Pokrenuti aplikaciju: npm start
  3.Aplikacija će biti dostupna na: http://localhost:3000
  Docker:


Baza podataka: 
  Najbolje unutar app dodavati rucno producte zbog sinhronizacije MySql i Elastic Search baze. 
  Sto se tice usera rucno registrovati customera a admina rucno uneti iz razloga sto ne postoji registracija za admina. 

  My sql ce biti dostupan na: http://localhost:3306

Funkcionalnosti aplikacije: 

  HomePage:
    Kada se app pokrene vodi nas na pocetnu stranicu (HomePage).
    U nav baru ce postojati samo dugme za prijavu, dok ce unutar home page postojati u gornjem delu deo za visestruko filtriranje po kategoriji proizvoda, 
    filtriranje po prikazu cene od manje ka vecoj i obrnuto i filtriranje po minimalnoj i maksimalnoj ceni. 
    Ispod filtera koji su sinhronizovani jedan sa drugim postoji polje za pretragu koje je takodje sinhronizovano sa filterima.
    Pretraga ima funkcionalno zanemarivanje malog i velikog slova, da sadrzi unutar sebe tekst koji smo uneli,nudi kada unesemo par karatkera opciju proizvoda koji se nastavlja na to.

    Sto se tice proizvoda i njihovog prikaza sastoje se od slike naziva i cene. Cena moze imati popust (10%,20%,30%) zavisi od tipa korisnika.
    Kada se klikne na polje proizvoda ono nas vodi na page zasebno tog proizvoda gde ce se prikazivati dodatno i opis tog proizvoda.
    Oznaka kada je nesto snizeno ce postojati unutar slike u krugu sa prikazanim popustom, Cena ce biti stara preskrabana i pored ce se prikazivati snizena.
    U slucaju da neki proizvod nema trenutno u ponudi a da je njegovo polje idalje tu pisace gde se cena nalazi, da nema na stanju i nece moci da se dodaje u korpu. 
    Polje proizvoda takodje ima button korpe u sebi koji ga prilikom klika dodaje u korpu.

  Korpa: 
    Kada se korisnik prijavi i doda artikal u korpu on ce unutar korpe imati prikazanu sliku naziv i snizenost artikla ukoliko je ima.
    Imace takodje prikazanu kolicinu mogucnost dodavanja jos kolicinski uklanjanja kolicinski i totalnog uklanjanja.
    Ispod svih proizvoda koje ima on ispisivati njihovu ukupnu cenu i ispod toga formu za unos adrese.
    Kada sve to korisnik unese mocice da klikne ispod na dugme poruci koje i kreira datu porudzbinu.

  Porudzbine: 
    Kada korisnik klikne na dugme porudzbine ono ga vodi na prikaz svih porudzbina.
    Tu ce biti prikazan id , status porudzbine i ukupna cena. 
    Svaki od njih se ponasa kao button koji nas vodi u detaljnije informacije o datoj porudzbini na koju kliknemo.
    Unutar detaljnije porudzbine se prikazuje se id ,datum ,ukupna cena ,status, popust, stavke i adresa porudzbine.

  Profil:
    Unutar ove stranice imacemo detaljan prikaz podataka o korisniku.
    Ispod cemo imati informaciono dugme o tipu korisnika i kako se tip korisnika menja prilikom porucivanja. 
    REGULAR: 0 porudžbina - Nema dodatnog popusta.
    PREMIUM: 1-2 porudžbine - 10% popusta na artikle.
    PLATINUM: 3-4 porudžbine - 20% popusta na artikle.
    VIP: 5 ili više porudžbina - 30% popusta na artikle.
    Ispod svega ce se nalaziti dugme za promenu lozinke.

  KorisniciPage: (Admin)
    Svim ostalim prethodno navedenim pages ce moci i Customer i Admin da pristupe. 
    KorisniciPage ce moci samo admin.
    Tu se nalazi prikaz svih korisnika i korisnickih podataka sa poljima za azuriranje i njihovo brisanje.

  ProizvodiPage: (Admin)
    Administratorska stranica sa detaljnim prikazom svih proizvoda.
    Unutar ovog page imamo mogucnost da dodamo novi proizvod.
    Takodje imamo mogucnost da izmenimo ili obrisemo postojece proizvode.

  PorudzbineKorisnikaPage: (Admin)
    Administratorska stranica sa prikazom svih porudzbina. 
    Unutar ove stranice admin ima mogucnost da izmeni status porudzbine ( PENDING, SHIPPED, DELIVERED, CANCELLED).
    Imamo dugme koje nas vodi u detaljan prikaz porudzbine. 
    Takodje imamo dugme za kompletno brisanje porudzbine.



    

 
