import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;

import static ij.plugin.filter.PlugInFilter.DOES_ALL;

public class Cell_Counting implements PlugIn {

    private double contrast;
    private double minCircularity;
    private double maxCircularity;
    private int minSize;
    private int maxSize;
    private boolean saveParameters;

    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {
        try {
            GenericDialog gd = new GenericDialog("Counter Parameters");
            gd.addNumericField("Contrast:", contrast, 0);
            gd.addNumericField("Minimum Circularity:", minCircularity, 0);
            gd.addNumericField("Maximum Circularity:", maxCircularity, 0);
            gd.addNumericField("Minimum Size:", minSize, 0);
            gd.addNumericField("Maximum Size:", maxSize, 0);
            gd.addCheckbox("Save Parameters", saveParameters);
            gd.showDialog();

            if (gd.wasCanceled()) {
                return;
            }

            contrast = gd.getNextNumber();
            minCircularity = gd.getNextNumber();
            maxCircularity = gd.getNextNumber();
            minSize = (int) gd.getNextNumber();
            maxSize = (int) gd.getNextNumber();
            saveParameters = gd.getNextBoolean();

            ip.setMinAndMax(0, (int) contrast);
            ImageProcessor ip8bit = ip.convertToByte(true);
            ip8bit.findEdges();
            ip8bit.setAutoThreshold("Default");

            ResultsTable rt = new ResultsTable();
            ParticleAnalyzer pa = new ParticleAnalyzer(
                    ParticleAnalyzer.SHOW_OUTLINES | ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES,
                    ParticleAnalyzer.CIRCULARITY | ParticleAnalyzer.ADD_TO_MANAGER,
                    rt, minSize, maxSize, minCircularity, maxCircularity
            );

            ImagePlus adjustedImage = new ImagePlus("Adjusted Image", ip8bit);
            pa.analyze(adjustedImage);

            if (rt.getCounter() == 0) {
                throw new RuntimeException("No objects found!");
            }

            String resultMessage = "Number of objects in the image: " + rt.getCounter();

            if (saveParameters) {
                saveParameters();
                resultMessage += "\nParameters saved successfully.";
            }

            IJ.showMessage("Detected Objects", resultMessage);

        } catch (Exception e) {
            IJ.handleException(e);
        }
    }

    private void saveParameters() {
        Prefs.set("Cell_Counting.contrast", Double.toString(contrast));
        Prefs.set("Cell_Counting.minCircularity", Double.toString(minCircularity));
        Prefs.set("Cell_Counting.maxCircularity", Double.toString(maxCircularity));
        Prefs.set("Cell_Counting.minSize", Integer.toString(minSize));
        Prefs.set("Cell_Counting.maxSize", Integer.toString(maxSize));
        Prefs.savePreferences();
    }

    @Override
    public void run(String s) {
        ImagePlus imagePlus = IJ.getImage();
        ImageProcessor ip = imagePlus.getProcessor();
        run(ip);
    }

    public static void main(String[] args) {
        new Cell_Counting().run("");
    }
}
