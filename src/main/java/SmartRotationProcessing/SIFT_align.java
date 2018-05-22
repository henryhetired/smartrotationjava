package SmartRotationProcessing;


import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;

/**
 * Align a stack consecutively using automatically extracted robust landmark
 * correspondences.
 * <p>
 * The plugin uses the Scale Invariant Feature Transform (SIFT) by David Lowe
 * \cite{Lowe04} and the Random Sample Consensus (RANSAC) by Fishler and Bolles
 * \citet{FischlerB81} to identify landmark correspondences.
 * <p>
 * It identifies a rigid transformation for the second of two slices that maps
 * the correspondences of the second optimally to those of the first.
 * <p>
 * BibTeX:
 * <pre>
 * &#64;article{Lowe04,
 *   author    = {David G. Lowe},
 *   title     = {Distinctive Image Features from Scale-Invariant Keypoints},
 *   journal   = {International Journal of Computer Vision},
 *   year      = {2004},
 *   volume    = {60},
 *   number    = {2},
 *   pages     = {91--110},
 * }
 * &#64;article{FischlerB81,
 * 	 author    = {Martin A. Fischler and Robert C. Bolles},
 *   title     = {Random sample consensus: a paradigm for model fitting with applications to image analysis and automated cartography},
 *   journal   = {Communications of the ACM},
 *   volume    = {24},
 *   number    = {6},
 *   year      = {1981},
 *   pages     = {381--395},
 *   publisher = {ACM Press},
 *   address   = {New York, NY, USA},
 *   issn      = {0001-0782},
 *   doi       = {http://doi.acm.org/10.1145/358669.358692},
 * }
 * </pre>
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @version 0.4b
 */
public class SIFT_align {
    final private List<Feature> fs1 = new ArrayList<Feature>();
    final private List<Feature> fs2 = new ArrayList<Feature>();
    public Mapping mapping;

    static private class Param {
        final public FloatArray2DSIFT.Param sift = new FloatArray2DSIFT.Param();

        /**
         * Closest/next closest neighbour distance ratio
         */
        public float rod = 0.92f;

        /**
         * Maximal allowed alignment error in px
         */
        public float maxEpsilon = 25.0f;

        /**
         * Inlier/candidates ratio
         */
        public float minInlierRatio = 0.05f;

        /**
         * Implemeted transformation models for choice
         */
        final static public String[] modelStrings = new String[]{"Translation", "Rigid", "Similarity", "Affine"};
        public int modelIndex = 1;

        public boolean interpolate = true;

        public boolean showInfo = false;

    }

    final static Param p = new Param();


    final public ImageProcessor run(ImagePlus imgold, ImagePlus imgnew) {
        fs1.clear();
        fs2.clear();



        ImageProcessor ip1 = imgold.getProcessor();
        ImageProcessor ip2 = imgnew.getProcessor();

        final FloatArray2DSIFT sift = new FloatArray2DSIFT(p.sift);
        final SIFT ijSIFT = new SIFT(sift);

        long start_time = System.currentTimeMillis();
        System.out.println("Processing SIFT ...");
        ijSIFT.extractFeatures(ip1, fs1);
        System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms.");
        System.out.println(fs1.size() + " features extracted.");


        AbstractAffineModel2D model;
        switch (p.modelIndex) {
            case 0:
                model = new TranslationModel2D();
                break;
            case 1:
                model = new RigidModel2D();
                break;
            case 2:
                model = new SimilarityModel2D();
                break;
            case 3:
                model = new AffineModel2D();
                break;
            default:
                model = new RigidModel2D();
        }
        mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        fs2.clear();

        start_time = System.currentTimeMillis();
        System.out.println("Processing SIFT ...");
        ijSIFT.extractFeatures(ip2, fs2);
        System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms.");
        System.out.println(fs2.size() + " features extracted.");

        start_time = System.currentTimeMillis();
        System.out.print("identifying correspondences using brute force ...");
        final Vector<PointMatch> candidates =
                FloatArray2DSIFT.createMatches(fs2, fs1, 1.5f, null, Float.MAX_VALUE, p.rod);
        System.out.println(" took " + (System.currentTimeMillis() - start_time) + "ms");

        System.out.println(candidates.size() + " potentially corresponding features identified");

        final Vector<PointMatch> inliers = new Vector<PointMatch>();

        // TODO Implement other models for choice
        AbstractAffineModel2D<?> currentModel;
        switch (p.modelIndex) {
            case 0:
                currentModel = new TranslationModel2D();
                break;
            case 1:
                currentModel = new RigidModel2D();
                break;
            case 2:
                currentModel = new SimilarityModel2D();
                break;
            case 3:
                currentModel = new AffineModel2D();
                break;
            default:
                currentModel = new RigidModel2D();
        }

        boolean modelFound;
        try {
            modelFound = currentModel.filterRansac(
                    candidates,
                    inliers,
                    1000,
                    p.maxEpsilon,
                    p.minInlierRatio);
        } catch (final Exception e) {
            modelFound = false;
            System.err.println(e.getMessage());
        }
        if (modelFound) {
            model.concatenate(currentModel);
        }

        final ImageProcessor originalimg = imgnew.getProcessor();
        originalimg.setInterpolationMethod(ImageProcessor.BILINEAR);
        final ImageProcessor alignedimg = originalimg.createProcessor(ip1.getWidth(), ip1.getHeight());
        alignedimg.setMinAndMax(originalimg.getMin(), originalimg.getMax());

        if (p.interpolate)
            mapping.mapInterpolated(originalimg, alignedimg);
        else
            mapping.map(originalimg, alignedimg);
        System.out.println("Done.");
        return(alignedimg);
    }


}





