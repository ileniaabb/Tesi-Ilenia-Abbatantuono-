package grafic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class GeneratoreGrafici1 {

    public static void main(String[] args) {
        Path csvPath = Paths.get("DISPO_GAP_PULITO.csv");
        Path csvPopolazionePath = Paths.get("popolazione_it.csv");

        Map<String, Integer> macchinariPuglia = new HashMap<>();
        Map<String, Integer> macchinariPerRegione = new HashMap<>();

        // Usiamo il parser dedicato
        Map<String, Long> popolazionePerRegione = PopolazioneParser.caricaESommaPopolazione(csvPopolazionePath);

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String riga = br.readLine();
            if (riga == null) return;

            String[] intestazioni = riga.split(";");
            int idxRegione = -1, idxTipo = -1, idxQuantita = -1;

            for (int i = 0; i < intestazioni.length; i++) {
                String col = intestazioni[i].replace("\uFEFF", "").replace("\"", "").trim().toLowerCase();
                if (col.equals("regione")) idxRegione = i;
                else if (col.equals("tipo_apparecchiatura")) idxTipo = i;
                else if (col.equals("num_apparecchiature")) idxQuantita = i;
            }

            while ((riga = br.readLine()) != null) {
                String[] campi = riga.split(";");
                int maxIdx = Math.max(idxRegione, Math.max(idxTipo, idxQuantita));
                if (campi.length <= maxIdx) continue;

                String regione = PopolazioneParser.normalizzaNomeRegione(campi[idxRegione]);
                String tipologia = campi[idxTipo].replace("\"", "").trim().toUpperCase();
                int quantita = parseQuantita(campi[idxQuantita]);

                if (!regione.isEmpty()) {
                    macchinariPerRegione.put(regione, macchinariPerRegione.getOrDefault(regione, 0) + quantita);

                    if (regione.contains("PUGLIA")) {
                        macchinariPuglia.put(tipologia, macchinariPuglia.getOrDefault(tipologia, 0) + quantita);
                    }
                }
            }

            // 1. CONTEGGIO APPARECCHIATURE PER REGIONE
            System.out.println("DISTRIBUZIONE APPARECCHIATURE PER REGIONE ");
            for (Map.Entry<String, Integer> entry : macchinariPerRegione.entrySet()) {
                System.out.println("In " + entry.getKey() + ": " + entry.getValue() + " apparecchiature");
            }
            

            // 2. STRATEGY 1: TASSO PESATO PER OGNI SINGOLA REGIONE
            Strategy strategiaRegionale = new CalcoloTassoPer100k();
            Map<String, Double> tassiRegionali = strategiaRegionale.calcola(macchinariPerRegione, popolazionePerRegione);

            System.out.println("MEDIA PESATA PER OGNI SINGOLA REGIONE (Per 100.000 abitanti)");
            for (Map.Entry<String, Double> entry : tassiRegionali.entrySet()) {
                String reg = entry.getKey();
                double tasso = entry.getValue();
                long pop = popolazionePerRegione.getOrDefault(reg, 0L);
                int macch = macchinariPerRegione.getOrDefault(reg, 0);

                if (pop > 0) {
                    System.out.printf("In %-22s: %6.2f apparecchiature/100k ab. (%d macchinari su %,d ab.)%n",
                            reg, tasso, macch, pop);
                } else {
                    System.out.printf("In %-22s: %d apparecchiature (Popolazione N.D.)%n", reg, macch);
                }
            }
            

            //3. STRATEGY 2: MEDIE NAZIONALI
            Strategy strategiaNazionale = new CalcoloMediaNazionale();
            Map<String, Double> medieNazionali = strategiaNazionale.calcola(macchinariPerRegione, popolazionePerRegione);

            double mediaNonPesataTotale = medieNazionali.get("MEDIA_NON_PESATA_TOTALE");
            double mediaNonPesataSenzaPuglia = medieNazionali.get("MEDIA_NON_PESATA_SENZA_PUGLIA");
            double mediaPesataTotale100k = medieNazionali.get("MEDIA_PESATA_TOTALE_100K");
            double mediaPesataSenzaPuglia100k = medieNazionali.get("MEDIA_PESATA_SENZA_PUGLIA_100K");
            double tassoPuglia100k = tassiRegionali.getOrDefault("PUGLIA", 0.0);

            System.out.println("SINTESI E CONFRONTO MEDIE ");
            System.out.printf("1. MEDIA NAZIONALE NON PESATA (Inclusa Puglia): %.2f apparecchiature per regione%n", mediaNonPesataTotale);
            System.out.printf("2. MEDIA NAZIONALE NON PESATA (Esclusa Puglia): %.2f apparecchiature per regione%n", mediaNonPesataSenzaPuglia);
            System.out.printf("3. MEDIA NAZIONALE PESATA (Inclusa Puglia)   : %.2f apparecchiature / 100k ab.%n", mediaPesataTotale100k);
            System.out.printf("4. MEDIA NAZIONALE PESATA (Esclusa Puglia)   : %.2f apparecchiature / 100k ab.%n", mediaPesataSenzaPuglia100k);
            System.out.printf("5. TASSO PUGLIA                              : %.2f apparecchiature / 100k ab.%n", tassoPuglia100k);
           

            // GRAFICI
         // Primo grafico a torta (Tipologie in Puglia)
            DefaultPieDataset datasetTorta = new DefaultPieDataset();
            for (Map.Entry<String, Integer> entry : macchinariPuglia.entrySet()) {
                datasetTorta.setValue(entry.getKey(), entry.getValue());
            }
            
            JFreeChart chartTorta = ChartFactory.createPieChart(
                "Apparecchiature Sanitarie in Puglia", // Titolo
                datasetTorta,                          // Dataset
                true,                                  // Legenda
                true,                                  // Tooltips
                false                                  // URL
            );
            
       
            PiePlot plot = (PiePlot) chartTorta.getPlot();
            plot.setSimpleLabels(false);
       
            plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", 
                new java.text.DecimalFormat("0"), 
                new java.text.DecimalFormat("0.00%")
            ));
            plot.setBackgroundPaint(new java.awt.Color(190,225,245));
            
            ChartUtils.saveChartAsPNG(new File("grafico_puglia_tipologie.png"), chartTorta, 800, 500);

            // 2. Secondo grafico 
            DefaultCategoryDataset datasetBarrePesato = new DefaultCategoryDataset();
            
            // Inserimento dati: addValue(valore, "Nome Serie", "Etichetta Categoria")
            datasetBarrePesato.addValue(tassoPuglia100k, "Densità", "Puglia");
            datasetBarrePesato.addValue(mediaPesataSenzaPuglia100k, "Densità", "Media Altre Regioni");

            JFreeChart chartBarrePesato = ChartFactory.createBarChart(
                "Confronto Densità Apparecchiature per 100.000 Abitanti(puglia -", // Titolo
                "Area Geografica",                              // Etichetta Asse X
                "N. Apparecchiature per 100k Ab.",              // Etichetta Asse Y
                datasetBarrePesato, 
                PlotOrientation.VERTICAL, 
                true, // Legenda (
                true,  // Tooltip
                false  
            );
            
      
            CategoryPlot plotBarre = chartBarrePesato.getCategoryPlot();
            org.jfree.chart.renderer.category.BarRenderer rendererBarre = (org.jfree.chart.renderer.category.BarRenderer) plotBarre.getRenderer();
            rendererBarre.setSeriesPaint(0, new java.awt.Color(185)); //blu

            //Mostra i valori esatti sulle barre
            rendererBarre.setDefaultItemLabelsVisible(true); 
            rendererBarre.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("0.00")
            ));
             rendererBarre.setDefaultItemLabelsVisible(true); 
             rendererBarre.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator(
                 "{2}", new java.text.DecimalFormat("0.00")
             ));
           // Aggiungi la griglia orizzontale tratteggiata
            plotBarre.setRangeGridlinePaint(java.awt.Color.WHITE);
            plotBarre.setBackgroundPaint(new java.awt.Color(190,225,245));
            
            ChartUtils.saveChartAsPNG(new File("grafico_confronto_pesato_puglia.png"), chartBarrePesato, 700, 500);
            
         // 3. ISTOGRAMMA: Densità apparecchiature per 100.000 abitanti (TUTTE LE REGIONI)
            DefaultCategoryDataset datasetTutteRegioni = new DefaultCategoryDataset();
         // 1. Estrai le entry dalla mappa tassiRegionali in una lista
            List<Map.Entry<String, Double>> tassiOrdinati = new ArrayList<>(tassiRegionali.entrySet());

            // 2. Ordina la lista in base ai valori (Densità) in modo crescente
            tassiOrdinati.sort(Map.Entry.comparingByValue());
            // 3. Popoliamo il dataset usando la lista ORDINATA (tassiOrdinati)
            for (Map.Entry<String, Double> entry : tassiOrdinati) {
                if (entry.getValue() > 0) {
                    datasetTutteRegioni.addValue(entry.getValue(), entry.getKey(), entry.getKey());
                }
            }

            JFreeChart chartTutteRegioni = ChartFactory.createBarChart(
                "Densità Apparecchiature per 100.000 Abitanti (Tutte le Regioni)", 
                "Regione", 
                "Densità (per 100k Ab.)", 
                datasetTutteRegioni, 
                PlotOrientation.VERTICAL, 
                true,    // Legenda ABILITATA
                true,    // Tooltip
                false    // URL
            );
            
            CategoryPlot plotTutte = chartTutteRegioni.getCategoryPlot();
            
            // TRUCCO: Usiamo lo StackedBarRenderer per compattare le barre
            // e avere un colore diverso per ciascuna regione senza lasciare buchi
            StackedBarRenderer renderer = new StackedBarRenderer();
            renderer.setShadowVisible(false);
            renderer.setBarPainter(new StandardBarPainter()); 

            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("0.00")
            ));

            plotTutte.setRenderer(renderer);
            plotTutte.setBackgroundPaint(new java.awt.Color(190,225,245));
            // Ruotiamo le etichette dell'asse X di 45 gradi
            CategoryAxis xAxis = plotTutte.getDomainAxis();
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            
            // Spostiamo la legenda a DESTRA 
            LegendTitle legend = chartTutteRegioni.getLegend();
            legend.setPosition(RectangleEdge.RIGHT);
            
            // Salvataggio del grafico
            ChartUtils.saveChartAsPNG(new File("grafico_densita_tutte_regioni.png"), chartTutteRegioni, 1200, 600);
            System.out.println("Grafici PNG generati correttamente.");

        } catch (IOException e) {
            System.err.println("Errore di esecuzione: " + e.getMessage());
        }
    }

    private static int parseQuantita(String val) {
        try {
            return Integer.parseInt(val.replace("\"", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}