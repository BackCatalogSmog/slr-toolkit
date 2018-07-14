package de.tudresden.slr.latexexport.latexgeneration.documentclasses;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;

import de.tudresden.slr.metainformation.data.SlrProjectMetainformation;
import de.tudresden.slr.metainformation.util.DataProvider;
import de.tudresden.slr.model.taxonomy.Term;

public class TemplatePlainArticle extends SlrLatexTemplate {

	/**
	 * Constructor for this template. Initializes which auxiliary files are to be copied and where the template is found in the resources.
	 * @throws MalformedURLException
	 */
	public TemplatePlainArticle() throws MalformedURLException {
		this.name = "Plain Article";
		this.templatePath = new URL(resourcePrefix + "latexTemplates/plain/plainArticle.tex");
	}

	@Override
	public String fillDocument(String document, SlrProjectMetainformation metainformation, DataProvider dataProvider,
			Map<Term, String> mainDimensions) {
		Map<String, String> valuesMap = new HashMap<String, String>();
		valuesMap.put("SLR_TITLE", metainformation.getTitle());
		valuesMap.put("SLR_ABSTRACT", metainformation.getProjectAbstract());
		valuesMap.put("SLR_KEYWORDS", metainformation.getKeywords());
		valuesMap.put("SLR_AUTHORS", metainformation.getAuthorsList().toString());
		valuesMap.put("SLR_STATISTICS", this.generateStatistics(dataProvider));
		
		double imageToTextWidthFactor = 1;
		valuesMap.put("SLR_DIMENSIONCHARTS", this.generateDimensionCharts(mainDimensions, imageToTextWidthFactor));

		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		String resolvedString = sub.replace(document);

		return resolvedString;
	}

}
