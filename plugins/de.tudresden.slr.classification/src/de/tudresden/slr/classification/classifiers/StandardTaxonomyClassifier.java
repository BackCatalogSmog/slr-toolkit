package de.tudresden.slr.classification.classifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;

import de.tudresden.slr.model.bibtex.Document;
import de.tudresden.slr.model.modelregistry.ModelRegistryPlugin;
import de.tudresden.slr.model.taxonomy.Model;
import de.tudresden.slr.model.taxonomy.Term;
import de.tudresden.slr.model.utils.TaxonomyUtils;
import de.tudresden.slr.utils.taxonomy.manipulation.TermCreator;

public class StandardTaxonomyClassifier {

	
	
	public StandardTaxonomyClassifier() {
		
	}
	
	public void createStandardTaxonomy(Document doc, String crossRefTitle) {
		Map<String,String> fields = doc.getAdditionalFields();
		Set<String> fieldKeys = fields.keySet();
		if(fieldKeys.contains("document_type")) {
			classifyDocument(doc,"Document Type", fields.get("document_type"));
		} else {
			classifyDocument(doc,"Document Type", doc.getType());
		}
		
		String[] lookUp = {"booktitle","journal","howpublished"};
		String venueType = "";
		boolean venueFound = false;
		for(String s: lookUp) {
			if(fields.containsKey(s)) {
				venueType = s;
				venueFound = true;
				break;
			}
			
		}
		if(venueFound) {
			String field = "";
			if(crossRefTitle == null) {
				field = fields.get(venueType).replaceAll("[^A-Za-z0-9 ]", "");
			} else {
				field = crossRefTitle.replaceAll("[^A-Za-z0-9 ]", "");
			}
			
			if(isTermNameMalformed(field)) {
				field = handleMalformedTermName(field,true);
			}
			
			classifyDocument(doc,"Document Venue", venueType, field);
		}
		
	 }
	
	private boolean isTermNameMalformed(String name) {
		return !Character.isLetter(name.charAt(0)) || !name.matches("[A-Za-z0-9 ]+");
	}
	
	private String handleMalformedTermName(String name,boolean useDialog) {
		
		String returnName = "T " + name;
		if(useDialog) {
			InputDialog dlg = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					 "Invalid Term name",
					 "Please enter a new term name that starts with a letter.",
					 returnName,
					 (s -> (!Character.isLetter(s.charAt(0))) ? "" : null)
							 );
			 returnName = (dlg.open() == 0)? name = dlg.getValue() : returnName;
			 returnName.replaceAll("[^A-Za-z0-9 ]", "");
		}
		return returnName;
	}
	
	public void createStandardTaxonomy(Document d) {
		createStandardTaxonomy(d,null);
	}
	 
	 public void classifyDocument(Document doc, String ...types) {
		 Optional<Model> modelOptional =  ModelRegistryPlugin.getModelRegistry().getActiveTaxonomy();
		 if(modelOptional.isPresent()) {
			 Model model = modelOptional.get();
			 
			 EObject taxonomyElement = model;
			 EObject docTaxonomyElement = doc.getTaxonomy();
			 
			for(String type: types) {
				 //create element in general taxonomy 
				 taxonomyElement = TermCreator.createChildIfNotExisting(taxonomyElement,type);
				 docTaxonomyElement = TermCreator.createChildIfNotExisting(docTaxonomyElement,type,false);
			 }
			 
			 TaxonomyUtils.saveTaxonomy(model);
		 }
	 }
	 
	 public void classifyDocumentsInProject(IProject project) {
		 List<IFile> bibFiles = new ArrayList<IFile>();
		 IFile taxonomyFile = null;
		 try {
			 for(IResource res:project.members()) {
				 if(res.getFileExtension().equals("bib")) {
					 bibFiles.add(project.getFile(res.getFullPath().lastSegment()));
					 
				 }
				 
				 if(res.getFileExtension().equals("taxonomy")) {
					 taxonomyFile = project.getFile(res.getFullPath().lastSegment());
				 }
			 }
			 
			 ModelRegistryPlugin.getModelRegistry().setTaxonomyFile(taxonomyFile);
			 Optional<AdapterFactoryEditingDomain> domainOpt = ModelRegistryPlugin.getModelRegistry().getEditingDomain();
			 if(domainOpt.isPresent()) {
				 for(IFile file:bibFiles) {
						URI uri = URI.createURI(file.getFullPath().toString());	
						Resource bibRes = domainOpt.get().getResourceSet().getResource(uri, true);
						
						//create map to associate a doc with its key to efficiently find crossrefs later
						Map<String,String> docMap = new HashMap<>();

						for(EObject e:bibRes.getContents()) {
							docMap.put(((Document) e).getKey(), ((Document) e).getTitle());	
						}
						
						for(EObject e:bibRes.getContents()) { 
							Document doc = (Document) e;
							
							Command c = new AbstractCommand() {
									@Override
									public boolean prepare() {
										return true;
									}

									@Override
									public void redo() {
										execute();
									}
									
									@Override
									public void execute() {
										if(doc.getAdditionalFields().containsKey("crossref")) {
											createStandardTaxonomy(doc,docMap.get(doc.getAdditionalFields().get("crossref")));
										} else {
											createStandardTaxonomy(doc);
										}
									}
							};
							
							Optional<AdapterFactoryEditingDomain> editingDomain = ModelRegistryPlugin.getModelRegistry().getEditingDomain();
							editingDomain.ifPresent((domain) -> domain.getCommandStack().execute(c));
						}
				 }
			 }
			
			 
			 
			 
		 } catch(CoreException e) {
			 
		 }
	 }

}