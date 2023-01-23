Questa applicazione é stata creata come progetto per l'esame di Elementi di Programmazione di Sistemi Embedded

Docente: Fantozzi Carlo

Studente: Federico Finotto 

Matricola: 1136211

Progetto B:
Si chiede di implementare un’app che registri e visualizzi i dati di localizzazione precisa forniti 
dal dispositivo mobile Android. Una prima interfaccia dell’app deve mostrare, con aggiornamento in tempo reale, 
i dati di latitudine, longitudine e altitudine. Una seconda interfaccia deve mostrare in maniera grafica i dati 
registrati negli ultimi cinque minuti; l’aggiornamento in tempo reale non è richiesto. 
I dati devono essere registrati con periodicità costante anche quando il dispositivo non si muove: in altre parole, 
anche se il dispositivo è immobile deve essere registrato un nuovo dato ogni T ms. È sufficiente un periodo T ≥ 1000 ms. 
La registrazione deve continuare anche quando l’app non è visibile sullo schermo; se il dispositivo Android viene riavviato 
non è richiesto che la registrazione continui, ma l’app deve ripartire in uno stato consistente.
Per implementare la visualizzazione dei dati è consentito utilizzare librerie esterne. È consentito utilizzare il servizio di 
localizzazione di Google Play Services (classe FusedLocationProviderClient) in alternativa al servizio di localizzazione 
standard di Android (classe LocationManager).
