# Slick with Flyway, Scala and Play-Framework Example

Dieses Beispielprojekt demonstriert die Verwendung von slick in einer Scala / Play-Framework Umgebung. 

für Details, siehe auch: https://scalac.io/scala-slick-experience/


# Vorbereitung

Eine neue MariaDB-Datenbank "testdb" in seinem lokalen XAMPP (LAMP) anlegen. XAMPP wird mit MariaDB ausgeliefert, daher habe ich auch überall die MariaDB-Treiber verwendet. Wenn man eine andere Datenbank benötigt, ist dies recht einfach möglich, indem man an allen relevanten Stellen in der "build.sbt", "application.conf", "testdb.xml" und der Klasse "app.database.DB.scala" die entsprechenden Einstellungen für seinen Datenbanktyp anpasst.

Man sollte auf jeden Fall seine Login-Daten für die Datenbank in der `conf/application.conf` und `conf/testdb.xml` eintragen.  

Das Projekt in Intellij importieren und dann die SBT-Shell starten. Hier dann nacheinander folgende Kommandos ausführen:

```
clean
flywayMigrate
run
```
Mit `clean` löscht man alles in seinem target-Folder. Mit `flywayMigrate` befüllt man seine testdb-Datenbank mit den erforderlichen Tabellen und Testdaten. Mit `run` startet man die Play-Applikation.

Dies sollte alles ohne Fehler ausgeführt werden. Nun kann man im Webbrowser die folgenden URLs ansteuern:

```
http://localhost:9000/
http://localhost:9000/book          (Bücher anzeigen, anlegen, löschen)
http://localhost:9000/author        (Autoren anzeigen, anlegen, löschen)
http://localhost:9000/test1         (Komplexer Select mit Join)
http://localhost:9000/test2         (5000 Autoren mit Batch-Insert einfügen)
http://localhost:9000/test3         (Alle Autoren anhand eines LIKE löschen)
http://localhost:9000/test4         (Transaktions-Beispiel)
http://localhost:9000/test5         (Transaktions-Beispiel für Fehlerfall / Rollback)
```

