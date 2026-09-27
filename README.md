# DATI APERTI IN SANITÀ: PROGETTAZIONE DI UN SISTEMA JAVA PER LA PULIZIA E L’ANALISI DEL CENSIMENTO NAZIONALE DELLE APPARECCHIATURE BIOMEDICALI

Questo repository contiene il codice sorgente integrale e riproducibile che ho progettato e sviluppato per l'analisi della distribuzione delle grandi apparecchiature sanitarie sul territorio nazionale italiano, con un focus specifico sulla Regione Puglia e sulle fasce tecnologiche.

Come promesso nell'appendice del mio elaborato, qui trovate tutto il necessario per far girare l'applicativo e riprodurre esattamente i calcoli e i grafici discussi nella mia tesi.

# Struttura del progetto
Ho diviso l'architettura software in due macro-fasi strettamente sequenziali:

1. Pipeline di bonifica (data cleaning): Si occupa di leggere il dataset ministeriale "sporco", sanificare gli errori testuali, risolvere le anomalie di formattazione, dedurre e correggere i codici CND errati (tramite la distanza edit di Levenshtein) ed eliminare i duplicati.

2. Pipeline di Analisi e Visualizzazione: Prende il file appena bonificato, incrocia i dati con quelli demografici regionali, divide i macchinari in fasce (Alta, Media, Bassa) e genera in automatico i grafici statistici in formato PNG.

# Requisiti di sistema
Per compilare ed eseguire il progetto sul tuo computer, assicurati di avere **Java (JDK)** in versione 11 o superiore. Ho utilizzato metodi introdotti nelle versioni recenti di Java (come `Path.of()`), quindi con Java 8 il programma non compilerà.
Come **ambiente di sviluppo (IDE):** Ti consiglio  **Eclipse IDE for Java Developers**, che è l'ambiente in cui il progetto è nato.
Per le **librerie esterne** ho utilizzato [JFreeChart](https://www.jfree.org/jfreechart/) per la renderizzazione dei grafici `.png.
Per far avviare il codice dovrai procurarti e inserire i seguenti file CSV direttamente nella cartella radice del progetto (allo stesso livello delle cartelle `src` e `bin`):
  *DISPO_GAP.csv** (Il dataset grezzo originale del Ministero)
  *popolazione_it.cs** (Il dizionario demografico ISTAT per il calcolo delle densità)
  *catalogo_cnd.csv** (Il catalogo ufficiale per la normalizzazione dei codici)

# Come importare e configurare il progetto in Eclipse
Per importare il codice su Eclipse e prepararlo al lancio, segui questi 5 passaggi:

1. **Scarica il progetto:** Clona questo repository (tramite `git clone`) o scarica lo `.zip` ed estrailo in una cartella a tuo piacimento.
2. Apri Eclipse, clicca su `File` in alto a sinistra e poi su `Import...`.
3. Nel menu, scegli `General` -> `Projects from Folder or Archive` (oppure `Existing Projects into Workspace`), e clicca su *Next*.
4. Clicca su `Directory...`, seleziona la cartella dove hai estratto il mio progetto e clicca su *Finish*.
5. **Configura JFreeChart (Importante!):**
   * Fai clic destro sul nome del progetto importato nel pannello di sinistra (*Package Explorer*) e vai su `Build Path` -> `Configure Build Path...`
   * Apri la scheda `Libraries`, seleziona `Classpath` e clicca su `Add External JARs...`
   * Trova e seleziona i file `.jar` della libreria JFreeChart che hai precedentemente scaricato, poi clicca su *Apply and Close*. Ora i grafici sono pronti per essere generati.

#Come eseguire il codice (Guida alla riproducibilità)
Il mio sistema è stato progettato per funzionare "a cascata".È fondamentale rispettare il seguente ordine di esecuzione, altrimenti i moduli di analisi andranno in eccezione non trovando il dataset bonificato.

** Fase 1: Bonifica del Dataset Grezzo**
**Avviare la classe** *Main.java**
In console potrai vedere in tempo reale i log con le righe scartate e la distribuzione degli error. Al termine, il programma creerà un nuovo file fondamentale chiamato *DISPO_GAP_PULITO.csv** (i dati puliti e aggregati).

### Fase 2: Analisi Statistica e Grafici
Una volta generato il file pulito è possibile avviare i moduli di analisi. Ognuno di essi estrae metriche specifiche discusse nei vari capitoli della mia tesi:

* **1. Analisi Generale, Medie e Densità:**
  avviare la classe *GeneratoreGrafici1.java**.
  Questa calcola le medie pesate per 100.000 abitanti (distinguendo la Puglia dal resto d'Italia) e salva *grafico_puglia_tipologie_barre.png**,    grafico_confronto_pesato_puglia.png** e *grafico_densita_tutte_regioni.png*.
* **2. Analisi per Fascia Tecnologica (Alta, Media, Bassa):**
  avviare la classe  *GeneratoreGrafici3.java**.
  Questa genera l'istogramma a barre compattate *grafico_quota_fascia_bassa.png** per tutte le regioni.
* **3. Estrazione Dettaglio Regionale (Puglia High-Tech):**
  avviare *PipelinePugliaHighTech.java** 
  Questa genera un report tabellare in console aggregato per province pugliesi e produce il file CSV specializzato *PUGLIA_HIGH_TECH.csv**, che ho utilizzato nel mio elaborato per la mappatura geospaziale interattiva su Kepler.gl.


Grazie per l'interesse!