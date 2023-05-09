/** Ez egy ImageJ plugin, mely optimalizalas reven kepes kisebb vagy nagyobb sejtcsoportok felismeresere es analizisere.
 * @author: Emma
 */

import ij.*;
import ij.gui.GenericDialog;
import ij.process.*;
import ij.measure.*;
import ij.plugin.filter.*;

public class CellCounter implements PlugInFilter {
    /**Megadjuk, hogy milyen formatumu (RGB, 8-bit, grayscale) kepek feldolgozasara alkalmas.
     * @param arg
     * @param imp
     * @return
     */
    public int setup(String arg, ImagePlus imp) {

        return DOES_ALL;
    }

    /**Ebben a run fuggvenyben futtatjuk a kep analiziset.
     * @param ip
     */
    public void run(ImageProcessor ip){
        try {
            //A kovetkezo paranccsal kepunket 8-bit formatumuva konvertaljuk.
            ip = ip.convertToByte(true);

            //Automatikus threshold erteket allitunk be a kepunk parametereihez igazitva.
            //Egy kepzeletbeli mikroszkopos kepnel is alkalmazhatnak a default dark beallitasokat,
            //hiszen a kepek hattere sotet, fekete szinu.
            ip.setAutoThreshold("Default dark");

            // Kivalasztjuk a ParticleAnalyzer osztaly metodusait, melyeket a kepunk analizisenel alkalmazni fogunk.
            int options = ParticleAnalyzer.SHOW_OUTLINES;
            //Ilyen pl. az outlineok, az eredemnenyek logolasa es a kulonbozo valtozok inicializialasa is
            //mely szemelyre szabhato a kodunk szerkesztese soran.
            ResultsTable rt = new ResultsTable();
            ParticleAnalyzer pa = new ParticleAnalyzer(options, 0, rt, 0, Double.POSITIVE_INFINITY, 0, 1.0);

            // A pa(particle) analyze metodussal kepunket
            pa.analyze(new ImagePlus("Image", ip));

            // check if any blue objects were found
            if (rt.getCounter() == 0) {
                throw new RuntimeException("Nem találtam objektumokat a képen!");
            }

            //Eredmenyeinket egy dialog box-ban taroljuk.
            GenericDialog gd = new GenericDialog("Talalt elemek");
            gd.addMessage("Elemek szama a kepen: " + rt.getCounter());
            gd.showDialog();

        } catch (Exception e) {
            IJ.handleException(e);
        }


    }
}


