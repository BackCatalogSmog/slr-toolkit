package de.tudresden.slr.latexexport.latexgeneration.documentclasses;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import de.tudresden.slr.metainformation.data.SlrProjectMetainformation;
import de.tudresden.slr.metainformation.util.DataProvider;
import de.tudresden.slr.model.taxonomy.Term;

public abstract class SlrLatexTemplate {
	//Strings representing the template's names. Used in the wizard's dialog.
	public static final String TEMPLATE_PLAIN = "Plain Article";
	public static final String TEMPLATE_TUDSCR = "tudscr";
	public static final String TEMPLATE_IEEE = "IEEEconf";
	public static final String TEMPLATE_ACM = "ACM SIGPLAN conf";
	public static final String TEMPLATE_SPRINGER_LNCS = "Springer LNCS";
	
	public static final String[] documentTypes = { TEMPLATE_PLAIN , TEMPLATE_TUDSCR , TEMPLATE_IEEE , TEMPLATE_SPRINGER_LNCS, TEMPLATE_ACM};
	
	protected final String resourcePrefix = "platform:/plugin/de.tudresden.slr.latexExport/resources/";
	
	/**
	 * .cls and auxiliary files of the LaTex template. Specified in subclasses!
	 */
	protected URL[] filesToCopy = {};
	protected URL templatePath;
	protected String name;
	
	public SlrLatexTemplate() throws MalformedURLException {
		
	}

	public URL[] getFilesToCopy() {
		return filesToCopy;
	}

	public URL getTemplatePath() {
		return templatePath;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Fills LaTex template with information.
	 * @param document LaTex template, represented as String
	 * @param metainformation Metainformation object from project
	 * @param dataProvider DataProvider from project
	 * @param mainDimensions Mapping from Term/it's chart to relative path
	 * @return LaTex code for the filled out document
	 */
	public abstract String fillDocument(String document, SlrProjectMetainformation metainformation, DataProvider dataProvider, Map<Term, String> mainDimensions);

	/**
	 * Generates a block for a section in a LaTex-Template.
	 * @param title Title of the secion
	 * @return LaTex-Code for a section
	 */
	public String generateSectionTemplate(String title) {
		String sectionPreTitle = "\\section{";
		String sectionPostTitle = "}";

		return sectionPreTitle + title + sectionPostTitle;
	}

	/**
	 * Generates a figure (consisting of an image and a caption) for a image in LaTex.
	 * @param path Path to the picture. By default, images are exported to /images in the LaTex document's folder
	 * @param caption Caption for the picture
	 * @param imageToTextWidthFactor Scaling from picture to textwidth
	 * @return LaTex-Code for a figure
	 */
	public String generateImageFigure(String path, String caption, double imageToTextWidthFactor) {
		return "\\begin{figure}[!htb]\r\n" + "\\centering\r\n" + "\\includegraphics[width = "+imageToTextWidthFactor+"\\textwidth]{" + path
				+ "}\r\n" + "\\caption{" + caption + "}\r\n" + "\\end{figure}\r\n";
	}
	
	/**
	 * Generates LaTex figures for the main dimensions.
	 * @param mainDimensions Mapping from Term/it's Chart to the relative path
	 * @param imageToTextWidthFactor Scaling from picture to textwidth
	 * @return LaTex code for the figures of the main dimensions and captions.
	 */
	public String generateDimensionCharts(Map<Term, String> mainDimensions, double imageToTextWidthFactor) {
		String toReturn = "";
		for (Map.Entry<Term, String> entry : mainDimensions.entrySet()) {
			toReturn = toReturn + generateImageFigure(entry.getValue(), entry.getKey().getName(), imageToTextWidthFactor);
		}
		
		return toReturn;
	}
	
	/**
	 * Generates statistics analyzing the documents and the taxonomy which are used in the project.
	 * @param dataProvider
	 * @return LaTex code for statistics section
	 */
	public String generateStatistics(DataProvider dataProvider) {
		int dimensionsCounWithoutMainDimensions = dataProvider.getAllDimensionsOrdered().size()-dataProvider.getMainDimensions().size();
		String toReturn = "During this systematic literature review, "
				+ dataProvider.getDocuments().size() 
				+ " documents were analyzed. They were mapped to a taxonomy of " 
				+ dataProvider.getMainDimensions().size() 
				+ " main dimensions which themselves are subcategorised in a total of "
				+ dimensionsCounWithoutMainDimensions
				+ " dimensions";
		return toReturn;
	}
	
}
